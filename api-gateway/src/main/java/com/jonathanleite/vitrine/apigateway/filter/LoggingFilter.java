package com.jonathanleite.vitrine.apigateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);
    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        long startTime = System.currentTimeMillis();

        String path = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod().name();
        String ip = exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
        String correlationId = exchange.getRequest().getHeaders().getFirst(CORRELATION_ID_HEADER);

        log.info("Request: {} {} | CorrelationId: {} | IP: {}", method, path, correlationId, ip);

        return chain.filter(exchange)
                .doOnError(ex -> log.error(
                        "Request failed: {} {} | CorrelationId: {} | Error: {}",
                        method,
                        path,
                        correlationId,
                        ex.getClass().getSimpleName()
                ))
                .then(Mono.fromRunnable(() -> {

                    long duration = System.currentTimeMillis() - startTime;

                    int status = exchange.getResponse().getStatusCode() != null
                            ? exchange.getResponse().getStatusCode().value()
                            : 0;

                    log.info("Response: {} {} | CorrelationId: {} | Status: {} | Time: {} ms",
                            method, path, correlationId, status, duration);
                }));
    }

    @Override
    public int getOrder() {
        return -2;
    }
}
