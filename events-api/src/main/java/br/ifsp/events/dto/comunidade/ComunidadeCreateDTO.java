package br.ifsp.events.dto.comunidade;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO para criação de uma nova Comunidade")
public class ComunidadeCreateDTO {
    @NotBlank(message = "O nome é obrigatório")
    @Size(min = 3, max = 50, message = "O nome deve ter entre 3 e 50 caracteres") 
    @Schema(description = "Nome único da comunidade", example = "Clube de Xadrez BTV")
    private String nome;
    
    @NotBlank(message = "A descrição é obrigatória")
    @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres")
    @Schema(description = "Breve descrição sobre o tópico da comunidade", example = "Espaço para fãs de xadrez do campus.")
    private String descricao;
}
