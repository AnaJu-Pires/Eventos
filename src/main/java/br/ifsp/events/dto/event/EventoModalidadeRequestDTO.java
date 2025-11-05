package br.ifsp.events.dto.event;

import java.time.LocalDate;

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

    @NotNull(message = "A data de fim de inscrição é obrigatória")
    private LocalDate dataFimInscricao;
    

    @NotNull(message = "O formato é obrigatório")
    private FormatoEventoModalidade formatoEventoModalidade;
}
