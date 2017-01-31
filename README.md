# vlc-video-view [![Release](https://jitpack.io/v/dhleong/vlc-video-view.svg)](https://jitpack.io/#dhleong/vlc-video-view)

*A VLC-powered VideoView for Android*

## What

`VlcVideoView` is like the native `VideoView`, but relies on [VLC][1] for playback. As such, it
supports a much wider range of protocols, formats, and codecs than the built-in player. The downside,
however, is that it is huge: the demo app is just shy of 100mb from all the native libs, so you will
probably need to use APK splits. Future work may explore removing some of the libs specific to older
versions of the platform that we don't support.

## How

`VlcVideoView` is distributed using [JitPack][2]:

```groovy
   repositories {
        jcenter()
        maven { url "https://jitpack.io" }
   }
   dependencies {
         compile 'com.github.dhleong:vlc-video-view:<VERSION>'
   }
```

Where `<VERSION>` is your prefered version (the latest is shown in the badge up top). `VlcVideoView` can be
included in any layout as usual. See the [demo app][3] for a usage example.

[1]: https://github.com/mrmaffen/vlc-android-sdk
[2]: https://jitpack.io
[3]: app/src/main/java/net/dhleong/vlcvideoview/demo/MainActivity.java
