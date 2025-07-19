val ktormVersion: String by project

group = "apoy2k"
version = "1.0.0"

application {
    mainClass = "apoy2k.robby.ApplicationKt"
}

plugins {
    kotlin("jvm") version "2.1.21"
    kotlin("plugin.serialization") version "2.1.21"
    id("io.ktor.plugin") version "3.2.2"
    id("com.gradleup.shadow") version "8.3.8"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.ktorm:ktorm-core:$ktormVersion}")
    implementation("org.ktorm:ktorm-support-sqlite:$ktormVersion")
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("io.ktor:ktor-server-call-logging")
    implementation("io.ktor:ktor-server-host-common")
    implementation("io.ktor:ktor-server-resources")
    implementation("io.ktor:ktor-server-sessions")
    implementation("io.ktor:ktor-server-status-pages")
    implementation("io.ktor:ktor-server-html-builder")
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-server-websockets")
    implementation("io.ktor:ktor-server-netty")
    implementation("org.jetbrains.kotlinx:kotlinx-html:0.12.0")
    implementation("ch.qos.logback:logback-classic:1.5.13")
    implementation("org.apache.httpcomponents:httpclient:4.5.13")
    implementation("org.apache.commons:commons-lang3:3.18.0")
    implementation("org.xerial:sqlite-jdbc:3.41.2.2")

    testImplementation("io.ktor:ktor-server-test-host")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
