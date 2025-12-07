package com.ifsp.gamificacao.infrastructure.persistence.entity;

import com.ifsp.gamificacao.core.domain.model.NivelEngajamento;
import com.ifsp.gamificacao.core.domain.model.RankEngajamento;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "scores_gamificacao")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScoreEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false)
    private Long pontosSaldo;

    @Column(nullable = false)
    private Long pontosRecorde;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NivelEngajamento nivel;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_rank", nullable = false) 
    private RankEngajamento rank;
}