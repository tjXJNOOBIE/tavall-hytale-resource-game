package org.tavall.control.cloud;

import java.util.Map;
import java.util.UUID;

public final class DnsRecordDesiredStateHandler implements IDnsRecordDesiredStateHandler, CloudControlDomain {
    @Override
    public DnsRecord createRecord(String hostname, DnsRecordType recordType, String value, int ttl) {
        if (hostname == null || hostname.isBlank()) {
            throw new IllegalArgumentException("DNS hostname is required.");
        }
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("DNS record value is required.");
        }
        DnsRecord record = new DnsRecord(UUID.randomUUID(), hostname, recordType, value, ttl, CloudDesiredState.DESIRED,
                CloudDesiredState.DESIRED, Map.of());
        getCloudRepository().saveDnsRecord(record);
        return record;
    }
}
