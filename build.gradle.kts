import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    id("org.jetbrains.kotlinx.kover") version "0.4.2"
}

group = "com.almeydajuan"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation(platform("org.http4k:http4k-bom:4.10.1.0"))
    implementation("org.http4k:http4k-core")
    implementation("org.http4k:http4k-server-apache")
    implementation("org.http4k:http4k-format-jackson")

    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
    testImplementation("org.assertj:assertj-core:3.21.0")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "11"
            apiVersion = "1.6"
            languageVersion = "1.6"
        }
    }

    withType<Test> {
        useJUnitPlatform()
    }

    withType<kotlinx.kover.tasks.KoverVerificationTask> {
        rule {
            name = "Minimal line coverage rate in percents"
            bound {
                minValue = 95
            }
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
    withSourcesJar()
}