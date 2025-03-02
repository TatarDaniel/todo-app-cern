package ch.cern.todo.controller;

import ch.cern.todo.dto.RegisterRequest;
import ch.cern.todo.entity.User;
import ch.cern.todo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<String> registerUser(@Valid @RequestBody final RegisterRequest request) {
        final User user = userService.registerUser(request.getUsername(), request.getPassword(), "ROLE_USER");

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("User " + user.getUsername() + " registered successfully!");
    }
}
