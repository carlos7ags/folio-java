plugins {
    application
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(22)
    }
}

dependencies {
    implementation(project(":lib"))
}

val outputDir = layout.buildDirectory.dir("output")

application {
    mainClass = providers.gradleProperty("mainClass").orElse("dev.foliopdf.examples.Hello")
    applicationDefaultJvmArgs = listOf("--enable-native-access=ALL-UNNAMED")
}

tasks.named<JavaExec>("run") {
    doFirst { outputDir.get().asFile.mkdirs() }
    workingDir = outputDir.get().asFile
}
