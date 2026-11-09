package com.powerconnect.controllers;

import com.powerconnect.entities.User;
import com.powerconnect.repositories.UserRepository;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

// ADMIN: manage the people who use the app (add technicians, admins and councillors, reset passwords)
@CrossOrigin
@RestController
public class UserController {

    public record NewUserRequest(String fullName, String email, String phone, String role,
                                 String area, String serviceType, String password) { }
    public record PasswordRequest(String newPassword) { }

    // The start of the user code for each role, e.g. TEC-005
    private static final Map<String, String> CODE_PREFIX = Map.of(
            "ADMIN", "ADM", "TECHNICIAN", "TEC", "COUNCILLOR", "CLR", "COMMUNITY", "CM");

    private final UserRepository userRepository;
    private final LoginCheck loginCheck;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserController(UserRepository userRepository, LoginCheck loginCheck) {
        this.userRepository = userRepository;
        this.loginCheck = loginCheck;
    }

    @GetMapping("/users")
    public List<User> list(@RequestHeader(value = "Token", required = false) String token) {
        loginCheck.userWithRole(token, "ADMIN");
        return userRepository.findAllByOrderByRoleAscFullNameAsc();
    }

    @PostMapping("/users")
    public User create(@RequestHeader(value = "Token", required = false) String token,
                       @RequestBody NewUserRequest request) {
        loginCheck.userWithRole(token, "ADMIN");
        if (!CODE_PREFIX.containsKey(request.role())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown role: " + request.role());
        }
        if (isEmpty(request.fullName()) || isEmpty(request.email()) || isEmpty(request.password())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name, email and password are required");
        }
        if (request.role().equals("TECHNICIAN") && isEmpty(request.serviceType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A technician needs a service");
        }
        if (userRepository.findByEmail(request.email()) != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "That email is already registered");
        }

        long number = userRepository.countByRole(request.role()) + 1;

        User user = new User();
        user.setUserCode(String.format("%s-%03d", CODE_PREFIX.get(request.role()), number));
        user.setRole(request.role());
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setArea(request.area());
        user.setServiceType(request.role().equals("TECHNICIAN") ? request.serviceType() : null);
        user.setLanguage("en");
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        return userRepository.save(user);
    }

    // Set a new password for someone who forgot theirs. Their old login is ended too.
    @PutMapping("/users/{id}/password")
    public void resetPassword(@RequestHeader(value = "Token", required = false) String token,
                              @PathVariable Integer id,
                              @RequestBody PasswordRequest request) {
        loginCheck.userWithRole(token, "ADMIN");
        if (isEmpty(request.newPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The new password is empty");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setToken(null);
        userRepository.save(user);
    }

    private boolean isEmpty(String text) {
        return text == null || text.isBlank();
    }
}
