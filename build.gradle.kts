import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.0.4"
    id("io.spring.dependency-management") version "1.0.15.RELEASE"

    kotlin("jvm") version "1.6.21"
    kotlin("plugin.spring") version "1.6.21"
    kotlin("plugin.jpa") version "1.6.21"
    id("org.jlleitschuh.gradle.ktlint") version "10.2.1"
}
noArg {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.Embeddable")
}

group = "com.isel"
version = "1.0.0"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
    maven("https://plugins.gradle.org/m2/")
}

dependencies {

    implementation("org.postgresql:postgresql:42.5.4")

    implementation("org.springframework.amqp:spring-rabbit:3.0.3")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("io.hypersistence:hypersistence-utils-hibernate-60:3.3.0")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.2")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.6.2")
    implementation("junit:junit:4.13.1")

    implementation("org.springframework.boot:spring-boot-starter-security")

    developmentOnly("org.springframework.boot:spring-boot-devtools")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.amqp:spring-rabbit-test")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    this.environment(
        "JDBC_DATABASE_URL" to "jdbc:postgresql://localhost:5432/postgres?user=postgres&password=postgres",
        "RABBITMQ_HOST" to "localhost",
        "INSTANCE_CTL_QUEUE" to "instance_ctl_test",
        "INSTANCE_ACK_QUEUE_DEVICE_STATE" to "instance_ack_device_state_test",
        "INSTANCE_ACK_QUEUE_DEVICE_DELETE" to "instance_scheduler_notification_test",
    )
}
