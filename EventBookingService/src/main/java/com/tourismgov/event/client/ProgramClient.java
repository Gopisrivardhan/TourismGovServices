package com.tourismgov.event.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.tourismgov.event.dto.ProgramDto;

@FeignClient(name = "PROGRAM-SERVICE")
public interface ProgramClient {
	@GetMapping("/tourismgov/v1/programs/{id}")
    ProgramDto getProgramById(@PathVariable("id") Long id);
}