val ktorVersion: String by project
val kotlinVersion: String by project
val logbackVersion: String by project

plugins {
    application
    kotlin("jvm") version "1.8.0"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    kotlin("plugin.serialization") version "1.6.21"
}

group = "com.speculator"
version = "0.0.1"
application {
    mainClass.set("com.speculator.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap") }
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-cio-jvm:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("io.ktor:ktor-server-html-builder:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging:$ktorVersion")
    implementation("io.ktor:ktor-client-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-cio-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-forwarded-header:$ktorVersion")
    testImplementation("io.ktor:ktor-server-tests-jvm:$ktorVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
    testImplementation("io.ktor:ktor-server-tests-jvm:2.2.4")
}

//tasks {
//    compileKotlin {
//        kotlinOptions.freeCompilerArgs += "-Xuse-k2"
//        kotlinOptions.freeCompilerArgs += "-XXLanguage:+ExplicitBackingFields"
//    }
//}
