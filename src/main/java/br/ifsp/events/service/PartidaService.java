package br.ifsp.events.service;

import java.util.List;

import br.ifsp.events.dto.partida.PartidaResponseDTO;

public interface PartidaService {

    List<PartidaResponseDTO> listByEvento(Long eventoId);
}