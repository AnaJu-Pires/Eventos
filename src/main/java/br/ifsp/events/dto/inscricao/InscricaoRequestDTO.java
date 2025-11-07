package br.ifsp.events.dto.inscricao;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InscricaoRequestDTO {
    @NotNull(message = "O ID do time é obrigatório")
    private Long timeId;
}
