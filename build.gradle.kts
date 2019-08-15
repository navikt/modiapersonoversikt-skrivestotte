import com.moowork.gradle.node.npm.NpmTask

val ktorVersion = "1.2.0"
val prometheusVersion = "0.4.0"
val logbackVersion = "1.2.3"
val logstashVersion = "5.1"
val amazonS3Version = "1.11.555"
val konfigVersion = "1.6.10.0"

val mainClass = "no.nav.modiapersonoversikt.ApplicationKt"

plugins {
    application
    kotlin("jvm") version "1.3.21"
    id("com.moowork.node") version "1.2.0"
}

buildscript {
    repositories {
        maven("https://repo.adeo.no/repository/maven-releases")
        maven("https://repo.adeo.no/repository/maven-central")
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
    implementation("io.prometheus:simpleclient_hotspot:$prometheusVersion")
    implementation("io.prometheus:simpleclient_common:$prometheusVersion")
    implementation("io.prometheus:simpleclient_dropwizard:$prometheusVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashVersion")
    implementation("com.amazonaws:aws-java-sdk-s3:$amazonS3Version")
    implementation("com.natpryce:konfig:$konfigVersion")

    testImplementation("io.mockk:mockk:1.9")
}

repositories {
    maven("https://repo.adeo.no/repository/maven-releases")
    maven("https://repo.adeo.no/repository/maven-central")
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
