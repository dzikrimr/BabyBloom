plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hiltAndroid)
    alias(libs.plugins.google.services)
    id("com.google.devtools.ksp")
    kotlin("plugin.serialization") version "2.0.21"
}

android {
    namespace = "com.example.bubtrack"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.bubtrack"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose.android)
    implementation(libs.play.services.cast.tv)
    implementation(libs.androidx.espresso.core)
    implementation(libs.vision.common)
    implementation(libs.play.services.mlkit.barcode.scanning)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.media3.common.ktx)
    implementation(libs.generativeai)

    implementation (libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    implementation (libs.google.accompanist.systemuicontroller)
    implementation(libs.accompanist.permissions)
    implementation(libs.foundation.pager)


    //room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.paging)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    //hilt
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.compiler)

    //paging
    implementation(libs.paging.runtime)
    implementation(libs.paging.compose)

    // firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.play.services.auth)
    implementation("com.google.firebase:firebase-database-ktx")

    //googleAuth
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    // coil
    implementation(libs.coil.compose)

    // retrofit
    implementation(libs.retrofit)

    // gson
    implementation(libs.converter.gson)

    //splash
    implementation(libs.core.splashscreen)

    //chart
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    //calendar
    implementation("com.kizitonwose.calendar:compose:2.6.1")

    //cloudinary
    implementation ("com.cloudinary:cloudinary-android:3.0.2")

    // camera
    implementation ("androidx.camera:camera-core:1.3.4")
    implementation ("androidx.camera:camera-camera2:1.3.4")
    implementation ("androidx.camera:camera-lifecycle:1.3.4")
    implementation ("androidx.camera:camera-view:1.3.4")

    // mlkit
    implementation ("com.google.mlkit:face-detection:16.1.5")
    implementation ("com.google.mlkit:pose-detection:18.0.0-beta3")
    implementation ("com.google.mlkit:pose-detection-accurate:18.0.0-beta3")

    // QR Code generation
    implementation ("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation ("com.google.zxing:core:3.5.1")
    implementation("com.google.mlkit:barcode-scanning:17.2.0")

    // webrtc
    implementation("com.mesibo.api:webrtc:1.0.5")

    //tensorflow
    implementation("org.tensorflow:tensorflow-lite:2.16.1")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")
}