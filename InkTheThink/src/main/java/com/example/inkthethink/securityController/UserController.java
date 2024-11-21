package com.example.inkthethink.securityController;


import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class UserController {

    @GetMapping("/user-info")
    public ResponseEntity<String> getUserInfo(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(username);
    }
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        // Get the username of the currently authenticated user
        String username = authentication.getName();

        // Optionally, you can fetch the full user details (e.g., from your database)
        // For example, using your `MyAppUserService` to get user info:
        // MyAppUser user = myAppUserService.loadUserByUsername(username);

        // For now, let's just return the username:
        return ResponseEntity.ok("Profile of user: " + username);
    }

}