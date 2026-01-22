package com.nequi.franchise.infrastructure.config;

import io.micrometer.tracing.Tracer;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
public class TracingConfig implements WebFilter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String CORRELATION_ID_MDC_KEY = "correlationId";
    private static final String TRACE_ID_MDC_KEY = "traceId";
    private static final String SPAN_ID_MDC_KEY = "spanId";

    private final Tracer tracer;

    public TracingConfig(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String correlationId = exchange.getRequest().getHeaders().getFirst(CORRELATION_ID_HEADER);
        
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = java.util.UUID.randomUUID().toString();
        }

        exchange.getResponse().getHeaders().add(CORRELATION_ID_HEADER, correlationId);

        String finalCorrelationId = correlationId;
        
        return chain.filter(exchange)
                .contextWrite(ctx -> {
                    MDC.put(CORRELATION_ID_MDC_KEY, finalCorrelationId);
                    
                    if (tracer.currentSpan() != null) {
                        String traceId = tracer.currentSpan().context().traceId();
                        String spanId = tracer.currentSpan().context().spanId();
                        MDC.put(TRACE_ID_MDC_KEY, traceId);
                        MDC.put(SPAN_ID_MDC_KEY, spanId);
                    }
                    
                    return ctx.put(CORRELATION_ID_MDC_KEY, finalCorrelationId);
                })
                .doFinally(signalType -> {
                    MDC.remove(CORRELATION_ID_MDC_KEY);
                    MDC.remove(TRACE_ID_MDC_KEY);
                    MDC.remove(SPAN_ID_MDC_KEY);
                });
    }
}
