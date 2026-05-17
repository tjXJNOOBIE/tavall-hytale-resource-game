package com.tavall.resourcegame.distribution.remote;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

public interface IDistributedTestRunnerHandler extends IDependencyInjectableInterface {
    DistributedSmokeTestResult runDistributedSmokeTest(DistributedTestPlan plan);
}
