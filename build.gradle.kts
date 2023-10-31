plugins {
	java
	application
	id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "ru.mts2200"
version = "1.0.0"

tasks.withType<Jar>().configureEach {
	destinationDirectory.set(project.file("compiled"))
}

tasks.withType<JavaCompile>().configureEach {
	options.encoding = "UTF-8"
}

application {
	mainClass.set("ru.mts2200.minincode.launch.Starter")
}

java {
	sourceCompatibility = JavaVersion.VERSION_17
	targetCompatibility = JavaVersion.VERSION_17
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.telegram:telegrambots:6.8.0")
	implementation("com.zaxxer:HikariCP:4.0.3")
	implementation("mysql:mysql-connector-java:8.0.33")
	implementation("org.slf4j:slf4j-api:2.0.9")
	implementation("org.slf4j:slf4j-simple:2.0.9")
	implementation("com.google.guava:guava:32.1.3-jre")

	compileOnly("org.projectlombok:lombok:1.18.30")
	annotationProcessor("org.projectlombok:lombok:1.18.20")
}
