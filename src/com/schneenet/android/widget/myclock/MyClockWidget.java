package com.schneenet.android.widget.myclock;

import java.util.Calendar;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.RemoteViews;
import com.schneenet.android.common.AlarmClockNotFoundException;
import com.schneenet.android.common.LaunchAlarmClock;

public class MyClockWidget extends AppWidgetProvider
{

	public static final String MYCLOCK_UPDATE = "com.schneenet.android.widget.myclock.UPDATE";

	private static final long ONE_SECOND = 1000l;

	@Override
	public void onReceive(Context context, Intent intent)
	{
		super.onReceive(context, intent);
		if (MYCLOCK_UPDATE.equals(intent.getAction()))
		{
			updateAppWidgets(context);
		}
	}

	@Override
	public void onDisabled(Context context)
	{
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(buildUpdatePendingIntent(context));
	}

	@Override
	public void onEnabled(Context context)
	{
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), ONE_SECOND, buildUpdatePendingIntent(context));
	}

	private PendingIntent buildUpdatePendingIntent(Context context)
	{
		Intent intent = new Intent(MYCLOCK_UPDATE);
		return PendingIntent.getBroadcast(context, 0, intent, 0);
	}

	private void updateAppWidgets(Context context)
	{

		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		ComponentName component = new ComponentName(context, MyClockWidget.class);

		Calendar now = Calendar.getInstance();
		String nextAlarm = Settings.System.getString(context.getContentResolver(), Settings.System.NEXT_ALARM_FORMATTED);

		RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
		updateViews.setImageViewBitmap(R.id.display_clock, renderClock(context, now));

		try
		{
			updateViews.setOnClickPendingIntent(R.id.widget_container, LaunchAlarmClock.getLaunchAlarmClockPendingIntent(context));
		}
		catch (AlarmClockNotFoundException e)
		{
			Log.e("MyClockWidget", e.getLocalizedMessage(), e);
		}

		updateViews.setTextViewText(R.id.display_next_alarm, nextAlarm);
		appWidgetManager.updateAppWidget(component, updateViews);
	}

	// Rendering constants
	private static final String MEASURE_TEXT_CLOCK = "00:00";
	private static final String MEASURE_TEXT_SECONDS = "00";
	private static final String FORMAT_12 = "%1$tl:%1$tM";
	private static final String FORMAT_24 = "%1$tH:%1$tM"; // With leading zero
	private static final int PADDING = 5;

	private Bitmap renderClock(Context context, Calendar cal)
	{
		// Load resources
		Resources res = context.getResources();
		Typeface clock = Typeface.createFromAsset(context.getAssets(), "fonts/Clockopia.ttf");
		float fontSize = res.getDimension(R.dimen.clock_font_size);
		float secondsFontSize = res.getDimension(R.dimen.seconds_font_size);
		float ampmFontSize = res.getDimension(R.dimen.ampm_font_size);
		String am = res.getString(R.string.am);
		String pm = res.getString(R.string.pm);
		String clockText = String.format(DateFormat.is24HourFormat(context) ? FORMAT_24 : FORMAT_12, cal);

		// Prepare base clock paint
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setSubpixelText(true);
		paint.setTypeface(clock);
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(Color.WHITE);
		paint.setShadowLayer(8, 8, 8, 0xAA666666);
		paint.setTextSize(fontSize);
		paint.setTextAlign(Align.RIGHT);

		// Modify for seconds
		Paint secondsPaint = new Paint(paint);
		secondsPaint.setTextAlign(Align.LEFT);
		secondsPaint.setTextSize(secondsFontSize);

		// Modify for AMPM
		Paint ampmPaint = new Paint(paint);
		ampmPaint.setTextAlign(Align.LEFT);
		ampmPaint.setTextSize(ampmFontSize);

		// Calculate text boundaries
		Rect clockBounds = new Rect();
		paint.getTextBounds(MEASURE_TEXT_CLOCK, 0, MEASURE_TEXT_CLOCK.length(), clockBounds);
		Rect ampmBounds = new Rect();
		ampmPaint.getTextBounds(pm, 0, pm.length(), ampmBounds);
		Rect secondsBounds = new Rect();
		secondsPaint.getTextBounds(MEASURE_TEXT_SECONDS, 0, MEASURE_TEXT_SECONDS.length(), secondsBounds);

		// Calculate image dimensions
		int primaryWidth = clockBounds.width();
		int secondaryWidth = ampmBounds.width() > secondsBounds.width() ? ampmBounds.width() : secondsBounds.width();
		int width = primaryWidth + PADDING + PADDING + secondaryWidth;
		int height = clockBounds.height();

		// Create image and canvas to render to
		Bitmap clockBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(clockBitmap);

		// Render clock
		canvas.drawText(clockText, primaryWidth, height - 1, paint);
		int xOffset = primaryWidth + PADDING;
		if (!DateFormat.is24HourFormat(context))
		{
			canvas.drawText(cal.get(Calendar.AM_PM) == Calendar.PM ? pm : am, xOffset, height - ampmPaint.descent(), ampmPaint);
		}
		canvas.drawText(String.format("%02d", cal.get(Calendar.SECOND)), xOffset, secondsBounds.height(), secondsPaint);

		// Done, return th bitmap
		return clockBitmap;
	}
}
