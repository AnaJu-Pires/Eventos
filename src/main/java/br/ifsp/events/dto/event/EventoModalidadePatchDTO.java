package br.ifsp.events.dto.event;

import java.time.LocalDate;

import br.ifsp.events.model.FormatoEventoModalidade;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventoModalidadePatchDTO {
    private Long modalidadeId;
    private int maxTimes;
    private LocalDate dataFimInscricao;
    private FormatoEventoModalidade formatoEventoModalidade;
}
