package com.tourismgov.user.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.tourismgov.user.dto.TouristSyncRequest;

@FeignClient(name = "TOURIST-SERVICE")
public interface TouristClient {

    @PostMapping("/tourismgov/v1/tourist/internal/sync")
    void syncTouristProfile(@RequestBody TouristSyncRequest request);
}
