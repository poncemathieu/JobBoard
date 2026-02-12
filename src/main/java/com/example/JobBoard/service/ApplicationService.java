package com.example.JobBoard.service;

import com.example.JobBoard.domain.Application;
import com.example.JobBoard.repository.InMemoryApplicationRepository;
import com.example.JobBoard.web.ApplicationRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ApplicationService {

    private final InMemoryApplicationRepository repository;

    public ApplicationService(InMemoryApplicationRepository repository) {
        this.repository = repository;
    }

    public Mono<Application> createApplication(ApplicationRequest request) {
        String id = UUID.randomUUID().toString();

        //Mock
        int score = ThreadLocalRandom.current().nextInt(50, 101);

        Application app = new Application(
                id,
                request.jobId(),
                request.candidateName(),
                request.candidateEmail(),
                request.message(),
                score,
                "PENDING",
                Instant.now()
        );

        return repository.save(app);
    }

    public Flux<Application> getAllApplications() {
        return repository.findAll();
    }

    public Flux<Application> getApplicationsForJob(String jobId) {
        return repository.findByJobId(jobId);
    }

    public Mono<Application> getById(String id) {
        return repository.findById(id);
    }


}
