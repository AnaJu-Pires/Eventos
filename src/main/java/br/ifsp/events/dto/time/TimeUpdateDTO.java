package br.ifsp.events.dto.time;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Atualização de um time")
public class TimeUpdateDTO {

    @NotBlank(message = "O nome do time não pode estar vazio")
    @Schema(description = "Novo nome do time", example = "Time B")
    private String nome;
}