package br.ifsp.events.dto.gamification;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GamificationScoreDTO {
    private Long userId;
    private Long pontosSaldo;
    private Long pontosRecorde;
    private String nivel; 
    private String rank;
}