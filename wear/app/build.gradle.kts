plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.rafaelboban.activitytracker.wear"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.rafaelboban.activitytracker.wear"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.immutable.collection)
    implementation(libs.kotlin.serialization)
    implementation(libs.material.icons.extended)
    implementation(libs.lifecycle.compose)
    implementation(libs.navigation.compose.wear)

    implementation(libs.preferences)

    implementation(libs.dagger.hilt.android)
    implementation(libs.hilt.navigation)
    ksp(libs.dagger.hilt.compiler)
    ksp(libs.hilt.compiler)

    implementation(libs.coroutines.core)

    implementation(libs.health.services.client)

    implementation(libs.wear.compose.ui.tooling)
    implementation(libs.wear.compose.foundation)
    implementation(libs.wear.compose.material)

    implementation(libs.core)
    implementation(libs.androidx.wear.ongoing)
    implementation(libs.horologist.compose.layout)
    implementation(libs.horologist.compose.material)
    implementation(libs.horologist.health.composables)
    implementation(libs.horologist.health.service)

    implementation(libs.constraintlayout.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.play.services.wearable)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.wear.compose.foundation)
    implementation(libs.activity.compose)
    implementation(libs.splashscreen)

    implementation(libs.timber)
    implementation(libs.accompanist.permissions)

    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.compose.ui.tooling)

    implementation(projects.core.theme)
    implementation(projects.core.tracker)
}
