import com.moowork.gradle.node.npm.NpmTask

val mainClass = "no.nav.modiapersonoversikt.ApplicationKt"
val kotlinVersion = "1.3.70"
val ktorVersion = "1.3.1"
val prometheusVersion = "0.4.0"

plugins {
    application
    kotlin("jvm") version "1.3.70"
    id("com.moowork.node") version "1.2.0"
}

buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath("org.junit.platform:junit-platform-gradle-plugin:1.2.0")
    }
}

application {
    mainClassName = mainClass
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-auth-jwt:$ktorVersion")
    implementation("io.ktor:ktor-jackson:$ktorVersion")
    implementation("io.ktor:ktor-metrics:$ktorVersion")
    implementation("io.ktor:ktor-client-apache:$ktorVersion")
    implementation("io.ktor:ktor-client-gson:$ktorVersion")
    implementation("com.squareup.okhttp3:mockwebserver:4.4.0")
    implementation("io.prometheus:simpleclient_hotspot:$prometheusVersion")
    implementation("io.prometheus:simpleclient_common:$prometheusVersion")
    implementation("io.prometheus:simpleclient_dropwizard:$prometheusVersion")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("net.logstash.logback:logstash-logback-encoder:5.1")
    implementation("com.natpryce:konfig:1.6.10.0")
    implementation("no.nav:vault-jdbc:1.3.1")
    implementation("org.flywaydb:flyway-core:6.3.1")
    implementation("com.github.seratch:kotliquery:1.3.0")

    testImplementation("io.mockk:mockk:1.9")
    testImplementation("org.junit.jupiter:junit-jupiter:5.6.2")
    testRuntimeOnly("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    testImplementation("com.h2database:h2:1.4.200")
}

repositories {
    maven("https://plugins.gradle.org/m2/")
    maven("https://dl.bintray.com/kotlin/ktor/")
    jcenter()
    mavenCentral()
}

node {
    version = "10.15.3"
    download = true
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.withType<Wrapper> {
    gradleVersion = "5.3.1"
}

task<NpmTask>("npmCI") {
    setWorkingDir(file("${project.projectDir}/frontend"))
    setArgs(listOf("ci"))
}

val syncFrontend = copy {
    from("frontend/build")
    into("src/main/resources/webapp")
}
task<NpmTask>("npmBuild") {
    setWorkingDir(file("${project.projectDir}/frontend"))
    setArgs(listOf("run", "build"))

    doLast {
        copy {
            from("frontend/build")
            into("build/resources/main/webapp")
        }
    }
}

task("syncFrontend") {
    copy {
        from("frontend/build")
        into("src/main/resources/webapp")
    }
}

task<Jar>("fatJar") {
    baseName = "app"

    manifest {
        attributes["Main-Class"] = mainClass
        configurations.runtimeClasspath.get().joinToString(separator = " ") {
            it.name
        }
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    with(tasks.jar.get() as CopySpec)
}

tasks {
    "npmBuild" {
        dependsOn("npmCI")
    }
    "fatJar" {
        dependsOn("npmBuild")
    }
    "jar" {
        dependsOn("fatJar")
    }
}
