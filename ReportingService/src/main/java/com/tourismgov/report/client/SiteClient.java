package com.tourismgov.report.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import com.tourismgov.report.dto.SiteDTO;

import java.util.List;

@FeignClient(name = "SITE-SERVICE")
public interface SiteClient {
    @GetMapping("/tourismgov/v1/sites")
    List<SiteDTO> getAllSites();
}
