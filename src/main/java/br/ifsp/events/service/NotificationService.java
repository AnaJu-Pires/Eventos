package br.ifsp.events.service;

import br.ifsp.events.model.Evento;
import br.ifsp.events.model.Partida;
import br.ifsp.events.model.User;

public interface NotificationService {

    void notifyMatchFinalized(Partida partida);

    void notifyEventCreated(Evento evento);

    void notifyEventUpdated(Evento evento);

    void notifyEventCancelled(Evento evento);

    void sendNotification(User user, String subject, String message);
}
