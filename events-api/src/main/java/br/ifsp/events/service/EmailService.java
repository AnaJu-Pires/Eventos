package br.ifsp.events.service;

public interface EmailService {

    /**
     * @param to
     * @param token
     * @param nome
     */
    void sendConfirmationEmail(String to, String token, String nome);

    /**
     * @param to
     * @param subject
     * @param body
     */
    void sendEmail(String to, String subject, String body);
}