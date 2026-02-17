package com.example.JobBoard.service;

import com.example.JobBoard.domain.Application;
import com.example.JobBoard.repository.InMemoryApplicationRepository;
import com.example.JobBoard.service.exception.DuplicateApplicationException;
import com.example.JobBoard.service.exception.JobNotFoundException;
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

    private final JobService jobService;

    public ApplicationService(InMemoryApplicationRepository repository, JobService jobService) {
        this.repository = repository;
        this.jobService = jobService;
    }

    public Mono<Application> createApplication(ApplicationRequest request) {
        String jobId = request.jobId();
        String email = request.candidateEmail();


        return jobService.getJobById(jobId)
                .switchIfEmpty(Mono.error(new JobNotFoundException(jobId)))
                .flatMap(job ->
                        repository.findByJobIdAndEmail(jobId, email)
                                .flatMap(existing ->
                                        Mono.<Application>error(new DuplicateApplicationException())
                                )
                                .switchIfEmpty(Mono.defer(() -> {
                                    String id = UUID.randomUUID().toString();
                                    int score = ThreadLocalRandom.current().nextInt(1, 101);

                                    Application app = new Application(
                                            id,
                                            jobId,
                                            request.candidateName(),
                                            email,
                                            request.message(),
                                            score,
                                            "PENDING",
                                            Instant.now()
                                    );

                                    return repository.save(app);
                                }))
                );
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
