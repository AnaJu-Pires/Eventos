package br.ifsp.events.dto.user;

import br.ifsp.events.model.PerfilUser;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Informações públicas de um usuário")
public class UserResponseDTO {

    @Schema(description = "ID do usuário", example = "1")
    private Long id;

    @Schema(description = "Nome do usuário", example = "Ana Julia")
    private String nome;

    @Schema(description = "E-mail do usuário", example = "ana@aluno.ifsp.edu.br")
    private String email;

    @Schema(description = "Perfil do usuário", example = "ALUNO")
    private PerfilUser perfilUser;
}