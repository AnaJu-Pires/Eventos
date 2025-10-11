package br.ifsp.events.service.impl;

import br.ifsp.events.service.EmailService;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    @Async //executado em segundo plano
    public void sendConfirmationEmail(String to, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            
            String confirmationUrl = "http://localhost:8081/cadastro/confirmar?token=" + token;

            message.setFrom("nao-responda@eventosesportivosifsp.com.br");
            message.setTo(to);
            message.setSubject("Confirmação de Cadastro - Eventos Esportivos IFSP");
            message.setText(
                "Olá!\n\n" +
                "Seja bem vindo a nossa API de Eventos Esportivos do IFSP. " +
                "Por favor, clique no link abaixo para ativar sua conta:\n\n" +
                confirmationUrl
            );
            
            mailSender.send(message);

        } catch (Exception e) {
            System.err.println("Erro ao enviar e-mail de confirmação: " + e.getMessage());
        }
    }
}