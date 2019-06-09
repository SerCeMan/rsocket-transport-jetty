plugins {
    java
}

group = "me.serce"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation("org.eclipse.jetty.websocket:websocket-server:9.4.17.v20190418")
    implementation("io.rsocket:rsocket-core:0.12.2-RC3")

    testCompile("junit", "junit", "4.12")
}

