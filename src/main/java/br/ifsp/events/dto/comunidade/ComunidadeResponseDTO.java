package br.ifsp.events.dto.comunidade;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ComunidadeResponseDTO {
    private Long id;
    private String nome;
    private String descricao;
    private String criadorNome;
    private LocalDateTime dataCriacao;
}
