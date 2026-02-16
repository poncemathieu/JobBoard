package com.example.JobBoard.service;

import com.example.JobBoard.domain.Job;
import com.example.JobBoard.repository.InMemoryJobRepository;
import com.example.JobBoard.service.exception.InvalidSalaryRangeException;
import com.example.JobBoard.web.dto.JobRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class JobService {

    private final InMemoryJobRepository repository;

    public JobService(InMemoryJobRepository repository) {
        this.repository = repository;
    }

    public Flux<Job> getAllJobs() {
        return repository.findAll();
    }

    public Mono<Job> getJobById(String id) {
        return repository.findById(id);
    }

    public Mono<Job> createJob(JobRequest request) {

        if(request.salaryMax() < request.salaryMin()) {
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

        return repository.save(newJob);
    }

    public Mono<Void> deleteJob(String id) {
        return repository.deleteById(id);
    }
}
