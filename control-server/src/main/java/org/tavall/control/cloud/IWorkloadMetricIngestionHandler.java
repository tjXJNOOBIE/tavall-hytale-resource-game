package org.tavall.control.cloud;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

public interface IWorkloadMetricIngestionHandler extends IDependencyInjectableInterface {
    void ingest(WorkloadMetricSnapshot snapshot);
}
