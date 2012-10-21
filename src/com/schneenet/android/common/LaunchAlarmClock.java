package com.schneenet.android.common;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

public class LaunchAlarmClock
{
	private static final String[][] clockImpls =
	{
			{
					"HTC Alarm Clock",
					"com.htc.android.worldclock",
					"com.htc.android.worldclock.WorldClockTabControl" },
			{
					"Standard Alarm Clock",
					"com.android.deskclock",
					"com.android.deskclock.AlarmClock" },
			{
					"Froyo Nexus Alarm Clock",
					"com.google.android.deskclock",
					"com.android.deskclock.DeskClock" },
			{
					"Moto Blur Alarm Clock",
					"com.motorola.blur.alarmclock",
					"com.motorola.blur.alarmclock.AlarmClock" },
			{
					"Samsung Galaxy Clock",
					"com.sec.android.app.clockpackage",
					"com.sec.android.app.clockpackage.ClockPackage" } };

	private static final String TAG = "LaunchAlarmClock";

	public static PendingIntent getLaunchAlarmClockPendingIntent(Context context) throws AlarmClockNotFoundException
	{
		PackageManager packageManager = context.getPackageManager();
		Intent alarmClockIntent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);

		boolean foundClockImpl = false;

		for (int i = 0; i < clockImpls.length; i++)
		{
			String vendor = clockImpls[i][0];
			String packageName = clockImpls[i][1];
			String className = clockImpls[i][2];
			try
			{
				ComponentName cn = new ComponentName(packageName, className);
				packageManager.getActivityInfo(cn, PackageManager.GET_META_DATA);
				alarmClockIntent.setComponent(cn);
				Log.d(TAG, "Found " + vendor + " --> " + packageName + "/" + className);
				foundClockImpl = true;
			}
			catch (NameNotFoundException e)
			{
				Log.d(TAG, vendor + " does not exist on device.");
			}
		}

		if (foundClockImpl)
		{
			return PendingIntent.getActivity(context, 0, alarmClockIntent, 0);
		}
		else
		{
			throw new AlarmClockNotFoundException("Unable to find an alarm clock application installed on the device.");
		}
	}
}
