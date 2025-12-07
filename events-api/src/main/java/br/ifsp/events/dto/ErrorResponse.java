package br.ifsp.events.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.Map;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "DTO para representar uma resposta de erro padronizada da API")
public class ErrorResponse {

    @Schema(description = "Carimbo de data/hora de quando o erro ocorreu (UTC)", example = "2025-10-02T22:30:00.12345Z", readOnly = true)
    private final Instant timestamp = Instant.now();

    @Schema(description = "Código de status HTTP", example = "404", readOnly = true)
    private int status;

    @Schema(description = "Breve descrição do status HTTP", example = "Not Found", readOnly = true)
    private String error;

    @Schema(description = "Mensagem detalhada do erro.", example = "Tarefa com ID 999 não encontrada.", readOnly = true)
    private String message;

    @Schema(description = "Caminho da URL onde o erro ocorreu.", example = "/tasks/999", readOnly = true)
    private String path;

    @Schema(description = "Mapa contendo erros de validação específicos por campo (aparece apenas em erros de validação).", readOnly = true)
    private Map<String, String> validationErrors;
}