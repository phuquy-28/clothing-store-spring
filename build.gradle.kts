import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
    java
    id("org.springframework.boot") version "3.2.4"
    id("io.spring.dependency-management") version "1.1.4"
    id("io.freefair.lombok") version "8.6"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2023.0.1")
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.springframework.retry:spring-retry")
    implementation("org.springframework:spring-aspects")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")
    implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")
    implementation("org.json:json:20210307")
    implementation(platform("com.google.cloud:libraries-bom:26.31.0"))
    implementation("com.google.cloud:google-cloud-storage")
    implementation("com.turkraft.springfilter:jpa:3.1.7")
    implementation("me.paulschwarz:spring-dotenv:2.5.4")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    implementation("com.sendgrid:sendgrid-java:4.10.1")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("com.mysql:mysql-connector-j")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("com.h2database:h2")

    // MapStruct
    implementation("org.mapstruct:mapstruct:1.6.0")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.6.0")

    // Apache POI for Excel manipulation
    implementation("org.apache.poi:poi:5.2.5")
    implementation("org.apache.poi:poi-ooxml:5.2.5")

    // Apache Commons IO
    implementation("commons-io:commons-io:2.15.1")

    // Firebase Admin SDK
    implementation("com.google.firebase:firebase-admin:9.2.0")

    // OpenAI
    implementation("com.openai:openai-java:2.7.0")

    // Pinecone client - compatible with protobuf 4.x
    implementation("io.pinecone:pinecone-client:5.0.0") {
        // Exclude transitive protobuf to avoid conflicts
        exclude(group = "com.google.protobuf", module = "protobuf-java")
    }
    
    // Force protobuf 4.29.3 to match Pinecone gencode version
    implementation("com.google.protobuf:protobuf-java:4.29.3")
}

// Force resolution strategy for protobuf 4.29.3
configurations.all {
    resolutionStrategy.force("com.google.protobuf:protobuf-java:4.29.3")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.example.clothingstore.ClothingstoreApplication"
    }
}

tasks.withType<BootRun> {
    val profile = project.findProperty("profile")?.toString() ?: "dev"
    systemProperties["spring.profiles.active"] = profile
    systemProperties["dotenv.filename"] = ".env"
}

tasks.withType<Test> {
    useJUnitPlatform()
    val profile = project.findProperty("profile")?.toString() ?: "test"
    systemProperties["spring.profiles.active"] = profile
    systemProperties["dotenv.filename"] = ".env"
}
