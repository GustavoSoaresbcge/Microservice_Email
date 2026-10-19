package com.user.email.controllers;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/dlq")
public class DeadLetterController {
	
	private final RabbitTemplate rabbitTemplate;
	
	@Value("${broker.queue.email.name}")
	private String emailQueueName;
	
	public DeadLetterController(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}
	
	@PostMapping("/reprocess")
	public String reprocess() {
		int count = 0;
		Message message;
		while ((message = rabbitTemplate.receive("email.dlq")) != null) {
			rabbitTemplate.send("", emailQueueName, message);
			count ++;
		}
		return count + " mensagem(ns) movida(s) de volta para " + emailQueueName;
	}

}
