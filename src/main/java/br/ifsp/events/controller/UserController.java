package br.ifsp.events.controller;

import br.ifsp.events.dto.user.UserResponseDTO;
import br.ifsp.events.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getMyInfo(Authentication authentication) {
        UserResponseDTO userInfo = userService.getMyInfo(authentication);
        return ResponseEntity.ok(userInfo);
    }
}