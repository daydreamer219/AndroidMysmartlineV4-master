package eu.mysmartline.appv3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.gc.materialdesign.views.ProgressBarCircularIndeterminate;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;


import eu.mysmartline.appv3.models.ApiActivateValidateRequest;
import eu.mysmartline.appv3.models.ApiActivateValidateResponce;
import eu.mysmartline.appv3.models.DeviceRegistrationResponceModel;
import eu.mysmartline.appv3.models.GcmWarmUp;
import eu.mysmartline.appv3.models.MyKeys;

public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks{
	/**
	 * This is the project number from the API Console, as described in
	 * "Getting Started."
	 */
	String SENDER_ID = MyKeys.PROPERTY_GCM_PROJECT_NUMBER;

	public static final String MY_SMARTLINE_PREFS = "MY_SMARTLINE_PREFS";
//	public static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	TextView statusView;
	Context context;
	GoogleCloudMessaging gcm;
	String gcmRegid;

	// Tags use on log messages
	static final String TAG = "mysmartlineV3";


    private NavigationDrawerFragment mNavigationDrawerFragment;

	@SuppressLint("Wakelock")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// get the views that will have content modified
		statusView = (TextView) findViewById(R.id.textView1);
		context = getApplicationContext();
		
		//SharedPreferences prefs = getGcmPreferences();
		
		//use wake lock
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		@SuppressWarnings("deprecation")
		PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "My tag");
		wl.acquire();
		
		
		/*boolean deviceActivated;
		deviceActivated = prefs.getBoolean(MyKeys.PROPERTY_DEVICE_ACTIVATED, false);*/
		/**
		 *The follwoing code is part of the original
		 */
		/*if (deviceActivated)
		{
			*//**
			 *
			 * If device is active then open the Activity3ListActivityes
			 *//*
			Intent intent = new Intent (this, Activity3ListActivityes.class);
			startActivity(intent);
			return;
		}*/
		// check device for Play Services APK
        actionbarInit();
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
        startLogin();



	}


    private void startLogin(){
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            gcmRegid = getRegistrationId(context);
            if (gcmRegid.isEmpty()) {
                SharedPreferences.Editor editor = getEditor();
                editor.putBoolean(
                        MyKeys.PROPERTY_RECEVED_WARM_UP_MESSAGE_FROM_SERVER, false);
                editor.commit();
                showStatus("Get gcm register id...");
                registerInBackground();
            }
            else{
                startConnectToServer();
            }
        } else {
            alertError("No valid Google Play Services APK found.");

        }
    }

    private void actionbarInit(){
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        LayoutInflater mInflater = LayoutInflater.from(context);

        View mCustomView = mInflater.inflate(R.layout.action_bar_main, null);
        actionBar.setCustomView(mCustomView, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.defaultThemeColor)));
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/**
	 * Check the device to make sure it has the Google Play Services APK. If it
	 * doesn't, display a dialog that allows users to download the APK from the
	 * Google Play Store or enable it in the device's system settings.
	 */
	private boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this,
						PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				Log.i(TAG, "This device is not supported.");
				finish();
			}
			return false;
		}
		return true;
	}

	/**
	 * Gets the current registration ID for application on GCM service, if there
	 * is one.
	 * <p>
	 * If result is empty, the app needs to register.
	 *
	 * @return registration ID, or empty string if there is no existing
	 *         registration ID.
	 */
	private String getRegistrationId(Context context) {
		SharedPreferences prefs = getGcmPreferences();
		String registrationId = prefs.getString(MyKeys.PROPERTY_REG_ID, "");
		if (registrationId.isEmpty()) {
			return "";
		}
		// Check if app was updated; if so, it must clear the registration ID
		// since the existing regID is not guaranteed to work with the new
		// app version.
		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION,
				Integer.MIN_VALUE);
		int currentVersion = getAppVersion(context);
		if (registeredVersion != currentVersion) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(MyKeys.PROPERTY_REG_ID, "");
            editor.putInt(PROPERTY_APP_VERSION, currentVersion);
            editor.commit();
			return "";
		}
		return registrationId;
	}

	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			// should never happen
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	/**
	 * @return Application's {@code SharedPreferences}.
	 */
	private SharedPreferences getGcmPreferences() {
		// This sample app persists the registration ID in shared preferences,
		// but
		// how you store the regID in your app is up to you.

		return getSharedPreferences(MY_SMARTLINE_PREFS, Context.MODE_PRIVATE);
	}

	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p>
	 * Stores the registration ID and the app versionCode in the application's
	 * shared preferences.
	 */
	private void registerInBackground() {
		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				String msg = "";
				try {
					if (gcm == null) {
						gcm = GoogleCloudMessaging.getInstance(context);
					}
					gcmRegid = gcm.register(SENDER_ID);
					msg = "Device registered, registration ID=" + gcmRegid;

					// You should send the registration ID to your server over
					// HTTP, so it
					// can use GCM/HTTP or CCS to send messages to your app.


				} catch (IOException ex) {
					msg = "Error :" + ex.getMessage();
					// If there is an error, don't just keep trying to register.
					// Require the user to click a button again, or perform
					// exponential back-off.
				}
				return msg;
			}

			@Override
			protected void onPostExecute(String msg) {
				/**
				 */
                if (gcmRegid == null || gcmRegid.equals("")){
                    alertError(msg);
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                }else {
                    sendRegistrationIdToBackend();
                }
			}
		}.execute(null, null, null);
	}

	/**
	 * Sends the registration ID to your server over HTTP, so it can use
	 * GCM/HTTP or CCS to send messages to your app.
	 */

	private void sendRegistrationIdToBackend() {
		DownloadWebPageTask accesWeb = new DownloadWebPageTask();
		String registrationUrl = MyKeys.PROPERTY_HOME + "DeviceEntryPoint/registerPost";
		accesWeb.execute(new String[] { registrationUrl });
	}

    @Override
    public void onNavigationDrawerItemSelected(int position) {

    }

    // http://www.mysmartline.eu/deviceEntryPoint/registerPost
	private class DownloadWebPageTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {
			String response = "";

			for (String url : urls) {
				DefaultHttpClient client = new DefaultHttpClient();
				Log.d("mysmartlineV3", "bogdan post url = " + url);
				HttpPost httppost = new HttpPost(url);
				try {
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
							1);
					nameValuePairs.add(new BasicNameValuePair("gcmRegId",
							gcmRegid));
					httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

					HttpResponse execute = client.execute(httppost);
					InputStream content = execute.getEntity().getContent();

					BufferedReader buffer = new BufferedReader(
							new InputStreamReader(content));
					String s = "";
					while ((s = buffer.readLine()) != null) {
						response += s;
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return response;
		}

		@Override
		protected void onPostExecute(String result) {

			/**
			 * decodeJsonAndShowContent(result); send the result to activation
			 * view
			 * */
			Log.d("mysmartlineV3", "bogdan result from server: " + result);

			DeviceRegistrationResponceModel regModel = decodeResponseFromServer(result);
			if (regModel.isDatabaseError()) {
				alertError("Back end eror");
			}
			// Log.i(TAG, "start save data");
			if (!regModel.isActive() && gcmRegid != null) {
				statusView
						.setText("Registered with the back end. \nShort id = "
								+ regModel.getShortId());
				SharedPreferences prefs = getGcmPreferences();
				SharedPreferences.Editor editor = prefs.edit();
				editor.putString(MyKeys.PROPERTY_REG_ID, gcmRegid);
				editor.putString(MyKeys.PROPERTY_CLOUD_ACTIVATION_ID,
						regModel.getShortId());
				editor.commit();
				statusView.setText(statusView.getText()
						+ "\n"
						+ "Result from the server = "
						+ result
						+ "\n short id = "
						+ prefs.getString("PROPERTY_CLOUD_ACTIVATION_ID",
								"note served this is an error")
						+ "\nYou should redirect to actiation view");
				//startActivateActivity();
				startConnectToServer();
				// Log.i(TAG, "end save data");
			}else{
                alertError("Back end eror");
                findViewById(R.id.progressBar).setVisibility(View.GONE);
            }
		}
	}

	@SuppressWarnings("unused")
	private void startActivateActivity() {
		Intent intent = new Intent(this, Activity4ActivateDevice.class);
		startActivity(intent);
	}

	private DeviceRegistrationResponceModel decodeResponseFromServer(
			String response) {
		Gson gson = new Gson();
		DeviceRegistrationResponceModel model = gson.fromJson(response,
				DeviceRegistrationResponceModel.class);
		return model;
	}



    String email;
    String activateValidateUrl;
    String sendWarmUpUrl;
    String shortAndLongId;
    String gcmRegId;
	public void startConnectToServer(){
        statusView.setText("Get account...");
        // <account>
        AccountManager accountManager = AccountManager.get(this);
        Account[] accounts = accountManager.getAccounts();
        accountManager.getAccountsByType("com.google");
        List<Account> sellectedAccounts = new ArrayList<Account>();
        Pattern emailPattern = Patterns.EMAIL_ADDRESS;
        for (int i = 0; i < accounts.length; i++) {
            Account account = accounts[i];
            if (account.type.contains("com.google")) {
                if (emailPattern.matcher(account.name).matches()) {
                    sellectedAccounts.add(account);
                }
            }
        }
        if (sellectedAccounts.size() > 0) {
            email = sellectedAccounts.get(1).name;
            activateValidateUrl = MyKeys.PROPERTY_HOME
                    + "Api/activateValidateDevice";
            sendWarmUpUrl = MyKeys.PROPERTY_HOME + "Api/warmUpDevice";
            // set the gcmRegId
            SharedPreferences prefs = getSharedPreferences(
                    MyKeys.MY_SMARTLINE_PREFS, Context.MODE_PRIVATE);
            gcmRegId = prefs.getString(MyKeys.PROPERTY_REG_ID, "no value");
            shortAndLongId = prefs.getString(
                    MyKeys.PROPERTY_CLOUD_ACTIVATION_ID, "no value");

            // set a global parameter for RecevedTestMessageFromServer = false;
            SharedPreferences pref =  getSharedPreferences(
                    MyKeys.MY_SMARTLINE_PREFS, Context.MODE_PRIVATE);
            boolean isActiviated = pref.getBoolean(MyKeys.PROPERTY_RECEVED_WARM_UP_MESSAGE_FROM_SERVER, false);

            if (!isActiviated) {
                // Start the ActivateValidate Task
                showStatus("Activating account...");
                new ActivateValidate()
                        .execute(new String[]{activateValidateUrl});
            }else{
                navigateToListActivities();
            }
        }
	}

    private SharedPreferences.Editor getEditor() {
        SharedPreferences prefs = getSharedPreferences(
                MyKeys.MY_SMARTLINE_PREFS, Context.MODE_PRIVATE);
        return prefs.edit();
    }

    private ApiActivateValidateResponce decodeResponseFromServerForActiviate(String response) {
        Gson gson = new Gson();
        try {
            ApiActivateValidateResponce responce = gson.fromJson(response,
                    ApiActivateValidateResponce.class);
            return responce;
        } catch (JsonParseException e) {
            return null;
        }
    }
    private class ActivateValidate extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            ApiActivateValidateRequest apiRequest = new ApiActivateValidateRequest();
            apiRequest.setEmail(email);
            apiRequest.setGcmRegId(gcmRegId);
            apiRequest.setShortAndLongId(shortAndLongId);
            for (String url : urls) {
                DefaultHttpClient client = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(url);
                try {
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
                            1);
					/*
					 * nameValuePairs.add(new BasicNameValuePair("gcmRegId",
					 * gcmRegid));
					 */
                    nameValuePairs.add(new BasicNameValuePair("email",
                            apiRequest.getEmail()));
                    nameValuePairs.add(new BasicNameValuePair("gcmRegId",
                            apiRequest.getGcmRegId()));
                    nameValuePairs.add(new BasicNameValuePair("shortAndLongId",
                            apiRequest.getShortAndLongId()));

                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    HttpResponse execute = client.execute(httppost);
                    InputStream content = execute.getEntity().getContent();

                    BufferedReader buffer = new BufferedReader(
                            new InputStreamReader(content));
                    String s = "";
                    while ((s = buffer.readLine()) != null) {
                        response += s;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            int duration = Toast.LENGTH_LONG;
            Context context = getApplicationContext();
            ApiActivateValidateResponce responce = decodeResponseFromServerForActiviate(result);
            if (responce == null) {
                alertError("Server down. Please try later");
                findViewById(R.id.progressBar).setVisibility(View.GONE);
            } else {
                if (!responce.isSuccesfull()) {
                    alertError("---> Not able to activate <---\n"
                            + responce.getDetailsAboutFailure());
                    findViewById(R.id.progressBar).setVisibility(View.GONE);

                } else {

					/*
					 * String content = "Device is active You should " +
					 * "You should start initiating periodic " +
					 * "Google Cloud Messages"; Toast.makeText(context,
					 * "---> Server Up <---\n" + content, duration).show();
					 */
                    // Try to connect to the server every 60 seconds.
                    new SendWarmUpMessages().executeOnExecutor(
                            AsyncTask.THREAD_POOL_EXECUTOR,
                            new String[] { sendWarmUpUrl });

                }
            }
        }
    }

    public void alertError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void alertSuccess() {
        Toast.makeText(this, R.string.alert_title_success, Toast.LENGTH_SHORT).show();

    }

    private class SendWarmUpMessages extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            StringBuilder stringBuilder = new StringBuilder();
            // create GcmWarmUp as request
            GcmWarmUp gcmWarmUp = new GcmWarmUp();
            gcmWarmUp.setDeviceShortLongId(shortAndLongId);
            for (String url : urls) {
                DefaultHttpClient client = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(url);
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
                        1);
                nameValuePairs.add(new BasicNameValuePair("deviceShortLongId", gcmWarmUp
                        .getDeviceShortLongId()));

                try {
                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    HttpResponse execute = client.execute(httppost);
                    InputStream content = execute.getEntity().getContent();

                    BufferedReader buffer = new BufferedReader(
                            new InputStreamReader(content));
                    String s = "";
                    while ((s = buffer.readLine()) != null) {
                        stringBuilder.append(s);
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            try {
                Thread.sleep(20000);
                if (!warmUpReceved()) {
                    Thread.sleep(50000);
                    if (!warmUpReceved()){
                        Thread.sleep(120000);
                    }
                }

            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();

            }
            return stringBuilder.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            if (warmUpReceved()) {
                navigateToListActivities();
                findViewById(R.id.textView2).setOnClickListener(new View.OnClickListener(){

                    @Override
                    public void onClick(View view) {

                    }
                });
            } else {
                //send again
                new SendWarmUpMessages().executeOnExecutor(
                        AsyncTask.THREAD_POOL_EXECUTOR,
                        new String[]{sendWarmUpUrl});
            }
        }
    }

    private boolean warmUpReceved() {
        SharedPreferences prefs = getSharedPreferences(
                MyKeys.MY_SMARTLINE_PREFS, Context.MODE_PRIVATE);
        boolean state = prefs.getBoolean(
                MyKeys.PROPERTY_RECEVED_WARM_UP_MESSAGE_FROM_SERVER, false);
        return state;
    }

    private void showStatus(String msg){
        statusView.setText(msg);
    }

    private void navigateToListActivities(){
        ProgressBarCircularIndeterminate progressBar = (ProgressBarCircularIndeterminate)findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        showStatus("");
        ((TextView)findViewById(R.id.textView2)).setText(R.string.login_success);
//        alertSuccess();
        mNavigationDrawerFragment.loadListData();
    }
}
