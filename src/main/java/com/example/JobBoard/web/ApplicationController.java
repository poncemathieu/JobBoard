package com.example.JobBoard.web;

import com.example.JobBoard.domain.Application;
import com.example.JobBoard.service.ApplicationService;
import com.example.JobBoard.web.dto.UpdateApplicationStatusRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/applications")
public class ApplicationController {

    private final ApplicationService service;

    public ApplicationController(ApplicationService service) {
        this.service = service;
    }

    //GET /applications?jobId=...
    @GetMapping
    public Flux<Application> getApplications(@RequestParam(required = false) String jobId) {
        if(jobId != null && !jobId.isBlank()) {
            return service.getApplicationsForJob(jobId);
        }
        return service.getAllApplications();
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Application>> getById(@PathVariable String id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    //POST /applications
    @PostMapping
    public Mono<Application> create(@Valid @RequestBody ApplicationRequest request) {
        return service.createApplication(request);
    }

    @PatchMapping("/{id}/status")
    public Mono<Application> updateStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateApplicationStatusRequest request
            ) {
        return service.updateStatus(id, request.status());
    }
}