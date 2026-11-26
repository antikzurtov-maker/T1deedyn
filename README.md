# T1deedyn

Client-side Fabric mod for Minecraft **1.21.0**.

## Requirements
- Java 21
- Fabric Loader 0.16.5 or newer
- Fabric API for Minecraft 1.21.0

## Build
The repository includes a GitHub Actions workflow that builds the mod on each push. You can also build locally with Gradle 8.8 or newer:

```bash
gradle build
```

The JAR is generated in `build/libs/`.

## Controls
- **X**: Open or close the T1deedyn menu.

## Current stage
Initial client menu scaffold with saved toggles and a UI scale slider. More visual modules and tab interactions will be added in later stages.
