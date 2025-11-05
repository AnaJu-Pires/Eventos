package br.ifsp.events.dto.event;

import java.time.LocalDate;

import br.ifsp.events.model.FormatoEventoModalidade;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventoModalidadeResponseDTO {

    private Long id;
    private String modalidadeNome;
    private int maxTimes;
    private LocalDate dataFimInscricao;
    private FormatoEventoModalidade formatoEventoModalidade;
}
