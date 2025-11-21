package br.ifsp.events.dto.event;

import br.ifsp.events.model.FormatoEventoModalidade;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;


@Data
@Schema(description = "DTO para requisição de geração de chave (ex.: MATA_MATA ou PONTOS_CORRIDOS).")
public class GerarChaveRequestDTO {
    @NotNull
    @Schema(description = "Formato da modalidade de evento", example = "MATA_MATA")
    private FormatoEventoModalidade formato;
}