package br.ifsp.events.dto.comentario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "DTO para criação de um novo Comentário")
public class ComentarioCreateDTO {

    @NotBlank(message = "O conteúdo não pode estar vazio")
    @Schema(description = "Corpo de texto do comentário")
    private String conteudo;

    @Schema(description = "ID do comentário pai (nulo se for um comentário principal no post)")
    private Long comentarioPaiId;
}