package com.tavall.resourcegame.middleware.cloud;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

public interface INodeMetricIngestionHandler extends IDependencyInjectableInterface {
    void ingest(NodeMetricSnapshot snapshot);
}
