package com.tourismgov.event.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.tourismgov.event.dto.TouristDTO;

@FeignClient(name = "TOURIST-SERVICE")
public interface TouristClient {
    @GetMapping("/tourismgov/v1/tourists/{touristId}")
    TouristDTO getTouristById(@PathVariable("touristId") Long touristId);
}