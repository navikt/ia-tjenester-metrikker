import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.4.3"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("com.github.johnrengelman.shadow") version "6.1.0"
    kotlin("jvm") version "1.4.30"
    kotlin("plugin.spring") version "1.4.30"

    application
}

group = "no.nav.arbeidsgiver"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

application {
    // Det kommer en fix på denne: https://github.com/johnrengelman/shadow/issues/609
    mainClassName = "no.nav.arbeidsgiver.iatjenester.metrikker.AppKt"
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

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.4.30")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")

    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("net.logstash.logback:logstash-logback-encoder:6.3")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.0")
    implementation("com.github.ben-manes.caffeine:caffeine:2.8.8")
    implementation("io.github.cdimascio:dotenv-kotlin:6.2.2")
    implementation("com.zaxxer:HikariCP:3.3.0")
    implementation("org.flywaydb:flyway-core:5.0.2")
    implementation("org.postgresql:postgresql:42.2.18")
    implementation("com.h2database:h2:1.4.200")
    implementation("org.springframework.boot:spring-boot-starter-web")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor:2.4.3")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation ("org.postgresql:postgresql:42.2.18")
    testImplementation("io.mockk:mockk:1.10.5")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
    testImplementation("com.h2database:h2:1.4.197")
    testImplementation("org.assertj:assertj-core:3.18.1")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.0.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
}

