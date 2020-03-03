import groovy.lang.GroovyObject
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.50"
    `maven-publish`
    application
}

java.sourceCompatibility = JavaVersion.VERSION_1_8
application.mainClassName = "four.VehiclePositionConsumerKt"

repositories {
    mavenCentral()
    maven( "http://packages.confluent.io/maven/" )
}

dependencies {
    compile("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.0")
    compile ("org.apache.kafka:kafka-clients:2.3.0")
//    compile ("org.apache.kafka:kafka-streams:2.3.0")
//    compile ("org.apache.avro:avro:1.8.2")
//    compile ("org.apache.avro:avro-tools:1.8.2")

    compile ("io.confluent:monitoring-interceptors:5.3.0")
//    compile ("io.confluent:kafka-avro-serializer:5.3.0")
//    compile ("io.confluent:kafka-serde-tools-package:5.3.0")

    compile ("org.slf4j:slf4j-log4j12:1.7.25")
    compile("org.jetbrains.kotlin:kotlin-reflect:1.3.50")
    compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.50")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}