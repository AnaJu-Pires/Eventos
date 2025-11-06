package br.ifsp.events.dto.partida;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PartidaResponseDTO {
    private Long id;
    private int round;
    private String statusPartida;
    
    private String time1Nome;
    private int time1Placar;

    private String time2Nome;
    private int time2Placar;

    private String vencedorNome;
}