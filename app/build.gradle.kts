plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.composeCompiler)
    id("kotlin-parcelize")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

secrets {
    propertiesFileName = "secrets.properties"
    defaultPropertiesFileName = "secrets.properties"
    ignoreList.add("sdk.*")
}

android {
    namespace = "com.rafaelboban.activitytracker"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.rafaelboban.activitytracker"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    hilt {
        enableAggregatingTask = true
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
        buildConfig = true
        viewBinding = true
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    lint {
        abortOnError = false
    }
}

dependencies {
    implementation(libs.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.immutable.collection)
    implementation(libs.kotlin.serialization)
    implementation(libs.kotlin.serialization.retrofit)
    implementation(libs.material.icons.extended)

    implementation(libs.credentails)
    implementation(libs.credentails.auth)
    implementation(libs.google.id)
    implementation(libs.auth0.jwt)
    implementation(libs.preferences)
    implementation(libs.security.crypto)
    implementation(libs.splashscreen)

    implementation(libs.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.material.compose)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.foundation)

    implementation(libs.constraintlayout.compose)
    implementation(libs.lifecycle.compose)
    implementation(libs.viewmodel.compose)
    implementation(libs.androidx.startup.runtime)

    implementation(libs.dagger.hilt.android)
    implementation(libs.hilt.navigation)
    implementation(libs.androidx.lifecycle.service)
    ksp(libs.dagger.hilt.compiler)
    ksp(libs.hilt.compiler)

    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    implementation(libs.retrofit.core)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    implementation(libs.sandwich)
    implementation(libs.sandwich.retrofit)

    implementation(libs.compose.coil)
    implementation(libs.navigation.compose)

    implementation(libs.hilt.work)
    implementation(libs.work.runtime)

    implementation(libs.timber)
    implementation(libs.accompanist.permissions)

    implementation(libs.google.android.gms.play.services.location)
    implementation(libs.google.maps.android.compose)
    implementation(libs.google.maps.android.utils.ktx)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.wearable)

    debugImplementation(libs.chucker)
    releaseImplementation(libs.chucker.no.op)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.compose.ui.tooling)

    implementation(projects.core.theme)
    implementation(projects.core.shared)
}
