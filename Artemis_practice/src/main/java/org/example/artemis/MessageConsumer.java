package org.example.artemis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.Queue;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
public class MessageConsumer {

    @Value("${app.queues.output}")
    private String outputQueue;

    @Value("${app.queues.rs}")
    private String rsQueue;

    private final Object logLock = new Object();

    @JmsListener(destination = "${app.queues.output}")
    public void receiveFromOutputQueue(String message,
                                       @Header(name = "JMSReplyTo", required = false) String replyTo,
                                       Message jmsMessage) throws JMSException {
        synchronized (logLock) {
            logMessage("OUTPUT", message, jmsMessage);
            if (replyTo != null) {
                log.info("│   JMSReplyTo: {}", replyTo);
            }
        }
    }

    @JmsListener(destination = "${app.queues.rs}")
    public void receiveFromRsQueue(String message,
                                   @Header(name = "UIPSYSTEMDATA", required = false) String uipSystemData,
                                   Message jmsMessage) throws JMSException {
        synchronized (logLock) {
            logMessage("RS", message, jmsMessage);
        }
    }

    private void logMessage(String queueType, String message, Message jmsMessage) throws JMSException {
        Map<String, Object> customHeaders = getCustomHeaders(jmsMessage);

        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("\n┌── [").append(queueType).append("] Received message ──\n");
        logBuilder.append("│   Content: ").append(message).append("\n");

        // Добавляем JMSReplyTo, если он есть
        try {
            jakarta.jms.Destination replyTo = jmsMessage.getJMSReplyTo();
            if (replyTo != null) {
                String replyToStr = (replyTo instanceof Queue) ?
                        ((Queue) replyTo).getQueueName() : replyTo.toString();
                logBuilder.append("│   JMSReplyTo: ").append(replyToStr).append("\n");
            }
        } catch (JMSException e) {
            log.warn("Error getting JMSReplyTo: {}", e.getMessage());
        }

        if (!customHeaders.isEmpty()) {
            logBuilder.append("├── Headers ──\n");
            customHeaders.forEach((key, value) ->
                    logBuilder.append("│   ").append(key).append(": ").append(value).append("\n"));
        }

        logBuilder.append("└──────────────────────────");
        log.info(logBuilder.toString());
    }

    private Map<String, Object> getCustomHeaders(Message message) throws JMSException {
        Map<String, Object> headers = new LinkedHashMap<>();
        Enumeration<?> propertyNames = message.getPropertyNames();

        while (propertyNames.hasMoreElements()) {
            String name = (String) propertyNames.nextElement();
            if (!name.startsWith("_AMQ_") && !name.startsWith("JMSX")) {
                try {
                    Object value = message.getObjectProperty(name);
                    headers.put(name, value);
                } catch (JMSException e) {
                    log.warn("Error reading header {}: {}", name, e.getMessage());
                }
            }
        }
        return headers;
    }
}