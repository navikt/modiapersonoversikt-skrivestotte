import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.gradle.node.yarn.task.YarnInstallTask
import com.github.gradle.node.yarn.task.YarnTask

val mainClass = "no.nav.modiapersonoversikt.MainKt"
val kotlinVersion = "1.7.10"
val ktorVersion = "2.0.3"
val javaVersion = "17"
val prometheusVersion = "1.9.0"
val logbackVersion = "1.2.11"
val logstashVersion = "7.2"
val modiaCommonVersion = "1.2022.08.01-11.26-97308e41bb8d"

plugins {
    kotlin("jvm") version "1.7.10"
    id("com.github.node-gradle.node") version "7.0.2"
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

idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("io.ktor:ktor-server-auth:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jwt:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
    implementation("io.ktor:ktor-server-cors:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging:$ktorVersion")
    implementation("io.ktor:ktor-server-forwarded-header:$ktorVersion")

    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-jackson:$ktorVersion")

    implementation("io.ktor:ktor-server-metrics-micrometer:$ktorVersion")
    implementation("io.micrometer:micrometer-registry-prometheus:$prometheusVersion")

    implementation("io.ktor:ktor-client-cio:$ktorVersion")

    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashVersion")

    implementation("no.nav:vault-jdbc:1.3.9")
    implementation("no.nav.personoversikt:kotlin-utils:$modiaCommonVersion")
    implementation("no.nav.personoversikt:ktor-utils:$modiaCommonVersion")
    implementation("no.nav.personoversikt:crypto:$modiaCommonVersion")
    implementation("org.flywaydb:flyway-core:8.5.12")
    implementation("com.github.seratch:kotliquery:1.8.0")

    implementation("com.squareup.okhttp3:mockwebserver:4.4.0")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-apache-jvm:$ktorVersion")

    testRuntimeOnly("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    testImplementation("io.ktor:ktor-server-test-host-jvm:$ktorVersion")
    testImplementation("org.testcontainers:postgresql:1.17.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

node {
    version.set("20.13.1")
    download.set(true)
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = javaVersion
    kotlinOptions.freeCompilerArgs = listOf("-Xcontext-receivers")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

task<YarnInstallTask>("yarnInstall") {
    workingDir.set(file("${project.projectDir}/frontend"))
    args.set(listOf("--frozen-lockfile"))
}

val syncFrontend = copy {
    from("${project.projectDir}/frontend/dist")
    into("${project.projectDir}/src/main/resources/webapp")
}
task<YarnTask>("yarnBuild") {
    workingDir.set(file("${project.projectDir}/frontend"))
    args.set(listOf("build"))

    doLast {
        syncFrontend
    }
}

task("syncFrontend") {
    syncFrontend
}

task<Jar>("fatJar") {
    archiveBaseName.set("app")
    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    manifest {
        attributes["Main-Class"] = mainClass
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    with(tasks.jar.get() as CopySpec)
}

tasks {
    "yarnBuild" {
        dependsOn("yarnInstall")
    }
    "fatJar" {
        dependsOn("yarnBuild")
    }
    "build" {
        dependsOn("fatJar")
    }
}
