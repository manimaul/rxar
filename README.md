# RxAR (Rx Activity Result)
Simple tool to "start activity for result" in Android using [Rx](http://reactivex.io/)

Example:

```kotlin 
startActivityForResult(supportFragmentManager, Intent(this, OtherActivity::class.java))
    .observeOn(AndroidSchedulers.mainThread())
    .subscribeBy { result ->
        ...
    }
```

Gradle:
```
repositories {
 maven { url  "https://dl.bintray.com/madrona/maven" }
}

dependencies {
 implementation group: 'com.willkamp', name: 'rxar', version: '0.0.3'
}
```
