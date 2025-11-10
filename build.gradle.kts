import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.5.7"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.spring") version "2.2.0"
    application
}

val arrowKtVersion = "2.2.0"
val flywayVersion = "11.15.0"
// Kan ikke oppdatere til versjon 9.x enda, da denne knekker PersonnummerValueMasker > net.logstash.logback.mask.ValueMasker
val logbackEncoderVersion = "8.1"
val mockkVersion = "1.14.6"
val navTokenSupportVersion = "5.0.30"
val postgresqlVersion = "42.7.8"
val prometheusVersion = "1.15.5"
val springCloudStubRunnerVersion = "4.3.0"
val springdocOpenapiVersion = "1.8.0"

group = "no.nav.arbeidsgiver"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

application {
    mainClass.set("no.nav.arbeidsgiver.iatjenester.metrikker.AppKt")
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    val javaToolchains = project.extensions.getByType<JavaToolchainService>()
    javaLauncher.set(
        javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(21))
        },
    )
}

tasks.getByName<Jar>("jar") {
    enabled = false
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")

    implementation("org.springdoc:springdoc-openapi-ui:$springdocOpenapiVersion")
    implementation("org.springdoc:springdoc-openapi-kotlin:$springdocOpenapiVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:$logbackEncoderVersion")
    implementation("io.arrow-kt:arrow-core:$arrowKtVersion")
    implementation("org.flywaydb:flyway-core:$flywayVersion")
    implementation("org.flywaydb:flyway-database-postgresql:$flywayVersion")
    implementation("org.postgresql:postgresql:$postgresqlVersion")
    implementation("no.nav.security:token-validation-spring:$navTokenSupportVersion")
    implementation("io.micrometer:micrometer-registry-prometheus:$prometheusVersion")
    implementation("org.apache.commons:commons-lang3:3.19.0")
    implementation("net.minidev:json-smart:2.6.0")

    compileOnly("jakarta.servlet:jakarta.servlet-api:6.1.0")

    // Test dependencies
    testImplementation("com.h2database:h2:2.4.240")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("no.nav.security:token-validation-spring-test:$navTokenSupportVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("org.springframework.cloud:spring-cloud-starter-contract-stub-runner:$springCloudStubRunnerVersion")

    developmentOnly("org.springframework.boot:spring-boot-devtools")

    constraints {
        implementation("net.minidev:json-smart") {
            version {
                require("2.6.0")
            }
            because(
                "versjoner < 2.5.2 har diverse sårbarheter",
            )
        }
        implementation("org.apache.commons:commons-lang3") {
            version {
                require("3.19.0")
            }
            because(
                "versjon 3.17.0 har en sårbarhet (CVE-2025-48924)",
            )
        }
        testImplementation("net.java.dev.jna:jna-platform") {
            version {
                require("5.1.0") // fra 4.1.0 (brukt i test via org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
            }
            because(
                "versjoner < 5.0.0 har en sårbarhet (WS-2014-0065)",
            )
        }
    }

}
