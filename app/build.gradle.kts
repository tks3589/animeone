plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    id("kotlin-parcelize")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.aaron.chen.animeone"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.aaron.chen.animeone"
        minSdk = 24
        targetSdk = 35
        versionCode = 2
        versionName = "1.1"

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
    testOptions {
        unitTests.all {
            it.useJUnitPlatform()
        }
    }
    buildFeatures {
        compose = true
    }

    packagingOptions.resources.excludes += setOf(
        "META-INF/LICENSE*",
        "META-INF/AL2.0",
        "META-INF/LGPL2.1",
        "META-INF/licenses/ASM",
        "**/attach_hotspot_windows.dll"
    )
}

dependencies {
    implementation(kotlin("reflect", libs.versions.kotlin.get()))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.androidx.ui)
    implementation(libs.accompanist.placeholder)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.navigation.compose)
    implementation(libs.bundles.retrofit)
    implementation(libs.bundles.coil)
    implementation(libs.bundles.exoplayer)
    api(libs.bundles.kotest)
    implementation(libs.material)
    implementation(libs.okhttp3.logging.interceptor)
    implementation(libs.kotlin.datetime)
    implementation(libs.gson)
    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.paging.runtime)
    implementation(libs.bundles.androidx.room)
    implementation(libs.jsoup)
    api(libs.turbine)
    api(platform(libs.koin.bom))
    api(libs.bundles.koin)
    ksp(libs.koin.ksp.compiler)
    ksp(libs.androidx.room.compiler)
    testImplementation(libs.junit)
    testImplementation(libs.mockk.main)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}