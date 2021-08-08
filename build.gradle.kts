import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.21"
}

group = "com.almeydajuan"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation("com.eclipsesource.minimal-json:minimal-json:0.9.4")

    implementation(platform("org.http4k:http4k-bom:4.10.1.0"))
    implementation("org.http4k:http4k-core")
    implementation("org.http4k:http4k-server-apache")
    implementation("org.http4k:http4k-format-moshi")
    implementation("org.http4k:http4k-format-jackson")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

tasks.withType<Test> {
    useJUnitPlatform()
}