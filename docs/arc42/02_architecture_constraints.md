# 2. Architecture Constraints

Architecture constraints define the organizational, technical, and process-oriented boundaries that shape Cheesse's design.

---

## 2.1 Technical Constraints

### 2.1.1 Platform & Toolchain Constraints

- **Programming Language**: Kotlin is the uniform language across all modules (`:shared`, `:server`, `:composeApp`, `:androidApp`).
- **Java Virtual Machine**: A single JVM toolchain/target version (configured in the module `build.gradle.kts` files) is used for backend compilation, desktop client execution, and Android builds.
- **Client Platforms**:
  - **Desktop (JVM)**: Native desktop distribution (macOS DMG/ICNS, Linux DEB, Windows MSI).
  - **Web (Wasm)**: Kotlin/Wasm (`wasmJs`) with browser distribution executing over modern WebAssembly-enabled browsers.
  - **Android**: Android SDK levels (`minSdk`, `compileSdk`, `targetSdk`) are defined in `gradle/libs.versions.toml`.
  - **iOS**: Target architectures `iosArm64` (physical device) and `iosSimulatorArm64` packaged as static frameworks (`ComposeApp.framework`).

### 2.1.2 Framework & Runtime Constraints

- **Backend Reactive Stack**:
  - Spring Boot built exclusively on reactive Spring WebFlux.
  - Asynchronous database communication via Spring Data R2DBC (`r2dbc-postgresql`) connecting to PostgreSQL.
  - Strict prohibition against blocking calls (no `Mono.block()`, `Thread.sleep()`, or blocking JDBC drivers).
- **Client UI & Architecture**:
  - Compose Multiplatform utilizing Material 3 components.
  - Voyager navigation framework with `ScreenModel` architecture.
  - Dependency injection powered by Kodein DI (`kodein-di-framework-compose`).
- **Networking & Serialization**:
  - Ktor Client utilizing engine adapters per target (`ktor-client-cio` on JVM, `ktor-client-darwin` on iOS, `ktor-client-js-wasm-js` on Web, `ktor-client-android` on Android).
  - WebSockets for bi-directional real-time communication.
  - `kotlinx.serialization` with polymorphic JSON encoding across both client and server.
- **Chess Engine**:
  - `xyz.daaren:chess-core` is the multiplatform chess engine for board validation, legal move computation, and FEN/PGN string parsing.

---

## 2.2 Organizational Constraints

- **Build System**: Gradle multi-project build with Kotlin DSL (`build.gradle.kts`, `settings.gradle.kts`) and typesafe project accessors (`enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")`).
- **Centralized Dependencies**: All library and plugin versions must be managed through the Gradle Version Catalog (`gradle/libs.versions.toml`).
- **Open Source Licensing**: All components, dependencies, and media assets must adhere to permissive open-source licenses compatible with Apache 2.0 / MIT.
- **Automated Verification**:
  - Integration testing backed by Testcontainers (`testcontainers-postgresql`, managed by the Spring Boot BOM) with Spring Boot `@ServiceConnection` running on an active Docker daemon.

---

## 2.3 Conventions and Standards

- **Code Style & Linting**:
  - Ktlint (via `org.jlleitschuh.gradle.ktlint`) is enforced across all subprojects.
  - Code formatting checked via `./gradlew ktlintCheck` and corrected via `./gradlew ktlintFormat`.
  - Composable functions use PascalCase naming convention per `.editorconfig`.
- **Compiler Flags**:
  - Strict JSR-305 compiler arguments (`-Xjsr305=strict`, `-Xannotation-default-target=param-property`) enabled on the `:server` module.
- **Testing Conventions**:
  - JUnit 5 (`org.junit.jupiter`) and `kotlin.test`.
  - Test naming follows descriptive backtick syntax: `fun `anonymous users can create a game and join exactly once`()`.
- **Architectural Guidelines**:
  - Code inside `:shared/src/commonMain` must remain strictly target-agnostic without platform imports.
  - Controller methods must delegate directly to `GameService` without handling raw transport-specific try/catch blocks; errors are mapped using `@RestControllerAdvice` (`GameExceptionHandler`).
