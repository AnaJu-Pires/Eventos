package br.ifsp.events.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.ifsp.events.model.Comentario;

public interface ComentarioRepository extends JpaRepository<Comentario, Long> {
    
}