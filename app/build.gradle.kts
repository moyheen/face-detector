plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    compileSdkVersion(28)
    buildToolsVersion("28.0.3")

    defaultConfig {
        applicationId = "com.moyinoluwa.facedetection"
        minSdkVersion(16)
        targetSdkVersion(28)
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    val kotlinVersion = rootProject.extra.get("kotlinVersion")

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    androidTestImplementation("com.android.support.test.espresso:espresso-core:2.2.2") {
        exclude("com.android.support", "support-annotations")
    }
    implementation("com.android.support:appcompat-v7:28.0.0")
    implementation("com.google.android.gms:play-services-vision:17.0.2")
    testImplementation("junit:junit:4.12")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlinVersion")
}

repositories {
    mavenCentral()
}