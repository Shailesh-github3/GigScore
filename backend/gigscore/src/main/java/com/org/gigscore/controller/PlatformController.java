package com.org.gigscore.controller;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.org.gigscore.util.Platform;

@RestController
@RequestMapping("/api/v1/platforms")
@Tag(name = "Platforms", description = "Allowed gig platform values")
public class PlatformController {

    @Operation(summary = "List the allowed platform values")
    @GetMapping
    public List<String> getAllPlatforms() {
        return Platform.allowedValues();
    }
}