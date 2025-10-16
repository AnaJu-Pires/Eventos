package br.ifsp.events.dto.modalidade;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ModalidadeDTO {

    private Long id;

    @NotBlank(message = "O nome da modalidade é obrigatório")
    @Size(min = 3, max = 100)
    private String nome;

    // Novo campo adicionado
    @Size(max = 255, message = "A descrição não pode exceder 255 caracteres.")
    private String descricao;
}