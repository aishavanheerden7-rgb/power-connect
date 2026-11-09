package com.powerconnect.controllers;

import com.powerconnect.entities.User;
import com.powerconnect.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

// Every request (except login) sends a "Token" header.
// This class turns that token back into the logged-in user, and checks their role.
@Component
public class LoginCheck {

    private final UserRepository userRepository;

    public LoginCheck(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Returns the logged-in user, or stops the request with 401 Unauthorized
    public User user(String token) {
        User user = null;
        if (token != null) {
            user = userRepository.findByToken(token);
        }
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in");
        }
        return user;
    }

    // Same as above, but the user must also have the given role, otherwise 403 Forbidden
    public User userWithRole(String token, String role) {
        User user = user(token);
        if (!user.getRole().equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only " + role + " users can do this");
        }
        return user;
    }
}
