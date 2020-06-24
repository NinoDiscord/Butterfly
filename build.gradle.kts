plugins {
    java
    kotlin("jvm") version "1.3.72"
}

group = "dev.augu.nino"
version = "1.0-SNAPSHOT"


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

    // Testing tools
    testImplementation("junit", "junit", "4.12")
    testImplementation("io.kotest", "kotest-runner-junit5-jvm", "4.1.0.RC2")
    testImplementation("io.kotest", "kotest-assertions-core-jvm", "4.1.0.RC2")
    testImplementation("io.kotest", "kotest-property-jvm", "4.1.0.RC2")
    testImplementation("io.mockk", "mockk", "1.10.0")
    testImplementation("org.jetbrains.kotlinx", "kotlinx-coroutines-test", "1.3.7")

    // Login
    api("org.slf4j", "slf4j-api", "1.6.1")
    testImplementation("org.slf4j", "slf4j-simple", "1.6.1")
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

tasks.withType<Test> {
    useJUnitPlatform()
}