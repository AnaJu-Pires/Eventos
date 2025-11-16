package br.ifsp.events.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import br.ifsp.events.dto.comentario.ComentarioCreateDTO;
import br.ifsp.events.dto.comentario.ComentarioResponseDTO;

public interface ComentarioService {

    ComentarioResponseDTO create(ComentarioCreateDTO dto, Long postId);

    Page<ComentarioResponseDTO> listByPost(Long postId, Pageable pageable);

    void deleteComentario(Long postId, Long comentarioId);
}