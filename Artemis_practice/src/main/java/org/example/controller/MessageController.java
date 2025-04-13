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
            @RequestHeader(value = "JMSReplyTo", required = false) String replyTo) {

        log.info("Received POST request with message: {}", message);

        Map<String, Object> headers = new HashMap<>();
        if (uipSystemData != null) {
            headers.put("UIPSYSTEMDATA", uipSystemData);
        }
        if (replyTo != null) {
            headers.put("JMSReplyTo", replyTo);
        }

        messageProducer.sendToInputQueue(message, headers);
        return ResponseEntity.ok("Message sent to INPUT queue with headers: " + headers);
    }
}