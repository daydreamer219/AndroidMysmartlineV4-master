/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.mysmartline.appv3;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;

import eu.mysmartline.appv3.models.GcmCurrentNumberChanged;
import eu.mysmartline.appv3.models.GcmMessage;
import eu.mysmartline.appv3.models.GcmTestMessage;
import eu.mysmartline.appv3.models.GcmType;
import eu.mysmartline.appv3.models.MyKeys;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class GcmIntentService extends IntentService {
	public static final int NOTIFICATION_ID = 1;
	private NotificationManager mNotificationManager;
	NotificationCompat.Builder builder;

	public GcmIntentService() {
		super("GcmIntentService");
	}

	public static final String TAG = "mysmartlineV3";

	@Override
	protected void onHandleIntent(Intent intent) {

		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		// The getMessageType() intent parameter must be the intent you received
		// in your BroadcastReceiver.
		String messageType = gcm.getMessageType(intent);

		if (!extras.isEmpty()) { // has effect of unparcelling Bundle
			/*
			 * Filter messages based on message type. Since it is likely that
			 * GCM will be extended in the future with new message types, just
			 * ignore any message types you're not interested in, or that you
			 * don't recognize.
			 */
			if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR
					.equals(messageType)) {
				sendNotification("Send error: " + extras.toString());
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED
					.equals(messageType)) {
				sendNotification("Deleted messages on server: "
						+ extras.toString());
				// If it's a regular GCM message, do some work.
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE
					.equals(messageType)) {
				String rawMessage = extras.getString("data1");
				if (rawMessage != null) {
					/**
					 * This is the place where wee process the message
					 */
					GcmMessage message = decodeMessage(rawMessage);
					processMessage(message);
					 Log.i(TAG,
					 "Message receved in GcmIntentService message content = "
					 + rawMessage);
				} else {
					Log.i(TAG, "No message");
				}

			}
		}
		// Release the wake lock provided by the WakefulBroadcastReceiver.
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

	// Put the message into a notification and post it.
	// This is just one simple example of what you might choose to do with
	// a GCM message.
	private void sendNotification(String msg) {
		mNotificationManager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, MainActivity.class), 0);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this).setContentTitle("GCM Notification")
				.setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
				.setContentText(msg);

		mBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}

	private GcmMessage decodeMessage(String json) {
		Gson gson = new Gson();
		GcmMessage message = gson.fromJson(json, GcmMessage.class);
		return message;
	}

	private void processMessage(GcmMessage message) {

		Log.i(TAG, "Message receved in GcmIntentService message content = " +
		 message);
		String type = message.getType();
		if (type.equals(GcmType.GCM_ACTIVATE_DEVICE)) {
			SharedPreferences.Editor editor = getEditor();
			editor.putBoolean(MyKeys.PROPERTY_DEVICE_ACTIVATED, true);
			editor.commit();

			// open Activity3ListActivityes
			Intent intent = new Intent(GcmBroadcastReceiver.context,
					Activity3ListActivityes.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			return;
		}
		if (type.equals(GcmType.GCM_TEST_MESSAGE)) {
			// extract message
			String jsonObject = message.getJsonObject();
			Gson gson = new Gson();
			GcmTestMessage gcmTestMessage = gson.fromJson(jsonObject,
					GcmTestMessage.class);
			if (gcmTestMessage != null) {
				if (gcmTestMessage.getText() != null) {
					// save message
					SharedPreferences.Editor editor = getEditor();
					editor.putString(MyKeys.PROPERTY_TEST_MESSAGE_VALUE,
							gcmTestMessage.getText());
					editor.commit();
				}
			}
			// open Activiti5
			Intent intent = new Intent(GcmBroadcastReceiver.context,
					Activity5TestActivity1.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			return;
		}
		if (type.equals(GcmType.GCM_LINE_REGISTRATION_RESOURCE_WAS_ACCESSED)) {
			/**
			 * check if current navigation intention is activity2 if corect chec
			 * navigation value if different then navigate to activity 2
			 */
			SharedPreferences prefs = getSharedPreferences(
					MyKeys.MY_SMARTLINE_PREFS, Context.MODE_PRIVATE);
			String navigationIntention = prefs.getString(
					MyKeys.PROPERTY_NAVIGATETO_INTENTION, "no value");
			if (navigationIntention
					.equals(MyKeys.PROPERTY_NAVIGATETO_SHOW_LINES)) {
				String navigationValue = prefs.getString(
						MyKeys.PROPERTY_NAVIGATETO_VALUE, "no value");
				if (!navigationValue
						.equals(MyKeys.PROPERTY_NAVIGATETO_SHOW_LINES)) {
					Intent intent = new Intent(GcmBroadcastReceiver.context,
							Activity2ShowLines.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("push", true);
					startActivity(intent);
				}
			}
			return;
		}
		if (type.equals(GcmType.GCM_CURRENT_NUMBER_CHANGED)) {
			/**
			 * verify if the intention is to navigate to display panel
			 */
			SharedPreferences prefs = getSharedPreferences(
					MyKeys.MY_SMARTLINE_PREFS, Context.MODE_PRIVATE);
			String navigationIntention = prefs.getString(
					MyKeys.PROPERTY_NAVIGATETO_INTENTION, "no value");
			String jsonObject = message.getJsonObject();
			Gson gson = new Gson();
			GcmCurrentNumberChanged gcmCurrentNumberChanged = gson.fromJson(
					jsonObject, GcmCurrentNumberChanged.class);
			String currentNumber = gcmCurrentNumberChanged.getCurrentNumber();
			Log.d(TAG, "Json = " + jsonObject);
			//asign A to B and B to C
			String currentNumberB = prefs.getString(MyKeys.PROPERTY_CURRENT_NUMBER, "");
			String currentNumberC = prefs.getString(MyKeys.PROPERTY_CURRENT_NUMBER_B, "");
			
			SharedPreferences.Editor editor = getEditor();
			editor.putString(MyKeys.PROPERTY_CURRENT_NUMBER, currentNumber);
			editor.putString(MyKeys.PROPERTY_CURRENT_NUMBER_B, currentNumberB);
			editor.putString(MyKeys.PROPERTY_CURRENT_NUMBER_C, currentNumberC);
			editor.commit();
			
			DisplayCounter counter = DisplayCounter.getInstance();
			counter.add(currentNumber);

            try {
                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone r = RingtoneManager.getRingtone(this, notification);
                r.play();
            } catch (Exception e) {
                e.printStackTrace();
            }

			if (navigationIntention.equals(MyKeys.PROPERTY_NAVIGATETO_SHOW_DISPLAY_PANEL)){
				
				
				Intent intent = new Intent(GcmBroadcastReceiver.context,
						Activity7ShowDisplayPanel.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("push", true);
				startActivity(intent);
			}
			return;
		}
		if (type.equals(GcmType.GCM_RESET_DEVICE)) {
			SharedPreferences.Editor editor = getEditor();
			editor.putBoolean(MyKeys.PROPERTY_DEVICE_ACTIVATED, false);
			editor.putString(MyKeys.PROPERTY_CURRENT_NUMBER, ":)");
			editor.putString(MyKeys.PROPERTY_CURRENT_NUMBER_B, ":)");
			editor.putString(MyKeys.PROPERTY_CURRENT_NUMBER_C, ":)");
			editor.commit();
			Intent intent = new Intent(GcmBroadcastReceiver.context, MainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			return;
		}
		if (type.equals(GcmType.GCM_WORM_UP)){
			SharedPreferences.Editor editor = getEditor();
			editor.putBoolean(
					MyKeys.PROPERTY_RECEVED_WARM_UP_MESSAGE_FROM_SERVER, true);
			editor.commit();
		}
	}

	private SharedPreferences.Editor getEditor() {
		SharedPreferences prefs = getSharedPreferences(
				MyKeys.MY_SMARTLINE_PREFS, Context.MODE_PRIVATE);
		return prefs.edit();
	}
}
