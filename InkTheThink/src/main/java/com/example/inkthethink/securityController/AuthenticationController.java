package com.example.inkthethink.securityController;

import com.example.inkthethink.jwt.JwtUtil;
import com.example.inkthethink.model.MyAppUser;
import com.example.inkthethink.service.MyAppUserService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Getter
@Setter
@AllArgsConstructor
public class AuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private MyAppUserService userDetailsService;

    @PostMapping("/req/authenticate")
    public ResponseEntity<?> authenticate(@RequestBody MyAppUser user) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword())
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Incorrect username or password");
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        final String jwt = jwtUtil.generateToken(userDetails.getUsername());

        return ResponseEntity.ok(new JwtResponse(jwt));
    }

    public static class JwtResponse {
        private String jwt;

        public JwtResponse(String jwt) {
            this.jwt = jwt;
        }

        public String getJwt() {
            return jwt;
        }
    }
}
