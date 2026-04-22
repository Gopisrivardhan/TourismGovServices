package com.tourismgov.report.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import com.tourismgov.report.dto.ProgramDTO;

import java.util.List;

@FeignClient(name = "tourismgov-app")
public interface ProgramClient {
    @GetMapping("/tourismgov/v1/programs")
    List<ProgramDTO> getAllPrograms();
}
