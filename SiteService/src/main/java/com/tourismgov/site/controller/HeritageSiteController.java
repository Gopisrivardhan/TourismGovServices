package com.tourismgov.site.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tourismgov.site.dto.HeritageSiteRequest;
import com.tourismgov.site.dto.HeritageSiteResponse;
import com.tourismgov.site.service.HeritageSiteService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/tourismgov/v1/sites")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Validated // Required to validate @Positive on path variables
public class HeritageSiteController {

    private final HeritageSiteService siteService;

    /**
     * Creates a new Heritage Site.
     * Accessible by: ADMIN, MANAGER, OFFICER
     */
    @PostMapping
    public ResponseEntity<HeritageSiteResponse> createSite(
            @Valid @RequestBody HeritageSiteRequest request) {
        
        log.info("REST request to create Heritage Site: {}", request.getName());
        HeritageSiteResponse response = siteService.createSite(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Updates an existing Heritage Site.
     * Accessible by: ADMIN, MANAGER, OFFICER
     */
    @PutMapping("/{siteId}")
    public ResponseEntity<HeritageSiteResponse> updateSite(
            @PathVariable("siteId") @Positive(message = "Site ID must be positive") Long siteId,
            @Valid @RequestBody HeritageSiteRequest request) {
        
        log.info("REST request to update Heritage Site ID: {}", siteId);
        return ResponseEntity.ok(siteService.updateSite(siteId, request));
    }

    /**
     * Retrieves all Heritage Sites.
     * Accessible by: ALL (Public)
     */
    @GetMapping
    public ResponseEntity<List<HeritageSiteResponse>> getAllSites() {
        log.info("REST request to fetch all Heritage Sites");
        List<HeritageSiteResponse> sites = siteService.getAllSites();
        return ResponseEntity.ok(sites);
    }

    /**
     * Retrieves a specific Heritage Site by ID.
     * Accessible by: ALL (Public)
     */
    @GetMapping("/{siteId}")
    public ResponseEntity<HeritageSiteResponse> getSiteById(
            @PathVariable("siteId") @Positive(message = "Site ID must be positive") Long siteId) {
        
        log.info("REST request to get Heritage Site ID: {}", siteId);
        return ResponseEntity.ok(siteService.getSiteById(siteId));
    }

    /**
     * Deletes a Heritage Site.
     * Accessible by: ADMIN
     */
    @DeleteMapping("/{siteId}")
    public ResponseEntity<Void> deleteSite(
            @PathVariable("siteId") @Positive(message = "Site ID must be positive") Long siteId) {
        
        log.info("REST request to delete Heritage Site ID: {}", siteId);
        siteService.deleteSite(siteId);
        return ResponseEntity.noContent().build(); // Returns 204 No Content
    }
}