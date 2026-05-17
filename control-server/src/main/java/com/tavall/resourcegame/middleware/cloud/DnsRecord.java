package com.tavall.resourcegame.middleware.cloud;

import java.util.Map;
import java.util.UUID;

public record DnsRecord(
        UUID recordId,
        String hostname,
        DnsRecordType recordType,
        String value,
        int ttl,
        CloudDesiredState desiredState,
        CloudDesiredState actualState,
        Map<String, String> metadata
) {
    public DnsRecord {
        recordType = recordType == null ? DnsRecordType.A : recordType;
        ttl = ttl <= 0 ? 300 : ttl;
        desiredState = desiredState == null ? CloudDesiredState.DESIRED : desiredState;
        actualState = actualState == null ? CloudDesiredState.DESIRED : actualState;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
