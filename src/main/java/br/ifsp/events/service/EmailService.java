package br.ifsp.events.service;

public interface EmailService {

    /**
     * e-mail de confirmação de cadastro
     * @param to endereço de e-mail do destinatário
     * @param token token de confirmação a ser incluído no link
     */
    void sendConfirmationEmail(String to, String token);
}