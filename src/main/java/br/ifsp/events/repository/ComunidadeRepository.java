package br.ifsp.events.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.ifsp.events.model.Comunidade;

public interface ComunidadeRepository extends JpaRepository<Comunidade, Long> {
    /**
     * Busca uma comunidade pelo nome exato (ignorando case)
     * Usado para verificar se um nome ja esta em uso
     */
    Optional<Comunidade> findByNomeIgnoreCase(String nome);
}
