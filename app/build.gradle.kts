plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    id("com.google.devtools.ksp") version "2.1.0-1.0.29"
}

android {
    namespace = "com.mitron.connect"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.mitron.connect"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        
        // Strip unused architectures from heavy SDKs (like Agora) to massively reduce APK size
        ndk {
            abiFilters.add("arm64-v8a")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
    
    flavorDimensions += "env"
    productFlavors {
        create("dev") {
            dimension = "env"
            buildConfigField("String", "BASE_URL", "\"https://connect-mitron.vercel.app/\"")
            buildConfigField("String", "AI_BASE_URL", "\"http://51.79.143.65:3001/\"")
        }
        create("prod") {
            dimension = "env"
            buildConfigField("String", "BASE_URL", "\"https://connect-mitron.vercel.app/\"")
            buildConfigField("String", "AI_BASE_URL", "\"http://51.79.143.65:3001/\"")
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    debugImplementation(libs.androidx.ui.tooling)

    // Firebase BoM and Auth removed as per self-hosted requirements
    implementation("io.coil-kt:coil-compose:2.6.0")
    // QR Code generation
    implementation("com.google.zxing:core:3.5.3")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    
    // Gemini AI
    // implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
    
    // OkHttp for Ollama API
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:okhttp-sse:4.12.0")
    // Agora RTC Calling
    implementation("io.agora.rtc:full-sdk:4.2.6")
    
    // Retrofit for Vercel Backend
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")

    // Room
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    ksp("androidx.room:room-compiler:$room_version")
    
    // Social Login
    implementation("androidx.browser:browser:1.8.0")
    implementation("androidx.credentials:credentials:1.3.0-alpha01")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0-alpha01")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    
    // Maps Compose
    implementation("com.google.maps.android:maps-compose:4.3.3")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
}

