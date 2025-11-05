package br.ifsp.events.service;

public interface EmailService {

    /**
     * @param to
     * @param token
     */
    void sendConfirmationEmail(String to, String token, String nome);
}