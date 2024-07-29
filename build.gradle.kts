import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.3.2"
    id("io.spring.dependency-management") version "1.1.5"
//    id("org.graalvm.buildtools.native") version "0.10.2"
    kotlin("jvm") version "2.0.0"
    kotlin("plugin.spring") version "2.0.0"
}

group = "io.github.jvmusin"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

val jsoupVersion = "1.17.2"
val kotestVersion = "5.9.1"
val kotestExtensionsSpringVersion = "1.3.0"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    testImplementation("io.projectreactor:reactor-test")

    implementation("org.jsoup:jsoup:$jsoupVersion")

    implementation("org.apache.commons:commons-compress:1.26.2") // For reading zip archives from Polygon
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml") // For parsing problem.xml

    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.kotest.extensions:kotest-extensions-spring:$kotestExtensionsSpringVersion")
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        exceptionFormat = TestExceptionFormat.FULL
    }
}

if (project.hasProperty("copyFrontend")) {
    val copyFrontend = tasks.register<Copy>("copyFrontend") {
        group = "build"
        description = "Copies the frontend build to the resources."
        from("frontend/dist")
        into("build/resources/main/static")
    }
    tasks.named<ProcessResources>("processResources") {
        dependsOn(copyFrontend)
        exclude("static/index.html")
    }
}
