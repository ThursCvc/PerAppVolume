import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.miuixvolume"

    // Miuix 0.9.5-beta04 的 AAR 要求 minCompileSdk 37 + minCompileMinorSdk 2。
    // 当前使用旧 DSL（为了兼容 Kotlin 插件），所以用字符串形式指定 37.2。
    compileSdkVersion("android-37.2")
    buildToolsVersion = "37.0.0"

    defaultConfig {
        applicationId = "com.example.miuixvolume"
        minSdk = 33
        targetSdk = 37
        versionCode = 7
        versionName = "1.5.0"
    }

    buildFeatures {
        compose = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    packagingOptions {
        // 确保 META-INF/xposed 下的模块信息进入 APK
        resources.merges += "META-INF/xposed/*"
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }

    dependenciesInfo {
        includeInApk = false
    }
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(libs.miuix.ui)
    implementation(libs.miuix.preference)
    implementation(libs.miuix.icons)
    implementation(libs.miuix.glass)

    compileOnly(libs.libxposed.api)
    implementation(libs.libxposed.service)
}
