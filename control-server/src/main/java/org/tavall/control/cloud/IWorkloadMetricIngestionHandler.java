package org.tavall.control.cloud;

import org.tavall.dependency.IDependencyInjectableInterface;

public interface IWorkloadMetricIngestionHandler extends IDependencyInjectableInterface {
    void ingest(WorkloadMetricSnapshot snapshot);
}
