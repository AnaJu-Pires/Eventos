package br.ifsp.events.dto.partida;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO de resposta para uma Partida")
public class PartidaResponseDTO {
    @Schema(description = "ID único da partida", example = "1")
    private Long id;
    @Schema(description = "Rodada da partida", example = "1")
    private int round;
    @Schema(description = "Status da partida", example = "AGENDADA")
    private String statusPartida;
    @Schema(description = "Nome do Time 1", example = "Time A")
    private String time1Nome;
    @Schema(description = "Placar do Time 1", example = "2")
    private int time1Placar;
    @Schema(description = "Nome do Time 2", example = "Time B")
    private String time2Nome;
    @Schema(description = "Placar do Time 2", example = "3")
    private int time2Placar;
    @Schema(description = "Nome do Vencedor", example = "Time A")
    private String vencedorNome;
}