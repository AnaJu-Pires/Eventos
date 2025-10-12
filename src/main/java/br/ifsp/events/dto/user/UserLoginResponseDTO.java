package br.ifsp.events.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Resposta de login de usuário")
public class UserLoginResponseDTO {
    @NotBlank
    @Schema(description = "Token de autenticação")
    private String token;
}
