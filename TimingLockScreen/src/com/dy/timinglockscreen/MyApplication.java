package com.dy.timinglockscreen;

import java.util.Date;

import android.app.Application;

public class MyApplication extends Application {
	
	public static int TimerLockScreenMinute=0;
	public static Date lockScreenDate=null;
	public final static String KEY_SETTIME="SetTime";
	public final static String KEY_SETTIME_HOUR="SetTime_Hour";
	public final static String KEY_SETTIME_MINUTE="SetTime_Minute";
	public final static String KEY_SETTIME_STARTTIME="SetTime_StartTime";
	public final static String KEY_TIMER_ACTION="Timer-Action";
	public final static int TIMER_ACTION_STOP=0;
	public final static int TIMER_ACTION_START=1;
	public final static String KEY_ISTIMER="Is_Timer";
	

}
