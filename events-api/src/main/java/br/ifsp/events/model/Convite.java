package br.ifsp.events.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "convites")
@Data
@NoArgsConstructor
public class Convite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timeId")
    private Time time;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuarioConvidadoId")
    private User usuarioConvidado;

    @NotNull
    @Enumerated(EnumType.STRING)
    private StatusConvite status;

    @NotNull
    private LocalDateTime dataExpiracao;

    public Convite(Time time, User usuarioConvidado) {
        this.time = time;
        this.usuarioConvidado = usuarioConvidado;
        this.status = StatusConvite.PENDENTE;
        this.dataExpiracao = LocalDateTime.now().plusDays(7);
    }
}