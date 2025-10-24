plugins {
    kotlin("jvm") version "2.0.21"
    application
}

group = "com.example.demo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // http4k core
    implementation("org.http4k:http4k-core:5.10.2.0")
    implementation("org.http4k:http4k-server-netty:5.10.2.0")
    
    // jsoup for HTML parsing
    implementation("org.jsoup:jsoup:1.16.2")
    
    // H2 in-memory database
    implementation("com.h2database:h2:2.2.224")
    
    // Testing
    testImplementation(kotlin("test"))
    testImplementation("org.http4k:http4k-testing-hamkrest:5.10.2.0")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("com.example.demo.MainKt")
}
