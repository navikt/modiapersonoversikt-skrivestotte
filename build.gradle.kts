import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.gradle.node.yarn.task.YarnInstallTask
import com.github.gradle.node.yarn.task.YarnTask

val mainClass = "no.nav.modiapersonoversikt.MainKt"
val kotlinVersion = "1.9.24"
val ktorVersion = "3.0.0"
val javaVersion = "21"
val prometheusVersion = "1.9.0"
val logbackVersion = "1.2.11"
val logstashVersion = "8.0"
val modiaCommonVersion = "1.2024.10.25-12.01-5d2c60264f4e"

plugins {
    kotlin("jvm") version "2.0.21"
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
    implementation("com.github.navikt.modia-common-utils:kotlin-utils:$modiaCommonVersion")
    implementation("com.github.navikt.modia-common-utils:ktor-utils:$modiaCommonVersion")
    implementation("com.github.navikt.modia-common-utils:crypto:$modiaCommonVersion")
    implementation("org.flywaydb:flyway-core:8.5.12")
    implementation("com.github.seratch:kotliquery:1.8.0")

    implementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-apache-jvm:$ktorVersion")

    testRuntimeOnly("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    testImplementation("io.ktor:ktor-server-test-host-jvm:$ktorVersion")
    testImplementation("org.testcontainers:postgresql:1.17.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
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
    if(System.getenv("BASE_PATH") != null) {
        args.addAll("--base", System.getenv("BASE_PATH"))
    }

    doLast {
        copy {
            from("frontend/dist")
            into("build/resources/main/webapp")
        }
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
