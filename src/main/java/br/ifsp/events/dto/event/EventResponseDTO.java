package br.ifsp.events.dto.event;

import java.time.LocalDate;
import java.util.Set;
import java.util.List;

import br.ifsp.events.model.StatusEvento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponseDTO {
    
    private Long id;
    private String nome;
    private String descricao;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private StatusEvento status;
    private String organizadorNome;
    private Set<EventoModalidadeResponseDTO> eventoModalidades;
    private List<br.ifsp.events.dto.partida.PartidaResponseDTO> partidas;
}
