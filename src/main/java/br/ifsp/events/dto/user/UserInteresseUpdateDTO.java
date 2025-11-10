package br.ifsp.events.dto.user;

import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para atualizar a lista de interesses de um usuário")
public class UserInteresseUpdateDTO {

    @Schema(description = "Lista de IDs das modalidades de interesse", 
            example = "[1, 5, 12]", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "A lista de interesses não pode estar vazia")
    private List<Long> modalidadeIds;
}