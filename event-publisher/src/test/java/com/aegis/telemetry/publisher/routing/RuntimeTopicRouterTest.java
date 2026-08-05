package com.aegis.telemetry.publisher.routing;

import com.aegis.telemetry.contracts.enums.RuntimeEventType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RuntimeTopicRouterTest {

    private final RuntimeTopicRouter router = new RuntimeTopicRouter();

    @Test
    void routesRequestAndServiceEventsToRuntimeEventsTopic() {
        assertThat(router.route(RuntimeEventType.REQUEST_STARTED)).isEqualTo("runtime.events");
        assertThat(router.route(RuntimeEventType.REQUEST_COMPLETED)).isEqualTo("runtime.events");
        assertThat(router.route(RuntimeEventType.SERVICE_CALL_STARTED)).isEqualTo("runtime.events");
        assertThat(router.route(RuntimeEventType.SERVICE_CALL_COMPLETED)).isEqualTo("runtime.events");
    }

    @Test
    void routesSpecializedEventsToTheirSpecificTopics() {
        assertThat(router.route(RuntimeEventType.ERROR_OCCURRED)).isEqualTo("runtime.errors");
        assertThat(router.route(RuntimeEventType.RETRY_TRIGGERED)).isEqualTo("runtime.retries");
        assertThat(router.route(RuntimeEventType.HEARTBEAT)).isEqualTo("runtime.heartbeats");
    }
}
