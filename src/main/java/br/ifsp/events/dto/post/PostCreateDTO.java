package br.ifsp.events.dto.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PostCreateDTO {

    @NotBlank(message = "O título é obrigatório")
    @Size(min = 3, max = 255, message = "O título deve ter entre 3 e 255 caracteres")
    private String titulo;

    @NotBlank(message = "O conteúdo é obrigatório")
    private String conteudo;
}