@file:Suppress("UnstableApiUsage")

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.minar.randomix"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.minar.randomix"
        minSdk = 23
        targetSdk = 36
        versionCode = 41
        versionName = "3.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    lint {
        abortOnError = false
    }

    androidResources {
        generateLocaleConfig = true
    }
}

dependencies {
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.preference)
    implementation(libs.appintro)
    implementation(libs.gson)
    implementation(libs.splashscreen)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
