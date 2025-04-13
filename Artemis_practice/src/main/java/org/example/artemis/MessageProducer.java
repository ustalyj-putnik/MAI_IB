package org.example.artemis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import jakarta.jms.JMSException;
import java.util.Collections;
import java.util.Map;

@Slf4j
@Service
public class MessageProducer {

    private final JmsTemplate jmsTemplate;

    @Value("${app.queues.input}")
    private String inputQueue;

    @Value("${app.queues.rq}")
    private String rqQueue;

    public MessageProducer(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    // Этот метод остаётся для совместимости с MessageController
    public void sendToInputQueue(String message) {
        sendToInputQueue(message, Collections.emptyMap());
    }

    // Новый метод с поддержкой заголовков
    public void sendToInputQueue(String message, Map<String, Object> headers) {
        log.info("Attempting to send message to {} queue: {}", inputQueue, message);
        jmsTemplate.convertAndSend(inputQueue, message, m -> {
            headers.forEach((key, value) -> {
                try {
                    m.setObjectProperty(key, value);
                } catch (JMSException e) {
                    log.error("Failed to set header {}: {}", key, e.getMessage());
                }
            });
            return m;
        });
        log.info("Message successfully sent to {} queue", inputQueue);
    }

    public void sendToRqQueue(String message, Map<String, Object> headers) {
        log.info("Attempting to send message to {} queue: {}", rqQueue, message);
        jmsTemplate.convertAndSend(rqQueue, message, m -> {
            headers.forEach((key, value) -> {
                try {
                    m.setObjectProperty(key, value);
                    if ("JMSReplyTo".equals(key)) {
                        m.setJMSReplyTo(jmsTemplate.getConnectionFactory().createConnection()
                                .createSession().createQueue((String) value));
                    }
                } catch (JMSException e) {
                    log.error("Failed to set header {}: {}", key, e.getMessage());
                }
            });
            return m;
        });
        log.info("Message successfully sent to {} queue", rqQueue);
    }
}