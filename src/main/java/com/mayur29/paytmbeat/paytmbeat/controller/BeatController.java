package com.mayur29.paytmbeat.paytmbeat.controller;

import com.mayur29.paytmbeat.paytmbeat.dto.ShowRequestDTO;
import com.mayur29.paytmbeat.paytmbeat.entities.Show;
import com.mayur29.paytmbeat.paytmbeat.entities.User;
import com.mayur29.paytmbeat.paytmbeat.service.ShowService;
import com.mayur29.paytmbeat.paytmbeat.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/paytm-beats")
public class BeatController {

    @Autowired
    private UserService userService;

    @Autowired
    private ShowService showService;

    @PostMapping("/user/create")
    public ResponseEntity<?> createUser(@RequestBody User user){
        return new ResponseEntity<>(userService.addNewUser(user),HttpStatus.CREATED);
    }

    @PostMapping("/user/login")
    public ResponseEntity<?> login(@RequestBody User user){
        return new ResponseEntity<>(userService.login(user.getUsername()),HttpStatus.OK);
    }

    @GetMapping("/shows/{id}")
    public ResponseEntity<?> createShow(@PathVariable int id){
        return new ResponseEntity<>(showService.getShowState(id),HttpStatus.OK);
    }

    @PostMapping("/shows")
    public ResponseEntity<?> createShow(@RequestBody ShowRequestDTO showRequestDTO){
        return new ResponseEntity<>(showService.addNewShow(showRequestDTO),HttpStatus.CREATED);
    }
}
