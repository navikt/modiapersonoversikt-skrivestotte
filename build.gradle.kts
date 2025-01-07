import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.gradle.node.yarn.task.YarnInstallTask
import com.github.gradle.node.yarn.task.YarnTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val mainClass = "no.nav.modiapersonoversikt.MainKt"
val kotlinVersion = "2.1.0"
val ktorVersion = "3.0.3"
val javaVersion = "21"
val prometheusVersion = "1.14.2"
val logbackVersion = "1.5.16"
val logstashVersion = "8.0"
val modiaCommonVersion = "1.2024.12.12-09.31-50e75d3f64f9"
val flywayVersion = "11.1.0"
val hikariVersion = "6.2.1"
val postgresVersion = "42.7.4"

plugins {
    kotlin("jvm") version "2.1.0"
    id("com.github.node-gradle.node") version "7.1.0"
    id("com.gradleup.shadow") version "8.3.5"
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

    implementation("com.zaxxer:HikariCP:$hikariVersion")
    runtimeOnly("org.postgresql:postgresql:$postgresVersion")
    implementation("com.github.navikt.modia-common-utils:kotlin-utils:$modiaCommonVersion")
    implementation("com.github.navikt.modia-common-utils:ktor-utils:$modiaCommonVersion")
    implementation("com.github.navikt.modia-common-utils:crypto:$modiaCommonVersion")
    compileOnly("org.flywaydb:flyway-core:$flywayVersion")
    runtimeOnly("org.flywaydb:flyway-database-postgresql:$flywayVersion")
    implementation("com.github.seratch:kotliquery:1.9.0")

    implementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-apache-jvm:$ktorVersion")

    testRuntimeOnly("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    testImplementation("io.ktor:ktor-server-test-host-jvm:$ktorVersion")
    testImplementation("org.testcontainers:postgresql:1.20.4")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
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
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
        freeCompilerArgs.set(listOf("-Xcontext-receivers"))
    }
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

tasks {
    "yarnBuild" {
        dependsOn("yarnInstall")
    }
    shadowJar {
        dependsOn("yarnBuild")

        archiveBaseName.set("app")
        archiveClassifier.set("")
        duplicatesStrategy = DuplicatesStrategy.INCLUDE

        manifest {
            attributes["Main-Class"] = mainClass
        }
        from(sourceSets.main.get().output)
        configurations = listOf(project.configurations.runtimeClasspath.get())
    }

    "build" {
        dependsOn("shadowJar")
    }
}
