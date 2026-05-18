package org.tavall.control.distribution.health;

import org.tavall.control.distribution.remote.RemoteTarget;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

public final class RemoteHealthCheckHandler implements IRemoteHealthCheckHandler {
    public RemoteHealthCheckResult checkRemoteServerHealth(RemoteTarget target) {
        boolean redis = verifyRedisConnectivity(target);
        boolean postgres = verifyPostgresConnectivity(target);
        boolean hytale = verifyHytaleServerReachable(target);
        return new RemoteHealthCheckResult(
                target.targetId(),
                redis,
                postgres,
                hytale,
                Instant.now(),
                Map.of(
                        "redisPort", Integer.toString(target.portOrDefault("redis", 6379)),
                        "postgresPort", Integer.toString(target.portOrDefault("postgres", 5432)),
                        "hytalePort", Integer.toString(target.portOrDefault("hytale", 25565))
                )
        );
    }

    public boolean verifyRedisConnectivity(RemoteTarget target) {
        return isPortReachable(target.host(), target.portOrDefault("redis", 6379), Duration.ofSeconds(2));
    }

    public boolean verifyPostgresConnectivity(RemoteTarget target) {
        return isPortReachable(target.host(), target.portOrDefault("postgres", 5432), Duration.ofSeconds(2));
    }

    public boolean verifyHytaleServerReachable(RemoteTarget target) {
        return isPortReachable(target.host(), target.portOrDefault("hytale", 25565), Duration.ofSeconds(2));
    }

    public boolean isPortReachable(String host, int port, Duration timeout) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), Math.toIntExact(timeout.toMillis()));
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }
}
