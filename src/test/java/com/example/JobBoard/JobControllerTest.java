package com.example.JobBoard;

import com.example.JobBoard.domain.Job;
import com.example.JobBoard.service.JobService;
import com.example.JobBoard.service.exception.InvalidSalaryRangeException;
import com.example.JobBoard.service.exception.JobNotFoundException;
import com.example.JobBoard.web.dto.JobRequest;
import com.example.JobBoard.web.dto.JobsPageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;
import static org.mockito.Mockito.when;

@WebFluxTest(com.example.JobBoard.web.JobController.class)
@WithMockUser
public class JobControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private JobService jobService;

    private Job job1;
    private Job job2;

    @BeforeEach
    void setUp() {
        webTestClient = webTestClient.mutateWith(csrf());
        job1 = new Job("1", "Développeur Angular", "TechCorp", "Montréal", 60000, 80000);
        job2 = new Job("2", "Développeur Java", "JavaCorp", "Québec", 70000, 90000);
    }

    // GET /jobs/all

    @Test
    void getAllJobs_shouldReturn200WithJobs() {
        when(jobService.getAllJobs()).thenReturn(Flux.just(job1, job2));

        webTestClient.get().uri("/jobs/all")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Job.class)
                .hasSize(2);
    }

    @Test
    void getAllJobs_shouldReturn200WithEmptyList() {
        when(jobService.getAllJobs()).thenReturn(Flux.empty());

        webTestClient.get().uri("/jobs/all")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Job.class)
                .hasSize(0);
    }

    //GET /jobs/{id}

    @Test
    void getJobsById_shouldReturn200WhenJobExists() {
        when(jobService.getJobById("1")).thenReturn(Mono.just(job1));

        webTestClient.get().uri("/jobs/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Job.class)
                .isEqualTo(job1);
    }

    @Test
    void getJobById_shouldReturn404WhenJobNotFound() {
        when(jobService.getJobById("99")).thenReturn(Mono.error(new JobNotFoundException("99")));

        webTestClient.get().uri("/jobs/99")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.jobId").isEqualTo("99");
    }


    // GET /jobs

    @Test
    void getJobs_shouldReturn200WithPage() {
        JobsPageResponse page = new JobsPageResponse(List.of(job1, job2), 20, 0, 2L);
        when(jobService.getJobsPage(any(Integer.class), any(Integer.class), any(), any()))
                .thenReturn(Mono.just(page));

        webTestClient.get().uri("/jobs")
                .exchange()
                .expectStatus().isOk()
                .expectBody(JobsPageResponse.class)
                .isEqualTo(page);
    }

    // POST /jobs

    @Test
    void createJob_shouldReturn201WhenValid() {
        JobRequest request = new JobRequest("Développeur Angular", "TechCorp", "Montréal", 60000, 80000);
        when(jobService.createJob(any())).thenReturn(Mono.just(job1));

        webTestClient.post().uri("/jobs")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Job.class)
                .isEqualTo(job1);
    }

    @Test
    void createJob_shouldReturn400WhenTitleIsBlank() {
        JobRequest request = new JobRequest("", "TechCorp", "Montréal", 60000, 80000);

        webTestClient.post().uri("/jobs")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.errors.title").isEqualTo("Le titre est obligatoire");
    }

    @Test
    void createJob_shouldReturn400WhenSalaryMaxLessThanMin() {
        JobRequest request = new JobRequest("Développeur", "TechCorp", "Montréal", 80000, 60000);

        when(jobService.createJob(any()))
                .thenReturn(Mono.error(new InvalidSalaryRangeException()));

        webTestClient.post().uri("/jobs")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    // PUT /jobs/{id}

    @Test
    void updatejob_shouldReturn200WhenValid() {
        JobRequest request = new JobRequest("Développeur Sénior", "TechCorp", "Montréal", 80000, 100000);
        Job updated = new Job("1","Développeur Sénior", "TechCorp", "Montréal", 80000, 100000);
        when(jobService.updateJob(eq("1"), any())).thenReturn(Mono.just(updated));

                webTestClient.put().uri("/jobs/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(request)
                        .exchange()
                        .expectStatus().isOk()
                        .expectBody(Job.class)
                        .isEqualTo(updated);
    }

    @Test
    void updatejob_shouldReturn404WhenJobNotFound() {
        JobRequest request = new JobRequest("Développeur Senior", "TechCorp", "Montréal", 80000, 100000);
        when(jobService.updateJob(eq("99"), any())).thenReturn(Mono.error(new JobNotFoundException("99")));

        webTestClient.put().uri("/jobs/99")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isNotFound();
    }

    // DELETE /jobs/{id}

    @Test
    void deleteJon_shouldReturn204WhenJobExists() {
        when(jobService.getJobById("1")).thenReturn(Mono.just(job1));
        when(jobService.deleteJob("1")).thenReturn(Mono.empty());

        webTestClient.delete().uri("/jobs/1")
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void deleteJon_shouldReturn404WhenJobNotFound() {
        when(jobService.getJobById("99")).thenReturn(Mono.error(new JobNotFoundException("99")));

        webTestClient.delete().uri("/jobs/99")
                .exchange()
                .expectStatus().isNotFound();
    }
}
