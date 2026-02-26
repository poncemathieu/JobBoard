package com.example.JobBoard.filters;

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

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        // tentative de récupération du traceId
        String traceId = exchange.getRequest().getHeaders().getFirst(TRACE_ID_HEADER);

        //Si pas de traceId, generation automatique
        if(traceId == null || traceId.isBlank()) {
            traceId = generateShortUUID();
        }

        String finalTraceId = traceId;

        //Ajout du traceId dans le header de la reponse HTTP
        exchange.getResponse().getHeaders()
                .add(TRACE_ID_HEADER, finalTraceId);

        return chain.filter(exchange)
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
