package br.ifsp.events.dto.event;

import br.ifsp.events.model.FormatoEventoModalidade;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO para requisição de geração de chave (ex.: MATA_MATA ou PONTOS_CORRIDOS).
 */
@Data
public class GerarChaveRequestDTO {
    @NotNull
    private FormatoEventoModalidade formato;
}