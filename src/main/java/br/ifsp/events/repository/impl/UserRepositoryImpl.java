package br.ifsp.events.repository.impl;

import br.ifsp.events.model.StatusUser;
import br.ifsp.events.model.User;
import br.ifsp.events.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepositoryImpl implements UserRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public User save(User user) {
        if (user.getId() == null) {
            entityManager.persist(user); // insere um novo
            return user;
        } else {
            return entityManager.merge(user); // atualiza um que ja existe
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        String jpql = "SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email";
        TypedQuery<Boolean> query = entityManager.createQuery(jpql, Boolean.class);
        query.setParameter("email", email);
        return query.getSingleResult();
    }

    @Override
    public Optional<User> findByTokenConfirmacao(String token) {
        String jpql = "SELECT u FROM User u WHERE u.tokenConfirmacao = :token";
        TypedQuery<User> query = entityManager.createQuery(jpql, User.class);
        query.setParameter("token", token);
        try {
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<User> findAllByStatusUserAndDataExpiracaoTokenConfirmacaoBefore(StatusUser status, LocalDateTime now) {
        String jpql = "SELECT u FROM User u WHERE u.statusUser = :status AND u.dataExpiracaoTokenConfirmacao < :now";
        TypedQuery<User> query = entityManager.createQuery(jpql, User.class);
        query.setParameter("status", status);
        query.setParameter("now", now);
        return query.getResultList();
    }

    @Override
    @Transactional
    public void deleteAll(List<User> users) {
        for (User user : users) {
            // garante que o usuário foi carregado antes de ser removido
            if (entityManager.contains(user)) {
                entityManager.remove(user);
            } else {
                entityManager.remove(entityManager.merge(user));
            }
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String jpql = "SELECT u FROM User u WHERE u.email = :email";
        TypedQuery<User> query = entityManager.createQuery(jpql, User.class);
        query.setParameter("email", email);
        try {
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}