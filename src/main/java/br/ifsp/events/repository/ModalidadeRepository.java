package br.ifsp.events.repository;

import br.ifsp.events.model.Modalidade;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ModalidadeRepository {

    Optional<Modalidade> findByNome(String nome);

    Set<Modalidade> findByIdIn(Set<Long> ids);

    long count();

    Modalidade save(Modalidade modalidade);

    void delete(Modalidade modalidade);

    List<Modalidade> findAll();

    Optional<Modalidade> findById(Long id);
}