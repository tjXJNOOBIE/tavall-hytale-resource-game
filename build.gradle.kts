import java.util.zip.ZipFile

plugins {
    base
}

group = "com.tavall"
extra["versionTagPrefix"] = "tavall-hytale-resource-game"
extra["fallbackVersion"] = "0.1.1"
apply(from = "gradle/git-version.gradle.kts")
version = extra["gitVersion"] as String

val hytaleServerVersion = "2026.03.26-89796e57b"
val jacksonVersion = "2.18.3"
val junitVersion = "6.0.3"
val springBootVersion = "4.1.0"
val tavallToolsVersion = "1.0.0"
val cacheVersion = "1.0.0"
val pluginManifestVersion = version.toString().let { buildVersion ->
    if (buildVersion.endsWith("-SNAPSHOT")) {
        "${buildVersion.substringBefore('-')}-SNAPSHOT"
    } else {
        buildVersion
    }
}

subprojects {
    group = rootProject.group
    version = rootProject.version

    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    extensions.configure<JavaPluginExtension> {
        toolchain.languageVersion = JavaLanguageVersion.of(25)
        withSourcesJar()
    }

    repositories {
        mavenLocal()
        mavenCentral()
        maven {
            name = "CodeMCHytale"
            url = uri("https://repo.codemc.io/repository/hytale/")
        }
        maven {
            name = "CurseMaven"
            url = uri("https://www.cursemaven.com")
        }
        maven {
            name = "PaperMC"
            url = uri("https://repo.papermc.io/repository/maven-public/")
        }
        maven {
            name = "SpigotSnapshots"
            url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        }
    }

    dependencies {
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher:6.0.3")
    }

    dependencyLocking {
        lockAllConfigurations()
    }

    tasks.withType<JavaCompile>().configureEach {
        options.release = 25
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }

    tasks.withType<Jar>().configureEach {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }

    val verifyThinJar = tasks.register("verifyThinJar") {
        dependsOn(tasks.named("jar"))
        val archive = tasks.named<Jar>("jar").flatMap { it.archiveFile }
        inputs.file(archive)
        doLast {
            val forbidden = listOf(
                "com/fasterxml/",
                "com/hypixel/",
                "com/velocitypowered/",
                "io/netty/",
                "org/bukkit/",
                "org/postgresql/",
                "org/springframework/",
                "redis/clients/",
            )
            ZipFile(archive.get().asFile).use { jar ->
                val embedded = jar.entries().asSequence().map { it.name }
                    .firstOrNull { entry -> forbidden.any(entry::startsWith) }
                check(embedded == null) { "Third-party class embedded in thin JAR: $embedded" }
            }
        }
    }

    tasks.named("check") {
        dependsOn(verifyThinJar)
    }

    extensions.configure<PublishingExtension> {
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])
                artifactId = project.name
            }
        }
        repositories {
            val token = providers.environmentVariable("GITHUB_TOKEN")
            if (token.isPresent) {
                maven {
                    name = "GitHubPackages"
                    url = uri("https://maven.pkg.github.com/TavallStudios/tavall-hytale-resource-game")
                    credentials {
                        username = providers.environmentVariable("GITHUB_ACTOR").orNull
                        password = token.get()
                    }
                }
            }
        }
    }
}

project(":game-api") {
    dependencies {
        "api"("org.tavall:tavall-di:$tavallToolsVersion")
        "api"("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")
        "api"("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
        "api"("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
        "api"("com.fasterxml.jackson.module:jackson-module-parameter-names:$jacksonVersion")
        "testImplementation"("org.junit.jupiter:junit-jupiter:$junitVersion")
    }
}

project(":minecraft-framework") {
    dependencies {
        "testImplementation"("org.junit.jupiter:junit-jupiter:$junitVersion")
    }
}

project(":minecraft-framework:minecraft-backend-api") {
    dependencies {
        "api"(project(":game-api"))
        "api"("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")
        "api"("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
        "api"("org.postgresql:postgresql:42.7.10")
        "testImplementation"("org.junit.jupiter:junit-jupiter:$junitVersion")
    }
}

project(":control-server") {
    apply(plugin = "application")
    configurations.create("hyui")
    configurations.create("standaloneRuntime")

    extensions.configure<JavaApplication> {
        mainClass = "org.tavall.control.cli.ControlConsoleApplication"
    }

    dependencies {
        "implementation"(project(":game-api"))
        "implementation"(project(":minecraft-framework"))
        "implementation"(project(":minecraft-framework:minecraft-backend-api"))
        "implementation"("org.tavall:tavall-logging:$tavallToolsVersion")
        "implementation"("org.tavall:tavall-di:$tavallToolsVersion")
        "implementation"("org.tavall:tavall-eventbus:$tavallToolsVersion")
        "implementation"("org.tavall:tavall-concurrency:$tavallToolsVersion")
        "implementation"("org.tavall:tavall-scheduler:$tavallToolsVersion")
        "implementation"("org.tavall:tavall-reflection:$tavallToolsVersion")
        "implementation"("org.tavall:abstract-cache-system:$cacheVersion")
        "implementation"("org.tavall:abstract-cache-semantic:$cacheVersion")
        "runtimeOnly"("org.tavall:abstract-cache-storage-memory:$cacheVersion")
        "runtimeOnly"("org.tavall:abstract-cache-storage-postgres:$cacheVersion")
        "runtimeOnly"("org.tavall:abstract-cache-storage-redis:$cacheVersion")
        "compileOnly"("com.hypixel.hytale:Server:$hytaleServerVersion")
        "testImplementation"("com.hypixel.hytale:Server:$hytaleServerVersion")
        "standaloneRuntime"("com.hypixel.hytale:Server:$hytaleServerVersion")
        "compileOnly"("curse.maven:hyui-1431415:7820303")
        "testImplementation"("curse.maven:hyui-1431415:7820303")
        "hyui"("curse.maven:hyui-1431415:7820303")
        "standaloneRuntime"("curse.maven:hyui-1431415:7820303")
        "implementation"("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
        "implementation"("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:$jacksonVersion")
        "implementation"("com.fasterxml.jackson.module:jackson-module-parameter-names:$jacksonVersion")
        "implementation"("org.postgresql:postgresql:42.7.10")
        "implementation"("redis.clients:jedis:5.2.0")
        "implementation"("org.springframework.boot:spring-boot-starter-web:$springBootVersion")
        "implementation"("org.springframework.boot:spring-boot-jackson2:$springBootVersion")
        "testImplementation"("org.junit.jupiter:junit-jupiter:$junitVersion")
        "testImplementation"("org.springframework.boot:spring-boot-starter-test:$springBootVersion")
    }

    tasks.withType<JavaCompile>().configureEach {
        options.compilerArgs.add("--enable-preview")
    }
    tasks.withType<Test>().configureEach {
        jvmArgs("--enable-preview")
    }
    tasks.named<JavaExec>("run") {
        jvmArgs("--enable-preview")
    }
    tasks.named<ProcessResources>("processResources") {
        inputs.property("pluginManifestVersion", pluginManifestVersion)
        filesMatching("manifest.json") {
            expand(
                mapOf(
                    "project" to mapOf(
                        "groupId" to project.group.toString(),
                        "name" to "TavallResourceGame",
                        "version" to pluginManifestVersion,
                    ),
                    "hytale" to mapOf(
                        "server" to mapOf("version" to hytaleServerVersion),
                    ),
                    "plugin" to mapOf(
                        "main" to mapOf("class" to "org.tavall.control.ResourceGamePlugin"),
                    ),
                ),
            )
        }
        from("schema") {
            into("schema")
        }
    }
    tasks.named<Jar>("jar") {
        archiveFileName = "control-server.jar"
        manifest {
            attributes["Main-Class"] = "org.tavall.control.cli.ControlConsoleApplication"
        }
    }
}

project(":minecraft-game-server") {
    dependencies {
        "implementation"(project(":game-api"))
        "implementation"(project(":minecraft-framework"))
        "implementation"("org.tavall:tavall-di:$tavallToolsVersion")
        "implementation"("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
        "compileOnly"("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT") {
            exclude(group = "org.apache.maven")
            exclude(group = "org.apache.maven.resolver")
        }
        "testImplementation"("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT") {
            exclude(group = "org.apache.maven")
            exclude(group = "org.apache.maven.resolver")
        }
        "testImplementation"("org.junit.jupiter:junit-jupiter:$junitVersion")
    }
    tasks.named<ProcessResources>("processResources") {
        inputs.property("pluginManifestVersion", pluginManifestVersion)
        filesMatching("plugin.yml") {
            filter { line ->
                line.replace("version: 0.1.0-SNAPSHOT", "version: $pluginManifestVersion")
            }
        }
    }
    tasks.named<Jar>("jar") {
        archiveFileName = "minecraft-game-server.jar"
    }
}

project(":minecraft-proxy") {
    dependencies {
        "implementation"(project(":game-api"))
        "implementation"("org.tavall:tavall-logging:$tavallToolsVersion")
        "implementation"("org.tavall:tavall-di:$tavallToolsVersion")
        "implementation"("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
        "implementation"("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
        "implementation"("com.fasterxml.jackson.module:jackson-module-parameter-names:$jacksonVersion")
        "compileOnly"("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
        "testImplementation"("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
        "testImplementation"("org.junit.jupiter:junit-jupiter:$junitVersion")
    }
    tasks.named<ProcessResources>("processResources") {
        inputs.property("pluginManifestVersion", pluginManifestVersion)
        filesMatching("velocity-plugin.json") {
            filter { line ->
                line.replace("\"version\": \"0.1.0-SNAPSHOT\"", "\"version\": \"$pluginManifestVersion\"")
            }
        }
    }
    tasks.named<Jar>("jar") {
        archiveFileName = "minecraft-proxy.jar"
    }
}

val stageDistribution = tasks.register("stageDistribution") {
    val distributionDir = layout.projectDirectory.dir("distribution")
    val controlServer = project(":control-server")
    val gameServer = project(":minecraft-game-server")
    val proxy = project(":minecraft-proxy")

    dependsOn(
        controlServer.tasks.named("jar"),
        gameServer.tasks.named("jar"),
        proxy.tasks.named("jar"),
    )

    doLast {
        delete(distributionDir)
        copy {
            from(controlServer.tasks.named<Jar>("jar").flatMap { it.archiveFile })
            into(distributionDir.dir("control-server"))
            rename { "application.jar" }
        }
        copy {
            from(controlServer.configurations.named("runtimeClasspath"))
            from(controlServer.configurations.named("standaloneRuntime"))
            into(distributionDir.dir("control-server/libs"))
        }
        copy {
            from(gameServer.tasks.named<Jar>("jar").flatMap { it.archiveFile })
            into(distributionDir.dir("minecraft-game-server/plugins"))
        }
        copy {
            from(gameServer.configurations.named("runtimeClasspath"))
            into(distributionDir.dir("minecraft-game-server/libs"))
        }
        copy {
            from(proxy.tasks.named<Jar>("jar").flatMap { it.archiveFile })
            into(distributionDir.dir("minecraft-proxy/plugins"))
        }
        copy {
            from(proxy.configurations.named("runtimeClasspath"))
            into(distributionDir.dir("minecraft-proxy/libs"))
        }
    }
}

tasks.register<Copy>("resolveHyuiDependency") {
    from(project(":control-server").configurations.named("hyui"))
    into(layout.buildDirectory.dir("hyui"))
}

tasks.named("assemble") {
    dependsOn(stageDistribution)
}
