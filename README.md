# MolecularFoundry (PaperMC Plugin)

Modernized to Java 21 LTS and aligned with Paper API 1.21.4.

## Requirements
- Java 21 (LTS)
- Maven 3.9+
- Paper API 1.21.4-R0.1-SNAPSHOT for compilation

## Build
```bash
# Windows PowerShell
$env:JAVA_HOME="C:\Users\marvi\.jdk\jdk-21.0.8"
$env:Path="$env:JAVA_HOME\bin;C:\Users\marvi\.maven\maven-3.9.12\bin;$env:Path"
mvn -B clean package
```
Artifacts are produced under `target/`.

## Toolchains
This project uses Maven Toolchains to ensure builds run on JDK 21:
- Project file: .mvn/toolchains.xml
- User file: C:\Users\marvi\.m2\toolchains.xml
The `maven-toolchains-plugin` and `maven-enforcer-plugin` in [pom.xml](pom.xml) enforce Java 21.

## Quick Start (Server)
1. Build the plugin.
2. Copy `target/MolecularFoundry-*.jar` into your Paper server `plugins/`.
3. Start the server and inspect logs for plugin enable messages.

## Notes
- Compiler `source/target` are set from `java.version=21` in [pom.xml](pom.xml).
- If Maven reports "No toolchain found", ensure your user toolchains file points to the JDK 21 installation.

## Changelog
- Java 21 upgrade: toolchains + enforcer; builds green on JDK 21.
