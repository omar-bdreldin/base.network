plugins {
    java
    kotlin("jvm") version "1.4.21"
}

group = "org.omarbadreldin"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    testImplementation("junit", "junit", "4.12")
    implementation("com.squareup.okhttp3:okhttp:4.9.0")
    implementation("io.reactivex.rxjava3:rxjava:3.0.9")
    implementation("com.google.code.gson:gson:2.8.6")
    implementation(kotlin("reflect", "1.4.21"))
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}