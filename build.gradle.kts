import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.4.4"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.4.32"
    id("com.github.ben-manes.versions") version "0.38.0"
    kotlin("jvm") version "1.4.32"
    kotlin("plugin.spring") version "1.4.32"
    application
}

val navSecurityVersion = "1.3.4"
val altinnRettigheterProxyKlientVersion = "2.0.1"

group = "no.nav.arbeidsgiver"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

application {
    mainClass.set("no.nav.arbeidsgiver.iatjenester.metrikker.AppKt")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

repositories {
    jcenter()
    maven {
        url = uri("https://packages.confluent.io/maven/")
    }
}

ext["nimbus-jose-jwt.version"] = "8.20" // https://nav-it.slack.com/archives/C01381BAT62/p1611056940004800
ext["okhttp3.version"] = "4.9.0" // For at token support testen kjører

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springdoc:springdoc-openapi-ui:1.5.6")
    implementation("org.springdoc:springdoc-openapi-kotlin:1.5.6")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")

    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("net.logstash.logback:logstash-logback-encoder:6.6")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.2")
    implementation("com.google.code.gson:gson:2.8.6")

    implementation("com.zaxxer:HikariCP:3.4.5")
    implementation("org.flywaydb:flyway-core:5.2.4")
    implementation("org.postgresql:postgresql:42.2.19")
    implementation("com.h2database:h2:1.4.200")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.retry:spring-retry")
    implementation("org.springframework.kafka:spring-kafka:2.6.7")
    implementation("no.nav.security:token-validation-spring:${navSecurityVersion}")
    implementation("no.nav.arbeidsgiver:altinn-rettigheter-proxy-klient:${altinnRettigheterProxyKlientVersion}:kotlin-client")
    implementation("kscience.plotlykt:plotlykt-server:0.3.0")

    // Test dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("no.nav.security:token-validation-spring-test:${navSecurityVersion}")
    testImplementation("com.github.tomakehurst:wiremock:2.27.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
    testImplementation("io.mockk:mockk:1.11.0")
    testImplementation("org.assertj:assertj-core:3.19.0")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.7.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.1")
}

