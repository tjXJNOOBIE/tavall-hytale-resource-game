package com.tavall.resourcegame.middleware.cloud;

import com.tavall.resourcegame.dependency.IDependencyInjectableInterface;

public interface INodeMetricIngestionHandler extends IDependencyInjectableInterface {
    void ingest(NodeMetricSnapshot snapshot);
}
