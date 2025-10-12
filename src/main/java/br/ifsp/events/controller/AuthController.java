package br.ifsp.events.controller;

import br.ifsp.events.dto.user.UserLoginDTO;
import br.ifsp.events.dto.user.UserLoginResponseDTO;
import br.ifsp.events.dto.user.UserRegisterDTO;
import br.ifsp.events.dto.user.UserResponseDTO;
import br.ifsp.events.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid UserRegisterDTO registerDTO) {
        userService.registerUser(registerDTO);
        return ResponseEntity.ok("Solicitação de cadastro criada com sucesso. Um e-mail de confirmação foi enviado.");
    }

    @GetMapping("/confirm")
    public ResponseEntity<UserResponseDTO> confirm(@RequestParam("token") String token) {
        UserResponseDTO confirmedUser = userService.confirmUser(token);
        return ResponseEntity.ok(confirmedUser);
    }

    @PostMapping("/login")
    public ResponseEntity<UserLoginResponseDTO> login(@RequestBody @Valid UserLoginDTO loginDTO) {
        return ResponseEntity.ok(userService.login(loginDTO));
    }
    
}