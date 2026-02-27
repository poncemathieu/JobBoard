package com.example.JobBoard.service;

import com.example.JobBoard.domain.Job;
import com.example.JobBoard.filters.TraceIdFilter;
import com.example.JobBoard.repository.InMemoryJobRepository;
import com.example.JobBoard.service.exception.InvalidSalaryRangeException;
import com.example.JobBoard.service.exception.JobNotFoundException;
import com.example.JobBoard.web.dto.JobRequest;
import com.example.JobBoard.web.dto.JobsPageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class JobService {

    private final InMemoryJobRepository repository;
    private static final Logger log = LoggerFactory.getLogger(JobService.class);

    public JobService(InMemoryJobRepository repository) {
        this.repository = repository;
    }

    public Flux<Job> getAllJobs() {
        return Flux.deferContextual(ctx -> {
            String traceId = ctx.getOrDefault(TraceIdFilter.TRACE_ID_KEY, "no-trace");
            log.info("[{}] getAllJobs called",  traceId);

            return repository.findAll()
                    .doOnComplete(() -> log.info("[{}] getAllJobs completed",traceId));
        });

    }

    public Mono<Job> getJobById(String id) {
        return Mono.deferContextual(ctx -> {
            String traceId = ctx.getOrDefault(TraceIdFilter.TRACE_ID_KEY, "");
            log.info("[{}] getJobById called - id={}",  traceId, id);

            return repository.findById(id)
                    .switchIfEmpty(
                            Mono.defer(() -> {
                               return Mono.error(new JobNotFoundException(id));
                            }))
                    .doOnSuccess(job -> log.info("[{}] getJobById completed",traceId));
        });

    }

    public Mono<JobsPageResponse> getJobsPage(int limit, int offset, Sort sort, String query) {
       return Mono.deferContextual(ctx -> {

           String traceId = ctx.getOrDefault(TraceIdFilter.TRACE_ID_KEY, "no-trace");

           log.info("[{}] getJobsPage called - limit={}, offset={}, query={}", traceId, limit, offset, query);

           return repository.count(query)
                   .zipWith(repository.findPage(limit, offset, sort, query).collectList())
                   .map(result -> {
                       log.info("[{}] getJobsPage success - total={}, returned={}", traceId, result.getT1(), result.getT2().size());
                               return new JobsPageResponse(
                                       result.getT2(),
                                       limit,
                                       offset,
                                       result.getT1()
                               );
                           });
       });
    }

    public Mono<Job> createJob(JobRequest request) {
        return Mono.deferContextual(ctx -> {
            String traceId = ctx.getOrDefault(TraceIdFilter.TRACE_ID_KEY, "");
            log.info("[{}] createJob called - title={}", traceId, request.title());

            if(request.salaryMax() < request.salaryMin()) {
                log.warn("[{}] createJob failed - invalid salary range min={}, max={}", traceId, request.salaryMin(), request.salaryMax());
                return Mono.error(new InvalidSalaryRangeException());
            }
            String id = UUID.randomUUID().toString();
            Job newJob = new Job(
                    id,
                    request.title(),
                    request.company(),
                    request.location(),
                    request.salaryMin(),
                    request.salaryMax()
            );
            return repository.save(newJob)
                    .doOnSuccess(job -> log.info("[{}] createJob success - id={}",traceId, job.id()));
        });
    }

    public Mono<Job> updateJob(String id, JobRequest request) {
        return Mono.deferContextual(ctx -> {
            String traceId = ctx.getOrDefault(TraceIdFilter.TRACE_ID_KEY, "");
            log.info("[{}] updateJob called - id={}", traceId, id);

            if(request.salaryMax() < request.salaryMin()) {
                log.warn("[{}] updateJob failed - invalid salary range min={}, max={}", traceId, request.salaryMin(), request.salaryMax());
                return Mono.error(new InvalidSalaryRangeException());
            }

            return repository.findById(id)
                    .switchIfEmpty(Mono.error(new JobNotFoundException(id)))
                    .flatMap(existing -> {
                        Job updated = new Job(
                                id,
                                request.title(),
                                request.company(),
                                request.location(),
                                request.salaryMin(),
                                request.salaryMax()
                        );
                        return repository.save(updated)
                                .doOnSuccess(job -> log.info("[{}] updateJob success - id={}",traceId, id));
                    });
        });

    }

    public Mono<Void> deleteJob(String id) {
        return Mono.deferContextual(ctx -> {
            String traceId = ctx.getOrDefault(TraceIdFilter.TRACE_ID_KEY, "");
            log.info("[{}] deleteJob called - id={}", traceId, id);
            return repository.deleteById(id)
                    .doOnSuccess(v -> log.info("[{}] deleteJob success - id={}",traceId, id));
        });

    }
}
