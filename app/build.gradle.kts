plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.prettyjson.android"
    compileSdk = 36

    // Enable incremental resource processing for faster builds
    buildFeatures {
        buildConfig = true
        compose = true
    }

    defaultConfig {
        applicationId = "com.prettyjson.android"
        minSdk = 24
        targetSdk = 35
        
        // Generate version code from timestamp (ensures unique, incrementing version codes)
        // Format: YYYYMMDDHHMM (e.g., 202411072037 for Nov 7, 2024, 20:37)
        // This gives us a unique version code for each build that's always incrementing
        val buildTime = System.currentTimeMillis() / 1000 // Unix timestamp in seconds
        val baseDate = 1704067200L // Jan 1, 2024 00:00:00 UTC (base date)
        val daysSinceBase = (buildTime - baseDate) / 86400 // Days since base date
        val secondsInDay = (buildTime - baseDate) % 86400 // Seconds within the day
        // Version code: days * 10000 + seconds/10 (gives us ~1000 builds per day max)
        // This ensures version code is always incrementing and fits in Int32
        versionCode = (daysSinceBase * 10000 + secondsInDay / 10).toInt()
        
        // Version name: Keep readable format (e.g., "1.0")
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // AdMob App ID - Production App ID
        buildConfigField("String", "ADMOB_APP_ID", "\"ca-app-pub-3137130387262789~9240936516\"")
    }

    signingConfigs {
        create("release") {
            // Keystore path: check app/ directory first (local), then root (CI/CD)
            val keystorePath = if (file("app/json-viewer-release-key.jks").exists()) {
                "app/json-viewer-release-key.jks"
            } else {
                "json-viewer-release-key.jks"
            }
            storeFile = file(keystorePath)
            // Use environment variables for CI/CD, fallback to hardcoded for local builds
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "jsonviewer2024"
            keyAlias = "json-viewer-key"
            keyPassword = System.getenv("KEY_PASSWORD") ?: "jsonviewer2024"
        }
    }

    buildTypes {
        // Only release build type - always signed
        release {
            isMinifyEnabled = false
            // MANDATORY: Release builds MUST be signed
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        
        // Debug builds are disabled for production - only use for local development
        // To build debug: ./gradlew assembleDebug (for local testing only)
        getByName("debug") {
            // Debug builds are unsigned - only for local development
            // Do not use for distribution
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
        // Enable incremental compilation for faster builds
        freeCompilerArgs += listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-Xjvm-default=all"
        )
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    
    // Customize output file names to include version information
    applicationVariants.all {
        val variant = this
        variant.outputs.all {
            // For APK outputs - include version name and code in filename
            val output = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
            output.outputFileName = "app-${variant.name}-v${variant.versionName}-code${variant.versionCode}.apk"
        }
    }
    
    // For bundle files (AAB), configure splits
    bundle {
        language {
            enableSplit = false
        }
        density {
            enableSplit = true
        }
        abi {
            enableSplit = true
        }
    }
}

// Task to rename AAB file with version information after build
// Note: Using afterEvaluate to access version info and avoid configuration cache issues
afterEvaluate {
    val versionName = android.defaultConfig.versionName
    val versionCode = android.defaultConfig.versionCode
    
    tasks.register("renameAAB") {
        doLast {
            val aabFile = file("build/outputs/bundle/release/app-release.aab")
            if (aabFile.exists()) {
                val newName = "app-release-v${versionName}-code${versionCode}.aab"
                val newFile = file("build/outputs/bundle/release/${newName}")
                aabFile.copyTo(newFile, overwrite = true)
                println("✅ AAB renamed to: ${newName}")
                println("📦 Location: ${newFile.absolutePath}")
                println("📋 Version Name: ${versionName}")
                println("🔢 Version Code: ${versionCode}")
            } else {
                println("⚠️  AAB file not found at: ${aabFile.absolutePath}")
            }
        }
    }
    
    // Make renameAAB run after bundleRelease
    tasks.named("bundleRelease") {
        finalizedBy("renameAAB")
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    
    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    
    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    
    // Navigation
    implementation(libs.androidx.navigation.compose)
    
    // Room Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    
    // DataStore
    implementation(libs.androidx.datastore.preferences)
    
    // JSON
    implementation(libs.google.gson)
    
    // AdMob
    implementation(libs.google.mobile.ads)
    
    // Play Billing
    implementation(libs.google.play.billing)
    
    // Dependency Injection
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    
    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    
    // Network
    implementation(libs.okhttp)
    
    // Material Icons Extended
    implementation(libs.material.icons.extended)
    
    // QR Code
    implementation(libs.zxing.core)
    implementation(libs.zxing.android.embedded)
    
    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation("org.json:json:20230618")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${libs.versions.coroutines.get()}")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}