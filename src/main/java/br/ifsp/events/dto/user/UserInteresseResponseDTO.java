package br.ifsp.events.dto.user;

import java.util.List;
import br.ifsp.events.dto.modalidade.ModalidadeResponseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para exibir os interesses (modalidades) de um usuário")
public class UserInteresseResponseDTO {

    @Schema(description = "Lista de modalidades que o usuário tem interesse", example = "[{\"id\":1,\"nome\":\"Futebol\",\"descricao\":\"Modalidade de futebol de campo.\"}]")
    private List<ModalidadeResponseDTO> interesses;
}