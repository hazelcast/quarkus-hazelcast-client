package io.quarkus.hazelcast.client.runtime.health;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.LifecycleEvent;
import com.hazelcast.core.LifecycleListener;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Liveness;

import javax.enterprise.context.ApplicationScoped;

@Liveness
@ApplicationScoped
public class HazelcastClientHealthCheck implements HealthCheck {

    private final HazelcastInstance hazelcastClient;

    private volatile boolean connected;
    private volatile LifecycleEvent.LifecycleState state;

    HazelcastClientHealthCheck(HazelcastInstance hazelcastClient) {
        this.hazelcastClient = hazelcastClient;
        this.hazelcastClient.getLifecycleService().addLifecycleListener(event -> {
            state = event.getState();
            if (event.getState() == LifecycleEvent.LifecycleState.CLIENT_CONNECTED) {
                connected = true;
            }

            if (event.getState() == LifecycleEvent.LifecycleState.CLIENT_DISCONNECTED) {
                connected = false;
            }
        });
    }

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder responseBuilder = HealthCheckResponse.named("Hazelcast client state health check");

        if (connected) {
            try {
                return responseBuilder
                  .up()
                  .withData("name", hazelcastClient.getName())
                  .withData("uuid", hazelcastClient.getLocalEndpoint().getUuid().toString())
                  .build();
            } catch (Throwable e) {
                return responseBuilder
                  .down()
                  .withData("name", hazelcastClient.getName())
                  .withData("uuid", hazelcastClient.getLocalEndpoint().getUuid().toString())
                  .withData("technical_error", e.getMessage())
                  .build();
            }
        } else {
            return responseBuilder.down()
              .withData("name", hazelcastClient.getName())
              .withData("state", state.toString())
              .withData("uuid", hazelcastClient.getLocalEndpoint().getUuid().toString())
              .build();
        }
    }
}
