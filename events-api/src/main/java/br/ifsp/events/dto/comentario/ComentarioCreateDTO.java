package br.ifsp.events.dto.comentario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Schema(description = "DTO para criação de um novo Comentário")
@AllArgsConstructor
@NoArgsConstructor
public class ComentarioCreateDTO {

    @NotBlank(message = "O conteúdo não pode estar vazio")
    @Schema(description = "Corpo de texto do comentário", example = "Muito bom o evento!")
    private String conteudo;

    @Schema(description = "ID do comentário pai (nulo se for um comentário principal no post)", example = "1")
    private Long comentarioPaiId;
}