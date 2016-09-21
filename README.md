## Description

## Gradle

```java
dependencies {
	...
	compile 'com.dualcores.swagpoints:x.y.z'
}
```

## Usage

* Simple usage in layout:

```xml
<com.dualcores.swagpoints.SwagPoints
    android:id="@+id/swagpoints"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:padding="40dp"
    swagpoints:indicatorIcon="@drawable/indicator"
    swagpoints:clockwise="true"
    swagpoints:arcColor="@android:color/white"
    swagpoints:progressColor="@color/colorPrimary"
    swagpoints:arcWidth="4dp"
    />
```

* All attributes supported:

```xml
<declare-styleable name="SwagPoints">
    <attr name="points" format="integer" />
    <attr name="max" format="integer" />
    <attr name="min" format="integer"/>
    <attr name="step" format="integer"/>
    <attr name="indicatorIcon" format="reference" />
    <attr name="progressWidth" format="dimension" />
    <attr name="progressColor" format="color" />
    <attr name="arcWidth" format="dimension" />
    <attr name="arcColor" format="color" />
    <attr name="clockwise" format="boolean" />
    <attr name="enabled" format="boolean" />
</declare-styleable>
```

## Licence