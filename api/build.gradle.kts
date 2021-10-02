plugins {
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("io.micronaut.application") version "2.0.6"
    id("org.openapi.generator") version "5.1.1"

}

version = "0.1"
group = "ish"

repositories {
    mavenCentral()
    maven {
        url = uri("https://m2.dv8tion.net/releases")
        name = "m2-dv8tion"
    }
    maven {
        url = uri("https://oss.jfrog.org/oss-snapshot-local")
    }
    maven {
        url = uri("https://jitpack.io")
    }
}


tasks.withType<org.openapitools.generator.gradle.plugin.tasks.GenerateTask> {
    dependsOn(
        tasks.named("classes")
    )
}

openApiGenerate {

    generatorName.set("typescript-fetch")
    inputSpec.set("$projectDir/out/production/classes/META-INF/swagger/chuu-api-0.0.yml")
    outputDir.set("$projectDir/../webapp/services/api")
    validateSpec.set(false)
    configOptions.set(
        mapOf(
            "supportsES6" to "true",
            "prefixParameterInterfaces" to "true",
            "typescriptThreePlus" to "true",
        )
    )
}


micronaut {
    runtime("netty")
    testRuntime("junit5")
    processing {
        incremental(true)
        annotations("ish.*")
    }
}
tasks.test {
    // Use the built-in JUnit support of Gradle.
    useJUnitPlatform()
}

dependencies {
    annotationProcessor("io.micronaut:micronaut-http-validation")
    annotationProcessor("io.micronaut.security:micronaut-security-annotations")
    annotationProcessor("io.micronaut.openapi:micronaut-openapi")
    testAnnotationProcessor("io.micronaut:micronaut-inject-java")


    implementation("io.micronaut:micronaut-http-client")
    implementation("io.micronaut:micronaut-runtime")
    implementation("io.micronaut.cache:micronaut-cache-caffeine")
    implementation("javax.annotation:javax.annotation-api")

    implementation("io.micronaut.security:micronaut-security-jwt")
    implementation("io.micronaut.security:micronaut-security-oauth2")
    implementation("net.dv8tion:JDA:4.3.0_324")
    implementation("io.swagger.core.v3:swagger-annotations")

    implementation("io.micronaut.rxjava3:micronaut-rxjava3")
    implementation("io.micronaut.rxjava3:micronaut-rxjava3-http-client")
    implementation("javax.annotation:javax.annotation-api")
    runtimeOnly("ch.qos.logback:logback-classic")
    implementation("io.micronaut:micronaut-validation")
    implementation(project(":model"))


    implementation("org.json:json:20200518")
    implementation("io.micronaut:micronaut-management")
// https://mvnrepository.com/artifact/com.github.scribejava/scribejava-apis
    implementation("com.github.scribejava:scribejava-apis:7.1.1")

    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.mockito:mockito-inline:3.12.4")
    testImplementation("io.micronaut.test:micronaut-test-junit5:3.0.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.2")


}


application {
    mainClass.set("ish.Application")
}

java {
    sourceCompatibility = JavaVersion.toVersion("17")
    targetCompatibility = JavaVersion.toVersion("17")
}

