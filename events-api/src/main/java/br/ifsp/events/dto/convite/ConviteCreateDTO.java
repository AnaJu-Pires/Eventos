package br.ifsp.events.dto.convite;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO para enviar um convite para um time")
public class ConviteCreateDTO {

    @Schema(description = "E-mail institucional do usuário a ser convidado",
            example = "membro@aluno.ifsp.edu.br",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Email(regexp = "^[A-Za-z0-9._%+-]+@(aluno\\.)?ifsp\\.edu\\.br$")
    private String emailUsuario;
}