package br.ifsp.events.repository.impl;

import br.ifsp.events.model.Convite;
import br.ifsp.events.model.StatusConvite;
import br.ifsp.events.model.Time;
import br.ifsp.events.model.User;
import br.ifsp.events.repository.ConviteRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class ConviteRepositoryImpl implements ConviteRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public Convite save(Convite convite) {
        if (convite.getId() == null) {
            entityManager.persist(convite);
            return convite;
        } else {
            return entityManager.merge(convite);
        }
    }

    @Override
    public Optional<Convite> findById(Long id) {
        Convite convite = entityManager.find(Convite.class, id);
        return Optional.ofNullable(convite);
    }

    @Override
    public Optional<Convite> findByIdAndUsuarioConvidado(Long id, User usuario) {
        String jpql = "SELECT c FROM Convite c WHERE c.id = :id AND c.usuarioConvidado = :usuario";
        TypedQuery<Convite> query = entityManager.createQuery(jpql, Convite.class);
        query.setParameter("id", id);
        query.setParameter("usuario", usuario);
        try {
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Convite> findByUsuarioConvidadoAndStatus(User usuario, StatusConvite status) {
        String jpql = "SELECT c FROM Convite c WHERE c.usuarioConvidado = :usuario AND c.status = :status";
        TypedQuery<Convite> query = entityManager.createQuery(jpql, Convite.class);
        query.setParameter("usuario", usuario);
        query.setParameter("status", status);
        return query.getResultList();
    }

    @Override
    public boolean existsByTimeAndUsuarioConvidadoAndStatus(Time time, User usuario, StatusConvite status) {
        String jpql = "SELECT COUNT(c) > 0 FROM Convite c WHERE c.time = :time AND c.usuarioConvidado = :usuario AND c.status = :status";
        TypedQuery<Boolean> query = entityManager.createQuery(jpql, Boolean.class);
        query.setParameter("time", time);
        query.setParameter("usuario", usuario);
        query.setParameter("status", status);
        return query.getSingleResult();
    }

    @Override
    @Transactional
    public void delete(Convite convite) {
        if (entityManager.contains(convite)) {
            entityManager.remove(convite);
        } else {
            entityManager.remove(entityManager.merge(convite));
        }
    }
}