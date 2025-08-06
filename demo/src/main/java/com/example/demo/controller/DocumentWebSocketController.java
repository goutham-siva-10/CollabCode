package com.example.demo.controller;

import com.example.demo.model.DocumentUpdate;
import com.example.demo.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class DocumentWebSocketController {

    private final DocumentRepository documentRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/edit")
    public void broadcastEdit(DocumentUpdate update) {
        documentRepository.findById(update.getDocumentId()).ifPresent(doc -> {
            doc.setContent(update.getContent());
            documentRepository.save(doc);
        });

        // Broadcast ONLY to subscribers of the specific document
        messagingTemplate.convertAndSend("/topic/updates/" + update.getDocumentId(), update);
    }
}
