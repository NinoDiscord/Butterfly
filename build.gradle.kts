import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.jfrog.bintray.gradle.BintrayExtension
import com.jfrog.bintray.gradle.tasks.BintrayUploadTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URL
import java.util.*

plugins {
    java
    kotlin("jvm") version "1.3.72"
    id("org.jetbrains.dokka") version "1.4.0-rc"
    id("com.jfrog.bintray") version "1.8.5"
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "6.0.0"
}

val artifact = "Butterfly"
group = "dev.augu.nino"
version = "0.3.0-ALPHA"


repositories {
    mavenCentral()
    jcenter()
    maven("https://dl.bintray.com/kotlin/kotlin-eap")
}

dependencies {
    // Kotlin
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.7")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.3.7")

    // JDA
    api("net.dv8tion:JDA:4.1.1_165") {
        exclude(module = "opus-java")
    }
    api("club.minnced:jda-reactor:1.1.0")

    // Testing tools
    testImplementation("junit:junit:4.12")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:4.1.0.RC2")
    testImplementation("io.kotest:kotest-assertions-core-jvm:4.1.0.RC2")
    testImplementation("io.kotest:kotest-property-jvm:4.1.0.RC2")
    testImplementation("io.mockk:mockk:1.10.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.3.7")

    // Logging
    api("org.slf4j:slf4j-api:1.6.1")
    testImplementation("org.slf4j:slf4j-simple:1.6.1")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
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
        kotlinOptions.jvmTarget = "1.8"
    }

    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }

    dokkaHtml {

        dokkaSourceSets {
            val commonMain by creating {
                displayName = "Butterfly"
                includes = file("docs").listFiles()!!.map { it.canonicalPath }
                samples = file("src/examples/kotlin/dev/augu/nino/butterfly/examples").listFiles()!!
                    .map { it.canonicalPath }
                sourceRoot {
                    path = kotlin.sourceSets.getByName("main").kotlin.srcDirs.first().toString()
                }
                externalDocumentationLink {
                    url = URL("https://ci.dv8tion.net/job/JDA/javadoc/index.html")
                    packageListUrl = URL("https://ci.dv8tion.net/job/JDA/javadoc/element-list")
                }
                externalDocumentationLink {
                    url = URL("https://projectreactor.io/docs/core/release/api/")
                    packageListUrl = URL("https://projectreactor.io/docs/core/release/api/package-list")
                }

            }
        }
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString()
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
