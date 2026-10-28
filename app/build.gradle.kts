import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.androidex.trailglance"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.androidex.trailglance"
        minSdk = 33
        targetSdk = 36
        versionCode = 2
        versionName = "0.2"
    }

    signingConfigs {
        // Checked-in debug key so every CI build installs over the previous one
        // (a new key per build would force an uninstall and lose notification access).
        getByName("debug") {
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        getByName("debug") {
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}
