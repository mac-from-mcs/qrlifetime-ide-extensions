import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.1.20"
    id("org.jetbrains.intellij.platform") version "2.20.1"
}

group = "com.qrlifetime"
version = "1.0.1"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    // Nayuki QR Code generator, MIT licence, no transitive dependency.
    implementation("io.nayuki:qrcodegen:1.8.0")

    testImplementation("junit:junit:4.13.2")

    intellijPlatform {
        intellijIdeaCommunity("2024.3.6")
        testFramework(TestFrameworkType.Platform)
    }
}

kotlin {
    jvmToolchain(21)
}

intellijPlatform {
    pluginConfiguration {
        id = "com.qrlifetime.qrcode"
        name = "QRLifetime QR Code Generator"
        version = project.version.toString()
        ideaVersion {
            sinceBuild = "243"
            untilBuild = provider { null }
        }
        vendor {
            name = "QRLifetime"
            url = "https://qrlifetime.com"
            email = "hello@qrlifetime.com"
        }
    }
    buildSearchableOptions = false
    pluginVerification {
        ides {
            // Oldest supported build and the build JetBrains Marketplace flagged in 1.0.0.
            create(IntelliJPlatformType.IntellijIdeaCommunity, "2024.3.6")
            create(IntelliJPlatformType.IntellijIdea, "263.8311.30")
        }
    }
}
