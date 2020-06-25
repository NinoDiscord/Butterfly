plugins {
    application
    kotlin("jvm") version "1.3.72"
    id("org.jetbrains.dokka") version "0.10.1"
}

group = "dev.augu.nino"
version = "1.0-SNAPSHOT"

application {
    mainClassName = ""
}

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    // Kotlin
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-core", "1.3.7")
    implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-reactor", "1.3.7")

    // JDA
    implementation("net.dv8tion", "JDA", "4.1.1_165") {
        exclude(module = "opus-java")
    }
    implementation("club.minnced", "jda-reactor", "1.1.0")
    files("..")

    // Logging
    api("org.slf4j", "slf4j-api", "1.6.1")
    implementation("org.slf4j", "slf4j-simple", "1.6.1")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}