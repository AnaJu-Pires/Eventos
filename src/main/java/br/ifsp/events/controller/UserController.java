package br.ifsp.events.controller;

import br.ifsp.events.dto.ErrorResponse;
import br.ifsp.events.dto.user.UserResponseDTO;
import br.ifsp.events.dto.user.UserRoleUpdateDTO;
import br.ifsp.events.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@Tag(name = "Usuários", description = "Endpoints para gerenciamento de usuários")
@SecurityRequirement(name = "bearerAuth") // é necessário colocar token para acessar os endpoints
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Busca as informações do usuário logado", description = "Retorna os dados do usuário que está autenticado via token JWT.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dados do usuário retornados com sucesso"),
        @ApiResponse(responseCode = "403", description = "Acesso negado. O token não foi fornecido ou é inválido")
    })
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getMyInfo(Authentication authentication) {
        UserResponseDTO userInfo = userService.getMyInfo(authentication);
        return ResponseEntity.ok(userInfo);
    }

    @Operation(summary = "Atualiza o perfil de um usuário", description = "Permite que um administrador (`ADMIN`) altere o perfil de qualquer usuário no sistema.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Perfil do usuário atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos (ex: perfil não existente)",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Acesso negado. Apenas usuários com o perfil 'ADMIN' podem executar esta ação"),
        @ApiResponse(responseCode = "404", description = "Usuário com o ID especificado não encontrado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> updateUserRole(
            @Parameter(description = "ID do usuário a ser atualizado") @PathVariable Long id, 
            @RequestBody @Valid UserRoleUpdateDTO roleUpdateDTO) {
        
        UserResponseDTO updatedUser = userService.updateUserRole(id, roleUpdateDTO);
        return ResponseEntity.ok(updatedUser);
    }
}