package com.org.gigscore.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.org.gigscore.DTO.ChatMessageDTO;
import com.org.gigscore.exception.BadRequestException;
import com.org.gigscore.exception.GeminiApiException;

@Service
public class GeminiChatService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient;

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.model:gemini-2.5-flash}")
    private String model;

    public GeminiChatService() {
        this.httpClient = HttpClient.newHttpClient();
    }

    public String generateReply(List<ChatMessageDTO> messages) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new GeminiApiException("Gemini API key is not configured on the server.");
        }

        List<Map<String, Object>> contents = messages.stream()
                .filter(message -> message != null && message.content() != null && !message.content().isBlank())
                .map(message -> Map.<String, Object>of(
                        "role", toGeminiRole(message.role()),
                        "parts", List.of(Map.of("text", message.content()))))
                .toList();

        if (contents.isEmpty()) {
            throw new BadRequestException("No chat messages provided.");
        }

        Map<String, Object> payload = Map.of(
                "contents", contents,
                "generationConfig", Map.of("temperature", 0.7, "topP", 0.9));

        String normalizedModel = normalizeModelName(model);
        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/" + normalizedModel
            + ":generateContent?key=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8);

        HttpRequest request;
        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new GeminiApiException("Unable to serialize Gemini request.", exception);
        }

        request = HttpRequest.newBuilder(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payloadJson))
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new GeminiApiException("Gemini request failed.", exception);
        } catch (java.io.IOException exception) {
            throw new GeminiApiException("Gemini request failed.", exception);
        }

        if (response.statusCode() >= 400) {
            throw new GeminiApiException("Gemini API error: " + response.body());
        }

        try {
            JsonNode root = objectMapper.readTree(response.body());
            String text = root.path("candidates")
                    .path(0)
                    .path("content")
                    .path("parts")
                    .path(0)
                    .path("text")
                    .asText("");

            if (text.isBlank()) {
                throw new GeminiApiException("Gemini returned an empty response.");
            }

            return text;
        } catch (JsonProcessingException exception) {
            throw new GeminiApiException("Unable to parse Gemini response.", exception);
        }
    }

    private String toGeminiRole(String role) {
        if (role == null) {
            return "user";
        }
        if ("assistant".equalsIgnoreCase(role) || "model".equalsIgnoreCase(role)) {
            return "model";
        }
        return "user";
    }

    private String normalizeModelName(String rawModel) {
        if (rawModel == null || rawModel.isBlank()) {
            return "gemini-2.5-flash";
        }

        String trimmed = rawModel.trim();
        if (trimmed.startsWith("models/")) {
            return trimmed.substring("models/".length());
        }
        return trimmed;
    }
}
