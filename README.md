# AndroidFileLogger
AndroidFileLogger

```
In Manifiest

<activity
android:name=".logs.FileLogsPreviewActivity"
android:launchMode="singleTask"
android:screenOrientation="portrait" />

<receiver android:name=".logs.FileLogCodeReceiver">
<intent-filter>
<action android:name="android.provider.Telephony.SECRET_CODE" />
<data android:scheme="android_secret_code" android:host="1221" />
</intent-filter>
</receiver>

To Log
FileLogHelper.appendLog( "niks here")

```