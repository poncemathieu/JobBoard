package com.example.JobBoard.web;

import com.example.JobBoard.domain.Job;
import com.example.JobBoard.service.JobService;
import com.example.JobBoard.web.dto.JobRequest;
import com.example.JobBoard.web.dto.JobsPageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/jobs")
public class JobController {

    private final JobService service;

    public JobController(JobService service) {
        this.service = service;
    }

    @GetMapping("/all")
    public Flux<Job> getAllJobs() {
        return service.getAllJobs();
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Job>> getJobById(@PathVariable String id) {
        return service.getJobById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping
    public Mono<JobsPageResponse> getJobs(
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(required = false) String query
    ) {
        int safeLimit = Math.min(Math.max(limit, 1), 100);
        int safeOffset = Math.max(offset, 0);

        Sort sort = Sort.by("desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        return service.getJobsPage(safeLimit, safeOffset, sort, query);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Job> createJob(@Valid @RequestBody JobRequest request) {
        return service.createJob(request);
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Job>> updateJob(@PathVariable String id, @Valid @RequestBody JobRequest request) {
        return service.updateJob(id, request)
                .map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Object>> deleteJob(@PathVariable String id) {
        return service.getJobById(id)
                .flatMap(existing ->
                        service.deleteJob(id)
                                .then(Mono.just(ResponseEntity.noContent().build()))
                )
                .defaultIfEmpty(ResponseEntity.<Object>notFound().build());
    }
}
