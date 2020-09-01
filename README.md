# 🦋 | Butterfly ![Kotlin CI with Gradle](https://github.com/NinoDiscord/Butterfly/workflows/Kotlin%20CI%20with%20Gradle/badge.svg)  [ ![Download](https://api.bintray.com/packages/dondishorg/oss-maven/Butterfly/images/download.svg) ](https://bintray.com/dondishorg/oss-maven/Butterfly/_latestVersion)
🦋 | Discord API Framework for JDA built in Kotlin

## How to use
### Documentation
Master: [https://docs.augu.dev/butterfly](https://docs.augu.dev/-butterfly)

Stable: [https://docs.augu.dev/butterfly-stable](https://docs.augu.dev/stable/-butterfly)
### Adding as dependency:
Fetch the version from the badge.
#### Add dependency using Gradle
Groovy DSL:
```groovy
repositories {
    jcenter()
}

dependencies {
    implementation 'dev.augu.nino:Butterfly:VERSION'
}
```
Kotlin DSL:
```kotlin
repositories {
    jcenter()
}

dependencies {
    implementation("dev.augu.nino:Butterfly:VERSION")
}
```

#### Add dependency using Maven
build.xml:
```xml
...
<repositories>
    <repository>
      <id>jcenter</id>
      <url>https://jcenter.bintray.com/</url>
    </repository>
</repositories>
...
```
```xml
...
<dependency>
  <groupId>dev.augu.nino</groupId>
  <artifactId>Butterfly</artifactId>
  <version>VERSION</version>
  <type>pom</type>
</dependency>
...
```

#### Add dependency using Ivy
ivysettings.xml:
```xml
...
<resolvers>
    <chain name="chain"> 
        <!-- https://jcenter.bintray.com/ -->
        <bintray />
            <!-- https://bintray.com/dondishorg/oss-maven     -->
            <!-- https://dl.bintray.com/dondishorg/oss-maven-->
            <bintray subject="dondishorg" repo="oss-maven"/>
    </chain>
</resolvers>
...
```

```xml
...
<dependency org='dev.augu.nino' name='Butterfly' rev='VERSION'>
  <artifact name='Butterfly' ext='pom' ></artifact>
</dependency>
...
```

## Contributing
WIP

## Examples
You can see the [Examples](https://github.com/NinoDiscord/Butterfly/tree/master/src/examples/kotlin/dev/augu/nino/butterfly/examples) directory on GitHub.
