package com.tavall.resourcegame.middleware.cloud;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

public interface IDnsRecordDesiredStateHandler extends IDependencyInjectableInterface {
    DnsRecord createRecord(String hostname, DnsRecordType recordType, String value, int ttl);
}
