import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.jfrog.bintray.gradle.BintrayExtension
import com.jfrog.bintray.gradle.tasks.BintrayUploadTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URL
import java.util.*

plugins {
    java
    kotlin("jvm") version "1.4.10"
    id("org.jetbrains.dokka") version "1.4.10"
    id("com.jfrog.bintray") version "1.8.5"
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

val artifact = "Butterfly"
group = "dev.augu.nino"
version = "0.3.3"


repositories {
    mavenCentral()
    jcenter()
    maven("https://dl.bintray.com/kotlin/kotlin-eap")
}

dependencies {
    // Kotlin
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.3.9")

    // JDA
    api("net.dv8tion:JDA:4.2.0_209") {
        exclude(module = "opus-java")
    }
    api("club.minnced:jda-reactor:1.2.0")

    // Testing tools
    testImplementation("junit:junit:4.13.1")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:4.2.6")
    testImplementation("io.kotest:kotest-assertions-core-jvm:4.3.0")
    testImplementation("io.kotest:kotest-property-jvm:4.2.6")
    testImplementation("io.mockk:mockk:1.10.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.3.9")

    // Logging
    api("org.slf4j:slf4j-api:1.7.30")
    testImplementation("org.slf4j:slf4j-simple:1.7.30")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

sourceSets {
    val examples by creating {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

val examplesImplementation by configurations.getting {
    extendsFrom(configurations.implementation.get())
}

configurations["examplesRuntimeOnly"].extendsFrom(configurations.runtimeOnly.get())

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "11"
    }

    compileTestKotlin {
        kotlinOptions.jvmTarget = "11"
    }

    dokkaHtml {

        dokkaSourceSets {
            val commonMain by creating {
                displayName.set("Butterfly")
                includes.from(file("docs").listFiles())
                samples.from(file("src/examples/kotlin/dev/augu/nino/butterfly/examples").listFiles())
                externalDocumentationLink {
                    url.set(URL("https://ci.dv8tion.net/job/JDA/javadoc/index.html"))
                    packageListUrl.set(URL("https://ci.dv8tion.net/job/JDA/javadoc/element-list"))
                }
                externalDocumentationLink {
                    url.set(URL("https://projectreactor.io/docs/core/release/api/"))
                    packageListUrl.set(URL("https://projectreactor.io/docs/core/release/api/package-list"))
                }

            }
        }
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
}

tasks.withType<Test> {
    useJUnitPlatform()
}

val shadowJar: ShadowJar by tasks

shadowJar.apply {
    archiveBaseName.set(artifact)
    archiveClassifier.set(null as String?)
}

val sourcesJar = task<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from("src/main/kotlin")
}

val javadocJar = task<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
    from(tasks.dokkaHtml)
}

publishing {
    publications {
        register("BintrayPublication", MavenPublication::class.java) {
            from(components["java"])
            artifactId = artifact
            groupId = project.group as String
            version = project.version as String
            artifact(sourcesJar)
            artifact(javadocJar)
        }
    }
}

val bintrayUpload: BintrayUploadTask by tasks

bintrayUpload.apply {
    onlyIf {
        System.getenv("BINTRAY_USER") != null
    }
    onlyIf {
        System.getenv("BINTRAY_KEY") != null
    }
}

bintray {
    user = System.getenv("BINTRAY_USER")
    key = System.getenv("BINTRAY_KEY")
    setPublications("BintrayPublication")
    pkg(delegateClosureOf<BintrayExtension.PackageConfig> {
        repo = "oss-maven"
        name = "Butterfly"
        userOrg = "dondishorg"
        vcsUrl = "https://github.com/NinoDiscord/Butterfly.git"
        publish = true
        version(delegateClosureOf<BintrayExtension.VersionConfig> {
            name = project.version as String
            released = Date().toString()
        })
    })
}
