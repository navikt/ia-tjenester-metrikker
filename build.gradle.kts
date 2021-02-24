plugins {
    id("org.jetbrains.kotlin.jvm") version "1.4.21"
    id("com.github.johnrengelman.shadow") version "6.1.0"
    application
}

application {
    mainClassName = "no.nav.arbeidsgiver.iatjenester.metrikker.AppKt"
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
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.4.30")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
    implementation("io.javalin:javalin:3.12.0")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("net.logstash.logback:logstash-logback-encoder:6.3")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.0")
    implementation("com.github.ben-manes.caffeine:caffeine:2.8.8")
    implementation("io.github.cdimascio:dotenv-kotlin:6.2.2")
    implementation("com.zaxxer:HikariCP:3.3.0")
    implementation("org.flywaydb:flyway-core:5.0.2")
    implementation("org.postgresql:postgresql:42.2.18")
    implementation("com.h2database:h2:1.4.200")

    testImplementation ("org.postgresql:postgresql:42.2.18")
    testImplementation("io.mockk:mockk:1.10.5")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
    testImplementation("com.h2database:h2:1.4.197")
    testImplementation("org.assertj:assertj-core:3.18.1")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.0.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
}

