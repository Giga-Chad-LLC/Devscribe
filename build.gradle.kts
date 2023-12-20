import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}

group = "com.ide-development"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.desktop.currentOs)
    implementation(compose.materialIconsExtended)

    // For implementing native file/folder pickers
    val lwjglVersion = "3.3.1"
    listOf("lwjgl", "lwjgl-nfd").forEach { lwjglDep ->
        implementation("org.lwjgl:${lwjglDep}:${lwjglVersion}")
        listOf(
            "natives-windows", "natives-windows-x86", "natives-windows-arm64",
            "natives-macos", "natives-macos-arm64",
            "natives-linux", "natives-linux-arm64", "natives-linux-arm32"
        ).forEach { native ->
            runtimeOnly("org.lwjgl:${lwjglDep}:${lwjglVersion}:${native}")
        }
    }
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "devscribe"
            packageVersion = "1.0.0"
        }
    }
}

tasks.test {
    // Use the built-in JUnit support of Gradle
    useJUnitPlatform()
}