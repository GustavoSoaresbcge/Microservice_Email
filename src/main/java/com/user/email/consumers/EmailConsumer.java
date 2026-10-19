package com.user.email.consumers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.user.email.dto.EmailRecordDto;
import com.user.email.models.EmailModel;
import com.user.email.services.EmailService;

@Component
public class EmailConsumer {
	
	private static final Logger log = LoggerFactory.getLogger(EmailConsumer.class);
	final EmailService emailService;
	
	public EmailConsumer(EmailService emailService) {
		this.emailService = emailService;
	}
	
	@RabbitListener(queues = "${broker.queue.email.name}")
	public void listenEmailQueue(@Payload EmailRecordDto emailRecordDto) {
		log.info("Mensagem recebida para: {}", emailRecordDto.emailTo());
		var emailModel = new EmailModel();
		BeanUtils.copyProperties(emailRecordDto, emailModel);
		emailService.sendEmail(emailModel);
		log.info("Email processado para: {}", emailRecordDto.emailTo());
	}

}
