package com.tavall.resourcegame.middleware.cloud;

import java.time.Instant;

public final class CloudControlPlaneRuntimeFactory implements ICloudControlDomain {
    private CloudControlPlaneRuntimeFactory() {
    }

    public static CloudControlPlaneRuntime createInMemoryRuntime() {
        new CloudControlDependencyModule().registerDependencies();
        CloudControlPlane controlPlane = new CloudControlPlane();
        controlPlane.getCloudControlPlaneFilesystemLayout().ensureLayout();
        controlPlane.getCloudOwnerAuthorityBootstrapHandler().ensureLocalOwnerAuthority(Instant.now());
        return controlPlane.runtime();
    }
}
