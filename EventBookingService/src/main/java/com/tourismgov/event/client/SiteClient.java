package com.tourismgov.event.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.tourismgov.event.dto.SiteDto;

@FeignClient(name = "SITE-SERVICE")
public interface SiteClient {
	@GetMapping("/tourismgov/v1/sites/{id}")
    SiteDto getSiteById(@PathVariable("id") Long id);
}