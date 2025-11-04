package br.ifsp.events.dto.event;

import br.ifsp.events.model.FormatoEventoModalidade;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventoModalidadePatchDTO {
    private Long modalidadeId;
    private Integer maxTimes;
    private Integer minJogadoresPorTime;
    private Integer maxJogadoresPorTime;
    private FormatoEventoModalidade formatoEventoModalidade;
}
