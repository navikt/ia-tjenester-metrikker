import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.2.2"
    id("io.spring.dependency-management") version "1.1.2"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.9.22"
    id("com.github.ben-manes.versions") version "0.47.0"
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.spring") version "1.9.22"
    application
}

val navTokenSupportVersion = "4.1.3"
val springdocOpenapiVersion = "1.7.0"
val log4jVersion = "2.20.0"

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
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "21"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    val javaToolchains = project.extensions.getByType<JavaToolchainService>()
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(21))
    })
}

tasks.getByName<Jar>("jar") {
    enabled = false
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://repo1.maven.org/maven2/")
    }
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
    implementation("net.logstash.logback:logstash-logback-encoder:7.4")
    implementation("io.arrow-kt:arrow-core:1.2.1")
    implementation("org.flywaydb:flyway-core:10.7.2")
    implementation("org.flywaydb:flyway-database-postgresql:10.7.2")
    implementation("org.postgresql:postgresql:42.7.1")
    implementation("org.springframework.retry:spring-retry")
    implementation("no.nav.security:token-validation-spring:${navTokenSupportVersion}")
    implementation("no.nav.arbeidsgiver:altinn-rettigheter-proxy-klient:3.1.0")
    implementation("io.micrometer:micrometer-registry-prometheus:1.12.2")

    compileOnly("jakarta.servlet:jakarta.servlet-api:6.0.0")

    // Test dependencies
    testImplementation("com.h2database:h2:2.2.224")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.2.2")
    testImplementation("no.nav.security:token-validation-spring-test:${navTokenSupportVersion}")
    testImplementation("io.mockk:mockk:1.13.9")
    testImplementation("org.springframework.cloud:spring-cloud-starter-contract-stub-runner:4.1.1")

    developmentOnly("org.springframework.boot:spring-boot-devtools")
}
