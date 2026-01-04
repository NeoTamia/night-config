![Night Config](logo.png)

[![Maven Central](https://img.shields.io/maven-central/v/re.neotamia.night-config/core.svg)](https://central.sonatype.com/search?q=g%3Are.neotamia.night-config)
[![javadoc](https://javadoc.io/badge2/re.neotamia.night-config/core/javadoc.svg)](https://javadoc.io/doc/re.neotamia.night-config/core)

# Introduction

Original Code made by ElectronWill
NightConfig is a powerful yet easy-to-use java configuration library, written in Java 21.

It supports the following formats:
- [JSON](https://www.json.org/)
- [YAML v1.2](https://yaml.org/)
- [TOML v1.0](https://github.com/toml-lang/toml)
- [HOCON](https://github.com/typesafehub/config/blob/master/HOCON.md)

---

# Table of Contents

- [Quick Start](#quick-start)
- [Features](#features)
  - [Basic Configuration Operations](#basic-configuration-operations)
  - [FileConfig: File-Based Configuration](#fileconfig-file-based-configuration)
  - [CommentedConfig: Configuration with Comments](#commentedconfig-configuration-with-comments)
  - [ConfigSpec: Validation & Correction](#configspec-validation--correction)
  - [Serde System: Advanced Serialization/Deserialization](#serde-system-advanced-serializationdeserialization)
  - [TypeAdapter: Custom Generic Type Handling](#typeadapter-custom-generic-type-handling)
  - [Concurrent Configurations](#concurrent-configurations)
- [Modules and Dependencies](#modules-and-dependencies)
- [Running the Examples](#running-the-examples)
- [Project Building](#project-building)

---

# Quick Start

```java
// Simple builder:
FileConfig conf = FileConfig.of("the/file/config.toml");

// Advanced builder, default resource, autosave and much more (-> cf the wiki)
CommentedFileConfig config = CommentedFileConfig.builder("myConfig.toml")
    .defaultResource("defaultConfig.toml")
    .autosave()
    .build();
config.load(); // This actually reads the config

String name = config.get("username"); // Generic return type!
List<String> names = config.get("users_list"); // Generic return type!
long id = config.getLong("account.id"); // Compound path: key "id" in subconfig "account"
int points = config.getIntOrElse("account.score", defaultScore); // Default value

config.set("account.score", points * 2);

String comment = config.getComment("user");
// NightConfig saves the config's comments (for TOML, HOCON and YAML)

// config.save(); not needed here thanks to autosave()
config.close(); // Close the FileConfig once you're done with it :)
```

---

# Features

## Basic Configuration Operations

The `Config` interface provides core operations for reading and writing configuration values.

### Creating Configurations

```java
// In-memory config (not thread-safe)
Config config = Config.inMemory();

// In-memory config with concurrent map (basic thread-safety for Map operations)
Config concurrentConfig = Config.inMemoryConcurrent();

// From specific format
Config tomlConfig = TomlFormat.instance().createConfig();
```

### Reading Values

```java
// Get with automatic type inference
String value = config.get("key");
List<String> list = config.get("myList");

// Get with type-specific methods
int intValue = config.getInt("number");
long longValue = config.getLong("bigNumber");
double doubleValue = config.getDouble("decimal");
boolean boolValue = config.getBoolOrElse("flag", false);

// Get with default values
String name = config.getOrElse("username", "anonymous");
int score = config.getIntOrElse("score", 0);

// Nested paths (dot notation)
String nested = config.get("database.connection.host");
```

### Writing Values

```java
// Set values
config.set("key", "value");
config.set("nested.path.key", 123);

// Add only if not exists
boolean added = config.add("key", "value");

// Remove values
Object removed = config.remove("key");

// Clear all values
config.clear();

// Bulk operations
config.putAll(anotherConfig);  // Overwrites existing
config.addAll(anotherConfig);  // Only adds missing entries
```

---

## FileConfig: File-Based Configuration

`FileConfig` ties a configuration to a file, with automatic format detection and many builder options.

### Basic Usage

```java
// Auto-detect format from extension
FileConfig config = FileConfig.of("config.toml");
config.load();

// Use values...
String value = config.get("key");
config.set("key", "newValue");

// Save and close
config.save();
config.close();
```

### Builder Pattern

```java
FileConfig config = FileConfig.builder("config.toml")
    .charset(StandardCharsets.UTF_8)              // Character encoding
    .writingMode(WritingMode.REPLACE_ATOMIC)      // Atomic file writes
    .parsingMode(ParsingMode.REPLACE)             // How to merge loaded data
    .onFileNotFound(FileNotFoundAction.CREATE_EMPTY) // Action when file missing
    .build();
```

### Default Resource

Copy a default config from your JAR resources if the file doesn't exist:

```java
FileConfig config = FileConfig.builder("config.toml")
    .defaultResource("defaultConfig.toml")  // Path in resources
    .build();
config.load(); // Creates file from resource if not found
```

### Autosave

Automatically save the config whenever it's modified:

```java
FileConfig config = FileConfig.builder("config.json")
    .autosave()
    .build();
config.load();

config.set("value", 123);
// File is saved automatically!

config.close();
```

### Autoreload

Automatically reload the config when the file changes on disk:

```java
FileConfig config = FileConfig.builder("config.json")
    .autoreload()
    .build();
config.load();

// Config automatically reloads when file is modified externally
// Reloading is throttled to prevent excessive reads

config.close();
```

### Sync vs Async Writing

```java
// Synchronous: save() blocks until complete
FileConfig syncConfig = FileConfig.builder("config.toml")
    .sync()
    .build();

// Asynchronous: save() returns immediately
FileConfig asyncConfig = FileConfig.builder("config.toml")
    .async()
    .build();

// Async with custom debouncing
FileConfig debouncedConfig = FileConfig.builder("config.toml")
    .asyncWithDebouncing(Duration.ofMillis(500))
    .build();
```

### Bulk Updates

Group multiple operations to avoid triggering autosave on each change:

```java
config.bulkUpdate(c -> {
    c.set("key1", "value1");
    c.set("key2", "value2");
    c.set("key3", "value3");
    // Autosave triggers only once at the end
});
```

---

## CommentedConfig: Configuration with Comments

For formats that support comments (TOML, YAML, HOCON), use `CommentedConfig`:

```java
CommentedFileConfig config = CommentedFileConfig.builder("config.toml").build();
config.load();

// Set values with comments
config.set("database.host", "localhost");
config.setComment("database.host", "The database server hostname");

// Get comments
String comment = config.getComment("database.host");

// Remove comments
config.removeComment("database.host");

config.save();
config.close();
```

---

## ConfigSpec: Validation & Correction

`ConfigSpec` defines constraints for configuration values and can automatically correct invalid configs.

### Defining a Spec

```java
ConfigSpec spec = new ConfigSpec();

// Define with default values
spec.define("username", "anonymous");

// Define numeric ranges (inclusive)
spec.defineInRange("port", 8080, 1, 65535);
spec.defineInRange("volume", 0.5, 0.0, 1.0);

// Define allowed values
spec.defineInList("difficulty", "normal", Arrays.asList("easy", "normal", "hard"));

// Define with enum class
spec.defineOfClass("logLevel", LogLevel.INFO, LogLevel.class);
```

### Validation and Correction

```java
Config config = Config.inMemory();
config.set("port", 99999);     // Invalid: out of range
config.set("extra_key", "x");  // Not in spec

// Check if config is valid
boolean isCorrect = spec.isCorrect(config);

// Auto-correct the config
spec.correct(config);
// - port is reset to default (8080)
// - extra_key is removed
// - missing entries are added with defaults
```

---

## Serde System: Advanced Serialization/Deserialization

The `serde` package provides a modern, flexible serialization system with `ObjectSerializer` and `ObjectDeserializer`.

### Basic Usage

```java
// Serialization: Object -> Config
ObjectSerializer serializer = ObjectSerializer.standard();
Config config = serializer.serializeFields(myObject, Config::inMemory);

// Deserialization: Config -> Object
ObjectDeserializer deserializer = ObjectDeserializer.standard();
MyClass object = deserializer.deserializeFields(config, MyClass::new);

// Deserialize to a Record (Java 17+)
MyRecord record = deserializer.deserializeToRecord(config, MyRecord.class);
```

### Serde Annotations

#### `@SerdeKey` - Custom Key Name

```java
class User {
    @SerdeKey("user_id")
    private String id;  // Serializes to "user_id" in config
}
```

#### `@SerdeSkip` - Skip Fields

```java
class User {
    @SerdeSkip  // Always skip this field
    private transient Object cache;

    @SerdeSkip(value = SkipIf.IS_NULL)  // Skip only if null
    private String optional;

    @SerdeSkip(value = SkipIf.IS_EMPTY)  // Skip if empty collection/string
    private List<String> items;

    @SerdeSkip(value = SkipIf.CUSTOM, customCheck = "shouldSkip")
    private String conditional;

    private boolean shouldSkip(Object value) {
        return value == null || value.toString().isEmpty();
    }
}
```

#### `@SerdeSkipDeserializingIf` / `@SerdeSkipSerializingIf`

Fine-grained control for skip behavior during specific phases:

```java
class Config {
    @SerdeSkipDeserializingIf(SkipIf.IS_NULL)  // Skip when deserializing if config value is null
    private String value;

    @SerdeSkipSerializingIf(SkipIf.IS_EMPTY)   // Skip when serializing if field is empty
    private List<String> items;
}
```

#### `@SerdeDefault` - Default Value Providers

```java
class MyConfig {
    // Default from a method
    @SerdeDefault(provider = "defaultServers")
    private List<String> servers;

    List<String> defaultServers() {
        return Arrays.asList("localhost");
    }

    // Default when null
    @SerdeDefault(provider = "getDefaultName", whenValue = {WhenValue.IS_NULL})
    private String name;

    // Default when missing, null, or empty
    @SerdeDefault(
        provider = "getDefaultItems",
        whenValue = {WhenValue.IS_MISSING, WhenValue.IS_NULL, WhenValue.IS_EMPTY}
    )
    private List<String> items;

    // Different defaults for serializing vs deserializing
    @SerdeDefault(provider = "defaultForDeserializing", phase = SerdePhase.DESERIALIZING)
    @SerdeDefault(provider = "defaultForSerializing", phase = SerdePhase.SERIALIZING)
    private String bidirectional;
}
```

#### `@SerdeComment` - Add Comments (for CommentedConfig)

```java
class ServerConfig {
    @SerdeComment("The server hostname or IP address")
    private String host;

    @SerdeComment("First line of comment")
    @SerdeComment("Second line of comment")
    private int port;
}
```

#### `@SerdeAssert` - Field Validation

Throw an exception if a field doesn't match specified conditions:

```java
class User {
    @SerdeAssert(AssertThat.NOT_NULL)  // Throws if null
    private String username;

    @SerdeAssert(AssertThat.NOT_EMPTY)  // Throws if empty
    private List<String> roles;

    @SerdeAssert(value = AssertThat.CUSTOM, customCheck = "isValidEmail")
    private String email;

    private boolean isValidEmail(String value) {
        return value != null && value.contains("@");
    }
}
```

Assertions can be scoped to specific phases:

```java
@SerdeAssert(value = AssertThat.NOT_NULL, phase = SerdePhase.DESERIALIZING)
private String requiredOnLoad;
```

#### `@SerdeConfig` - Combined Configuration

Consolidate multiple serde annotations into a single annotation:

```java
class Player {
    @SerdeConfig(
        key = "player_name",
        comments = @SerdeComment("The player's display name"),
        asserts = @SerdeAssert(AssertThat.NOT_NULL),
        defaults = @SerdeDefault(provider = "defaultName")
    )
    private String name;

    String defaultName() {
        return "Anonymous";
    }

    @SerdeConfig(
        skip = @SerdeSkip(value = SkipIf.IS_NULL),
        skipSerializingIf = @SerdeSkipSerializingIf(SkipIf.IS_EMPTY)
    )
    private List<String> achievements;
}
```

Available `@SerdeConfig` options:
- `key` - Custom config key name
- `comments` - Array of `@SerdeComment`
- `asserts` - Array of `@SerdeAssert`
- `defaults` - Array of `@SerdeDefault`
- `skip` - Array of `@SerdeSkip`
- `skipDeserializingIf` - Array of `@SerdeSkipDeserializingIf`
- `skipSerializingIf` - Array of `@SerdeSkipSerializingIf`

### Naming Strategies

Transform field names automatically during serialization:

```java
ObjectSerializer serializer = ObjectSerializer.builder()
    .withNamingStrategy(NamingStrategy.SNAKE_CASE)
    .build();

ObjectDeserializer deserializer = ObjectDeserializer.builder()
    .withNamingStrategy(NamingStrategy.SNAKE_CASE)
    .build();
```

Available strategies:
- `IDENTITY` - No transformation (default)
- `SNAKE_CASE` - `userName` → `user_name`
- `KEBAB_CASE` - `userName` → `user-name`
- `CAMEL_CASE` - `user_name` → `userName`
- `PASCAL_CASE` - `userName` → `UserName`

---

## TypeAdapter: Custom Generic Type Handling

`TypeAdapter` allows custom serialization/deserialization of generic types while preserving type information.

### Example: Box<T> Wrapper

```java
// Generic wrapper class
class Box<T> {
    private T value;

    public Box(T value) { this.value = value; }
    public T getValue() { return value; }
}

// TypeAdapter implementation
class BoxTypeAdapter<T> implements TypeAdapter<Box<T>, Object> {

    @Override
    public boolean canHandle(Type type) {
        if (type instanceof ParameterizedType pt) {
            return pt.getRawType() == Box.class;
        }
        return type == Box.class;
    }

    @Override
    public Object serialize(Box<T> value, Type type, SerializerContext ctx) {
        return ctx.serializeValue(value.getValue());
    }

    @Override
    public Box<T> deserialize(Object value, Type type, DeserializerContext ctx) {
        Type valueType = Object.class;
        if (type instanceof ParameterizedType pt) {
            Type[] typeArgs = pt.getActualTypeArguments();
            if (typeArgs.length > 0) {
                valueType = typeArgs[0];
            }
        }
        T innerValue = (T) ctx.deserializeValue(value, new TypeConstraint(valueType));
        return new Box<>(innerValue);
    }
}

// Usage
ObjectSerializer serializer = ObjectSerializer.builder()
    .withTypeAdapter(new BoxTypeAdapter<>())
    .build();

ObjectDeserializer deserializer = ObjectDeserializer.builder()
    .withTypeAdapter(new BoxTypeAdapter<>())
    .build();
```

---

## Concurrent Configurations

For multi-threaded applications, use thread-safe configurations from the `concurrent` package.

### Available Implementations

- **`SynchronizedConfig`** - Uses synchronized blocks
- **`StampedConfig`** - Uses `StampedLock` for better performance

### Bulk Operations

The key feature is atomic bulk operations:

```java
ConcurrentConfig config = new SynchronizedConfig();

// Atomic bulk update
List<String> result = config.bulkUpdate(c -> {
    List<String> players = c.get("players");
    players.add("NewPlayer");
    players.remove("BadPlayer");
    c.set("players", players);
    return players;
});

// Atomic bulk read
String data = config.bulkRead(c -> {
    String name = c.get("name");
    int score = c.getInt("score");
    return name + ": " + score;
});
```

> **Important:** In bulk operations, only use the config view provided to your function (`c`), not the original config reference.

### FileConfig Thread Safety

`FileConfig` uses `ConcurrentConfig` internally and supports the same bulk operations:

```java
FileConfig config = FileConfig.builder("config.toml").build();

config.bulkUpdate(c -> {
    c.set("key1", "value1");
    c.set("key2", "value2");
});
```

---

# Modules and Dependencies

NightConfig is modular. Add only what you need:

| Module | Description | Dependency |
|--------|-------------|------------|
| `core` | Core API (Config, FileConfig, etc.) | Always required |
| `toml` | TOML format support | `re.neotamia.night-config:toml` |
| `json` | JSON format support | `re.neotamia.night-config:json` |
| `yaml` | YAML format support | `re.neotamia.night-config:yaml` |
| `hocon` | HOCON format support | `re.neotamia.night-config:hocon` |

### Repository Configuration

Add the NeoTamia repository to your build configuration:

#### Gradle (Kotlin DSL)

```kotlin
repositories {
    maven {
        url = uri("https://repo.neotamia.re/releases")
    }
    // For snapshots:
    // maven {
    //     url = uri("https://repo.neotamia.re/snapshots")
    // }
}

dependencies {
    implementation("re.neotamia.night-config:core:VERSION")
    implementation("re.neotamia.night-config:toml:VERSION")
}
```

#### Gradle (Groovy DSL)

```groovy
repositories {
    maven { url 'https://repo.neotamia.re/releases' }
    // For snapshots:
    // maven { url 'https://repo.neotamia.re/snapshots' }
}

dependencies {
    implementation 're.neotamia.night-config:core:VERSION'
    implementation 're.neotamia.night-config:toml:VERSION'
}
```

#### Maven

```xml
<repositories>
    <repository>
        <id>neotamia-releases</id>
        <url>https://repo.neotamia.re/releases</url>
    </repository>
    <!-- For snapshots:
    <repository>
        <id>neotamia-snapshots</id>
        <url>https://repo.neotamia.re/snapshots</url>
    </repository>
    -->
</repositories>

<dependencies>
    <dependency>
        <groupId>re.neotamia.night-config</groupId>
        <artifactId>core</artifactId>
        <version>VERSION</version>
    </dependency>
    <dependency>
        <groupId>re.neotamia.night-config</groupId>
        <artifactId>toml</artifactId>
        <version>VERSION</version>
    </dependency>
</dependencies>
```

---

# Running the Examples

Each file in `examples/src/main/java` has a main function and shows how to use NightConfig for many different use cases.

To run an example:
1. Clone this repository.
2. `cd` to it
3. Run `./gradlew examples:run -PmainClass=${CLASS}` by replacing `${CLASS}` with the example of your choice.

For example, to run [FileConfigExample.java](examples/src/main/java/FileConfigExample.java):
```sh
./gradlew examples:run -PmainClass=FileConfigExample
```

Available examples:
- `FileConfigExample` - Basic file configuration usage
- `AutosaveExample` - Automatic saving on modification
- `AutoreloadExample` - Automatic reloading on file change
- `ConfigSpecExample` - Configuration validation and correction
- `TypeAdapterExample` - Custom generic type handling
- `TypeAdapterWithListExample` - TypeAdapter with collections
- `CommentedConfigExample` - Working with comments

---

# Project Building

NightConfig is built with Gradle. The project is divided in several modules, the "core" module plus one module per supported configuration format. Please [read the wiki for more information](https://github.com/TheElectronWill/Night-Config/wiki/Modules-and-dependencies).

## Old Android modules

Older versions of Android (before Android Oreo) didn't provide the packages `java.util.function` and `java.nio.file`, which NightConfig heavily uses.
To attempt to mitigate these issues, I made a special version of each modules, suffixed with `_android`, that you could use instead of the regular modules.

These old `_android` modules are deprecated and will no longer receive updates.
The maintainance burden of these modules is not worth it, and these versions of Android have reached end of life since several years already.
