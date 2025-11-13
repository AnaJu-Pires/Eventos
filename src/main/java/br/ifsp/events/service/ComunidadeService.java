package br.ifsp.events.service;

import br.ifsp.events.dto.comunidade.ComunidadeCreateDTO;
import br.ifsp.events.dto.comunidade.ComunidadeResponseDTO;
import java.util.List;

public interface ComunidadeService {
    
    ComunidadeResponseDTO create(ComunidadeCreateDTO dto);

    List<ComunidadeResponseDTO> listAll();

    ComunidadeResponseDTO findById(Long id);
}
