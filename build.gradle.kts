import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.50"
    id("com.github.johnrengelman.shadow") version "5.1.0"
    application
}

application {
    mainClassName = "lt.dzebleckis.MainKt"
}

repositories {
    jcenter()
}

tasks.test {
    useJUnitPlatform()
}

sourceSets {
    create("it") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

val itImplementation by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}

configurations["itRuntimeOnly"].extendsFrom(configurations.runtimeOnly.get())

val jettyVersion = "9.4.20.v20190813"
val jerseyVersion = "2.25"
val junitVersion = "5.5.2"

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    compile("org.eclipse.jetty:jetty-server:$jettyVersion")
    compile("org.eclipse.jetty:jetty-servlet:$jettyVersion")

    compile("org.glassfish.jersey.core:jersey-server:$jerseyVersion")
    compile("org.glassfish.jersey.containers:jersey-container-servlet:$jerseyVersion")
    compile("org.glassfish.jersey.media:jersey-media-json-jackson:$jerseyVersion")

    implementation("javax.xml.bind:jaxb-api:2.2.11")
    implementation("com.sun.xml.bind:jaxb-core:2.2.11")
    implementation("com.sun.xml.bind:jaxb-impl:2.2.11")
    implementation("javax.activation:activation:1.1.1")

    compile("javax.money:money-api:1.0.3")
    compile("org.javamoney:moneta:1.3")
    compile("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.+")

    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testCompile("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    testCompile("io.mockk:mockk:1.9.+")

    itImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    itImplementation("com.github.kittinunf.fuel:fuel:2.2.1")
    itImplementation("org.glassfish.jersey.test-framework:jersey-test-framework-core:$jerseyVersion")
    itImplementation("org.glassfish.jersey.test-framework.providers:jersey-test-framework-provider-grizzly2:$jerseyVersion")
}

val integrationTest = task<Test>("integrationTest") {
    description = "Runs integration tests."
    group = "verification"

    testClassesDirs = sourceSets["it"].output.classesDirs
    classpath = sourceSets["it"].runtimeClasspath
    shouldRunAfter("test")
    useJUnitPlatform()
}

tasks.check { dependsOn(integrationTest) }
