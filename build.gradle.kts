import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.2.3"
	id("io.spring.dependency-management") version "1.1.4"
	id("org.graalvm.buildtools.native") version "0.10.1"
	kotlin("jvm") version "1.9.22"
	kotlin("plugin.spring") version "1.9.22"
	kotlin("plugin.serialization") version "1.6.21" // added by me
}

group = "io.github.jvmusin"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
	mavenCentral()
}

val coroutinesVersion = "1.8.0"
val retrofitVersion = "2.9.0"
val okhttp3Version = "5.0.0-alpha.12"
val ktorVersion = "1.6.8"
val serializationVersion = "1.3.2"
val jsoupVersion = "1.17.2"
val kotestVersion = "5.8.0"

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-websocket")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.springframework.session:spring-session-core") // TODO: Do we need this?
	developmentOnly("org.springframework.boot:spring-boot-devtools") // TODO: Does it work?
	testImplementation("org.springframework.boot:spring-boot-starter-test")

	implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
	implementation("com.squareup.retrofit2:converter-scalars:$retrofitVersion")

	implementation("com.squareup.okhttp3:logging-interceptor:$okhttp3Version")
	implementation("com.squareup.okhttp3:okhttp:$okhttp3Version")
	implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:0.8.0")

	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${coroutinesVersion}")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:${coroutinesVersion}")

	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
	testImplementation("io.projectreactor:reactor-test")

	implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$serializationVersion")
	implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:$serializationVersion")

	implementation("org.jsoup:jsoup:$jsoupVersion")

	testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
	testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
	testImplementation("io.kotest:kotest-property:$kotestVersion")
	testImplementation("io.kotest.extensions:kotest-extensions-spring:1.1.3")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs += "-Xjsr305=strict"
		jvmTarget = "21"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
	testLogging {
		exceptionFormat = TestExceptionFormat.FULL
	}
}
