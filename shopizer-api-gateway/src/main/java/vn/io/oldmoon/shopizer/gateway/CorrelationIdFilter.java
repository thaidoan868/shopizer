package vn.io.oldmoon.shopizer.gateway;

import java.util.UUID;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;


@Component
public class CorrelationIdFilter implements GlobalFilter, Ordered {

    private static final String HEADER = "X-Correlation-ID";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String cid = UUID.randomUUID().toString();

        return chain.filter(exchange.mutate()
                .request(r -> r.headers(h -> h.set(HEADER, cid)))
                .build()
        );
    }

    @Override
    public int getOrder() {
        return -3;
    }
}
