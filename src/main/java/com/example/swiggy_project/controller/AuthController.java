package com.example.swiggy_project.controller;

import com.example.swiggy_project.model.User;
import com.example.swiggy_project.service.UserService;
import com.example.swiggy_project.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private JavaMailSender mailSender;

    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody User user) {
        logger.info("Received request to register user with username: {}", user.getUsername());
        try {
            User registeredUser = userService.addUser(user);
            logger.info("User registered successfully with ID: {}", registeredUser.getId());
            return ResponseEntity.ok(registeredUser);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to register user: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Unexpected error during user registration: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody AuthRequest authRequest) {
        logger.info("Received login request for username: {}", authRequest.getUsername());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtil.generateToken((org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal());
            logger.info("User {} logged in successfully. JWT issued.", authRequest.getUsername());
            return ResponseEntity.ok(jwt);
        } catch (Exception e) {
            logger.warn("Login failed for username: {}. Error: {}", authRequest.getUsername(), e.getMessage());
            return new ResponseEntity<>("Invalid username or password", HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        logger.info("Received logout request");
        // Since JWT is stateless, we can't invalidate the token on the server without a blacklist.
        // Instruct the client to discard the token.
        SecurityContextHolder.clearContext();
        logger.info("User logged out successfully (client-side token discard required)");
        return ResponseEntity.ok("Logout successful. Please discard the JWT token on the client side.");
    }

    @PostMapping("/email")
    public ResponseEntity<String> sendEmail(@RequestParam String email) {
        logger.info("Received request to send email to: {}", email);
        try {
            User user = userService.findByEmail(email);
            if (user == null) {
                logger.warn("No user found with email: {}", email);
                return new ResponseEntity<>("User with email " + email + " not found", HttpStatus.NOT_FOUND);
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Welcome to Swiggy Project");
            message.setText("Hello " + user.getUsername() + ",\n\nThank you for using Swiggy Project! This is a test email sent on " +
                    "June 11, 2025, at 10:14 PM IST.\n\nBest regards,\nSwiggy Team");
            mailSender.send(message);

            logger.info("Email sent successfully to: {}", email);
            return ResponseEntity.ok("Email sent successfully to " + email);
        } catch (Exception e) {
            logger.error("Failed to send email to {}: {}", email, e.getMessage(), e);
            return new ResponseEntity<>("Failed to send email: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

class AuthRequest {
    private String username;
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}