plugins {
    id("com.android.application")
}

android {
    compileSdk = 36

    defaultConfig {
        applicationId = "com.spendsages.walletwatch"
        minSdk = 28
        targetSdk = 36
        versionCode = 15
        versionName = "1.2.2"
    }

    buildFeatures {
        viewBinding = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    namespace = "com.spendsages.walletwatch"
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

kotlin {
    jvmToolchain(21)
}

base {
    archivesName.set("WalletWatch-Installer")
}

configurations.all {
    exclude(group = "com.android.support", module = "support-v4")
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    val kotlinVersion: String by rootProject.extra
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.viewpager2:viewpager2:1.1.0")
    implementation("com.github.lecho:hellocharts-library:1.5.8") {
        exclude(group = "com.android.support")
    }
    implementation("androidx.core:core-ktx:1.17.0") {
        exclude(group = "com.android.support")
    }
    implementation("androidx.legacy:legacy-support-v4:1.0.0") {
        exclude(group = "com.android.support")
    }
}
