package org.tavall.control.distribution.remote;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class RemoteCommandPolicy implements IRemoteCommandPolicy {
    private final Set<String> allowedExecutables;

    public RemoteCommandPolicy(Set<String> allowedExecutables) {
        this.allowedExecutables = allowedExecutables == null ? defaultAllowedExecutables() : Set.copyOf(allowedExecutables);
    }

    public static RemoteCommandPolicy defaults() {
        return new RemoteCommandPolicy(defaultAllowedExecutables());
    }

    public List<String> validate(RemoteCommand command) {
        List<String> errors = new ArrayList<>();
        String executable = command.executable().toLowerCase(Locale.ROOT);
        if (command.highRisk()) {
            errors.add("High-risk remote commands are not allowed by the default bridge.");
        }
        if (!allowedExecutables.contains(executable)) {
            errors.add("Executable is not allowlisted for remote bridge use: " + command.executable() + ".");
        }
        List<String> tokens = new ArrayList<>();
        tokens.add(command.executable());
        tokens.addAll(command.arguments());
        for (String token : tokens) {
            String lower = token.toLowerCase(Locale.ROOT);
            if (lower.contains("id_rsa")
                    || lower.contains("private_key")
                    || lower.contains("begin openssh private key")
                    || lower.contains("begin rsa private key")
                    || lower.contains("password=")
                    || lower.contains("secret=")) {
                errors.add("Command token references sensitive credential material.");
                break;
            }
        }
        return errors;
    }

    private static Set<String> defaultAllowedExecutables() {
        return Set.of(
                "hostname",
                "whoami",
                "pwd",
                "uname",
                "java",
                "curl",
                "nc",
                "ss",
                "systemctl",
                "test",
                "printf",
                "echo"
        );
    }
}
