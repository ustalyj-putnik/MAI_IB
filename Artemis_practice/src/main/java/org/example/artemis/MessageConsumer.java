package org.example.artemis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import jakarta.jms.JMSException;
import jakarta.jms.Message;

import java.util.Enumeration;

@Slf4j
@Service
public class MessageConsumer {

    @Value("${app.queues.output}")
    private String outputQueue;

    private final JmsTemplate jmsTemplate;

    public MessageConsumer(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    @JmsListener(destination = "${app.queues.rs}") // MAI.TEST.NOVIKOV.RS
    public void receiveFromRsQueue(
            String messageBody,
            Message jmsMessage,
            @Header(value = "ReplyTo", required = false) String replyToQueue) throws JMSException {

        log.info("Received message from RS queue with ReplyTo: {}", replyToQueue);

        // Обработка сообщения
        String processedMessage = processMessage(messageBody);

        // Отправляем ответ только если указана очередь ReplyTo
        if (replyToQueue != null && !replyToQueue.isEmpty()) {
            jmsTemplate.convertAndSend(replyToQueue, processedMessage);
            log.info("Sent response to {} queue", replyToQueue);
        }
    }

    @JmsListener(destination = "${app.queues.output}") // MAI.TEST.NOVIKOV.OUT
    public void receiveFromOutputQueue(String messageBody) {
        log.info("Received FINAL message: {}", messageBody);
        // Только обработка, без пересылки
    }

    private String processMessage(String message) {
        // Ваша логика обработки
        String processed = "PROCESSED: " + message;
        log.info("Processed message: {}", processed);
        return processed; // Возвращаем результат
    }

    private void logHeaders(Message jmsMessage) throws JMSException {
        Enumeration<?> propertyNames = jmsMessage.getPropertyNames();
        while (propertyNames.hasMoreElements()) {
            Object nameObj = propertyNames.nextElement();
            if (nameObj instanceof String) {
                String name = (String) nameObj;
                try {
                    Object value = jmsMessage.getObjectProperty(name);
                    log.info("JMS Property - {}: {}", name, value);
                } catch (JMSException e) {
                    log.warn("Failed to get property {}: {}", name, e.getMessage());
                }
            }
        }
    }

}