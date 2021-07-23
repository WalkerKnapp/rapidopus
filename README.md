# rapidopus
A performance-oriented Java wrapper for libopus using JNI.

## Installation


#### Gradle
```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation 'me.walkerknapp:rapidopus:2.0.0'
}
```

#### Maven
```xml
<dependency>
  <groupId>me.walkerknapp</groupId>
  <artifactId>rapidopus</artifactId>
  <version>2.0.0</version>
</dependency>
```

## Compiling

- Clone the repository, or download the zip archive and extract it:
  ```
  > git clone  https://github.com/WalkerKnapp/rapidopus.git 
  > cd devolay
  ```
- Run the automatic assembly:
  ```
  > ./gradlew assemble
  > ./gradlew install
  ```