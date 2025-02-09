package ch.cern.todo.controller;

import ch.cern.todo.dto.RegisterRequest;
import ch.cern.todo.entity.User;
import ch.cern.todo.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/auth")
public class AuthController {
    private final UserService userService;

    public AuthController(final UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody final RegisterRequest request) {
        try {
            final User user = userService.registerUser(request.getUsername(), request.getPassword(), "ROLE_USER");
            return ResponseEntity.ok("User " + user.getUsername() + " registered successfully!");
        } catch (final RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
