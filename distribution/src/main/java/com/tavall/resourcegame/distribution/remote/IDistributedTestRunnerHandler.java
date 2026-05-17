package com.tavall.resourcegame.distribution.remote;

import com.tavall.resourcegame.dependency.IDependencyInjectableInterface;

public interface IDistributedTestRunnerHandler extends IDependencyInjectableInterface {
    DistributedSmokeTestResult runDistributedSmokeTest(DistributedTestPlan plan);
}
