# 🧀♟️ Cheesse

Cheesse is a cross-platform chess application built with **Kotlin Multiplatform**. It features a shared codebase for business logic and a Spring Boot backend for multiplayer support.

## Project Structure

The project is divided into three main modules:

*   **`:composeApp`**: Contains the Compose Multiplatform UI code. Supports Android, iOS, Desktop (JVM), and Web (Wasm).
*   **`:shared`**: The shared module containing common business logic, models, and networking code used by both the frontend and the server.
*   **`:server`**: A Spring Boot-based backend server that handles game sessions and real-time communication.

## TODO

### Game
- promoting pieces
- handle game result
- show current player
- show captured pieces
- show file and rank of board
- rotate board so white is at the bottom / board rotation in general
- application icon
- creating lobby
- joining lobby
- websocket connection to the server
- move communication
- resigning
- time constraints

### Server
- UUID instead of Long
- encapsule chess engine in chessengineadapter
- redis repository for better everything
- global exception handler and custom exceptions
- resigning
- time constraints
- a lot of tests

### Misc
- github actions
- github pages