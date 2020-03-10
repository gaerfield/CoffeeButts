import com.palantir.gradle.gitversion.VersionDetails
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val kotlinVersion = "1.3.61"
    kotlin("jvm") version kotlinVersion
    kotlin("kapt") version kotlinVersion
    //id("org.jetbrains.kotlin.plugin.jpa") version "1.3.60"

    // spring-boot
    kotlin("plugin.spring") version kotlinVersion
    id("org.springframework.boot") version "2.2.2.RELEASE"
    id("io.spring.dependency-management") version "1.0.8.RELEASE"

    id("com.palantir.git-version") version "0.12.2"
    id("com.google.cloud.tools.jib") version "1.5.1"
}

fun versionDetails() = (extra["versionDetails"] as groovy.lang.Closure<*>)() as VersionDetails
group = "de.esag.coffeebutts"
version = versionDetails().version
java.sourceCompatibility = JavaVersion.VERSION_1_8

val developmentOnly : Configuration by configurations.creating
configurations {
    runtimeClasspath {
        extendsFrom(developmentOnly)
    }
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

tasks.withType<Test> {
    useJUnitPlatform()
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    freeCompilerArgs = listOf("-Xjsr305=strict -XXLanguage:+InlineClasses")
    jvmTarget = "1.8"
}

dependencies {
    val coroutinesVersion=
    //  ---  kotlin
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.github.microutils:kotlin-logging:1.7.6")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("io.mockk:mockk:1.9.3")

    // --- spring
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    kapt("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.springframework.plugin:spring-plugin-core:2.0.0.RELEASE")
    // implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux") {
        exclude(module = "hibernate-validator")
    }
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation("com.ninja-squad:springmockk:2.0.0")

    // --- reactive
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    // implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    testImplementation("io.projectreactor:reactor-test")

    // --- persistence
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
    // implementation("org.springframework.data:spring-data-r2dbc:1.0.0.RELEASE")
    // implementation("io.r2dbc:r2dbc-h2:0.8.2.RELEASE")
    // runtimeOnly("com.h2database:h2:1.4.200")
    testImplementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo")

    // --- documentation
    // TODO activate as soon, as [springdoc-openapi#159](https://github.com/springdoc/springdoc-openapi/issues/159) is fixed
     implementation("org.springdoc:springdoc-openapi-webflux-ui:1.2.32")
}

springBoot {
    buildInfo()
}

jib {
    to {
        image = "${System.getenv("AWS_REGISTRY_URL")}/payfree-server"
        tags = setOf("latest", "$version")
    }
}