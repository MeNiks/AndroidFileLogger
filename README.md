# AndroidFileLogger
AndroidFileLogger

```
Dail this on Phone dailer to see logs
*#*#1221#*#*

FileLogHelper.context = this

To Log
FileLogHelper.appendLog( "niks here")

Step 1. Add the JitPack repository to your build file
Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
Step 2. Add the dependency

	dependencies {
	        implementation 'com.github.MeNiks:AndroidFileLogger:Tag'
	}

```