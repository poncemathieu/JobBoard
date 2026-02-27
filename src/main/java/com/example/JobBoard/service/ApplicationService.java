package com.example.JobBoard.service;

import com.example.JobBoard.domain.Application;
import com.example.JobBoard.domain.ApplicationStatus;
import com.example.JobBoard.filters.TraceIdFilter;
import com.example.JobBoard.repository.InMemoryApplicationRepository;
import com.example.JobBoard.service.exception.ApplicationNotFoundException;
import com.example.JobBoard.service.exception.DuplicateApplicationException;
import com.example.JobBoard.service.exception.JobNotFoundException;
import com.example.JobBoard.web.ApplicationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ApplicationService {

    private static final Logger log = LoggerFactory.getLogger(ApplicationService.class);

    private final InMemoryApplicationRepository repository;

    private final JobService jobService;

    public ApplicationService(InMemoryApplicationRepository repository, JobService jobService) {
        this.repository = repository;
        this.jobService = jobService;
    }

    public Mono<Application> createApplication(ApplicationRequest request) {
        String jobId = request.jobId();
        String email = request.candidateEmail();

        return Mono.deferContextual(ctx -> {
            String traceId = ctx.getOrDefault(TraceIdFilter.TRACE_ID_KEY, "no-trace");

            log.info("[{}] createApplication called - jobId={}, email={}", traceId, jobId, email);

            return jobService.getJobById(jobId)
                    .switchIfEmpty(Mono.error(new JobNotFoundException(jobId)))
                    .flatMap(job ->
                            repository.findByJobIdAndEmail(jobId, email)
                                    .flatMap(existing -> {
                                        log.warn("[{}] createApplication failed - duplicate jobId={}, email={}", traceId, jobId, email);
                                        return Mono.<Application>error(new DuplicateApplicationException());
                                    })
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
                                                ApplicationStatus.PENDING,
                                                Instant.now()
                                        );

                                        return repository.save(app);
                                    }))
                    );
        });

    }

    public Mono<Application> updateStatus(String applicationId, ApplicationStatus status) {

        return Mono.deferContextual(ctx -> {
            String traceId = ctx.getOrDefault(TraceIdFilter.TRACE_ID_KEY, "no-trace");

            log.info("[{}] updateStatus called - applicationId={}, status={}", traceId, applicationId, status);

            return repository.findById(applicationId)
                    .switchIfEmpty(Mono.error(new ApplicationNotFoundException(applicationId)))
                    .flatMap(existing -> {
                        Application updated = new Application(
                                existing.id(),
                                existing.jobId(),
                                existing.candidateName(),
                                existing.candidateEmail(),
                                existing.message(),
                                existing.score(),
                                status,
                                existing.createdAt()
                        );
                        log.info("[{}] updateStatus success - applicationId={}", traceId, applicationId);
                        return repository.update(updated);
                    });
        });
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
