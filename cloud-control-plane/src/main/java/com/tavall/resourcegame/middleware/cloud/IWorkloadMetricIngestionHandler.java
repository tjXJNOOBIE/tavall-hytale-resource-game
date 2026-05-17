package com.tavall.resourcegame.middleware.cloud;

import com.tavall.resourcegame.dependency.IDependencyInjectableInterface;

public interface IWorkloadMetricIngestionHandler extends IDependencyInjectableInterface {
    void ingest(WorkloadMetricSnapshot snapshot);
}
