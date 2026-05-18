package org.tavall.control.cloud;

public final class CloudAgentApplication implements CloudAgentDomain {
    public static void main(String[] args) throws Exception {
        new CloudAgentDependencyModule().registerDependencies();
        new CloudAgentApplication().getCloudAgentRuntime().runForever();
    }
}
