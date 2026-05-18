package org.tavall.control.distribution.remote;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

public interface IDistributedTestRunnerHandler extends IDependencyInjectableInterface {
    DistributedSmokeTestResult runDistributedSmokeTest(DistributedTestPlan plan);
}
