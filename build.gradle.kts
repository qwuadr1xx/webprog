plugins {
    kotlin("jvm") version "2.2.21"
}

group = "ru.qwuadrixx"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(files("libs/fastcgi_lib.jar"))
    implementation("org.json:json:20260814")
}

kotlin {
    jvmToolchain(17)
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes("Main-Class" to "ru.qwuadrixx.webprog.MainKt")
    }
    from({
        configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }
    })
    exclude("META-INF/versions/**", "module-info.class")
    archiveFileName.set("web.jar")
}