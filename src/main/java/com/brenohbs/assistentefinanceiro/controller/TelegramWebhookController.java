package com.brenohbs.assistentefinanceiro.controller;

import com.brenohbs.assistentefinanceiro.service.ConversaService;
import com.brenohbs.assistentefinanceiro.service.TelegramService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook/telegram")
public class TelegramWebhookController {

    private final ConversaService conversaService;
    private final TelegramService telegramService;

    @Value("${telegram.webhook-secret}")
    private String webhookSecret;

    public TelegramWebhookController(ConversaService conversaService, TelegramService telegramService) {
        this.conversaService = conversaService;
        this.telegramService = telegramService;
    }

    @PostMapping
    public ResponseEntity<Void> receberMensagem(
            @RequestBody JsonNode payload,
            @RequestHeader(value = "X-Telegram-Bot-Api-Secret-Token", required = false) String secretRecebido) {

        if (webhookSecret != null && !webhookSecret.isBlank() && !webhookSecret.equals(secretRecebido)) {
            return ResponseEntity.status(403).build();
        }

        try {
            JsonNode mensagem = payload.path("message");
            if (mensagem.isMissingNode()) {
                return ResponseEntity.ok().build();
            }

            long chatId = mensagem.path("chat").path("id").asLong();
            String texto = mensagem.path("text").asText();

            if (chatId == 0 || texto.isBlank()) {
                return ResponseEntity.ok().build();
            }

            String resposta = conversaService.processarMensagem(String.valueOf(chatId), texto);
            telegramService.enviarMensagem(chatId, resposta);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok().build();
    }
}
