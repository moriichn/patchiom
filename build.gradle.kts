plugins {
    id("java")
    id("application")
    id("com.gradleup.shadow") version "9.6.1"
}

group = "dev.mori"
version = "1.0-SNAPSHOT"

application {
    mainClass = "dev.mori.patchiom.Main"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.ow2.asm:asm:9.9")
    implementation("com.google.code.gson:gson:2.13.2")
}