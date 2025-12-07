package br.ifsp.events.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import br.ifsp.events.service.impl.EmailServiceImpl;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "appBaseUrl", "http://localhost:8081");
        ReflectionTestUtils.setField(emailService, "fromEmail", "nao-responda@teste.com");
    }

    @Test
    @DisplayName("Deve construir e tentar enviar o e-mail de confirmação corretamente")
    void sendConfirmationEmailOk() {
        String to = "aluno@ifsp.edu.br";
        String token = "token-123";
        String nome = "Ana";
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        doNothing().when(mailSender).send(messageCaptor.capture());
        emailService.sendConfirmationEmail(to, token, nome);
        verify(mailSender, times(1)).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();

        assertEquals("nao-responda@teste.com", sentMessage.getFrom());
        assertArrayEquals(new String[]{to}, sentMessage.getTo());
        assertEquals("Confirmação de Cadastro - Eventos Esportivos IFSP", sentMessage.getSubject());

        String expectedText = "Olá, Ana!\n\n" +
                              "Seja bem-vindo(a) a nossa API de Eventos Esportivos do IFSP. " +
                              "Por favor, clique no link abaixo para ativar sua conta:\n\n" +
                              "http://localhost:8081/auth/confirm?token=token-123";
        
        assertEquals(expectedText, sentMessage.getText());
    }
}