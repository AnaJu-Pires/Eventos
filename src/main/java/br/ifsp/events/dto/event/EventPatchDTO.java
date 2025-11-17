package br.ifsp.events.dto.event;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para atualização parcial de um Evento")
public class EventPatchDTO {
    @Schema(description = "Nome do evento", example = "Campeonato de Futebol")
    private Optional<String> nome = Optional.empty();
    @Schema(description = "Descrição do evento", example = "Campeonato de Futebol 2023")
    private Optional<String> descricao = Optional.empty();
    @Schema(description = "Data de início do evento", example = "2023-01-01")
    private Optional<LocalDate> dataInicio = Optional.empty();
    @Schema(description = "Data de fim do evento", example = "2023-12-31")
    private Optional<LocalDate> dataFim = Optional.empty();
    @Schema(description = "Modalidades do evento")
    private Optional<Set<EventoModalidadePatchDTO>> modalidades = Optional.empty();
}