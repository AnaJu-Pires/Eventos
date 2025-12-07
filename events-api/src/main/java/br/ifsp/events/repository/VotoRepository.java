package br.ifsp.events.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.ifsp.events.model.Voto;

public interface VotoRepository extends JpaRepository<Voto, Long> {

    Optional<Voto> findByUsuarioIdAndPostId(Long usuarioId, Long postId);

    Optional<Voto> findByUsuarioIdAndComentarioId(Long usuarioId, Long comentarioId);

    long countByPostIdAndTipoVoto(Long postId, br.ifsp.events.model.TipoVoto tipoVoto);

    long countByComentarioIdAndTipoVoto(Long comentarioId, br.ifsp.events.model.TipoVoto tipoVoto);
}