package com.org.gigscore.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.org.gigscore.dto.GigEventRequest;
import com.org.gigscore.dto.UserDashboardResponse;
import com.org.gigscore.service.GigDataService;
import com.org.gigscore.security.CurrentUserResolver;

@RestController
@RequestMapping("/api")
@Tag(name = "Gigs", description = "Log gig events and update platform aggregates")
public class GigDataController {

    final GigDataService gigDataService;
    final CurrentUserResolver currentUserResolver;
    public GigDataController(GigDataService gigDataService, CurrentUserResolver currentUserResolver) {
        this.gigDataService = gigDataService;
        this.currentUserResolver = currentUserResolver;
    }

    @Operation(summary = "Add a gig event and return the updated dashboard")
    @PostMapping("/gigs")
    public UserDashboardResponse addGig(@Valid @RequestBody GigEventRequest request){
        request.setUserId(currentUserResolver.getCurrentUserId());
        return gigDataService.addGig(request);
    }
}