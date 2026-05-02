# 🧀♟️ Cheesse

Cheesse is a cross-platform chess application built with **Kotlin Multiplatform**. It features a shared codebase for business logic and a Spring Boot backend for multiplayer support.

## Project Structure

The project is divided into three main modules:

*   **`:composeApp`**: Contains the Compose Multiplatform UI code. Supports Android, iOS, Desktop (JVM), and Web (Wasm).
*   **`:shared`**: The shared module containing common business logic, models, and networking code used by both the frontend and the server.
*   **`:server`**: A Spring Boot-based backend server that handles game sessions and real-time communication.

## How to run 

1. Host a PostgreSQL instance (e.g. using docker-compose in this repository) and set the corresponding environment variables `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME` and `DB_PASSWORD`
2. Run the server (by building the repository or downloading the server artifact)
3. Go to https://daarenx.github.io/cheesse/ (or download and run one of the native apps) and enter the server url in the settings
