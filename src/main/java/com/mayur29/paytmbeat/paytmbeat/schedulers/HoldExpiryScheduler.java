package com.mayur29.paytmbeat.paytmbeat.schedulers;

import com.mayur29.paytmbeat.paytmbeat.repositories.SeatRepository;
import com.mayur29.paytmbeat.paytmbeat.service.SeatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class HoldExpiryScheduler {

    private final SeatRepository seatRepository;
    private final SeatService seatService;

    @Scheduled(fixedDelay = 5000)
    public void expireHolds() {

        List<String> expiredHoldIds =
                seatRepository.findExpiredHoldIds();

        for (String holdId : expiredHoldIds) {
            int releasedCount = seatService.expireHold(holdId);

            if (releasedCount > 0) {
                log.info(
                        "Expired holdId={}, releasedSeats={}",
                        holdId,
                        releasedCount
                );
            }
        }
    }
}
