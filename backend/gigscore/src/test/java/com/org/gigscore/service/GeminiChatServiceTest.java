package com.org.gigscore.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.org.gigscore.dto.ChatMessageDTO;
import com.org.gigscore.exception.BadRequestException;
import com.org.gigscore.exception.GeminiApiException;

class GeminiChatServiceTest {

    private GeminiChatService geminiChatService;

    @BeforeEach
    void setUp() {
        geminiChatService = new GeminiChatService();
    }

    private void setApiKey(String value) throws Exception {
        Field field = GeminiChatService.class.getDeclaredField("apiKey");
        field.setAccessible(true);
        field.set(geminiChatService, value);
    }

    @Test
    void missingApiKey_throwsGeminiApiException() {
        List<ChatMessageDTO> messages = List.of(new ChatMessageDTO("user", "help me improve my score"));
        assertThatThrownBy(() -> geminiChatService.generateReply(messages))
                .isInstanceOf(GeminiApiException.class)
                .hasMessageContaining("AI service is unavailable");
    }

    @Test
    void blankApiKey_throwsGeminiApiException() throws Exception {
        setApiKey("   ");
        List<ChatMessageDTO> messages = List.of(new ChatMessageDTO("user", "help"));
        assertThatThrownBy(() -> geminiChatService.generateReply(messages))
                .isInstanceOf(GeminiApiException.class);
    }

    @Test
    void allBlankMessages_throwsBadRequestException() throws Exception {
        setApiKey("dummy-key");
        List<ChatMessageDTO> messages = List.of(
                new ChatMessageDTO("user", "   "),
                new ChatMessageDTO("user", null));
        assertThatThrownBy(() -> geminiChatService.generateReply(messages))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void nullMessagesList_throwsBadRequestException() throws Exception {
        setApiKey("dummy-key");
        assertThatThrownBy(() -> geminiChatService.generateReply(List.of()))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    void apiKeySentInHeaderAndNotInUrl() throws Exception {
        java.net.http.HttpClient mockHttpClient = org.mockito.Mockito.mock(java.net.http.HttpClient.class);
        java.net.http.HttpResponse<String> mockResponse = org.mockito.Mockito.mock(java.net.http.HttpResponse.class);
        org.mockito.Mockito.when(mockResponse.statusCode()).thenReturn(200);
        org.mockito.Mockito.when(mockResponse.body()).thenReturn("{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"Hello!\"}]}}]}");

        org.mockito.ArgumentCaptor<java.net.http.HttpRequest> requestCaptor = org.mockito.ArgumentCaptor.forClass(java.net.http.HttpRequest.class);
        org.mockito.Mockito.doReturn(mockResponse)
                .when(mockHttpClient)
                .send(requestCaptor.capture(), org.mockito.ArgumentMatchers.any());

        Field httpClientField = GeminiChatService.class.getDeclaredField("httpClient");
        httpClientField.setAccessible(true);
        httpClientField.set(geminiChatService, mockHttpClient);

        setApiKey("my-test-secret-api-key");
        String reply = geminiChatService.generateReply(List.of(new ChatMessageDTO("user", "hi")));

        java.net.http.HttpRequest captured = requestCaptor.getValue();
        org.assertj.core.api.Assertions.assertThat(reply).isEqualTo("Hello!");
        org.assertj.core.api.Assertions.assertThat(captured.headers().firstValue("x-goog-api-key"))
                .contains("my-test-secret-api-key");
        org.assertj.core.api.Assertions.assertThat(captured.uri().toString()).doesNotContain("key=");
    }
}