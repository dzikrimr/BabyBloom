plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
<<<<<<< HEAD
    id("kotlin-kapt")
=======
    alias(libs.plugins.hiltAndroid)
    alias(libs.plugins.google.services)
    id("com.google.devtools.ksp")
    kotlin("plugin.serialization") version "2.0.21"
>>>>>>> 0605efe6db813be54e885793a75ec4c10fec73d6
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
<<<<<<< HEAD
    implementation(libs.androidx.room.common.jvm)
    implementation(libs.androidx.room.runtime.android)
    implementation(libs.androidx.foundation.android)
    implementation(libs.androidx.foundation.android)
    implementation(libs.androidx.foundation.android)
    implementation(libs.androidx.foundation.android)
    implementation(libs.androidx.foundation.android)
    implementation(libs.androidx.foundation.android)
    implementation(libs.androidx.navigation.compose.android)
    kapt("androidx.room:room-compiler:2.7.2")
=======
    implementation(libs.androidx.navigation.compose.android)
>>>>>>> 0605efe6db813be54e885793a75ec4c10fec73d6
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
<<<<<<< HEAD
    // ML Kit untuk Face Detection dan Pose Detection
    implementation ("com.google.mlkit:face-detection:16.1.5")
    implementation ("com.google.mlkit:pose-detection:18.0.0-beta3")
    implementation ("com.google.mlkit:pose-detection-accurate:18.0.0-beta3")

    // CameraX
    implementation ("androidx.camera:camera-camera2:1.3.0")
    implementation ("androidx.camera:camera-lifecycle:1.3.0")
    implementation ("androidx.camera:camera-view:1.3.0")

    // ViewModel
    implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // Permissions
    implementation ("com.google.accompanist:accompanist-permissions:0.32.0")

    implementation("org.tensorflow:tensorflow-lite:2.16.1")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")
    implementation("org.tensorflow:tensorflow-lite-select-tf-ops:2.15.0")

    implementation("androidx.compose.material:material-icons-extended:1.7.4")

    implementation("com.google.accompanist:accompanist-permissions:0.35.1-alpha")

=======

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

//    firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.play.services.auth)

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
>>>>>>> 0605efe6db813be54e885793a75ec4c10fec73d6
}