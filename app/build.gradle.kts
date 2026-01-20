plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.webviewapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.webviewapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        // 🔴 THIS IS REQUIRED
        buildConfigField(
            "String",
            "WEB_URL",
            "\"https://example.com\""
            )

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
    


}
