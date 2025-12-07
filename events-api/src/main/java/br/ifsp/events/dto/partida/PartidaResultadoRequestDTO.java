package br.ifsp.events.dto.partida;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para requisição de resultado de uma Partida")
public class PartidaResultadoRequestDTO {

    @NotNull
    @Min(0)
    @Schema(description = "Placar do Time 1", example = "2")
    private Integer time1Placar;

    @NotNull
    @Min(0)
    @Schema(description = "Placar do Time 2", example = "3")
    private Integer time2Placar;
}
