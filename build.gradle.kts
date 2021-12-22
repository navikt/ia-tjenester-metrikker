import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.5.3"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.5.21"
    id("com.github.ben-manes.versions") version "0.39.0"
    kotlin("jvm") version "1.5.30-M1"
    kotlin("plugin.spring") version "1.5.30-M1"
    application
}

val navSecurityVersion = "1.3.9"
val altinnRettigheterProxyKlientVersion = "2.0.6-rc"
val shedlockVersion = "4.25.0"


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

tasks.getByName<Jar>("jar") {
    enabled = false
}

repositories {
    mavenCentral()
    jcenter()
    maven {
        url = uri("https://repo1.maven.org/maven2/")
    }
}

ext["nimbus-jose-jwt.version"] = "9.15.2" // https://nav-it.slack.com/archives/C01381BAT62/p1611056940004800
ext["okhttp3.version"] = "4.9.0" // For at token support testen kjører

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springdoc:springdoc-openapi-ui:1.5.10")
    implementation("org.springdoc:springdoc-openapi-kotlin:1.5.10")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")

    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("net.logstash.logback:logstash-logback-encoder:6.6")
    implementation("org.apache.logging.log4j:log4j-api:2.17.0")
    implementation("org.apache.logging.log4j:log4j-core:2.17.0")
    implementation("org.apache.logging.log4j:log4j-to-slf4j:2.16.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.0-rc1")
    implementation("com.google.code.gson:gson:2.8.7")

    implementation("io.arrow-kt:arrow-core:1.0.1")
    implementation("com.zaxxer:HikariCP:5.0.0")
    implementation("org.flywaydb:flyway-core:8.2.3")
    implementation("org.postgresql:postgresql:42.2.23")
    implementation("com.h2database:h2:2.0.202")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.retry:spring-retry")
    implementation("org.springframework.kafka:spring-kafka:2.7.5")
    implementation("no.nav.security:token-validation-spring:${navSecurityVersion}")
    implementation("no.nav.arbeidsgiver:altinn-rettigheter-proxy-klient:${altinnRettigheterProxyKlientVersion}")
    runtimeOnlyDependenciesMetadata("com.github.kittinunf.result:result:2.2.1")

    implementation("net.javacrumbs.shedlock:shedlock-spring:$shedlockVersion")
    implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:$shedlockVersion")


    // Test dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("no.nav.security:token-validation-spring-test:${navSecurityVersion}")
    testImplementation("com.github.tomakehurst:wiremock:2.27.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("io.mockk:mockk:1.12.0")
    testImplementation("org.assertj:assertj-core:3.20.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.8.0-M1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.0-M1")
    testImplementation("org.springframework.cloud:spring-cloud-starter-contract-stub-runner:3.1.0")
}
