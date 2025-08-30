# Capsule

![Maven Central Version](https://img.shields.io/maven-central/v/top.ltfan.capsule/capsule)

A Compose Multiplatform library that provides smooth rounded corner shapes.

## Getting Started

To use `capsule` in your Kotlin Multiplatform project, add the following dependency to your `build.gradle.kts` file:

```kotlin
dependencies {
    implementation("top.ltfan.capsule:capsule:<version>")
}
```

Or if you are using Gradle Version Catalogs, add the following to your `gradle/libs.versions.toml`:

```toml
[versions]
capsule = "<version>"

[libraries]
capsule = { module = "top.ltfan.capsule:capsule", version.ref = "capsule" }
```

Make sure your `settings.gradle.kts` includes the repository:

```kotlin
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
```

## Usages

Replace the `RoundedCornerShape` with `ContinuousRoundedRectangle` or `ContinuousCapsule`:

```kotlin
// create a basic rounded corner shape
ContinuousRoundedRectangle(16.dp)

// create a capsule shape
ContinuousCapsule
```

Custom continuity:

```kotlin
val g1 = G1Continuity // no corner smoothness
val g2 = G2Continuity(
    circleFraction = 0.18,
    extendedFraction = 0.5,
    bezierCurvatureScale = 1.15,
    circleCurvatureScale = 1.15,
)

// create shapes with custom continuity
ContinuousRoundedRectangle(16.dp, continuity = g2)
ContinuousCapsule(continuity = g2)
```

## Performance

Drawing cubic Bézier curves on Android performs poorly. However, the Capsule library uses a very efficient method to
calculate the control points, achieving optimal theoretical performance.

When the shape area is large (almost fullscreen) and the corner radius is constantly changing, performance may decrease.
Use `animatedShape.copy(continuity = G1Continuity)` to temporarily disable corner smoothing during the animation.

## Credits

This project is forked from [Capsule](https://github.com/Kyant0/Capsule). Super big thanks to Kyant0.

## Contributing

We welcome contributions! Please submit issues or pull requests for any bugs, features, or improvements.

## License

This project is licensed under the Apache License 2.0. See the [LICENSE](LICENSE) file for details.
