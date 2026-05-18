package org.tavall.control.cloud;

public final class CloudControlPlaneApplication {
    private CloudControlPlaneApplication() {
    }

    public static void main(String[] args) {
        CloudControlPlaneRuntimeFactory.createInMemoryRuntime();
        new CloudControlPlaneConsole().run();
    }
}
