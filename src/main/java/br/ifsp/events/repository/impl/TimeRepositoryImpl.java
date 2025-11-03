package br.ifsp.events.repository.impl;

import br.ifsp.events.model.Time;
import br.ifsp.events.repository.TimeRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Repository
public class TimeRepositoryImpl implements TimeRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public Time save(Time time) {
        if (time.getId() == null) {
            entityManager.persist(time);
            return time;
        } else {
            return entityManager.merge(time);
        }
    }

    @Override
    public Optional<Time> findById(Long id) {
        Time time = entityManager.find(Time.class, id);
        return Optional.ofNullable(time);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        findById(id).ifPresent(time -> {
            entityManager.remove(time);
        });
    }


    @Override
    public Page<Time> findAll(Pageable pageable) {
  
        String jpql = "SELECT t FROM Time t" + getOrderString(pageable);
        TypedQuery<Time> query = entityManager.createQuery(jpql, Time.class);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        
        List<Time> times = query.getResultList();
        String countJpql = "SELECT COUNT(t) FROM Time t";
        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql, Long.class);
        long total = countQuery.getSingleResult();
        return new PageImpl<>(times, pageable, total);
    }
    
    private String getOrderString(Pageable pageable) {
        if (pageable.getSort().isSorted()) {
            String sortOrder = pageable.getSort().stream()
                    .map(order -> "t." + order.getProperty() + " " + order.getDirection().name())
                    .findFirst()
                    .orElse("");
            if (!sortOrder.isEmpty()) {
                return " ORDER BY " + sortOrder;
            }
        }
        return "";
    }

    @Override
    public long count() { 
        String jpql = "SELECT COUNT(t) FROM Time t";
        return entityManager.createQuery(jpql, Long.class).getSingleResult();
    }
}