package com.mayur29.paytmbeat.paytmbeat.service;

import com.mayur29.paytmbeat.paytmbeat.entities.User;
import com.mayur29.paytmbeat.paytmbeat.repositories.UserRepository;
import com.mayur29.paytmbeat.paytmbeat.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils;

    public User addNewUser(User user) {
        String endcodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(endcodedPassword);
        user.setRoles(Arrays.asList("ADMIN"));

        return userRepository.save(user);
    }

    public String login(String username) {
        return jwtUtils.generateToken(username);
    }
}
