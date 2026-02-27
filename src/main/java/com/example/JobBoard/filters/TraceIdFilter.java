package com.example.JobBoard.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class TraceIdFilter implements WebFilter {

    //Nom du header HTTP entrant et sortant
    public static final String TRACE_ID_HEADER = "X-Request-Id";
    // Clé utilisée pour stocker le traceId
    public static final String TRACE_ID_KEY = "traceId";

    private static final Logger log = LoggerFactory.getLogger(TraceIdFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        // tentative de récupération du traceId
        String traceId = exchange.getRequest().getHeaders().getFirst(TRACE_ID_HEADER);

        //Si pas de traceId, generation automatique
        if(traceId == null || traceId.isBlank()) {
            traceId = generateShortUUID();
        }

        String finalTraceId = traceId;
        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getPath().value();

        //Ajout du traceId dans le header de la reponse HTTP
        exchange.getResponse().getHeaders()
                .add(TRACE_ID_HEADER, finalTraceId);

        log.info("[{}] >>>> {} {}", finalTraceId, method, path);

        return chain.filter(exchange)
                .doFinally(signal -> {
                    int status = exchange.getResponse().getStatusCode() != null
                            ? exchange.getResponse().getStatusCode().value()
                            : 0;
                    log.info("[{}] <<<<<< {} {} - {}", finalTraceId, method, path, status);
                })
                .contextWrite(ctx -> ctx.put(TRACE_ID_KEY, finalTraceId));
    }

    //Generation d'un UUID aleatoire et garder uniquement les 8 premiers caracteres
    private String generateShortUUID() {
        return java.util.UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8);
    }
}
