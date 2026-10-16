package com.org.gigscore.controller;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import com.org.gigscore.service.GeminiChatService;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GeminiChatService geminiChatService;

    @Test
    void ask_validRequest_returns200WithReply() throws Exception {
        when(geminiChatService.generateReply(anyList())).thenReturn("Here is some advice.");

        mockMvc.perform(post("/api/chat/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"messages":[{"role":"user","content":"How do I improve my score?"}]}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reply").value("Here is some advice."));
    }

    @Test
    void ask_emptyMessages_returns400() throws Exception {
        mockMvc.perform(post("/api/chat/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"messages":[]}
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ask_nullBody_returns400() throws Exception {
        mockMvc.perform(post("/api/chat/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ask_serviceThrowsIllegalState_returns500() throws Exception {
        when(geminiChatService.generateReply(anyList()))
                .thenThrow(new IllegalStateException("API key not configured"));

        mockMvc.perform(post("/api/chat/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"messages":[{"role":"user","content":"Hello"}]}
                        """))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void ask_serviceThrowsRuntime_returns502() throws Exception {
        when(geminiChatService.generateReply(anyList()))
                .thenThrow(new RuntimeException("Network error"));

        mockMvc.perform(post("/api/chat/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"messages":[{"role":"user","content":"Hello"}]}
                        """))
                .andExpect(status().isBadGateway());
    }
}