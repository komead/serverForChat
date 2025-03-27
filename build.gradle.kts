plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("com.mysql:mysql-connector-j:9.2.0")
    implementation ("org.flywaydb:flyway-core:9.22.3")
    implementation("org.flywaydb:flyway-mysql:11.5.0")
    implementation("com.google.code.gson:gson:2.12.1")
}

tasks.test {
    useJUnitPlatform()
}