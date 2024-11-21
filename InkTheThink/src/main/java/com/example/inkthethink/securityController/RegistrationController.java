package com.example.inkthethink.securityController;

import com.example.inkthethink.model.MyAppUser;
import com.example.inkthethink.repository.MyAppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RegistrationController {

    @Autowired
    private MyAppUserRepository myAppUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping(value = "/req/signup", consumes = "application/json")
    public ResponseEntity<?> createUser(@RequestBody MyAppUser user){
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        System.out.println("hi hi hi "+myAppUserRepository.findByEmail(user.getEmail()).isPresent());
        if(myAppUserRepository.findByEmail(user.getEmail()).isPresent()||
                myAppUserRepository.findByUsername(user.getUsername()).isPresent()){
            return ResponseEntity.badRequest().body("not ok");

        }
        System.out.println("we we we ok done ");
        myAppUserRepository.save(user);
        return ResponseEntity.ok().body("ok");
    }

}
