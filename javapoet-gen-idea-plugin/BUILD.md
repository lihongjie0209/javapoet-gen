# IDEA Plugin Build Instructions

## Prerequisites

1. Install core module to Maven local repository first:
   ```bash
   cd javapoet-gen
   mvn install -pl javapoet-gen-core
   ```

2. Make sure you have Gradle installed or use the wrapper.

## Building the Plugin

```bash
cd javapoet-gen-idea-plugin

# Build the plugin
./gradlew buildPlugin

# The plugin zip will be created at:
# build/distributions/javapoet-gen-idea-plugin-1.0.0-SNAPSHOT.zip
```

## Running/Testing the Plugin

```bash
# Run IntelliJ IDEA with the plugin installed
./gradlew runIde
```

## Installing the Plugin

1. Build the plugin using `./gradlew buildPlugin`
2. In IntelliJ IDEA, go to `Settings` → `Plugins` → `⚙️` → `Install Plugin from Disk...`
3. Select the generated zip file from `build/distributions/`
4. Restart IDE

## Usage

1. Right-click on any Java file in the Project view or Editor
2. Select "Generate JavaPoet Code"
3. Choose one of the options:
   - Copy to clipboard
   - Open in new editor tab
   - Show in tool window
