package com.aegis.telemetry.instrumentation.aop;

import com.aegis.telemetry.instrumentation.exception.RuntimeExceptionHandler;
import com.aegis.telemetry.instrumentation.integration.RuntimeInstrumentationBridge;
import com.aegis.telemetry.instrumentation.metadata.RuntimeMetadataProvider;
import com.aegis.telemetry.instrumentation.thread.ThreadMetadataProvider;
import com.aegis.telemetry.sdk.RuntimeTelemetrySdk;
import com.aegis.telemetry.sdk.config.RuntimeSdkConfiguration;
import com.aegis.telemetry.trace.context.TraceContextHolder;
import com.aegis.telemetry.trace.factory.TraceContextFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RuntimeMethodInterceptorTest {

    private final RuntimeTelemetrySdk sdk = RuntimeTelemetrySdk.getInstance();

    @AfterEach
    void tearDown() {
        sdk.shutdown();
        TraceContextHolder.clear();
    }

    @Test
    void interceptsServiceMethodExecution() throws Throwable {
        sdk.initialize(RuntimeSdkConfiguration.builder()
                .applicationName("order-service")
                .instanceId("instance-1")
                .environment("test")
                .heartbeatInterval(Duration.ofSeconds(30))
                .build());

        RuntimeMethodInterceptor interceptor = new RuntimeMethodInterceptor(sdk, new TraceContextFactory("order-service"), new RuntimeInstrumentationBridge(sdk, new RuntimeMetadataProvider(sdk), new ThreadMetadataProvider()), new RuntimeExceptionHandler(new RuntimeInstrumentationBridge(sdk, new RuntimeMetadataProvider(sdk), new ThreadMetadataProvider())), null);
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getDeclaringTypeName()).thenReturn("com.example.OrderService");
        when(signature.getName()).thenReturn("placeOrder");
        when(joinPoint.getArgs()).thenReturn(new Object[]{"PAY-1"});
        when(joinPoint.proceed()).thenReturn("ok");

        Object result = interceptor.around(joinPoint);

        assertThat(result).isEqualTo("ok");
        assertThat(sdk.getCurrentTraceContext()).isPresent();
    }

    @Test
    void interceptsRepositoryMethodExecution() throws Throwable {
        sdk.initialize(RuntimeSdkConfiguration.builder()
                .applicationName("order-service")
                .instanceId("instance-1")
                .environment("test")
                .heartbeatInterval(Duration.ofSeconds(30))
                .build());

        RuntimeMethodInterceptor interceptor = new RuntimeMethodInterceptor(sdk, new TraceContextFactory("order-service"), new RuntimeInstrumentationBridge(sdk, new RuntimeMetadataProvider(sdk), new ThreadMetadataProvider()), new RuntimeExceptionHandler(new RuntimeInstrumentationBridge(sdk, new RuntimeMetadataProvider(sdk), new ThreadMetadataProvider())), null);
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getDeclaringTypeName()).thenReturn("com.example.OrderRepository");
        when(signature.getName()).thenReturn("save");
        when(joinPoint.getArgs()).thenReturn(new Object[]{"entity"});
        when(joinPoint.proceed()).thenReturn(1L);

        Object result = interceptor.around(joinPoint);

        assertThat(result).isEqualTo(1L);
    }
}
