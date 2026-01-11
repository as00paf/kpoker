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
        val gradlew = File(projectDir, if (isWindows) "gradlew.bat" else "gradlew").absolutePath
        
        println("Starting Server from: $gradlew")
        val serverProcess = if (isWindows) {
            ProcessBuilder("cmd", "/c", gradlew, ":server:run")
        } else {
            ProcessBuilder(gradlew, ":server:run")
        }
        serverProcess.inheritIO().start()
        
        // Wait a bit for server to bind
        Thread.sleep(3000)
        
        println("Starting Desktop App...")
        val appProcess = if (isWindows) {
            ProcessBuilder("cmd", "/c", gradlew, ":composeApp:run")
        } else {
            ProcessBuilder(gradlew, ":composeApp:run")
        }
        appProcess.inheritIO().start()
    }
}