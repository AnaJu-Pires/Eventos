package br.ifsp.events.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.ifsp.events.dto.ErrorResponse;
import br.ifsp.events.dto.user.UserInteresseResponseDTO;
import br.ifsp.events.dto.user.UserInteresseUpdateDTO;
import br.ifsp.events.dto.user.UserResponseDTO;
import br.ifsp.events.dto.user.UserRoleUpdateDTO;
import br.ifsp.events.exception.CsvGenerationException;
import br.ifsp.events.model.User;
import br.ifsp.events.service.UserService;
import br.ifsp.events.util.CsvGenerator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
@Tag(name = "Usuários", description = "Endpoints para gerenciamento de usuários")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Busca as informações do usuário logado", description = "Retorna os dados do usuário que está autenticado via token JWT.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dados do usuário retornados com sucesso",
            content = @Content(mediaType = "application/json", 
                         schema = @Schema(implementation = UserResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado. Token não fornecido ou inválido",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getMyInfo(
            @Parameter(hidden = true) Authentication authentication) {
        UserResponseDTO userInfo = userService.getMyInfo(authentication);
        return ResponseEntity.ok(userInfo);
    }

    @Operation(summary = "Atualiza o perfil de um usuário (Admin)", description = "Permite que um administrador (`ADMIN`) altere o perfil de qualquer usuário no sistema.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Perfil do usuário atualizado com sucesso",
            content = @Content(mediaType = "application/json", 
                         schema = @Schema(implementation = UserResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos (ex: perfil não existente)",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado. Token não fornecido ou inválido",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Acesso negado. Apenas usuários com o perfil 'ADMIN' podem executar esta ação",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Usuário com o ID especificado não encontrado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> updateUserRole(
            @Parameter(description = "ID do usuário a ser atualizado", example = "1") @PathVariable Long id, 
            @RequestBody @Valid UserRoleUpdateDTO roleUpdateDTO) {
        
        UserResponseDTO updatedUser = userService.updateUserRole(id, roleUpdateDTO);
        return ResponseEntity.ok(updatedUser);
    }

    @Operation(summary = "Exclui o perfil de um usuário (Admin)", description = "Permite que um administrador (`ADMIN`) remova o perfil de qualquer usuário no sistema.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Perfil do usuário removido com sucesso",
            content = @Content(mediaType = "application/json", 
                         schema = @Schema(implementation = UserResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos (ex: perfil não existente)",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado. Token não fornecido ou inválido",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Acesso negado. Apenas usuários com o perfil 'ADMIN' podem executar esta ação",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Usuário com o ID especificado não encontrado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Busca as modalidades de interesse do usuário logado", 
               description = "Retorna a lista de modalidades que o usuário autenticado marcou como interesse.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de interesses retornada com sucesso",
            content = @Content(mediaType = "application/json", 
                         schema = @Schema(implementation = UserInteresseResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado. Token não fornecido ou inválido",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/me/interesses")
    public ResponseEntity<UserInteresseResponseDTO> getMeusInteresses(
            @Parameter(hidden = true) Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        UserInteresseResponseDTO responseDTO = userService.getUserInteresses(user.getId());
        return ResponseEntity.ok(responseDTO);
    }

    @Operation(summary = "Atualiza as modalidades de interesse do usuário logado", 
               description = "Substitui a lista de interesses do usuário autenticado pela lista fornecida.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Interesses atualizados com sucesso",
            content = @Content(mediaType = "application/json", 
                         schema = @Schema(implementation = UserInteresseResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos (ex: lista vazia ou IDs não existentes)",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado. Token não fornecido ou inválido",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Uma das modalidades com o ID especificado não foi encontrada",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/me/interesses")
    public ResponseEntity<UserInteresseResponseDTO> atualizarMeusInteresses(
            @Parameter(hidden = true) Authentication authentication,
            @Valid @RequestBody UserInteresseUpdateDTO interessesDTO) {
        User user = (User) authentication.getPrincipal();
        UserInteresseResponseDTO responseDTO = userService.updateUserInteresses(user.getId(), interessesDTO);
        return ResponseEntity.ok(responseDTO);
    }

    @Operation(summary = "Exporta perfis de usuários como CSV (Admin)", 
               description = "Baixa um arquivo CSV contendo todos os usuários e seus perfis. Requer perfil de 'ADMIN'.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Arquivo CSV gerado com sucesso",
            content = @Content(mediaType = "text/csv", 
                         schema = @Schema(type = "string", format = "binary"))),
        @ApiResponse(responseCode = "401", description = "Não autenticado. Token não fornecido ou inválido",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Acesso negado. Apenas usuários com o perfil 'ADMIN' podem executar esta ação",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Erro interno ao gerar o arquivo CSV",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/perfis/csv")
    @PreAuthorize("hasRole('ADMIN')")
    public void downloadPerfisUsuariosCsv(
            @Parameter(hidden = true) HttpServletResponse response) {
        response.setContentType("text/csv");
        response.setCharacterEncoding("UTF-8");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"perfis_usuarios.csv\"");

        try (PrintWriter writer = response.getWriter()) {
            List<UserResponseDTO> userProfiles = userService.listarPerfisUsuarios();
            CsvGenerator.generateUserProfilesCsv(userProfiles, writer);
            writer.flush();
        } catch (IOException e) {
            throw new CsvGenerationException("Erro ao gerar o arquivo CSV de perfis de usuários.");
        }
    }

}