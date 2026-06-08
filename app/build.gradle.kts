import org.gradle.api.JavaVersion
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { load(it) }
    }
}

fun localProperty(key: String): String? =
    localProperties.getProperty(key)?.trim()?.takeIf { it.isNotEmpty() }

android {
    namespace = "com.cpumonitor.pro"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.cpumonitor.pro"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "0.2.0"

        buildConfigField("String", "GITHUB_REPO_OWNER", "\"TaoLaoVN\"")
        buildConfigField("String", "GITHUB_REPO_NAME", "\"MaHUD\"")
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
    signingConfigs {
        create("release") {
            val storeFilePath = localProperty("RELEASE_STORE_FILE")
            if (storeFilePath != null) {
                storeFile = rootProject.file(storeFilePath)
                storePassword = localProperty("RELEASE_STORE_PASSWORD")
                keyAlias = localProperty("RELEASE_KEY_ALIAS")
                keyPassword = localProperty("RELEASE_KEY_PASSWORD")
            }
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            val releaseStoreFile = localProperty("RELEASE_STORE_FILE")
            signingConfig = if (
                releaseStoreFile != null &&
                rootProject.file(releaseStoreFile).exists() &&
                signingConfigs.getByName("release").storeFile != null
            ) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
        }
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":data"))
    implementation(project(":core:common"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:ui"))
    implementation(project(":core:logging"))
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))
    implementation(project(":core:monitoring"))

    implementation(project(":feature-dashboard"))
    implementation(project(":feature-cpu"))
    implementation(project(":feature-memory"))
    implementation(project(":feature-battery"))
    implementation(project(":feature-thermal"))
    implementation(project(":feature-storage"))
    implementation(project(":feature-process"))
    implementation(project(":feature-deviceinfo"))
    implementation(project(":feature-benchmark"))
    implementation(project(":feature-history"))
    implementation(project(":feature-export"))
    implementation(project(":feature-overlay"))
    implementation(project(":feature-alerts"))
    implementation(project(":feature-analytics"))
    implementation(project(":feature-settings"))

    implementation(project(":service-monitoring"))
    implementation(project(":service-overlay"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    implementation(libs.timber)
    ksp(libs.hilt.compiler)
    ksp(libs.androidx.hilt.compiler)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
