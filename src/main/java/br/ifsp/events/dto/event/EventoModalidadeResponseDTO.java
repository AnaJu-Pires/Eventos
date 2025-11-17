package br.ifsp.events.dto.event;

import java.time.LocalDate;

import br.ifsp.events.model.FormatoEventoModalidade;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO de resposta para uma Modalidade de Evento")
public class EventoModalidadeResponseDTO {

    @Schema(description = "ID único da modalidade de evento", example = "1")
    private Long id;
    @Schema(description = "Nome da modalidade de evento", example = "Futebol")
    private String modalidadeNome;
    @Schema(description = "Número máximo de times permitidos", example = "10")
    private int maxTimes;
    @Schema(description = "Data de fim das inscrições", example = "2023-12-31")
    private LocalDate dataFimInscricao;
    @Schema(description = "Formato da modalidade de evento", example = "MATA_MATA")
    private FormatoEventoModalidade formatoEventoModalidade;
}
