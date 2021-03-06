package com.sadna.widgets.application;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.RemoteViews;

import com.sadna.android.content.LauncherIntent;
import com.sadna.data.Snapshot;
import com.sadna.service.StatisticsService;

@SuppressLint("NewApi")
public class ImplHC implements ContactWidget.WidgetImplementation {
	public static final String TAG = "EZ_Launch.ImplHC";
	private ContactWidget mWidget;
	
	public static final String EXTRA_DEFAULT_WIDTH = "EXTRA_DEFAULT_WIDTH";

	public void setWidget(ContactWidget widget) {
		Log.d(TAG, "setting owner widget");
		mWidget = widget;
	}
	
	@SuppressLint("NewApi")
	public void onUpdate(Context context, int appWidgetId, Snapshot snap) {
		Log.d(TAG, "onUpdate called!");
		// Here we setup the intent which points to the StackViewService which will
        // provide the views for this collection.
        Intent intent = new Intent(context, ContactWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		intent.putExtra(EXTRA_DEFAULT_WIDTH, mWidget.getWidth());
		intent.putExtra(StatisticsService.NEW_SNAPSHOT, snap);
		
		// When intents are compared, the extras are ignored, so we need to embed the extras
		// into the data so that the extras will not be ignored.
		intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
		
		final int mainlayout;
		boolean layoutICS = false;
		if (Preferences.getBGImage(context, appWidgetId) == Preferences.BG_ICS) {
			mainlayout = R.layout.main_ics;
			layoutICS = true;
		} else {
			switch(Preferences.getColumnCount(context, appWidgetId)) {
				case 6: mainlayout = R.layout.main_hc6; break;
				case 5: mainlayout = R.layout.main_hc5; break;
				case 4: mainlayout = R.layout.main_hc4; break;
				case 3: mainlayout = R.layout.main_hc3; break;
				case 2: mainlayout = R.layout.main_hc2; break;
				case 1: mainlayout = R.layout.main_hc1; break;
				default: mainlayout = R.layout.main_hc1; break;
			}
		}
					
		RemoteViews rv = new RemoteViews(context.getPackageName(), mainlayout);
		
		String text = Preferences.getDisplayLabel(context, appWidgetId);

        boolean withHeader = text != "";
		if (!layoutICS) {
	        if (Preferences.getBGImage(context, appWidgetId) == Preferences.BG_BLACK) {
	            rv.setImageViewResource(R.id.backgroundImg, withHeader ? R.drawable.background_dark_header : R.drawable.background_dark);
	            rv.setTextColor(R.id.group_caption, Color.WHITE);
	        } else if (Preferences.getBGImage(context, appWidgetId) == Preferences.BG_WHITE) {
	        	rv.setImageViewResource(R.id.backgroundImg, withHeader ? R.drawable.background_light_header : R.drawable.background_light);
	        	rv.setTextColor(R.id.group_caption, Color.BLACK);
	        } else {
	        	rv.setImageViewResource(R.id.backgroundImg, Color.TRANSPARENT);
	            rv.setTextColor(R.id.group_caption, Color.WHITE);
	        }
			rv.setTextViewText(R.id.group_caption, text);
			rv.setViewVisibility(R.id.group_caption, withHeader ? View.VISIBLE : View.GONE);
	     	rv.setInt(R.id.backgroundImg, "setAlpha", Preferences.getBackgroundAlpha(context, appWidgetId));
		}
		
		rv.setRemoteAdapter(R.id.my_gridview, intent);


		// Here we setup the a pending intent template. Individuals items of a collection
		// cannot setup their own pending intents, instead, the collection as a whole can
		// setup a pending intent template, and the individual items can set a fillInIntent
		// to create unique before on an item to item basis.
        Intent clickIntent = new Intent(context, mWidget.getClass());
		clickIntent.setAction(LauncherIntent.Action.ACTION_ITEM_CLICK);
		clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
		PendingIntent clickPendingIntent = PendingIntent.getBroadcast(context, 0, clickIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		rv.setPendingIntentTemplate(R.id.my_gridview, clickPendingIntent);

		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		appWidgetManager.updateAppWidget(appWidgetId, rv);
		appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.my_gridview);
	}
	
	public boolean onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
		if (TextUtils.equals(action, LauncherIntent.Action.ACTION_ITEM_CLICK)) {
			int appWidgetId = intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
			
			Intent launchIntent = intent.getParcelableExtra(com.sadna.data.WidgetItemInfo.LAUNCH_INTENT);
			launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mWidget.onClick(context, appWidgetId, intent.getSourceBounds(), launchIntent);
			return true;
		}
		return false;
	}
}