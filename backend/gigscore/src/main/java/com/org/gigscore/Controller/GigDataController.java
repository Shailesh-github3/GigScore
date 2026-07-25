package com.org.gigscore.Controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.org.gigscore.DTO.GigEventRequest;
import com.org.gigscore.DTO.UserDashboardResponse;
import com.org.gigscore.Service.GigDataService;
import com.org.gigscore.security.CurrentUserResolver;

@RestController
@RequestMapping("/api")
public class GigDataController {

    final GigDataService gigDataService;
    final CurrentUserResolver currentUserResolver;
    public GigDataController(GigDataService gigDataService, CurrentUserResolver currentUserResolver) {
        this.gigDataService = gigDataService;
        this.currentUserResolver = currentUserResolver;
    }

    @PostMapping("/gigs")
    public UserDashboardResponse addGig(@Valid @RequestBody GigEventRequest request){
        request.setUserId(currentUserResolver.getCurrentUserId());
        return gigDataService.addGig(request);
    }
}