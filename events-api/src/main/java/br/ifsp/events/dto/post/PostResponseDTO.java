package br.ifsp.events.dto.post;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "DTO de resposta para um Post")
public class PostResponseDTO {

    @Schema(description = "ID único do post", example = "1")
    private Long id;

    @Schema(description = "Título do post", example = "Início do Evento")
    private String titulo;

    @Schema(description = "Corpo de texto do post", example = "Estamos animados para o início do nosso evento!")
    private String conteudo;

    @Schema(description = "Nome do autor do post", example = "João Silva")
    private String autorNome;

    @Schema(description = "Nome da comunidade onde o post foi criado", example = "Comunidade de Eventos")
    private String comunidadeNome;

    @Schema(description = "Data e hora da criação", example = "2023-03-15T10:00:00")
    private LocalDateTime dataCriacao;

    @Schema(description = "Placar de votos (upvotes - downvotes)", example = "5")
    private int votos;
}