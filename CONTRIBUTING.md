# Contributing to Folio Java SDK

Thanks for your interest in contributing to Folio!

## Getting started

```bash
git clone https://github.com/carlos7ags/folio-java.git
cd folio-java
./gradlew build
```

Requires **JDK 22+** (Temurin recommended).

## Project structure

```
lib/                           Main SDK
  src/main/java/dev/foliopdf/         Public API (fluent builders)
  src/main/java/dev/foliopdf/internal Panama FFI bindings
  src/main/resources/natives/         Bundled native libraries (5 platforms)
  src/test/java/dev/foliopdf/         JUnit 5 tests
examples/                      Runnable examples
```

## How the SDK works

The Java SDK wraps the [Folio Go engine](https://github.com/carlos7ags/folio) via Panama FFI (JEP 454). The Go engine is compiled to a C shared library (`libfolio.so` / `.dylib` / `.dll`) and bundled in the JAR. At runtime, `NativeLoader` extracts the correct binary for the current platform and loads it via `System.load()`.

All native calls go through `FolioNative.java` which holds 372 `MethodHandle` downcalls. Public API classes in `dev.foliopdf.*` provide fluent Java wrappers around these raw bindings.

## Adding a new binding

When the Go engine adds a new C export:

1. Add the `MethodHandle` + Java method in `FolioNative.java` following the existing pattern
2. Add or update the public API class in `dev.foliopdf/`
3. Add a test in `lib/src/test/java/dev/foliopdf/`
4. Run `./gradlew build` to verify

## Code style

- Follow existing patterns — look at similar classes before writing new code
- Every `if (h == 0)` check must include `FolioNative.lastError()` in the exception message
- Public API methods should be fluent (return `this`) where it makes sense
- Use `AutoCloseable` + `HandleRef` for any class that owns a native handle
- Javadoc on all public classes and methods

## Running tests

```bash
./gradlew test
```

Tests run with `--enable-native-access=ALL-UNNAMED` automatically (configured in `build.gradle.kts`).

## Updating native libraries

Native libraries come from [Folio releases](https://github.com/carlos7ags/folio/releases):

```bash
gh release download vX.Y.Z -R carlos7ags/folio
# Copy each to lib/src/main/resources/natives/{platform}/
```

## Clean Room Policy

Folio is licensed under Apache 2.0 and developed independently. Do **not** reference, port, or adapt code from other PDF libraries. All contributions must be original work.

## Before submitting a PR

Run the full build locally:

```bash
./gradlew build
```

This compiles, runs tests, and generates javadoc. CI will reject failing builds.

## Submitting changes

1. Fork the repository
2. Create a feature branch (`git checkout -b feat/my-feature`)
3. Make your changes
4. Run `./gradlew build` (must pass)
5. Open a pull request

## Reporting issues

Open an issue at https://github.com/carlos7ags/folio-java/issues with:
- JDK version (`java -version`)
- Operating system and architecture
- Minimal reproduction code
- Stack trace (if applicable)

## License

By contributing, you agree that your contributions will be licensed under the Apache 2.0 License.
