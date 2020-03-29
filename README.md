# AndroidFileLogger
AndroidFileLogger

Dail this on Phone dailer to see logs
```
*#*#1221#*#*
```

To initialize library add this in Splash or Application Class
```
FileLogHelper.initialize(applicationContext)
```

To Log
```
FileLogHelper.log("This is the log")
```

Step 1. Add the JitPack repository to your build file
Add it in your root build.gradle at the end of repositories:
```
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

Step 2. Add the dependency
```
	dependencies {
		debugImplementation("com.github.MeNiks:AndroidFileLogger:Tag")
	}
```
