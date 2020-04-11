# Logging Utility

Dail this on Phone dailer to see logs you will get notification
```
*#*#1221#*#*
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

Step 3(Optional). Support Network Logs(Okhttp)
```

import com.niks.filelog.network.HttpLoggingInterceptor

httpClientBuilder.addInterceptor(HttpLoggingInterceptor())

```

Step 4(Optional). Timber Logs
```
import com.niks.filelog.timber.LoggingTree
Timber.plant(LoggingTree())
```

Step 5. Initialize library in Application or SplashScreen
```
FileLogHelper.initialize(applicationContext)
```

Apis
```
FileLogHelper.log("This is message")
FileLogHelper.log("This is message", longInfo = "This is longInfo")
FileLogHelper.log(tag = "SomeTag",message = "This is message", longInfo = "This is longInfo")
```
