plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.webviewapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.myapp.webview"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        // 🔴 THIS IS REQUIRED
        buildConfigField("String", "WEB_URL", "\"https://claude.ai/\"")
        buildConfigField("String", "SPLASH_COLOR", "\"#000000\"")
        buildConfigField("String", "SPLASH_LOGO_URL", "\"\"")
        buildConfigField("String", "SPLASH_TEXT", "\"Claude AI\"")
        buildConfigField("Boolean", "ENABLE_PUSH", "true")
        buildConfigField("Boolean", "ENABLE_BOTTOM_NAV", "true")
        buildConfigField("Boolean", "ENABLE_CAMERA", "false")
        buildConfigField("Boolean", "ENABLE_LOCATION", "false")
        buildConfigField("Boolean", "ENABLE_SHARE", "false")
        buildConfigField("String", "NAV_ITEMS_JSON", "\"[{\\\"label\\\":\\\"Home\\\",\\\"path\\\":\\\"https://claude.ai/new\\\"},{\\\"label\\\":\\\"Chats\\\",\\\"path\\\":\\\"https://claude.ai/recents\\\"},{\\\"label\\\":\\\"Accounts\\\",\\\"path\\\":\\\"https://claude.ai/settings/general\\\"}]\"")
        buildConfigField("String", "APP_ID", "\"claude\"")
        buildConfigField("String", "BACKEND_URL", "\"https://jacquie-unevocable-adaline.ngrok-free.dev\"")
        buildConfigField("String", "APP_NAME", "\"Claude\"")

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

























