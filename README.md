# rapidopus
A performance-oriented java wrapper for libopus using JNI

## Compiling

- Clone the repository, or download the zip archive and extract it:
  ```
  > git clone  https://github.com/WalkerKnapp/rapidopus.git 
  > cd devolay
  ```
- Download Opus build from https://ci.appveyor.com/project/rillian/opus
- Copy include folder to rapidopus-natives/libs/opus
- Copy static library file to rapidopus-natives/libs/opus/lib
- Run the automatic assembly:
  ```
  > ./gradlew assemble
  > ./gradlew install
  ```