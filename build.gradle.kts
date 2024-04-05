plugins {
    id("java")
}

group = "com.hedera.node.demo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
//    maven { url 'https://jitpack.io' }
}

dependencies {
//    api("net.devh:grpc-spring-boot-starter:3.0.0.RELEASE")

//    implementation("com.fasterxml.jackson.core:jackson-databind")
//    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
//    implementation(platform("io.cucumber:cucumber-bom"))
//    implementation("io.cucumber:cucumber-java")
//    implementation("org.junit.platform:junit-platform-launcher")
    implementation("org.springframework.boot:spring-boot-starter:3.2.4")
    implementation("io.github.cdimascio:dotenv-java:3.0.0")
//    implementation("org.springframework.boot:spring-boot-autoconfigure")
//    implementation("org.springframework.boot:spring-boot-configuration-processor")
//    implementation("org.springframework.boot:spring-boot-starter-logging")
//    implementation("org.springframework.boot:spring-boot-starter-validation")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
//    testImplementation("io.grpc:grpc-inprocess")
    testImplementation("com.esaulpaugh:headlong:10.0.2")
    testImplementation("com.google.guava:guava:33.1.0-jre")
    testImplementation("com.hedera.hashgraph:sdk:2.30.0")
    testImplementation("io.cucumber:cucumber-junit-platform-engine")
    testImplementation("io.cucumber:cucumber-spring")
//    testImplementation("io.grpc:grpc-okhttp")
//    testImplementation(
//        group = "io.netty",
//        name = "netty-resolver-dns-native-macos",
//        classifier = "osx-aarch_64"
//    )
//    testImplementation("jakarta.inject:jakarta.inject-api")
//    testImplementation("net.java.dev.jna:jna")
    testImplementation("org.apache.commons:commons-lang3")
//    testImplementation("org.awaitility:awaitility")
//    testImplementation("org.junit.platform:junit-platform-suite")
//    testImplementation("org.springframework.boot:spring-boot-starter-aop")
//    testImplementation("org.springframework.boot:spring-boot-starter-validation")
//    testImplementation("org.springframework.boot:spring-boot-starter-webflux")
//    testImplementation("org.springframework.retry:spring-retry")
    testImplementation("org.apache.tuweni:tuweni-bytes")
    testImplementation("commons-codec:commons-codec")

    implementation project(":sdk")
}

tasks.test {
    useJUnitPlatform()
}