package org.example.artemis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class MessageProducer {

    private final JmsTemplate jmsTemplate;

    // Разрешенные заголовки для передачи в JMS
    private static final Set<String> ALLOWED_HEADERS = Set.of(
            "UIPSYSTEMDATA",
            "ReplyTo",
            "SERVICENAME",
            "SENDER",
            "RECEIVER"
    );

    @Value("${app.queues.input}")
    private String inputQueue;

    public MessageProducer(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void sendToInputQueue(String message, Map<String, String> headers) {
        jmsTemplate.convertAndSend(inputQueue, message, msg -> {
            // Устанавливаем только явно переданные заголовки
            if (headers != null) {
                headers.forEach((key, value) -> {
                    try {
                        if (value != null) {
                            msg.setStringProperty(key, value);
                        }
                    } catch (JMSException e) {
                        log.warn("Failed to set property {}: {}", key, e.getMessage());
                    }
                });
            }
            return msg;
        });
    }
}