This is the Gemini CLI. We are setting up the context for our chat.
Today's date is Sunday, January 11, 2026 (formatted according to the user's locale).
My operating system is: win32
The project's temporary directory is: C:\Users\as00p\.gemini\tmp\01ccb79322f673b14e1662cb243af458399b8086231ba6ee1ea3059a617867dd
I'm currently working in the directory: C:\workspace\kotlin_workspace\kpoker
Here is the folder structure of the current working directories:

Showing up to 200 items (files + folders).

C:\workspace\kotlin_workspace\kpoker\
└──.idea\
    └──.gitignore
    └──AndroidProjectSystem.xml
    └──copilot.data.migration.agent.xml
    └──gradle.xml
    └──misc.xml
    └──workspace.xml

# KPoker Project (`gemini.md`)

This document provides a summary of the KPoker project to help guide AI-assisted development.

## 1. Overview

KPoker is a multiplayer, multiplatform Texas Hold'em poker game. This is the repository url: 
https://github.com/as00paf/kpoker/
The code will reside in an IntelliJ project 

## 2. Technology Stack

- **Language:** Kotlin 2.3.0
- **AGP:** 8.12.0
- **Framework:** Kotlin Multiplatform (KMP)
- **UI:** Compose Multiplatform
- **Build Tool:** Gradle
- **Network:** Ktor
- **Threading:** Coroutines

## 3. Target Platforms

- Desktop
- Android


## 4. Project Structure

The project follows a standard Kotlin Multiplatform structure:

The main project structure is already defined by the template used from the jetbrains website.

- **Server-side Code**: The backend logic is also part of this KMP project to maximize code reuse. It is a modern CLI application that allows for command inputs and shows logs and status messages, running games based on user-defined parameters. It uses Ktor websockets and appropriate service classes to manage connections and messages. It will likely be located in a `serverMain` source set or a dedicated server module.
- **Package Name**: The primary package for the application is `com.pafoid.kpoker`.

## 5. Build and Run Commands

The project is built and managed using Gradle.

- **Build Project:**
  ```bash
  ./gradlew build
  ```

- **Run Tests:**
  ```bash
  ./gradlew check
  ```

- **Run Applications:**
    - **Android:** Use the standard 'Run' configuration for the `composeApp` module in Android Studio.
    - **Server:** Use the standard CLI style application
    - **Desktop:** Use the standard Java application run configuration 
    
	  
## 6. Code and architecture

Make use of SOLID principles and the clean architecture

## 7. Server Application Details

The server application will be a modern command-line interface (CLI) that provides:
-   **Command Inputs**: Allows users to input commands to control game parameters and start/stop games.
-   **Logs and Status Messages**: Displays real-time logs and status updates for game events and server operations.
-   **Game Management**: Runs Texas Hold'em poker games based on user-defined parameters.
-   **Technology**: Utilizes Ktor for building robust websocket communication and appropriate service classes for managing connections and messages.

### "Hello World" Welcome Message

To ensure the server application is properly set up, it will display a welcome message upon startup. This will serve as a basic "Hello World" functionality.

```