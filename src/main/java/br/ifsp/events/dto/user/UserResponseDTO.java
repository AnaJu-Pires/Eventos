package br.ifsp.events.dto.user;

import br.ifsp.events.model.NivelEngajamento;
import br.ifsp.events.model.PerfilUser;
import br.ifsp.events.model.RankEngajamento;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Informações de um usuário")
public class UserResponseDTO {
    @Schema(description = "ID do usuário")
    private Long id;
    @Schema(description = "Nome do usuário")
    private String nome;
    @Schema(description = "E-mail do usuário")
    private String email;
    @Schema(description = "Perfil do usuário")
    private PerfilUser perfilUser;
    @Schema(description = "Pontos saldo do usuário")
    private Long pontosSaldo;
    @Schema(description = "Nivel de engajamento do usuário")
    private NivelEngajamento nivel;
    @Schema(description = "Rank de engajamento do usuário")
    private RankEngajamento rank;
}
