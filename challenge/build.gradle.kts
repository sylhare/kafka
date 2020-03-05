import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version "1.3.50"
  id("com.commercehub.gradle.plugin.avro") version "0.9.1"
  `maven-publish`
  application
}

java.sourceCompatibility = JavaVersion.VERSION_1_8
application.mainClassName = "four.VehiclePositionProducerKt"

repositories {
  jcenter()
  mavenCentral()
  maven("http://packages.confluent.io/maven/")
  maven("https://dl.bintray.com/gradle/gradle-plugins")
}

dependencies {
  implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.0")
  implementation("org.apache.kafka:kafka-clients:2.3.0")
  implementation("org.apache.kafka:kafka-streams:2.3.0")
  implementation("org.apache.avro:avro:1.8.2")
  implementation("org.apache.avro:avro-tools:1.8.2")

  implementation("io.confluent:monitoring-interceptors:5.3.0")
  implementation("io.confluent:kafka-avro-serializer:5.3.0")
  implementation("io.confluent:kafka-serde-tools-package:5.3.0")

  implementation("org.slf4j:slf4j-log4j12:1.7.25")

  implementation("org.jetbrains.kotlin:kotlin-reflect:1.3.50")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.50")

  testImplementation(kotlin("test"))
  testImplementation("org.junit.jupiter:junit-jupiter-api:5.2.0")
  testImplementation("org.junit.jupiter:junit-jupiter-engine:5.2.0")
  testImplementation("org.apache.curator:curator-test:2.8.0")
  testImplementation("org.apache.kafka:kafka_2.10:0.8.2.1")

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
}


tasks.create<Exec>("avro") {
  dependsOn("generateAvroJava")
  commandLine = listOf("echo", "avro")
}