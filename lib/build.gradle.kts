plugins {
    `java-library`
    jacoco
    alias(libs.plugins.spotless)
    alias(libs.plugins.jmh)
    alias(libs.plugins.maven.publish)
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)

    jmh(libs.jmh.core)
    jmhAnnotationProcessor(libs.jmh.annprocess)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
}

jmh {
    fork.set(1)
    warmupIterations.set(3)
    iterations.set(5)
    benchmarkMode.set(listOf("thrpt"))
}

spotless {
    java {
        googleJavaFormat()
    }
}

val group: String by project
val artifact: String by project
val version: String by project

mavenPublishing {
  publishToMavenCentral()
  signAllPublications()

  coordinates(group, artifact, version)

  pom {
    name.set("Sliding Window Counter")
    description.set("A Java library implementing a concurrent sliding window counter.")
    inceptionYear.set("2026")
    url.set("https://github.com/tnewman/sliding-window-counter-java")
    licenses {
      license {
        name.set("The Apache License, Version 2.0")
        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
        distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
      }
    }
    developers {
      developer {
        id.set("tnewman")
        name.set("Thomas Newman")
        url.set("https://github.com/tnewman")
      }
    }
    scm {
      url.set("https://github.com/tnewman/sliding-window-counter-java")
      connection.set("scm:git:https://github.com/tnewman/sliding-window-counter-java.git")
      developerConnection.set("scm:git:https://github.com/tnewman/sliding-window-counter-java.git")
    }
  }
}
