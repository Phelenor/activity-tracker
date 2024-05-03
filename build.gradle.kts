// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.ktlint)
}

buildscript {
    dependencies {
        classpath(libs.gradle.secrets)
    }
}

ktlint {
    version = "0.39.0"
    verbose = true
    android = true
    outputToConsole = true
    ignoreFailures = true
    enableExperimentalRules = true
    kotlinScriptAdditionalPaths {
        include(fileTree("scripts/"))
    }
    filter {
        exclude("**/generated/**")
        include("**/kotlin/**")
    }
}
