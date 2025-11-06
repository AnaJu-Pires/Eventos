package br.ifsp.events.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.ifsp.events.model.Inscricao;
import br.ifsp.events.model.StatusInscricao;

public interface InscricaoRepository extends JpaRepository<Inscricao, Long> {
    long countByEventoModalidadeIdAndStatusInscricaoIn(Long eventoModalidadeId, List<StatusInscricao> statuses);

    boolean existsByTimeIdAndEventoModalidadeId(Long timeId, Long eventoModalidadeId);

    List<Inscricao> findAllByEventoModalidade_Evento_IdAndStatusInscricao(Long eventoId, StatusInscricao status);
}
