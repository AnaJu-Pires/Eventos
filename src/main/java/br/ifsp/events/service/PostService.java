package br.ifsp.events.service;

import br.ifsp.events.dto.post.PostCreateDTO;
import br.ifsp.events.dto.post.PostResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostService {

    PostResponseDTO create(PostCreateDTO dto, Long comunidadeId);

    Page<PostResponseDTO> listByComunidade(Long comunidadeId, Pageable pageable);
}
