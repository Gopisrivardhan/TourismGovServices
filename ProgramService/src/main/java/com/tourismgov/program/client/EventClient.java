package com.tourismgov.program.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name = "EVENT-SERVICE")
public interface EventClient {

    /**
     * Fetches all unique Heritage Site IDs associated with a specific Program ID.
     */
    @GetMapping("/tourismgov/v1/events/program/{programId}/sites")
    List<Long> getSiteIdsByProgram(@PathVariable("programId") Long programId);

    /**
     * Safely unlinks a program from all associated events when the program is deleted.
     */
    @DeleteMapping("/tourismgov/v1/events/program/{programId}/unlink")
    void unlinkProgramFromAllEvents(@PathVariable("programId") Long programId);
    
    @PutMapping("/tourismgov/v1/events/program/{programId}/cancel")
    void cancelEventsByProgram(@PathVariable("programId") Long programId);
}