package com.itworx.common.utils;

import android.util.Log;


public class Logger  {
public static void logVerbose(String tag,String msg){	
	Log.v(tag, msg);
}
public static void logDebug(String tag,String msg){	
	Log.d(tag, msg);
}
public static void logInfo(String tag,String msg){	
	Log.i(tag, msg);
}
public static void logWarning(String tag,String msg){	
	Log.w(tag, msg);
}
public static void logError(String tag,String msg){	
	Log.e(tag, msg);
}
} 
