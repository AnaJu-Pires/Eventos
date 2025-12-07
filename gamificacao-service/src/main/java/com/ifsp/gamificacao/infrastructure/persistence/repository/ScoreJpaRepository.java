package com.ifsp.gamificacao.infrastructure.persistence.repository;

import com.ifsp.gamificacao.infrastructure.persistence.entity.ScoreEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ScoreJpaRepository extends JpaRepository<ScoreEntity, Long> {
    Optional<ScoreEntity> findByUserId(Long userId);
    List<ScoreEntity> findByOrderByPontosSaldoDesc(Pageable pageable);
}