package br.ifsp.events.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.ifsp.events.model.Partida;

public interface PartidaRepository extends JpaRepository<Partida, Long> {
    
    List<Partida> findAllByEventoModalidade_Evento_Id(Long eventoId);

    List<Partida> findAllByEventoModalidade_Id(Long eventoModalidadeId);
}