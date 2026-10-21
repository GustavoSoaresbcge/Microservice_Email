package com.user.email;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import com.user.email.enums.StatusEmail;
import com.user.email.models.EmailModel;
import com.user.email.repositories.EmailRepository;
import com.user.email.services.EmailService;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private EmailRepository emailRepository;

    @Mock
    private JavaMailSender emailSender;

    @InjectMocks
    private EmailService emailService;

    @Test
    void deveMarcarSentQuandoEnvioDaCerto() {
        EmailModel emailModel = new EmailModel();
        emailModel.setEmailTo("destino@teste.com");
        emailModel.setSubject("Assunto");
        emailModel.setText("Corpo");

        ReflectionTestUtils.setField(emailService, "emailFrom", "remetente@teste.com");
        when(emailRepository.save(any(EmailModel.class))).thenAnswer(inv -> inv.getArgument(0));

        EmailModel resultado = emailService.sendEmail(emailModel);

        assertEquals(StatusEmail.SENT, resultado.getStatusEmail());
        verify(emailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void deveMarcarErrorQuandoEnvioFalha() {
        EmailModel emailModel = new EmailModel();
        emailModel.setEmailTo("destino@teste.com");

        ReflectionTestUtils.setField(emailService, "emailFrom", "remetente@teste.com");
        doThrow(new MailSendException("Falha simulada")).when(emailSender).send(any(SimpleMailMessage.class));
        when(emailRepository.save(any(EmailModel.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThrows(MailSendException.class, () -> {
            emailService.sendEmail(emailModel);
        });

        verify(emailRepository, times(1)).save(argThat(email -> 
            email.getStatusEmail() == StatusEmail.ERROR
        ));
    }
}