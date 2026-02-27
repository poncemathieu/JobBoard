package com.example.JobBoard;

import com.example.JobBoard.domain.Application;
import com.example.JobBoard.domain.ApplicationStatus;
import com.example.JobBoard.service.ApplicationService;
import com.example.JobBoard.service.exception.ApplicationNotFoundException;
import com.example.JobBoard.service.exception.DuplicateApplicationException;
import com.example.JobBoard.web.ApplicationController;
import com.example.JobBoard.web.ApplicationRequest;
import com.example.JobBoard.web.dto.UpdateApplicationStatusRequest;
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

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

@WebFluxTest(ApplicationController.class)
@WithMockUser
public class ApplicationControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private ApplicationService applicationService;

    private Application app1;
    private Application app2;

    @BeforeEach
    void setUp() {
        webTestClient = webTestClient.mutateWith(csrf());
        app1 = new Application("1", "1", "Mathieu Ponce", "mathieu@example.com", "Motivé", 85, ApplicationStatus.PENDING, Instant.now());
        app2 = new Application("2", "1", "Jean Dupont", "jean@example.com", "Intéressé", 70, ApplicationStatus.PENDING, Instant.now());
    }

    // -------------------------
    // GET /applications
    // -------------------------

    @Test
    void getApplications_shouldReturn200WithAllApplications() {
        when(applicationService.getAllApplications()).thenReturn(Flux.just(app1, app2));

        webTestClient.get().uri("/applications")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Application.class)
                .hasSize(2);
    }

    @Test
    void getApplications_shouldReturn200FilteredByJobId() {
        when(applicationService.getApplicationsForJob("1")).thenReturn(Flux.just(app1, app2));

        webTestClient.get().uri("/applications?jobId=1")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Application.class)
                .hasSize(2);
    }

    @Test
    void getApplications_shouldReturn200WithEmptyList() {
        when(applicationService.getAllApplications()).thenReturn(Flux.empty());

        webTestClient.get().uri("/applications")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Application.class)
                .hasSize(0);
    }

    // -------------------------
    // GET /applications/{id}
    // -------------------------

    @Test
    void getById_shouldReturn200WhenExists() {
        when(applicationService.getById("1")).thenReturn(Mono.just(app1));

        webTestClient.get().uri("/applications/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Application.class)
                .isEqualTo(app1);
    }

    @Test
    void getById_shouldReturn404WhenNotFound() {
        when(applicationService.getById("99")).thenReturn(Mono.empty());

        webTestClient.get().uri("/applications/99")
                .exchange()
                .expectStatus().isNotFound();
    }

    // -------------------------
    // POST /applications
    // -------------------------

    @Test
    void create_shouldReturn200WhenValid() {
        ApplicationRequest request = new ApplicationRequest("1", "Mathieu Ponce", "mathieu@example.com", "Motivé");
        when(applicationService.createApplication(any())).thenReturn(Mono.just(app1));

        webTestClient.post().uri("/applications")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Application.class)
                .isEqualTo(app1);
    }

    @Test
    void create_shouldReturn409WhenDuplicate() {
        ApplicationRequest request = new ApplicationRequest("1", "Mathieu Ponce", "mathieu@example.com", "Motivé");
        when(applicationService.createApplication(any())).thenReturn(Mono.error(new DuplicateApplicationException()));

        webTestClient.post().uri("/applications")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.status").isEqualTo(409);
    }

    @Test
    void create_shouldReturn400WhenJobIdIsBlank() {
        ApplicationRequest request = new ApplicationRequest("", "Mathieu Ponce", "mathieu@example.com", "Motivé");

        webTestClient.post().uri("/applications")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.errors.jobId").isEqualTo("jobId est obligatoire");
    }

    @Test
    void create_shouldReturn400WhenEmailInvalid() {
        ApplicationRequest request = new ApplicationRequest("1", "Mathieu Ponce", "email-invalide", "Motivé");

        webTestClient.post().uri("/applications")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.errors.candidateEmail").isEqualTo("Email invalide");
    }

    // -------------------------
    // PATCH /applications/{id}/status
    // -------------------------

    @Test
    void updateStatus_shouldReturn200WhenValid() {
        UpdateApplicationStatusRequest request = new UpdateApplicationStatusRequest(ApplicationStatus.ACCEPTED);
        Application updated = new Application("1", "1", "Mathieu Ponce", "mathieu@example.com", "Motivé", 85, ApplicationStatus.ACCEPTED, Instant.now());
        when(applicationService.updateStatus(eq("1"), eq(ApplicationStatus.ACCEPTED))).thenReturn(Mono.just(updated));

        webTestClient.patch().uri("/applications/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Application.class)
                .isEqualTo(updated);
    }

    @Test
    void updateStatus_shouldReturn404WhenNotFound() {
        UpdateApplicationStatusRequest request = new UpdateApplicationStatusRequest(ApplicationStatus.ACCEPTED);
        when(applicationService.updateStatus(eq("99"), any())).thenReturn(Mono.error(new ApplicationNotFoundException("99")));

        webTestClient.patch().uri("/applications/99/status")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isNotFound();
    }
}