# Figma to JSON Exporter

A JavaFX application to export Figma design resources to JSON and generate project code.

## Features

- Export Figma designs to JSON format
- Cascading selection of Pages and Layers from Figma files
- Generate TouchGFX Design projects from exported data
- Settings persistence across sessions
- Internationalization support (English, Chinese, Japanese)
- Secure personal access token storage

## Requirements

- Java 21 or higher
- Maven 3.6+

## Build

```bash
mvn clean package
```

## Run

```bash
java -jar target/figma2json-1.0.0.jar
```

Or using Maven:

```bash
mvn javafx:run
```

## Usage

1. Enter your Figma personal access token in the Settings section
2. Paste a Figma file URL into the URL field
3. Click "Load" to fetch the file structure
4. Select a Page from the Pages tree
5. Select a Layer from the Layers tree
6. Choose output format and generator
7. Export to JSON or Generate a project

## Configuration

Settings are automatically saved and restored between sessions:

- Personal access token
- Language preference
- Output format
- Project generator type
- Output path
- Last used Figma URL

## Project Structure

```
src/main/java/com/tlcsdm/figma2json/
├── api/          # Figma REST API implementation
├── converter/    # Format conversion (JSON, etc.)
├── generator/    # Project generation (TouchGFX, etc.)
├── ui/           # JavaFX controllers
├── util/         # Utility classes
└── Main.java     # Application entry point
```

## Getting a Figma Access Token

1. Log in to your Figma account
2. Go to Settings → Personal access tokens
3. Generate a new token
4. Copy and paste the token into the application's Access Token field

## Supported Output Formats

- **JSON** (default): Exports layer data as formatted JSON

## Supported Project Generators

- **TouchGFX Design** (default): Generates a TouchGFX-compatible project structure
