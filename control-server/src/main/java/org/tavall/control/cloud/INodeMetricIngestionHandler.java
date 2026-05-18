package org.tavall.control.cloud;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

public interface INodeMetricIngestionHandler extends IDependencyInjectableInterface {
    void ingest(NodeMetricSnapshot snapshot);
}
