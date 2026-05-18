package org.tavall.control.cloud;

public final class CloudControlPanelViewHandler implements ICloudControlPanelViewHandler, CloudControlDomain {
    public String cloudBody() {
        StringBuilder builder = new StringBuilder();
        builder.append("<section><h2>Cloud Dashboard</h2><div class=\"metric-grid\">");
        builder.append(metric("Nodes", getCloudRepository().findNodes().size()));
        builder.append(metric("Workloads", getCloudRepository().findWorkloads().size()));
        builder.append(metric("Commands", getCloudRepository().findCommands().size()));
        builder.append(metric("Alerts", getCloudRepository().findAlerts().size()));
        builder.append(metric("Ports", getCloudRepository().findPorts().size()));
        builder.append(metric("Volumes", getCloudRepository().findVolumes().size()));
        builder.append(metric("Firewall", getCloudRepository().findFirewallRules().size()));
        builder.append(metric("Proxy Routes", getCloudRepository().findReverseProxyRoutes().size()));
        builder.append(metric("DNS", getCloudRepository().findDnsRecords().size()));
        builder.append(metric("Backups", getCloudRepository().findBackupJobs().size()));
        builder.append("</div></section>");
        builder.append(table("Nodes", "Node", "Status", getCloudRepository().findNodes().stream()
                .map(node -> row(node.hostname(), node.nodeStatus().name()))
                .toArray(String[]::new)));
        builder.append(table("Workloads", "Workload", "State", getCloudRepository().findWorkloads().stream()
                .map(workload -> row(workload.name(), workload.desiredState() + "/" + workload.actualState()))
                .toArray(String[]::new)));
        builder.append(table("Commands", "Command", "Status", getCloudRepository().findCommands().stream()
                .map(command -> row(command.commandType().name(), command.status().name()))
                .toArray(String[]::new)));
        builder.append(table("Alerts", "Alert", "State", getCloudRepository().findAlerts().stream()
                .map(alert -> row(alert.alertType().name(), alert.state().name()))
                .toArray(String[]::new)));
        builder.append(table("Ports", "Port", "Status", getCloudRepository().findPorts().stream()
                .map(port -> row(port.publicPort() + "/" + port.protocol(), port.status().name()))
                .toArray(String[]::new)));
        builder.append(table("Volumes", "Mount", "State", getCloudRepository().findVolumes().stream()
                .map(volume -> row(volume.mountPath(), volume.desiredState() + "/" + volume.actualState()))
                .toArray(String[]::new)));
        builder.append(table("Firewall", "Rule", "State", getCloudRepository().findFirewallRules().stream()
                .map(rule -> row(rule.action() + " " + rule.port() + "/" + rule.protocol(), rule.desiredState() + "/" + rule.actualState()))
                .toArray(String[]::new)));
        builder.append(table("Proxy Routes", "Route", "State", getCloudRepository().findReverseProxyRoutes().stream()
                .map(route -> row(route.hostname() + " -> " + route.targetHost() + ":" + route.targetPort(), route.desiredState() + "/" + route.actualState()))
                .toArray(String[]::new)));
        builder.append(table("DNS Records", "Record", "State", getCloudRepository().findDnsRecords().stream()
                .map(record -> row(record.hostname() + " " + record.recordType() + " " + record.value(), record.desiredState() + "/" + record.actualState()))
                .toArray(String[]::new)));
        builder.append(table("Backups", "Backup", "Status", getCloudRepository().findBackupJobs().stream()
                .map(backup -> row(backup.backupType().name(), backup.status().name() + "/" + backup.verificationStatus()))
                .toArray(String[]::new)));
        return builder.toString();
    }

    private String metric(String label, int value) {
        return "<div class=\"metric\"><span>" + escape(label) + "</span><strong>" + value + "</strong></div>";
    }

    private String table(String title, String firstHeader, String secondHeader, String[] rows) {
        StringBuilder builder = new StringBuilder();
        builder.append("<section><h2>").append(title).append("</h2><table><tr><th>")
                .append(firstHeader).append("</th><th>").append(secondHeader).append("</th></tr>");
        for (String row : rows) {
            builder.append(row);
        }
        if (rows.length == 0) {
            builder.append("<tr><td colspan=\"2\">No records</td></tr>");
        }
        builder.append("</table></section>");
        return builder.toString();
    }

    private String row(String firstValue, String secondValue) {
        return "<tr><td>" + escape(firstValue) + "</td><td>" + escape(secondValue) + "</td></tr>";
    }

    private String escape(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
