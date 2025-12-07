package br.ifsp.events.controller;

import br.ifsp.events.dto.ErrorResponse;
import br.ifsp.events.dto.user.UserLoginDTO;
import br.ifsp.events.dto.user.UserLoginResponseDTO;
import br.ifsp.events.dto.user.UserRegisterDTO;
import br.ifsp.events.dto.user.UserResponseDTO;
import br.ifsp.events.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import br.ifsp.events.dto.MessageResponseDTO;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "Endpoints para registro e login de usuários")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Inicia o registro de um novo usuário", description = "Recebe os dados de cadastro, cria um usuário inativo e dispara o envio de um e-mail de confirmação.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Solicitação de registro processada com sucesso", 
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos...",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<MessageResponseDTO> register(@RequestBody @Valid UserRegisterDTO registerDTO) {
        userService.registerUser(registerDTO);
        var response = new MessageResponseDTO("Solicitação de cadastro criada com sucesso. Um e-mail de confirmação foi enviado.");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Confirma o registro de um usuário", description = "Valida o token recebido por e-mail e, se válido, ativa a conta do usuário.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuário confirmado e ativado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Regra de negócio violada (ex: token expirado, conta já ativa)",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Token de confirmação não encontrado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/confirm")
    public ResponseEntity<UserResponseDTO> confirm(@Parameter(description = "Token de confirmação recebido por e-mail") @RequestParam("token") String token) {
        UserResponseDTO confirmedUser = userService.confirmUser(token);
        return ResponseEntity.ok(confirmedUser);
    }

    @Operation(summary = "Autentica um usuário", description = "Recebe e-mail e senha, e se as credenciais forem válidas, retorna um token JWT para acesso aos endpoints protegidos.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login bem-sucedido, token JWT retornado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado (credenciais inválidas ou conta inativa)")
    })
    @PostMapping("/login")
    public ResponseEntity<UserLoginResponseDTO> login(@RequestBody @Valid UserLoginDTO loginDTO) {
        return ResponseEntity.ok(userService.login(loginDTO));
    }
    
}