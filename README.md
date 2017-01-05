# SwagPoints
[![License](https://img.shields.io/badge/license-Apache%202-green.svg)](https://www.apache.org/licenses/LICENSE-2.0)
 [ ![Download](https://api.bintray.com/packages/enginebai/DualCores/SwagPoints/images/download.svg) ](https://bintray.com/enginebai/DualCores/SwagPoints/_latestVersion)

**SwagPoints** - An Android custom circular SeekBar that supports max/min range and step settings.

![SwagPoints](https://raw.githubusercontent.com/enginebai/SwagPoints/master/art/graphic.png)

## Gradle

```java
dependencies {
	...
	compile 'com.dualcores.swagpoints:swagpoints:1.0.2'
}
```

## Usage

* In XML layout: 

```xml
<com.dualcores.swagpoints.SwagPoints
    xmlns:swagpoints="http://schemas.android.com/apk/res-auto"
    android:id="@+id/seekbar_point"
    android:layout_width="match_parent"
    android:layout_height="340dp"
    android:layout_gravity="center"
    android:padding="64dp"
    swagpoints:min="100"
    swagpoints:max="1000"
    swagpoints:step="100"
    swagpoints:progressColor="@color/color_progress"
    swagpoints:progressWidth="12dp"
    swagpoints:arcColor="@color/color_arc"
    swagpoints:arcWidth="8dp"
    swagpoints:textSize="72sp"
    swagpoints:textColor="@color/colorText"
    swagpoints:indicatorIcon="@drawable/indicator"
    />
```
**Remember** to add `layout_padding` to make sure that there is enough space to display the whole widget and indicator drawable.

![](https://raw.githubusercontent.com/enginebai/SwagPoints/master/art/padding1.png)

If you don't add any `layout_padding`, the arc will extend the whole width/height, and the indicator drawable will be truncated (as below diagram).
![](https://raw.githubusercontent.com/enginebai/SwagPoints/master/art/padding2.png)

* All customizable attributes:

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
    <attr name="textSize" format="dimension"/>
    <attr name="textColor" format="color"/>
    <attr name="clockwise" format="boolean" />
    <attr name="enabled" format="boolean" />
</declare-styleable>
```

## Sample
* Clone the repository and check out the `app` module.
* Download the app [Swag – Exchange Personal Snaps](https://play.google.com/store/apps/details?id=com.machipopo.swag) on Google Play. This widget is used in the send point dialog.

## Licence
Copyright 2016 Engine Bai

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
