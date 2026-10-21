package com.user.email.services;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.user.email.enums.StatusEmail;
import com.user.email.models.EmailModel;
import com.user.email.repositories.EmailRepository;

@Service
public class EmailService {
		
	private static final Logger log = LoggerFactory.getLogger(EmailService.class);
	
	final EmailRepository emailRepository;
	final JavaMailSender emailSender;
	
	public EmailService(EmailRepository emailRepository, JavaMailSender emailSender) {
		this.emailRepository = emailRepository;
		this.emailSender = emailSender;
	}
	
	@Value(value = "${spring.mail.username}")
	private String emailFrom;
	
	private SimpleMailMessage buildMessage(EmailModel emailModel) {
		SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom(emailFrom);
			message.setTo(emailModel.getEmailTo());
			message.setSubject(emailModel.getSubject());
			message.setText(emailModel.getText());
			return message;
	}
	
	public EmailModel sendEmail(EmailModel emailModel) {
		try {
			emailModel.setSendDateEmail(LocalDateTime.now());
			emailModel.setEmailFrom(emailFrom);	
			emailSender.send(buildMessage(emailModel));
			emailModel.setStatusEmail(StatusEmail.SENT);
			log.info("Sucesso ao enviar o email para: {}", emailModel.getEmailTo());
		} catch (MailException e) {
			log.error("Falha ao enviar o email para {}: {}", emailModel.getEmailTo(), e.getMessage());
			emailModel.setStatusEmail(StatusEmail.ERROR);
			emailRepository.save(emailModel);
			throw e;
		} 
		
		return emailRepository.save(emailModel);

	}

}