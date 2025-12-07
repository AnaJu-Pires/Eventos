package br.ifsp.events.dto.time;

import java.util.Set;

import br.ifsp.events.dto.modalidade.ModalidadeResponseDTO;
import br.ifsp.events.dto.user.UserResponseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Representação de um time")
public class TimeResponseDTO {
    @Schema(description = "ID do time", example = "1")
    private Long id;
    @Schema(description = "Nome do time", example = "Time A")
    private String nome;
    @Schema(description = "Capitão do time")
    private UserResponseDTO capitao;
    @Schema(description = "Modalidade do time")
    private ModalidadeResponseDTO modalidade;
    @Schema(description = "Membros do time")
    private Set<UserResponseDTO> membros;
    @Schema(description = "Taxa de vitórias do time")
    private double winRate;
}