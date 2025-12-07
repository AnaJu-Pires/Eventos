package br.ifsp.events.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import br.ifsp.events.model.Comentario;

public interface ComentarioRepository extends JpaRepository<Comentario, Long> {

    Page<Comentario> findAllByPostIdAndComentarioPaiIsNull(Long postId, Pageable pageable);

    Optional<Comentario> findByIdAndPostId(Long comentarioId, Long postId);
}