package com.user.email.configs;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import tools.jackson.databind.json.JsonMapper;

@Configuration
public class RabbitMQConfig {
	
	public static final String DLX_NAME = "email.dlx";
	public static final String DLQ_NAME = "email.dlq";
	public static final String DLQ_ROUTING_KEY = "email.dlq";
	
	@Value("${broker.queue.email.name}")
	private String emailQueueName;
	
	@Bean
	public Queue emailQueue() {
		return QueueBuilder.durable(emailQueueName)
				.withArgument("x-dead-letter-exchange", DLX_NAME)
				.withArgument("x-dead-letter-routing-key", DLQ_ROUTING_KEY)
				.build();
	}
	
	@Bean
	public DirectExchange deadLetterExchange() {
		return new DirectExchange(DLX_NAME, true, false);
	}
	
	@Bean
	public Queue deadLetterQueue() {
		return QueueBuilder.durable(DLQ_NAME).build();
	}
	
	@Bean
	public Binding deadLetterBinding() {
		return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with(DLQ_ROUTING_KEY);
	}
	
	@Bean
    public MessageConverter messageConverter() {
		JsonMapper jsonMapper = JsonMapper.builder().build();
		return new JacksonJsonMessageConverter(jsonMapper);
    }

}
