plugins {
    id("java")
    id ("org.flywaydb.flyway") version ("9.22.3")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation ("org.flywaydb:flyway-core:9.22.3")
    implementation("com.google.code.gson:gson:2.12.1")
    implementation("com.mysql:mysql-connector-j:9.2.0")
}

flyway {
    url = ("jdbc:mysql://localhost:3306/test")
    user = ("root")
    password = ("root")
    baselineOnMigrate = true
}

tasks.test {
    useJUnitPlatform()
}