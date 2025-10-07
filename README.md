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
    profile = G2ContinuityProfile.RoundedRectangle.copy(
        extendedFraction = 0.5,
        arcFraction = 0.5,
        bezierCurvatureScale = 1.1,
        arcCurvatureScale = 1.1
    ),
    capsuleProfile = G2ContinuityProfile.Capsule.copy(
        extendedFraction = 0.5,
        arcFraction = 0.25
    )
)

// create shapes with custom continuity
ContinuousRoundedRectangle(16.dp, continuity = g2)
ContinuousCapsule(continuity = g2)
```

The following parameters are supported by `G2ContinuityProfile`:

- **Extended fraction:** the transition length between original corner and line, relative to the corner radius
- **Arc fraction:** the ratio of the arc to the corner
- **Bezier curvature scale**: the multiplier of the end curvature of the Bézier curve
- **Arc curvature scale**: the multiplier of the arc curvature

**Note:** It guarantees G1 continuity at least. Only if the Bezier curvature scale equals the arc curvature scale,
it will have exact G2 continuity.

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
