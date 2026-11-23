plugins {
    kotlin("jvm") version "2.2.21"
    war
}

group = "ru.qwuadrixx"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    compileOnly("jakarta.servlet:jakarta.servlet-api:6.0.0")
}

kotlin {
    jvmToolchain(17)
}

tasks.test {
    useJUnitPlatform()
}

tasks.war {
    archiveFileName.set("webprog.war")
}