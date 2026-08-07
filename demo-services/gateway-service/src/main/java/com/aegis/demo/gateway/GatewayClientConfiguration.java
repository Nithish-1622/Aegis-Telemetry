package com.aegis.demo.gateway;

import com.aegis.telemetry.contracts.trace.TraceHeaders;
import com.aegis.telemetry.trace.context.TraceContextHolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@Configuration
public class GatewayClientConfiguration {

    @Bean
    RestClient orderRestClient(RestClient.Builder builder, @Value("${app.order.base-url:http://localhost:8081}") String baseUrl) {
        return builder.baseUrl(baseUrl)
                .requestInterceptor((request, body, execution) -> {
                    TraceContextHolder.getContext().ifPresent(context -> {
                        request.getHeaders().set(TraceHeaders.TRACE_ID, context.traceId());
                        request.getHeaders().set(TraceHeaders.SPAN_ID, context.spanId());
                        if (context.parentSpanId() != null) {
                            request.getHeaders().set(TraceHeaders.PARENT_SPAN_ID, context.parentSpanId());
                        }
                    });
                    request.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                    return execution.execute(request, body);
                })
                .build();
    }
}
