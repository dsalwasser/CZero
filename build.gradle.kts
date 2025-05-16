plugins {
  id("java")
  application

  id("com.diffplug.spotless") version "7.0.3"
}

group = "edu.kit.kastel.logic"
version = "1.0"

application {
  mainModule = "edu.kit.kastel.vads.compiler"
  mainClass = "edu.kit.kastel.vads.compiler.CZeroCLI"
}

repositories {
    mavenCentral()
}

dependencies {
  implementation("info.picocli:picocli:4.7.7")
  annotationProcessor("info.picocli:picocli-codegen:4.7.7")

  implementation("org.jspecify:jspecify:1.0.0")

  testImplementation(platform("org.junit:junit-bom:5.10.0"))
  testImplementation("org.junit.jupiter:junit-jupiter")
}

java {
  toolchain.languageVersion = JavaLanguageVersion.of(24)
}

tasks.test {
  useJUnitPlatform()
}

spotless {
  java {
    googleJavaFormat()
    formatAnnotations()
  }

  format("misc") {
    target("build.sh", "run.sh", "*.kts", ".gitignore")

    leadingTabsToSpaces(2)
    trimTrailingWhitespace()
    endWithNewline()
  }
}
