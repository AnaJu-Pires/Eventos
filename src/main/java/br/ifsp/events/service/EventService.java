package br.ifsp.events.service;

import br.ifsp.events.dto.event.EventRequestDTO;
import br.ifsp.events.dto.event.EventResponseDTO;

public interface EventService {
    EventResponseDTO create(EventRequestDTO eventRequestDTO);

    EventResponseDTO update(Long id, EventRequestDTO eventRequestDTO);
}