package br.ifsp.events.service.impl;

import br.ifsp.events.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String appBaseUrl;

    @Value("${spring.mail.name}")
    private String fromEmail;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    @Async 
    public void sendConfirmationEmail(String to, String token, String nome) {
        
        SimpleMailMessage message = new SimpleMailMessage();
        
        String confirmationUrl = appBaseUrl + "/auth/confirm?token=" + token;

        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Confirmação de Cadastro - Eventos Esportivos IFSP");
        message.setText(
            "Olá, " + nome + "!\n\n" +
            "Seja bem-vindo(a) a nossa API de Eventos Esportivos do IFSP. " +
            "Por favor, clique no link abaixo para ativar sua conta:\n\n" +
            confirmationUrl
        );
        
        mailSender.send(message);
        logger.info("Email de confirmação enviado com sucesso para: {}", to);
    }
}