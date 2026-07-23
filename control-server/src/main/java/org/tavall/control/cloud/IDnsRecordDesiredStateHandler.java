package org.tavall.control.cloud;

import org.tavall.dependency.IDependencyInjectableInterface;

public interface IDnsRecordDesiredStateHandler extends IDependencyInjectableInterface {
    DnsRecord createRecord(String hostname, DnsRecordType recordType, String value, int ttl);
}
