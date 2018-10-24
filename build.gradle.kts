// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    extra.set("kotlinVersion", "1.2.71")

    repositories {
        jcenter()
        google()
    }
    dependencies {
        val kotlinVersion = rootProject.extra.get("kotlinVersion")

        classpath("com.android.tools.build:gradle:3.2.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        jcenter()
        google()
    }
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}