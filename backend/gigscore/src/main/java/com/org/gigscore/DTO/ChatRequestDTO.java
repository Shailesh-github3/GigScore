package com.org.gigscore.dto;

import java.util.List;

public record ChatRequestDTO(List<ChatMessageDTO> messages) {
}
