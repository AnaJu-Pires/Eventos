package br.ifsp.events.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Schema(description = "DTO para criação de um novo Post")
@AllArgsConstructor
@NoArgsConstructor
public class PostCreateDTO {

    @NotBlank(message = "O título é obrigatório")
    @Size(min = 3, max = 255, message = "O título deve ter entre 3 e 255 caracteres")
    @Schema(description = "Título do post", example = "Início do Evento")
    private String titulo;

    @NotBlank(message = "O conteúdo é obrigatório")
    @Schema(description = "Corpo de texto do post (pode conter markdown)", example = "Estamos animados para o início do nosso evento!")
    private String conteudo;
}