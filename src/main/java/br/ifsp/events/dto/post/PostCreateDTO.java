package br.ifsp.events.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "DTO para criação de um novo Post")
public class PostCreateDTO {

    @NotBlank(message = "O título é obrigatório")
    @Size(min = 3, max = 255, message = "O título deve ter entre 3 e 255 caracteres")
    @Schema(description = "Título do post")
    private String titulo;

    @NotBlank(message = "O conteúdo é obrigatório")
    @Schema(description = "Corpo de texto do post (pode conter markdown)")
    private String conteudo;
}