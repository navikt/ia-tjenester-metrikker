import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.1.2"
    id("io.spring.dependency-management") version "1.1.2"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.9.0"
    id("com.github.ben-manes.versions") version "0.47.0"
    kotlin("jvm") version "1.9.0"
    kotlin("plugin.spring") version "1.9.0"
    application
}

val navTokenSupportVersion = "3.1.0"
val shedlockVersion = "5.6.0"
val springdocOpenapiVersion = "1.7.0"
val log4jVersion = "2.20.0"
val jupiterVersion = "5.10.0"


group = "no.nav.arbeidsgiver"
version = "0.0.1-SNAPSHOT"

java.sourceCompatibility = JavaVersion.VERSION_17

application {
    mainClass.set("no.nav.arbeidsgiver.iatjenester.metrikker.AppKt")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    val javaToolchains = project.extensions.getByType<JavaToolchainService>()
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(17))
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

ext["nimbus-jose-jwt.version"] =
    "9.15.2" // https://nav-it.slack.com/archives/C01381BAT62/p1611056940004800
ext["okhttp3.version"] = "4.9.0" // For at token support testen kjører

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springdoc:springdoc-openapi-ui:$springdocOpenapiVersion")
    implementation("org.springdoc:springdoc-openapi-kotlin:$springdocOpenapiVersion")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("ch.qos.logback:logback-classic:1.4.8")
    implementation("net.logstash.logback:logstash-logback-encoder:7.4")
    implementation("org.apache.logging.log4j:log4j-api:${log4jVersion}")
    implementation("org.apache.logging.log4j:log4j-core:${log4jVersion}")
    implementation("org.apache.logging.log4j:log4j-to-slf4j:2.16.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")
    implementation("com.google.code.gson:gson:2.10.1")

    implementation("io.arrow-kt:arrow-core:1.2.0")
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("org.flywaydb:flyway-core:9.21.0")
    implementation("org.postgresql:postgresql:42.6.0")
    implementation("com.h2database:h2:2.2.220")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.retry:spring-retry")
    implementation("no.nav.security:token-validation-spring:${navTokenSupportVersion}")
    implementation("no.nav.arbeidsgiver:altinn-rettigheter-proxy-klient:3.1.0")
    implementation("com.github.kittinunf.result:result-jvm:5.4.0")

    implementation("net.javacrumbs.shedlock:shedlock-spring:$shedlockVersion")
    implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:$shedlockVersion")

    implementation("io.micrometer:micrometer-registry-prometheus:1.11.2")
    implementation("com.github.tomakehurst:wiremock-jre8:2.35.0")

    compileOnly("jakarta.servlet:jakarta.servlet-api:6.0.0")

    // Test dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("no.nav.security:token-validation-spring-test:${navTokenSupportVersion}")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("com.github.tomakehurst:wiremock-jre8-standalone:2.35.0")
    testImplementation("org.assertj:assertj-core:3.24.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:${jupiterVersion}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${jupiterVersion}")
    testImplementation("io.mockk:mockk:1.13.5")
    testImplementation("com.github.tomakehurst:wiremock-jre8:2.35.0") // TODO: Upgrade to v3-beta
    testImplementation("org.springframework.cloud:spring-cloud-starter-contract-stub-runner:4.0.3")
}
