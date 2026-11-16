import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.gradle.node.yarn.task.YarnInstallTask
import com.github.gradle.node.yarn.task.YarnTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val javaVersion = "21"

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.node.gradle)
    application
    idea
}

repositories {
    mavenCentral()

    val githubToken = System.getenv("GITHUB_TOKEN")
    if (githubToken.isNullOrEmpty()) {
        maven {
            name = "external-mirror-github-navikt"
            url = uri("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
        }
    } else {
        maven {
            name = "github-package-registry-navikt"
            url = uri("https://maven.pkg.github.com/navikt/maven-release")
            credentials {
                username = "token"
                password = githubToken
            }
        }
    }
}

dependencyLocking {
    lockAllConfigurations()
}

idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.forwarded.header)

    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.jackson)

    implementation(libs.ktor.server.metrics.micrometer)
    implementation(libs.micrometer.registry.prometheus)

    implementation(libs.ktor.client.cio)

    implementation(libs.logback.classic)
    implementation(platform(libs.jackson3.bom))
    implementation(platform(libs.jackson2.bom))
    implementation(libs.logstash.logback.encoder)

    implementation(libs.hikaricp)
    runtimeOnly(libs.postgresql)
    implementation(libs.modia.kotlin.utils)
    implementation(libs.modia.ktor.utils)
    implementation(libs.modia.crypto)
    implementation(libs.flyway.core)
    implementation(libs.flyway.database.postgresql)
    implementation(libs.kotliquery)

    implementation(libs.mockwebserver)
    implementation(libs.ktor.server.netty.jvm)
    implementation(libs.ktor.client.apache.jvm)

    testRuntimeOnly(libs.kotlin.reflect)
    testImplementation(libs.ktor.server.test.host.jvm)
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.kotlin.test)

    constraints {
        implementation(libs.guava)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

application {
    mainClass.set("no.nav.modiapersonoversikt.MainKt")
}

node {
    version.set("24.22.1")
    download.set(true)
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.register<YarnInstallTask>("yarnInstall") {
    workingDir.set(file("${project.projectDir}/frontend"))
    args.set(listOf("--frozen-lockfile"))
}

tasks.register<YarnTask>("yarnBuild") {
    workingDir.set(file("${project.projectDir}/frontend"))
    args.set(listOf("build"))
    if(System.getenv("BASE_PATH") != null) {
        args.addAll("--base", System.getenv("BASE_PATH"))
    }
}

tasks {
    "yarnBuild" {
        dependsOn("yarnInstall")
    }
    processResources {
        dependsOn("yarnBuild")
        from("frontend/dist") {
            into("webapp")
        }
    }
}
