package com.example.inkthethink.Security;


import com.example.inkthethink.jwt.JwtAuthenticationFilter;
import com.example.inkthethink.jwt.JwtUtil;
import com.example.inkthethink.service.MyAppUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


import lombok.AllArgsConstructor;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@AllArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private final MyAppUserService appUserService;

    @Autowired
    private final JwtUtil jwtUtil;


    @Bean
    public UserDetailsService userDetailsService(){
        return appUserService;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(appUserService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtUtil, appUserService);

        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .formLogin(httpForm -> {
                    httpForm.loginPage("/req/login").permitAll();
                    httpForm.defaultSuccessUrl("/index", true);
                    httpForm.usernameParameter("username");
                    httpForm.passwordParameter("password");
                })
                .authorizeHttpRequests(registry -> {
                    registry.requestMatchers("/req/signup", "/req/login", "/css/**", "/js/**").permitAll(); // allow without JWT
                    registry.anyRequest().authenticated();
                })
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .build();
    }


}
