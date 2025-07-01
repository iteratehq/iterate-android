# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Structure

This is the **Iterate Android SDK** - a library for collecting user feedback through targeted surveys in Android applications. The project follows a multi-module Gradle structure:

- `iterate/` - Main SDK library module (core implementation)
- `app/` - Basic example/test application using the SDK
- `compose/` - Jetpack Compose example application
- `examples/` - Additional example implementations

## Build Commands

**Build the project:**
```bash
./gradlew build
```

**Run tests:**
```bash
./gradlew test
./gradlew connectedAndroidTest  # for instrumented tests
```

**Lint checking:**
```bash
./gradlew ktlintCheck
```

**Auto-format code:**
```bash
./gradlew ktlintFormat
```

**Clean build:**
```bash
./gradlew clean
```

**Build specific module:**
```bash
./gradlew :iterate:build
./gradlew :app:build
./gradlew :compose:build
```

**Run single test:**
```bash
./gradlew :iterate:test --tests "IterateRepositoryTest.testSpecificMethod"
```

## Architecture Overview

The SDK follows a layered architecture pattern:

### Core Components

1. **Iterate.kt** (`iterate/src/main/java/com/iteratehq/iterate/Iterate.kt`) - Main SDK entry point providing public API methods:
   - `init()` - Initialize SDK with API key and configuration
   - `sendEvent()` - Send tracking events to trigger surveys
   - `identify()` - Associate user traits with responses
   - `preview()` - Preview mode for testing surveys
   - `reset()` - Clear user data (typically on logout)

2. **Repository Layer** (`iterate/src/main/java/com/iteratehq/iterate/data/`) - Data management:
   - `IterateRepository` - Interface defining data operations
   - `DefaultIterateRepository` - Main implementation handling API calls and local storage
   - Storage split between in-memory cache and encrypted SharedPreferences

3. **API Layer** (`iterate/src/main/java/com/iteratehq/iterate/data/remote/`) - Network communication:
   - `IterateApi` - Interface for API operations
   - `DefaultIterateApi` - HTTP client implementation for Iterate services

4. **UI Components** (`iterate/src/main/java/com/iteratehq/iterate/view/`) - Survey display:
   - `SurveyView` - WebView-based survey renderer with JavaScript bridge
   - `PromptView` - Native prompt dialog before surveys

5. **Models** (`iterate/src/main/java/com/iteratehq/iterate/model/`) - Data structures for surveys, events, responses, and API communication

### Key Patterns

- **Repository Pattern** - Abstracts data access from business logic
- **Callback Pattern** - Async API responses and user interaction events
- **Fragment-based UI** - DialogFragments for survey/prompt display
- **WebView Integration** - Surveys render in WebView with JavaScript bridge for native interaction
- **Encrypted Storage** - User data stored securely using Android Keystore (API 23+)

### Event Flow

1. App calls `Iterate.sendEvent()` with event name
2. SDK creates embed context with user traits and targeting info
3. API call to `/embed` endpoint determines if survey should show
4. If survey returned, display PromptView or SurveyView based on configuration
5. User interactions bridge back to native code via JavaScript interface
6. Response data sent back to API and local callbacks triggered

## Development Notes

- **Minimum SDK**: API 21 (Android 5.0)
- **Target SDK**: API 31
- **Language**: Kotlin with some Java interop
- **Kotlin Version**: 1.6.21
- **Gradle Version**: 8.9.1
- **Testing**: JUnit 4, Robolectric for unit tests, Espresso for UI tests
- **HTTP Client**: Built-in URLConnection (no external HTTP dependencies)
- **JSON**: Gson for serialization
- **UI Rendering**: WebView for surveys, native views for prompts
- **Pre-commit hooks**: Automatic installation via Gradle task for code formatting

The SDK is published to Maven Central as `com.iteratehq:iterate` and supports both View-based and Compose-based Android applications.