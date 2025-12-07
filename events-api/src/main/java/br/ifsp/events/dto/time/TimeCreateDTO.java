package br.ifsp.events.dto.time;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Objeto de criação de um time")
public class TimeCreateDTO {

    @Schema(description = "Nome do time", example = "Time A")
    @NotBlank(message = "O nome do time é obrigatório")
    private String nome;

    @Schema(description = "ID da modalidade", example = "1")
    @NotNull(message = "O ID da modalidade é obrigatório")
    private Long modalidadeId;
}