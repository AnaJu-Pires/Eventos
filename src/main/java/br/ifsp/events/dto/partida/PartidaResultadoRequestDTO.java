package br.ifsp.events.dto.partida;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PartidaResultadoRequestDTO {

    @NotNull
    @Min(0)
    private Integer time1Placar;

    @NotNull
    @Min(0)
    private Integer time2Placar;
}
