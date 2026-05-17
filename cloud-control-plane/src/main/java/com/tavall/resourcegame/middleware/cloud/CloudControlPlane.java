package com.tavall.resourcegame.middleware.cloud;

public final class CloudControlPlane implements ICloudControlDomain {
    public CloudControlPlaneRuntime runtime() {
        return new CloudControlPlaneRuntime();
    }
}
