import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.4.2"
    id("io.spring.dependency-management") version "1.1.6"
    kotlin("jvm") version "2.1.10"
    kotlin("plugin.spring") version "2.1.10"
    application
}

val navTokenSupportVersion = "5.0.16"
val springdocOpenapiVersion = "1.8.0"
val log4jVersion = "2.20.0"
val flywayVersion = "11.3.2"

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
    implementation("net.logstash.logback:logstash-logback-encoder:8.0")
    implementation("io.arrow-kt:arrow-core:2.0.1")
    implementation("org.flywaydb:flyway-core:$flywayVersion")
    implementation("org.flywaydb:flyway-database-postgresql:$flywayVersion")
    implementation("org.postgresql:postgresql:42.7.5")
    implementation("no.nav.security:token-validation-spring:$navTokenSupportVersion")
    implementation("com.github.navikt:altinn-rettigheter-proxy-klient:altinn-rettigheter-proxy-klient-5.0.0")
    implementation("io.micrometer:micrometer-registry-prometheus:1.14.4")

    compileOnly("jakarta.servlet:jakarta.servlet-api:6.1.0")

    // Test dependencies
    testImplementation("com.h2database:h2:2.3.232")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("no.nav.security:token-validation-spring-test:$navTokenSupportVersion")
    testImplementation("io.mockk:mockk:1.13.16")
    testImplementation("org.springframework.cloud:spring-cloud-starter-contract-stub-runner:4.2.0")

    developmentOnly("org.springframework.boot:spring-boot-devtools")
}
