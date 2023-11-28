import com.github.davidmc24.gradle.plugin.avro.GenerateAvroJavaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version "1.3.50"
  java
  //id("com.commercehub.gradle.plugin.avro") version "0.9.1"
  id( "com.github.davidmc24.gradle.plugin.avro-base") version "1.2.0"
  `maven-publish`
  application
}

val kotlinVersion = "1.3.50"
val kafkaVersion = "3.2.3"
val avroVersion = "1.11.3"
val confluentVersion = "7.2.1"

java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
  mavenCentral()
  maven("https://packages.confluent.io/maven/")
  gradlePluginPortal()
}

dependencies {
  implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.1")
  implementation("org.apache.kafka:kafka_2.13:${kafkaVersion}")
  implementation("org.apache.kafka:kafka-clients:${kafkaVersion}")
  implementation("org.apache.kafka:kafka-streams:${kafkaVersion}")
  implementation("org.apache.avro:avro:${avroVersion}")
  implementation("org.apache.avro:avro-tools:${avroVersion}"){
    exclude("ch.qos.logback", "logback-classic")
  }

  implementation("io.confluent:monitoring-interceptors:${confluentVersion}")
  implementation("io.confluent:kafka-avro-serializer:${confluentVersion}")
  implementation("io.confluent:kafka-json-serializer:${confluentVersion}")
  implementation("io.confluent:kafka-streams-avro-serde:${confluentVersion}")
  implementation("io.confluent:kafka-schema-registry-client:${confluentVersion}")

  implementation("org.slf4j:slf4j-log4j12:1.7.25")

  implementation("org.jetbrains.kotlin:kotlin-reflect:1.3.50")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.50")

  testImplementation(kotlin("test"))
  testImplementation("org.junit.jupiter:junit-jupiter-api:5.2.0")
  testImplementation("org.junit.jupiter:junit-jupiter-engine:5.2.0")
  testImplementation("org.apache.kafka:kafka-streams-test-utils:${kafkaVersion}")
}

configurations.all {
  resolutionStrategy.eachDependency {
    if (this.requested.name == "log4j") {
      //this.useTarget("org.slf4j:log4j-over-slf4j:1.7.5")
    }
  }
}

sourceSets {
  main {
    java.srcDir("./build/generated-main-avro-java")
  }
}

configurations.all {
  exclude(group = "org.slf4j", module = "slf4j-log4j12")
}


tasks.test {
  useJUnitPlatform()
  testLogging {
    events("passed", "skipped", "failed")
  }
}

tasks.withType<KotlinCompile> {
  kotlinOptions {
    freeCompilerArgs = listOf("-Xjsr305=strict")
    jvmTarget = "1.8"
  }

  dependsOn("generateAvroJava") // So avro is generated
}

avro {
  isCreateSetters.set(false)
  isCreateOptionalGetters.set(false)
  isGettersReturnOptional.set(false)
  isOptionalGettersForNullableFieldsOnly.set(false)
  fieldVisibility.set("PUBLIC_DEPRECATED")
  outputCharacterEncoding.set("UTF-8")
  stringType.set("String")
  templateDirectory.set(null as String?)
  isEnableDecimalLogicalType.set(true)
}

tasks.register<GenerateAvroJavaTask>("generateAvroJava") {
  source("src/main/avro")
  setOutputDir(file("build/generated-main-avro-java"))
}

tasks.create<Exec>("avro") { // Simpler task to generate avro schemas
  dependsOn("generateAvroJava")
  commandLine = listOf("echo", "avro")
}
