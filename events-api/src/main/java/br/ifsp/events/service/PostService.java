package br.ifsp.events.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import br.ifsp.events.dto.post.PostCreateDTO;
import br.ifsp.events.dto.post.PostResponseDTO;

public interface PostService {

    PostResponseDTO create(PostCreateDTO dto, Long comunidadeId);

    Page<PostResponseDTO> listByComunidade(Long comunidadeId, Pageable pageable);

    PostResponseDTO findById(Long postId);

    void deletePost(Long postId);
}
