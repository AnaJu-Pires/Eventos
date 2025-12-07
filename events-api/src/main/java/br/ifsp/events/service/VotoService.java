package br.ifsp.events.service;

import br.ifsp.events.dto.voto.VotoCreateDTO;
import br.ifsp.events.dto.voto.VotoResponseDTO;

public interface VotoService {

    VotoResponseDTO votar(VotoCreateDTO dto);
}