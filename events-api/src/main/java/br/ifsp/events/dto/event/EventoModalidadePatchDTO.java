package br.ifsp.events.dto.event;

import java.time.LocalDate;

import br.ifsp.events.model.FormatoEventoModalidade;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para atualização parcial de uma Modalidade de Evento")
public class EventoModalidadePatchDTO {
    @Schema(description = "ID da modalidade de evento", example = "1")
    private Long modalidadeId;
    @Schema(description = "Número máximo de times permitidos", example = "10")
    private int maxTimes;
    @Schema(description = "Data de fim das inscrições", example = "2023-12-31")
    private LocalDate dataFimInscricao;
    @Schema(description = "Formato da modalidade de evento", example = "PONTOS_CORRIDOS")
    private FormatoEventoModalidade formatoEventoModalidade;
}
