package com.mayur29.paytmbeat.paytmbeat.repositories;

import com.mayur29.paytmbeat.paytmbeat.entities.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShowRepository extends JpaRepository<Show,Integer> {
}
