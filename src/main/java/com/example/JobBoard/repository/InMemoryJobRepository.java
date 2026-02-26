package com.example.JobBoard.repository;

import com.example.JobBoard.domain.Job;
import com.example.JobBoard.service.JobService;
import com.example.JobBoard.service.SortSpec;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@Repository
public class InMemoryJobRepository {

    private final Map<String, Job> data = new ConcurrentHashMap<>();

    public InMemoryJobRepository() {
        Job j1 = new Job("1", "Développeur Angular", "Sollio", "Montréal", 55, 70);
        Job j2 = new Job("2", "Développeur Java", "BNC", "Montréal", 70, 80);
        data.put(j1.id(), j1);
        data.put(j2.id(), j2);
    }

    private String normalize(String input) {
        return Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase();
    }

    public Mono<Long> count(String query) {

        return findAll()
                .filter(job -> query == null || query.isBlank() ||
                        normalize(job.title()).contains(normalize(query)) ||
                        normalize(job.company()).contains(normalize(query)) ||
                        normalize(job.location()).contains(normalize(query)))
                .count();
    }

    public Flux<Job> findPage(int limit, int offset, Sort sort, String query) {

        Stream<Job> stream = data.values().stream();

        if(query != null && !query.isBlank()) {
            String q = normalize(query);

            stream = stream.filter(job ->
                    normalize(job.title()).contains(q)
                            || normalize(job.company()).contains(q)
                            || normalize(job.location()).contains(q)
            );
        }

        Comparator<Job> comparator = toComparator(sort);

        return Flux.fromStream(
                stream.sorted(comparator)
                        .skip(offset)
                        .limit(limit)
        );
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

    private Comparator<Job> toComparator(Sort sort) {
        Sort.Order order = sort.iterator().next();

        Comparator<Job> comparator = switch (order.getProperty()) {
            case "title" -> Comparator.comparing(Job::title, String.CASE_INSENSITIVE_ORDER);
            case "company" -> Comparator.comparing(Job::company, String.CASE_INSENSITIVE_ORDER);
            case "location" -> Comparator.comparing(Job::location, String.CASE_INSENSITIVE_ORDER);
            case "salaryMin" -> Comparator.comparing(Job::salaryMax);
            case "salaryMax" -> Comparator.comparing(Job::salaryMin);
            default -> Comparator.comparing(Job::id);
        };

        return order.isDescending() ? comparator.reversed() : comparator;
    }
}
