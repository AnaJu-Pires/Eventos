package br.ifsp.events.service.impl;

import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import br.ifsp.events.model.Evento;
import br.ifsp.events.model.Partida;
import br.ifsp.events.model.User;
import br.ifsp.events.service.NotificationService;
import br.ifsp.events.service.EmailService;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final EmailService emailService;

    public NotificationServiceImpl(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public void notifyMatchFinalized(Partida partida) {
        Set<User> usersToNotify = partida.getEventoModalidade().getModalidade()
                                         .getUsuariosInteressados()
                                         .stream()
                                         .collect(Collectors.toSet());

        String subject = String.format("Resultado da Partida: %s x %s",
                partida.getTime1().getNome(), partida.getTime2().getNome());

        String message = buildMatchResultMessage(partida);

        usersToNotify.forEach(user -> sendNotificationAsync(user, subject, message));
    }

    @Override
    public void notifyEventCreated(Evento evento) {
        Set<User> usersToNotify = evento.getEventoModalidades().stream()
                .flatMap(em -> em.getModalidade().getUsuariosInteressados().stream())
                .collect(Collectors.toSet());

        usersToNotify.forEach(user -> {
            String subject = "Novo Evento: " + evento.getNome();
            String message = String.format(
                    "Um novo evento na modalidade %s foi criado.\nData de início: %s\nData de fim: %s",
                    evento.getEventoModalidades().iterator().next().getModalidade().getNome(), // exemplo simples
                    evento.getDataInicio(),
                    evento.getDataFim()
            );
            sendNotificationAsync(user, subject, message);
        });
    }

    @Override
    public void notifyEventUpdated(Evento evento) {
        Set<User> usersToNotify = evento.getEventoModalidades().stream()
                .flatMap(em -> em.getModalidade().getUsuariosInteressados().stream())
                .collect(Collectors.toSet());

        usersToNotify.forEach(user -> {
            String subject = "Evento Atualizado: " + evento.getNome();
            String message = String.format(
                    "O evento %s teve alterações.\nData de início: %s\nData de fim: %s",
                    evento.getNome(),
                    evento.getDataInicio(),
                    evento.getDataFim()
            );
            sendNotificationAsync(user, subject, message);
        });
    }

    @Override
    public void notifyEventCancelled(Evento evento) {
        Set<User> usersToNotify = evento.getEventoModalidades().stream()
                .flatMap(em -> em.getModalidade().getUsuariosInteressados().stream())
                .collect(Collectors.toSet());

        usersToNotify.forEach(user -> {
            String subject = "Evento Cancelado: " + evento.getNome();
            String message = String.format("O evento %s foi cancelado.", evento.getNome());
            sendNotificationAsync(user, subject, message);
        });
    }

    @Async
    @Override
    public void sendNotification(User user, String subject, String message) {
        sendNotificationAsync(user, subject, message);
    }

    private void sendNotificationAsync(User user, String subject, String message) {
        try {
            emailService.sendEmail(user.getEmail(), subject, message);
            logger.info("Email enviado para {}: {}", user.getEmail(), subject);
        } catch (Exception e) {
            logger.error("Falha ao enviar email para {}: {}", user.getEmail(), e.getMessage());
        }
    }

    private String buildMatchResultMessage(Partida partida) {
        String base = String.format(
                "A partida entre %s e %s foi finalizada.\nPlacar: %s %d x %d %s\n",
                partida.getTime1().getNome(),
                partida.getTime2().getNome(),
                partida.getTime1().getNome(),
                partida.getTime1Placar(),
                partida.getTime2Placar(),
                partida.getTime2().getNome()
        );

        if (partida.getVencedor() != null) {
            return base + "Vencedor: " + partida.getVencedor().getNome();
        } else {
            return base + "Empate.";
        }
    }
}
