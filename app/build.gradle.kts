plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.easyway.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.myapp.webview"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        // 🔴 THIS IS REQUIRED
        buildConfigField("String", "WEB_URL", "\"https://gemini.google.com/\"")
        buildConfigField("String", "SPLASH_COLOR", "\"#534AB7\"")
        buildConfigField("String", "SPLASH_LOGO_URL", "\"\"")
        buildConfigField("String", "SPLASH_TEXT", "\"Google Gemni\"")
        buildConfigField("Boolean", "ENABLE_PUSH", "true")
        buildConfigField("Boolean", "ENABLE_BOTTOM_NAV", "true")
        buildConfigField("Boolean", "ENABLE_CAMERA", "false")
        buildConfigField("Boolean", "ENABLE_LOCATION", "false")
        buildConfigField("Boolean", "ENABLE_SHARE", "false")
        buildConfigField("String", "NAV_ITEMS_JSON", "\"[{\\\"label\\\":\\\"Home\\\",\\\"path\\\":\\\"https://gemini.google.com/\\\"},{\\\"label\\\":\\\"\\\",\\\"path\\\":\\\"\\\"},{\\\"label\\\":\\\"\\\",\\\"path\\\":\\\"\\\"}]\"")
        buildConfigField("String", "APP_ID", "\"googlegemini\"")
        buildConfigField("String", "BACKEND_URL", "\"https://jacquie-unevocable-adaline.ngrok-free.dev\"")
        buildConfigField("String", "APP_NAME", "\"Google Gemini\"")

    }

    // 🔴 THIS IS REQUIRED
    buildFeatures {
        buildConfig = true
    }

    signingConfigs {
        create("release") {
            storeFile = file("upload-keystore.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation("androidx.browser:browser:1.8.0")
    
    // Add Firebase BOM and Messaging
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-messaging")

    // Add Glide for image loading
    implementation("com.github.bumptech.glide:glide:4.16.0")

}

































