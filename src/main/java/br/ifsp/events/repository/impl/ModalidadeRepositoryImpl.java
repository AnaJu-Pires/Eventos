package br.ifsp.events.repository.impl;

import br.ifsp.events.model.Modalidade;
import br.ifsp.events.repository.ModalidadeRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class ModalidadeRepositoryImpl implements ModalidadeRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public Modalidade save(Modalidade modalidade) {
        if (modalidade.getId() == null) {
            entityManager.persist(modalidade);
            return modalidade;
        } else {
            return entityManager.merge(modalidade);
        }
    }

    @Override
    @Transactional
    public void delete(Modalidade modalidade) {
        if (entityManager.contains(modalidade)) {
            entityManager.remove(modalidade);
        } else {
            entityManager.remove(entityManager.merge(modalidade));
        }
    }

    @Override
    public Optional<Modalidade> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Modalidade.class, id));
    }

    @Override
    public List<Modalidade> findAll() {
        String jpql = "SELECT m FROM Modalidade m";
        TypedQuery<Modalidade> query = entityManager.createQuery(jpql, Modalidade.class);
        return query.getResultList();
    }

    @Override
    public long count() {
        String jpql = "SELECT COUNT(m) FROM Modalidade m";
        return entityManager.createQuery(jpql, Long.class).getSingleResult();
    }

    @Override
    public Optional<Modalidade> findByNome(String nome) {
        String jpql = "SELECT m FROM Modalidade m WHERE m.nome = :nome";
        TypedQuery<Modalidade> query = entityManager.createQuery(jpql, Modalidade.class);
        query.setParameter("nome", nome);
        try {
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public Set<Modalidade> findByIdIn(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new HashSet<>();
        }
        String jpql = "SELECT m FROM Modalidade m WHERE m.id IN :ids";
        TypedQuery<Modalidade> query = entityManager.createQuery(jpql, Modalidade.class);
        query.setParameter("ids", ids);
        return new HashSet<>(query.getResultList());
    }
}