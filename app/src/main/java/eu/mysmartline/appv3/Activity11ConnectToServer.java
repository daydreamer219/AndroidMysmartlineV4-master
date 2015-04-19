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
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import eu.mysmartline.appv3.models.ApiActivateValidateRequest;
import eu.mysmartline.appv3.models.ApiActivateValidateResponce;
import eu.mysmartline.appv3.models.GcmWarmUp;
import eu.mysmartline.appv3.models.MyKeys;

public class Activity11ConnectToServer extends Activity {
	String MyTag = MyKeys.PROPERTY_LOGTAG;
	String email;
	String gcmRegId;
	String activateValidateUrl;
	String sendWarmUpUrl;
	String shortAndLongId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_activity11_connect_to_server);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}

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
			SharedPreferences.Editor editor = getEditor();
			editor.putBoolean(
					MyKeys.PROPERTY_RECEVED_WARM_UP_MESSAGE_FROM_SERVER, false);
			editor.commit();
			// Start the ActivateValidate Task
			new ActivateValidate()
					.execute(new String[] { activateValidateUrl });
		}
		// </account>
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity11_connect_to_server, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(
					R.layout.fragment_activity11_connect_to_server, container,
					false);
			return rootView;
		}
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
				Log.d(MyTag,"responce from server "+ stringBuilder.toString());
			}

			try {
				Thread.sleep(20000);
				if (!warmUpReceved()) {
					Log.d(MyTag, "Warm up not receved. Wait and send again");
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
			} else {
				//send again
				new SendWarmUpMessages().executeOnExecutor(
						AsyncTask.THREAD_POOL_EXECUTOR,
						new String[] { sendWarmUpUrl });
			}
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
			ApiActivateValidateResponce responce = decodeResponseFromServer(result);
			if (responce == null) {
				Toast.makeText(context, "Server down. Please try later",
						duration).show();
			} else {
				if (!responce.isSuccesfull()) {
					Toast.makeText(
							context,
							"---> Not able to activate <---\n"
									+ responce.getDetailsAboutFailure(),
							duration).show();

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

	private SharedPreferences.Editor getEditor() {
		SharedPreferences prefs = getSharedPreferences(
				MyKeys.MY_SMARTLINE_PREFS, Context.MODE_PRIVATE);
		return prefs.edit();
	}

	private ApiActivateValidateResponce decodeResponseFromServer(String response) {
		Gson gson = new Gson();
		try {
			ApiActivateValidateResponce responce = gson.fromJson(response,
					ApiActivateValidateResponce.class);
			return responce;
		} catch (JsonParseException e) {
			return null;
		}
	}

	@SuppressWarnings("unused")
	private void navigateToListActivities() {
		Intent intent = new Intent(this, Activity3ListActivityes.class);
		startActivity(intent);
	}

	private boolean warmUpReceved() {
		SharedPreferences prefs = getSharedPreferences(
				MyKeys.MY_SMARTLINE_PREFS, Context.MODE_PRIVATE);
		boolean state = prefs.getBoolean(
				MyKeys.PROPERTY_RECEVED_WARM_UP_MESSAGE_FROM_SERVER, false);
		Log.d(MyTag, "Warm up check = " + state);
		return state;
	}
}
