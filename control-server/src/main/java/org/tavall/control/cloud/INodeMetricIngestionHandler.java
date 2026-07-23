package org.tavall.control.cloud;

import org.tavall.dependency.IDependencyInjectableInterface;

public interface INodeMetricIngestionHandler extends IDependencyInjectableInterface {
    void ingest(NodeMetricSnapshot snapshot);
}
