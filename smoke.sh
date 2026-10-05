#!/usr/bin/env bash
# Usage:
#   ./smoke.sh
#   ./smoke.sh http://localhost:8080/paytm-beats

set -u

BASE="${1:-http://localhost:8080}"
ADMIN_USER="${ADMIN_USER:-admin2}"
ADMIN_PASS="${ADMIN_PASS:-mySecurePassword123}"

J='Content-Type: application/json'
RUN_ID="$(date +%s)-$$-$(uuidgen | tr -d '-')"

step() {
  echo
  echo "=== $1"
}

token_of() {
  local body="$1"
  local token

  token=$(printf '%s' "$body" | python3 -c '
import json
import sys

body = sys.stdin.read().strip()

try:
    obj = json.loads(body)
    if isinstance(obj, dict):
        print(obj.get("token", ""))
    elif isinstance(obj, str):
        print(obj)
except Exception:
    print(body)
')

  if [[ -z "$token" || "$token" == *"{"* || "$token" == *" "* ]]; then
    echo "ERROR: Login did not return a valid token." >&2
    echo "Response: $body" >&2
    return 1
  fi

  echo "$token"
}

reserve() {
  local token="$1"
  local seats_json="$2"
  local key="$3"

  curl -s -w "\nstatus=%{http_code}\n" \
    -X POST "$BASE/shows/$ID/reserve" \
    -H "$J" \
    -H "Authorization: Bearer $token" \
    -d "{\"seats\":$seats_json,\"idempotency_key\":\"$key\"}"
}

step "1. Run information"
echo "BASE=$BASE"
echo "RUN_ID=$RUN_ID"

step "2. Create two normal users"
for u in alice bob; do
  curl -s -o /dev/null -w "$u status=%{http_code}\n" \
    -X POST "$BASE/user/create" \
    -H "$J" \
    -d "{\"username\":\"$u\",\"password\":\"pass12345\"}"
done

step "3. Login"
ALICE_LOGIN=$(curl -s -X POST "$BASE/user/login" \
  -H "$J" \
  -d '{"username":"alice","password":"pass12345"}')

BOB_LOGIN=$(curl -s -X POST "$BASE/user/login" \
  -H "$J" \
  -d '{"username":"bob","password":"pass12345"}')

ALICE_T=$(token_of "$ALICE_LOGIN") || exit 1
BOB_T=$(token_of "$BOB_LOGIN") || exit 1

echo "Alice token: ${ALICE_T:0:20}..."
echo "Bob token:   ${BOB_T:0:20}..."

step "4. create show"
SHOW=$(curl -s -X POST "$BASE/shows" -H "$J" -H "Authorization: Bearer $ALICE_T" \
  -d '{"name":"smoke","seats":["A1","A2","A3","A4","A5","A6"],"price_paise":25000}')
echo "Show Creation Response: $SHOW"

ID=$(printf '%s' "$SHOW" | grep -Eo '"id":[0-9]+' | head -1 | cut -d: -f2)

if [[ -z "$ID" ]]; then
  echo "ERROR: Could not extract show ID. Check show creation response above."
  exit 1
fi

echo "Show ID: $ID"

step "5. Alice reserves A1 (expect 201)"
ALICE_A1_KEY="alice-a1-$RUN_ID"
reserve "$ALICE_T" '["A1"]' "$ALICE_A1_KEY"

step "6. Alice retries same key and same seats (expect same reservation)"
reserve "$ALICE_T" '["A1"]' "$ALICE_A1_KEY"

step "7. Alice reuses same key with different seats (expect 409)"
reserve "$ALICE_T" '["A2"]' "$ALICE_A1_KEY"

step "8. Bob tries Alice's A1 (expect 409)"
reserve "$BOB_T" '["A1"]' "bob-a1-$RUN_ID"

step "9. Spoof test: Bob sends user_id=alice (reservation must belong to Bob)"
curl -s -w "\nstatus=%{http_code}\n" \
  -X POST "$BASE/shows/$ID/reserve" \
  -H "$J" \
  -H "Authorization: Bearer $BOB_T" \
  -d "{\"seats\":[\"A3\"],\"idempotency_key\":\"bob-a3-$RUN_ID\",\"user_id\":\"alice\"}"

step "10. Per-user limit: Alice already holds A1; try four more"
for s in A2 A4 A5 A6; do
  echo "-- Trying $s"
  reserve "$ALICE_T" "[\"$s\"]" "alice-limit-$s-$RUN_ID"
done
echo "Expected: A2, A4, A5 should succeed; A6 should be declined if limit is 4."

step "11. Request without token (expect 401/403)"
curl -s -o /dev/null -w "status=%{http_code}\n" \
  -X POST "$BASE/shows/$ID/reserve" \
  -H "$J" \
  -d "{\"seats\":[\"A1\"],\"idempotency_key\":\"no-token-$RUN_ID\"}"

step "12. Request with garbage token (expect 401/403, not 500)"
curl -s -o /dev/null -w "status=%{http_code}\n" \
  -X POST "$BASE/shows/$ID/reserve" \
  -H "$J" \
  -H "Authorization: Bearer not.a.jwt" \
  -d "{\"seats\":[\"A1\"],\"idempotency_key\":\"bad-token-$RUN_ID\"}"

step "13. Final show state"
curl -s "$BASE/shows/$ID" \
  -H "Authorization: Bearer $ALICE_T"
echo

step "14. Health checks"
curl -s -o /dev/null -w "liveness=%{http_code}\n" \
  "$BASE/actuator/health/liveness"

curl -s -o /dev/null -w "readiness=%{http_code}\n" \
  "$BASE/actuator/health/readiness"

step "15. Reservation/seat metrics"
curl -s "$BASE/actuator/prometheus" | grep -i -E "reservation|seat" | head -10

echo
echo "Smoke test completed. Run ID: $RUN_ID"