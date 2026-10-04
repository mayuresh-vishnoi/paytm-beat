package com.mayur29.paytmbeat.paytmbeat.repositories;

import com.mayur29.paytmbeat.paytmbeat.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User,Integer> {
    public User findByUsername(String username);
}
