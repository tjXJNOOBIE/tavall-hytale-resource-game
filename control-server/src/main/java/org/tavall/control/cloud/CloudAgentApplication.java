package org.tavall.control.cloud;

public final class CloudAgentApplication implements ICloudAgentDomain {
    public static void main(String[] args) throws Exception {
        new CloudAgentDependencyModule().registerDependencies();
        new CloudAgentApplication().getCloudAgentRuntime().runForever();
    }
}
