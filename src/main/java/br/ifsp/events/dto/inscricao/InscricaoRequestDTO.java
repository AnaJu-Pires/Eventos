package br.ifsp.events.dto.inscricao;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para requisição de inscrição em uma modalidade de evento")
public class InscricaoRequestDTO {
    @NotNull(message = "O ID do time é obrigatório")
    @Schema(description = "ID do time", example = "1")
    private Long timeId;
}
