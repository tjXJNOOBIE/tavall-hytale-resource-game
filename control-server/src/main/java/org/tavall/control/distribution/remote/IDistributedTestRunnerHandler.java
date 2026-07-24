package org.tavall.control.distribution.remote;

import org.tavall.dependency.IDependencyInjectableInterface;

public interface IDistributedTestRunnerHandler extends IDependencyInjectableInterface {
    DistributedSmokeTestResult runDistributedSmokeTest(DistributedTestPlan plan);
}
