package br.ifsp.events.dto.event;

import br.ifsp.events.model.FormatoEventoModalidade;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventoModalidadeRequestDTO {

    @NotNull(message = "O ID da modalidade é obrigatório")
    private Long modalidadeId;

    private int maxTimes;
    private int minJogadoresPorTime;
    private int maxJogadoresPorTime;

    @NotNull(message = "O formato é obrigatório")
    private FormatoEventoModalidade formatoEventoModalidade;
}
