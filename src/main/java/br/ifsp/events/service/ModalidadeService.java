package br.ifsp.events.service;

import java.util.List;

import br.ifsp.events.dto.modalidade.ModalidadePatchRequestDTO;
import br.ifsp.events.dto.modalidade.ModalidadeRequestDTO;
import br.ifsp.events.dto.modalidade.ModalidadeResponseDTO;

public interface ModalidadeService {

    ModalidadeResponseDTO create(ModalidadeRequestDTO requestDTO);

    ModalidadeResponseDTO update(Long id, ModalidadeRequestDTO requestDTO);

    ModalidadeResponseDTO patch(Long id, ModalidadePatchRequestDTO requestDTO);

    void delete(Long id);

    List<ModalidadeResponseDTO> findAll();

    ModalidadeResponseDTO findById(Long id);
}
