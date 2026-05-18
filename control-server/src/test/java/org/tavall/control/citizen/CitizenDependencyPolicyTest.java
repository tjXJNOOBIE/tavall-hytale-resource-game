package org.tavall.control.citizen;

import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;
import org.tavall.control.clock.KingdomClockControlSystem;
import org.tavall.control.event.RecordingDomainEventPublisher;
import org.tavall.control.identity.UniversalPlayerId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CitizenDependencyPolicyTest {
    @AfterEach
    void clearDependencies() {
        DependencyLoaderAccess.clear();
    }

    @Test
    void citizenDomainAccessorsResolveRegisteredDependencies() {
        KingdomClockControlSystem clockControlSystem = KingdomClockControlSystem.inMemory(new RecordingDomainEventPublisher());
        CitizenControlSystem citizenControlSystem = CitizenControlSystem.inMemory(clockControlSystem);

        assertSame(citizenControlSystem, citizenControlSystem.getCitizenControlSystem());
        assertInstanceOf(InMemoryCitizenRepository.class, citizenControlSystem.getCitizenRepository());
        assertInstanceOf(InMemoryCitizenSummaryCacheRepository.class, citizenControlSystem.getCitizenSummaryCacheRepository());

        UniversalPlayerId playerId = UniversalPlayerId.random();
        List<CitizenData> citizens = citizenControlSystem.getCitizenCreationHandler()
                .createCitizens(playerId, "kingdom-1", 1, Instant.parse("2026-05-05T12:00:00Z").toEpochMilli(), citizenControlSystem.getCitizenAgingConfig());

        assertSame(citizens.getFirst(), citizenControlSystem.getCitizenRepository().findCitizen(citizens.getFirst().citizenId()).orElseThrow());
    }

    @Test
    void citizenPackageUsesGeneratedDomainAccessorsForDependencies() throws IOException {
        Path sourceRoot = Path.of("src/main/java/org/tavall/control/citizen");
        Pattern collaboratorField = Pattern.compile(
                "private\\s+final\\s+.*(Repository|Handler|System|Handler|Config|Catalog|Registry|Resolver).*;"
        );
        Pattern directLoaderAccess = Pattern.compile("DependencyLoaderAccess\\.");
        List<String> violations = new ArrayList<>();

        try (Stream<Path> sourceFiles = Files.walk(sourceRoot)) {
            sourceFiles
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> collectCitizenPolicyViolations(path, collaboratorField, directLoaderAccess, violations));
        }

        assertTrue(
                violations.isEmpty(),
                "Citizen middleware should use ICitizenDomain default accessors instead of cached DI collaborators:%n%s"
                        .formatted(String.join(System.lineSeparator(), violations))
        );
    }

    @Test
    void citizenPackageDoesNotIntroduceServiceClasses() throws IOException {
        Path sourceRoot = Path.of("src/main/java/org/tavall/control/citizen");
        List<String> violations = new ArrayList<>();

        try (Stream<Path> sourceFiles = Files.walk(sourceRoot)) {
            sourceFiles
                    .filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .filter(fileName -> fileName.endsWith("Service.java"))
                    .forEach(violations::add);
        }

        assertTrue(
                violations.isEmpty(),
                "Citizen middleware should not add service classes:%n%s".formatted(String.join(System.lineSeparator(), violations))
        );
    }

    private void collectCitizenPolicyViolations(
            Path path,
            Pattern collaboratorField,
            Pattern directLoaderAccess,
            List<String> violations
    ) {
        try {
            String fileName = path.getFileName().toString();
            int lineNumber = 0;
            for (String line : Files.readAllLines(path)) {
                lineNumber++;
                if (collaboratorField.matcher(line).find()) {
                    violations.add(path + ":" + lineNumber + " -> " + line.trim());
                    continue;
                }
                if (directLoaderAccess.matcher(line).find() && !fileName.equals("ICitizenDomainGenerated.java")) {
                    violations.add(path + ":" + lineNumber + " -> " + line.trim());
                }
            }
        } catch (IOException ex) {
            violations.add(path + " -> failed to read: " + ex.getMessage());
        }
    }
}
