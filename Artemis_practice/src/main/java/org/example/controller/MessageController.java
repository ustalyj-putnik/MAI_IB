package org.example.controller;

import org.example.artemis.MessageProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageProducer messageProducer;

    @Autowired
    public MessageController(MessageProducer messageProducer) {
        this.messageProducer = messageProducer;
    }

    @PostMapping
    public ResponseEntity<String> sendMessage(
            @RequestBody String message,
            @RequestHeader(value = "UIPSYSTEMDATA", required = false) String uipSystemData,
            @RequestHeader(value = "ReplyTo", required = false) String replyTo,
            @RequestHeader(value = "SERVICENAME", required = false) String serviceName,
            @RequestHeader(value = "SENDER", required = false) String sender,
            @RequestHeader(value = "RECEIVER", required = false) String receiver) {

        // Создаем модифицируемую карту заголовков
        Map<String, String> headers = new HashMap<>();

        // Добавляем только не-null заголовки
        if (uipSystemData != null) {
            headers.put("UIPSYSTEMDATA", uipSystemData);
        }
        if (replyTo != null) {
            headers.put("ReplyTo", replyTo);
        }
        if (serviceName != null) {
            headers.put("SERVICENAME", serviceName);
        }
        if (sender != null) {
            headers.put("SENDER", sender);
        }
        if (receiver != null) {
            headers.put("RECEIVER", receiver);
        }

        messageProducer.sendToInputQueue(message, headers);
        return ResponseEntity.ok("Message sent to INPUT queue");
    }
}