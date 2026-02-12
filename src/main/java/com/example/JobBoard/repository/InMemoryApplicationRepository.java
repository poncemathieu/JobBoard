package com.example.JobBoard.repository;

import com.example.JobBoard.domain.Application;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@Repository
public class InMemoryApplicationRepository {

    private final Map<String, Application> data = new ConcurrentHashMap<>();

    public Flux<Application> findAll() {
        return Flux.fromIterable(data.values());
    }

    public Mono<Application> findById(String id) {
        return Mono.justOrEmpty(data.get(id));
    }

    public Flux<Application> findByJobId(String jobId) {
        return Flux.fromStream(
                data.values().stream()
                        .filter(app -> jobId.equals(app.jobId()))
        );
    }

    public Mono<Application> save(Application application) {
        data.put(application.id(), application);
        return Mono.just(application);
    }
}
