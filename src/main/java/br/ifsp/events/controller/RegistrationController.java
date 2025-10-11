package br.ifsp.events.controller;

import br.ifsp.events.dto.user.UserRegisterDTO;
import br.ifsp.events.dto.user.UserResponseDTO;
import br.ifsp.events.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cadastro")
public class RegistrationController {

    private final UserService userService;

    public RegistrationController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<String> register(@RequestBody @Valid UserRegisterDTO registerDTO) {
        userService.registerUser(registerDTO);
        return ResponseEntity.ok("Solicitação de cadastro criada com sucesso. Um e-mail de confirmação foi enviado.");
    }

    //recebe o token por parâmetro na URL e confirma o cadastro do usuário.
    @GetMapping("/confirmar")
    public ResponseEntity<UserResponseDTO> confirm(@RequestParam("token") String token) {
        UserResponseDTO confirmedUser = userService.confirmUser(token);
        return ResponseEntity.ok(confirmedUser);
    }
}