package br.ifsp.events.dto.event;

import java.time.LocalDate;

import br.ifsp.events.model.FormatoEventoModalidade;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para requisição de criação de uma Modalidade de Evento")
public class EventoModalidadeRequestDTO {

    @NotNull(message = "O ID da modalidade é obrigatório")
    @Schema(description = "ID da modalidade", example = "1")
    private Long modalidadeId;

    @NotNull(message = "O número máximo de times é obrigatório")
    @Schema(description = "Número máximo de times permitidos", example = "10")
    private int maxTimes;

    @NotNull(message = "A data de fim de inscrição é obrigatória")
    @Schema(description = "Data de fim das inscrições", example = "2023-12-31")
    private LocalDate dataFimInscricao;
    

    @NotNull(message = "O formato é obrigatório")
    @Schema(description = "Formato da modalidade de evento", example = "MATA_MATA")
    private FormatoEventoModalidade formatoEventoModalidade;
}
