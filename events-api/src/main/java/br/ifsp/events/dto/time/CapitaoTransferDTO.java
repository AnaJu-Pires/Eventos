package br.ifsp.events.dto.time;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Transferência de capitão")
public class CapitaoTransferDTO {

    @Schema(description = "ID do time", example = "1")
    @NotNull(message = "O ID do novo capitão é obrigatório")
    private Long novoCapitaoId;
}