plugins {
    java
}

group = "br.com.wellingtoncosta"
version = "0.0.1"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.javassist:javassist:3.24.0-GA")
}

tasks.withType<Jar> {
    configurations["compileClasspath"].forEach { file: File ->
        from(zipTree(file.absoluteFile))
    }

    manifest.apply {
        attributes["Premain-Class"] = "br.com.wellingtoncosta.javabytecode.playgroud.Agent"
    }
}