package br.ifsp.events.dto.event;

import java.time.LocalDate;
import java.util.Set;
import java.util.List;

import br.ifsp.events.model.StatusEvento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO de resposta para um Evento")
public class EventResponseDTO {
    
    @Schema(description = "ID único do evento", example = "1")
    private Long id;
    @Schema(description = "Nome do evento", example = "Campeonato de Futebol")
    private String nome;
    @Schema(description = "Descrição do evento", example = "Campeonato de Futebol 2023")
    private String descricao;
    @Schema(description = "Data de início do evento", example = "2023-01-01")
    private LocalDate dataInicio;
    @Schema(description = "Data de fim do evento", example = "2023-12-31")
    private LocalDate dataFim;
    @Schema(description = "Status do evento", example = "ATIVO")
    private StatusEvento status;
    @Schema(description = "Nome do organizador do evento", example = "Gestor")
    private String organizadorNome;
    @Schema(description = "Modalidades do evento")
    private Set<EventoModalidadeResponseDTO> eventoModalidades;
    @Schema(description = "Partidas do evento")
    private List<br.ifsp.events.dto.partida.PartidaResponseDTO> partidas;
}
