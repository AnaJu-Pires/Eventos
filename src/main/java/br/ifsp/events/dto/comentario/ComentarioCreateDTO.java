package br.ifsp.events.dto.comentario;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ComentarioCreateDTO {

    @NotBlank(message = "O conteúdo não pode estar vazio")
    private String conteudo;

    private Long comentarioPaiId;
}