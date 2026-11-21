# Capsule

![Maven Central Version](https://img.shields.io/maven-central/v/top.ltfan.capsule/capsule)

A Compose Multiplatform library that provides smooth rounded corner shapes.

## Migrate to M3X

The standalone `top.ltfan.capsule:capsule` artifact is deprecated. Its successor is Capsule in
[M3X](https://github.com/xfqwdsj/m3-extended). Use an M3X release that includes the Capsule module.

Replace the dependency in the Gradle configuration or source set that declares it:

```kotlin
dependencies {
    implementation("top.ltfan.m3x:capsule:<m3x-version>")
}
```

If you use a version catalog, update the module and version there. Keep `mavenCentral()` in
`settings.gradle.kts` and update Capsule imports to `top.ltfan.m3x.capsule` and its subpackages. See the [M3X documentation](https://github.com/xfqwdsj/m3-extended/blob/main/docs/README.md)
for shape APIs and theme integration.

## Credits

This project is forked from [Capsule](https://github.com/Kyant0/Capsule). Super big thanks to Kyant0.

## License

This project is licensed under the Apache License 2.0. See the [LICENSE](LICENSE) file for details.
