plugins {
    `java-library`
    `maven-publish`
    signing
    id("tech.yanand.maven-central-publish") version "1.3.0"
}

group = "dev.foliopdf"
version = "0.1.0"

base {
    archivesName = "folio-java"
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(22)
    }
    withSourcesJar()
    withJavadocJar()
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}

tasks.withType<JavaExec> {
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}

tasks.withType<Javadoc> {
    (options as StandardJavadocDocletOptions).apply {
        addStringOption("Xdoclint:none", "-quiet")
    }
    exclude("dev/foliopdf/internal/**")
    // Run javadoc on classpath instead of module path to avoid
    // module visibility issues with internal package imports.
    classpath = sourceSets["main"].compileClasspath
    setSource(sourceSets["main"].java.filter { it.name != "module-info.java" })
    isFailOnError = false
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "folio-java"
            from(components["java"])
            pom {
                name = "Folio Java SDK"
                description = "Java PDF generation, signing, and processing library. Apache 2.0 licensed."
                url = "https://foliopdf.dev"
                licenses {
                    license {
                        name = "Apache-2.0"
                        url = "https://www.apache.org/licenses/LICENSE-2.0"
                    }
                }
                developers {
                    developer {
                        id = "carlos7ags"
                        name = "Carlos Munoz"
                    }
                }
                scm {
                    connection = "scm:git:git://github.com/carlos7ags/folio-java.git"
                    developerConnection = "scm:git:ssh://github.com:carlos7ags/folio-java.git"
                    url = "https://github.com/carlos7ags/folio-java"
                }
            }
        }
    }
}

mavenCentral {
    repoDir = layout.buildDirectory.dir("staging-deploy")
    authToken = findProperty("mavenCentralToken") as String? ?: ""
}

signing {
    val signingKey = findProperty("signingInMemoryKey") as String?
    val signingPassword = findProperty("signingInMemoryKeyPassword") as String?
    if (signingKey != null) {
        useInMemoryPgpKeys(signingKey, signingPassword)
    }
    sign(publishing.publications["maven"])
}

tasks.withType<Sign>().configureEach {
    isRequired = !version.toString().endsWith("SNAPSHOT")
}
