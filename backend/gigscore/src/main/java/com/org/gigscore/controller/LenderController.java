package com.org.gigscore.controller;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.org.gigscore.dto.LenderWorkerDetail;
import com.org.gigscore.dto.LenderWorkerSummary;
import com.org.gigscore.service.LenderService;

@RestController
@RequestMapping("/api/v1/lender")
@Tag(name = "Lender", description = "Lender-only worker creditworthiness views")
public class LenderController {

    private final LenderService lenderService;

    public LenderController(LenderService lenderService) {
        this.lenderService = lenderService;
    }

    @Operation(summary = "Rank all workers by score with data-quality profiles")
    @GetMapping("/workers")
    public List<LenderWorkerSummary> getWorkers() {
        return lenderService.getAllWorkers();
    }

    @Operation(summary = "Get a single worker's full credit view")
    @GetMapping("/workers/{userId}")
    public LenderWorkerDetail getWorker(@PathVariable Long userId) {
        return lenderService.getWorkerDetail(userId);
    }
}