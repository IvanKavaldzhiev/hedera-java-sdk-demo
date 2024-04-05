import org.codehaus.groovy.tools.shell.util.Logger.io

plugins {
    id("java")
}

group = "com.hedera.node.demo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    annotationProcessor("org.projectlombok:lombok:1.18.22")
    implementation("org.springframework.boot:spring-boot-starter:3.2.4")
    implementation("io.github.cdimascio:dotenv-java:3.0.0")
    implementation("io.grpc:grpc-netty-shaded:1.57.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.1")
    implementation("org.projectlombok:lombok:1.18.22")
    implementation("com.hedera.hashgraph:sdk:2.30.0")
}

tasks.test {
    useJUnitPlatform()
}