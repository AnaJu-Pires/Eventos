package br.ifsp.events.service;

import br.ifsp.events.dto.event.EventPatchDTO;
import br.ifsp.events.dto.event.EventRequestDTO;
import br.ifsp.events.dto.event.EventResponseDTO;
import br.ifsp.events.dto.inscricao.InscricaoResponseDTO;

public interface EventService {
    EventResponseDTO create(EventRequestDTO eventRequestDTO);

    EventResponseDTO update(Long id, EventRequestDTO eventRequestDTO);

    void delete(Long id);

    EventResponseDTO patch(Long id, EventPatchDTO eventPatchDTO);

    InscricaoResponseDTO inscreverTime(Long eventoId, Long timeId);

    /**
     * Lista todos os eventos do sistema.
     */
    java.util.List<EventResponseDTO> listAll();

    /**
     * Busca um evento pelo id.
     */
    EventResponseDTO findById(Long id);

}