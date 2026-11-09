package com.powerconnect.controllers;

import com.powerconnect.entities.User;
import com.powerconnect.repositories.UserRepository;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

// Logging in and out, and choosing a language
@CrossOrigin   // lets the web version of the app (a different address) call this API
@RestController
public class AuthController {

    // What the app sends in the request body
    public record LoginRequest(String email, String password) { }
    public record LanguageRequest(String language) { }
    public record ChangePasswordRequest(String oldPassword, String newPassword) { }
    public record RegisterRequest(String fullName, String email, String phone, String area,
                                  String password, String language) { }

    private final UserRepository userRepository;
    private final LoginCheck loginCheck;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthController(UserRepository userRepository, LoginCheck loginCheck) {
        this.userRepository = userRepository;
        this.loginCheck = loginCheck;
    }

    // Check email + password. If correct, create a token the app sends with every later request.
    @PostMapping("/auth/login")
    public Map<String, Object> login(@RequestBody LoginRequest request) {
        User user = userRepository.findByEmail(request.email());
        if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong email or password");
        }
        // Keep the existing token if there is one, so the user can be logged in
        // on a phone and in the browser at the same time
        if (user.getToken() == null) {
            user.setToken(UUID.randomUUID().toString());
            userRepository.save(user);
        }
        return Map.of("token", user.getToken(), "user", user);
    }

    // A community member creates their own account, and is logged in straight away.
    // (Admins and technicians are added by the organisation, not through the app.)
    @PostMapping("/auth/register")
    public Map<String, Object> register(@RequestBody RegisterRequest request) {
        if (isEmpty(request.fullName()) || isEmpty(request.email()) || isEmpty(request.password())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name, email and password are required");
        }
        if (userRepository.findByEmail(request.email()) != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "That email is already registered");
        }

        // The next community code: CM-001, CM-002, ...
        long number = userRepository.countByRole("COMMUNITY") + 1;

        User user = new User();
        user.setUserCode(String.format("CM-%03d", number));
        user.setRole("COMMUNITY");
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setArea(request.area());
        user.setLanguage(request.language() == null ? "en" : request.language());
        user.setPasswordHash(passwordEncoder.encode(request.password()));   // never store the plain password
        user.setToken(UUID.randomUUID().toString());
        userRepository.save(user);

        return Map.of("token", user.getToken(), "user", user);
    }

    private boolean isEmpty(String text) {
        return text == null || text.isBlank();
    }

    @PostMapping("/auth/logout")
    public void logout(@RequestHeader(value = "Token", required = false) String token) {
        User user = loginCheck.user(token);
        user.setToken(null);
        userRepository.save(user);
    }

    // Who am I? (the app calls this when it starts, to check the saved token still works)
    @GetMapping("/auth/me")
    public User me(@RequestHeader(value = "Token", required = false) String token) {
        return loginCheck.user(token);
    }

    // Change your own password (you must know the old one)
    @PutMapping("/auth/password")
    public void changePassword(@RequestHeader(value = "Token", required = false) String token,
                               @RequestBody ChangePasswordRequest request) {
        User user = loginCheck.user(token);
        if (request.oldPassword() == null || !passwordEncoder.matches(request.oldPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The old password is wrong");
        }
        if (isEmpty(request.newPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The new password is empty");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    // Save the language the user picked: en, bem or ny
    @PutMapping("/auth/language")
    public User setLanguage(@RequestHeader(value = "Token", required = false) String token,
                            @RequestBody LanguageRequest request) {
        User user = loginCheck.user(token);
        user.setLanguage(request.language());
        return userRepository.save(user);
    }
}
