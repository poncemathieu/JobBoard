package com.example.JobBoard.repository;

import com.example.JobBoard.domain.Job;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryJobRepository {

    private final Map<String, Job> data = new ConcurrentHashMap<>();

    public InMemoryJobRepository() {
        Job j1 = new Job("1", "Développeur Angular", "Sollio", "Montréal", 55, 70);
        Job j2 = new Job("2", "Développeur Java", "BNC", "Montréal", 70, 80);
        data.put(j1.id(), j1);
        data.put(j2.id(), j2);
    }

    public Flux<Job> findAll() {
        return Flux.fromIterable(data.values());
    }

    public Mono<Job> findById(String id){
        return Mono.justOrEmpty(data.get(id));
    }

    public Mono<Job> save(Job job) {
        data.put(job.id(), job);
        return Mono.just(job);
    }

    public Mono<Void> deleteById(String id) {
        data.remove(id);
        return Mono.empty();
    }
}
