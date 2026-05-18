package org.tavall.control.cloud;

public final class CloudControlPlane implements ICloudControlDomain {
    public CloudControlPlaneRuntime runtime() {
        return new CloudControlPlaneRuntime();
    }
}
