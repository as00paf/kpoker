plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeHotReload) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.ktor) apply false
}

tasks.register("runAll") {
    group = "application"
    description = "Runs both the server and the desktop application"
    
    doLast {
        val isWindows = System.getProperty("os.name").lowercase().contains("win")
        val gradlew = if (isWindows) "gradlew.bat" else "./gradlew"
        
        println("Starting Server...")
        ProcessBuilder(gradlew, ":server:run").inheritIO().start()
        
        println("Starting Desktop App...")
        ProcessBuilder(gradlew, ":composeApp:run").inheritIO().start()
    }
}