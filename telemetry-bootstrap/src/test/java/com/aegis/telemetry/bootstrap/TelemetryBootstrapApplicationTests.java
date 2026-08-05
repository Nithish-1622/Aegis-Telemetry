package com.aegis.telemetry.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.kafka.listener.auto-startup=false",
        "spring.main.web-application-type=servlet"
})
class TelemetryBootstrapApplicationTests {

    @Test
    void contextLoads() {
        assertThat(true).isTrue();
    }
}
