package com.tavall.resourcegame.middleware.cloud;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

public interface IWorkloadMetricIngestionHandler extends IDependencyInjectableInterface {
    void ingest(WorkloadMetricSnapshot snapshot);
}
