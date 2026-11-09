import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Keep vulnerable build-tool transitives aligned without changing plugin runtime libraries.
buildscript {
    dependencies {
        classpath(platform("com.fasterxml.jackson:jackson-bom:2.22.4"))
        classpath("org.jsoup:jsoup:1.24.1")
    }
}

plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.20.0"
    id("org.jetbrains.kotlin.jvm") version "2.4.20"
}

group = "com.architect"
version = project.findProperty("pluginVersion")?.toString() ?: "1.0-SNAPSHOT"

sourceSets {
    create("integrationTest") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

configurations.named("integrationTestImplementation") {
    extendsFrom(configurations.testImplementation.get())
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdea("2026.1.3")
        testFramework(TestFrameworkType.Platform)
        testFramework(TestFrameworkType.Starter, configurationName = "integrationTestImplementation")
    }

    // Security updates for the isolated IntelliJ Starter/Driver test harness.
    add("integrationTestImplementation", platform("com.fasterxml.jackson:jackson-bom:2.22.4"))
    add("integrationTestImplementation", platform("io.netty:netty-bom:4.2.20.Final"))
    add("integrationTestImplementation", platform("io.opentelemetry:opentelemetry-bom:1.67.0"))
    add("integrationTestImplementation", "com.google.guava:guava:33.8.0-jre")
    add("integrationTestImplementation", "org.bouncycastle:bcpkix-jdk18on:1.86")
    add("integrationTestImplementation", "org.bouncycastle:bcprov-jdk18on:1.86")
    add("integrationTestImplementation", "org.jsoup:jsoup:1.24.1")

    testImplementation("junit:junit:4.13.2")
    add("integrationTestImplementation", "org.junit.jupiter:junit-jupiter:6.1.3")
    add("integrationTestImplementation", "org.jetbrains.kotlin:kotlin-stdlib:2.4.20")
    add("integrationTestImplementation", "org.kodein.di:kodein-di-jvm:7.33.0")
    add("integrationTestImplementation", "org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.11.0")
    add("integrationTestRuntimeOnly", "org.junit.platform:junit-platform-launcher:6.1.3")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks {
    withType<JavaCompile>().configureEach {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }

    withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
            apiVersion.set(KotlinVersion.KOTLIN_2_3)
            languageVersion.set(KotlinVersion.KOTLIN_2_3)
        }
    }
}

intellijPlatformTesting.testIdeUi.register("integrationTest") {
    task {
        val integrationTestSourceSet = sourceSets.getByName("integrationTest")
        testClassesDirs = integrationTestSourceSet.output.classesDirs
        classpath = integrationTestSourceSet.runtimeClasspath
        useJUnitPlatform()
    }
}

tasks.named("integrationTest") {
    doFirst {
        delete(layout.projectDirectory.dir("out/ide-tests/tests"))
    }
}

tasks.register("verifyIdePluginLogs") {
    group = "verification"
    description = "Fails when IntelliJ sandbox logs report errors blamed on this plugin."
    dependsOn("integrationTest")

    doLast {
        val ideTestLogs = layout.projectDirectory.dir("out/ide-tests/tests").asFile
        if (!ideTestLogs.exists()) {
            logger.lifecycle("No IntelliJ sandbox logs found under ${ideTestLogs.path}; skipping plugin log verification.")
            return@doLast
        }

        val pluginErrorPatterns = listOf(
            "Plugin to blame: CertView X.509",
            "Plugin to blame: X.509 Certificate Viewer",
            "[Plugin: com.architect.certviewer]",
            "SEVERE - #com.architect.certviewer",
        )
        val failures = ideTestLogs
            .walkTopDown()
            .filter { it.isFile && it.name == "idea.log" }
            .flatMap { logFile ->
                logFile.readLines()
                    .filter { line -> pluginErrorPatterns.any(line::contains) }
                    .map { line -> "${logFile.relativeTo(projectDir)}: $line" }
            }
            .toList()

        if (failures.isNotEmpty()) {
            throw GradleException(
                buildString {
                    appendLine("IntelliJ sandbox logs contain errors blamed on this plugin:")
                    failures.take(20).forEach { appendLine(it) }
                    if (failures.size > 20) {
                        appendLine("...and ${failures.size - 20} more plugin log error(s).")
                    }
                },
            )
        }
    }
}

tasks.register("validateFunctional") {
    group = "verification"
    description = "Runs parser tests, IntelliJ UI integration tests, IDE plugin log checks, and the plugin build."
    dependsOn("test", "verifyIdePluginLogs", "build")
}

val verifyPublishedCompatibility = tasks.register("verifyPublishedCompatibility") {
    group = "verification"
    description = "Checks that the generated Marketplace descriptor follows the compatibility-range policy."
    dependsOn("patchPluginXml")

    doLast {
        val descriptor = layout.buildDirectory.file("tmp/patchPluginXml/plugin.xml").get().asFile
        val document = javax.xml.parsers.DocumentBuilderFactory
            .newInstance()
            .newDocumentBuilder()
            .parse(descriptor)
        val ideaVersion = document.getElementsByTagName("idea-version").item(0)
        val attributes = ideaVersion.attributes

        check(attributes.getNamedItem("since-build")?.nodeValue == "261") {
            "Expected since-build=261 in ${descriptor.path}"
        }
        check(attributes.getNamedItem("until-build") == null) {
            "Expected no until-build cap in ${descriptor.path}; compatibility is guarded by Plugin Verifier."
        }
    }
}

tasks.named("check") {
    dependsOn(verifyPublishedCompatibility)
}

intellijPlatform {
    buildSearchableOptions = false

    pluginConfiguration {
        ideaVersion {
            sinceBuild = "261"
            untilBuild = provider { null }
        }
    }

    pluginVerification {
        ides {
            create(IntelliJPlatformType.IntellijIdeaUltimate, "2026.1.4")
            create(IntelliJPlatformType.IntellijIdeaUltimate, "2026.2.2")
        }
    }

    signing {
        certificateChain = System.getenv("CERTIFICATE_CHAIN")
        privateKey = System.getenv("PRIVATE_KEY")
        password = System.getenv("PRIVATE_KEY_PASSWORD")
    }

    publishing {
        token = System.getenv("PUBLISH_TOKEN")
    }
}

// The maintained LZ4 fork preserves net.jpountz APIs and fixes unpatched legacy advisories.
configurations.named("integrationTestRuntimeClasspath") {
    resolutionStrategy.dependencySubstitution {
        substitute(module("org.lz4:lz4-java")).using(module("at.yawk.lz4:lz4-java:1.12.1"))
    }
}
configurations.named("integrationTestCompileClasspath") {
    resolutionStrategy.dependencySubstitution {
        substitute(module("org.lz4:lz4-java")).using(module("at.yawk.lz4:lz4-java:1.12.1"))
    }
}
