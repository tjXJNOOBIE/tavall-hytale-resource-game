package org.tavall.control.cloud;

public final class CloudControlPlane implements CloudControlDomain {
    public CloudControlPlaneRuntime runtime() {
        return new CloudControlPlaneRuntime();
    }
}
