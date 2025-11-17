package br.ifsp.events.dto.convite;

import br.ifsp.events.dto.time.TimeResponseDTO;
import br.ifsp.events.model.StatusConvite;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO para representar um convite pendente")
public class ConviteResponseDTO {

    @Schema(description = "ID único do convite", example = "1", readOnly = true)
    private Long id;

    @Schema(description = "Dados do time que está fazendo o convite", example = "{ \"id\": 1, \"nome\": \"Time A\" }")
    private TimeResponseDTO time;

    @Schema(description = "Status atual do convite", example = "PENDENTE")
    private StatusConvite status;

    @Schema(description = "Data e hora em que o convite irá expirar", readOnly = true, example = "2023-12-31T23:59:59")
    private LocalDateTime dataExpiracao;
}