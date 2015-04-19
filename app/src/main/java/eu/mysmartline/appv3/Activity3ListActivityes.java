package eu.mysmartline.appv3;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.google.gson.Gson;

import eu.mysmartline.appv3.models.MyKeys;

/**
 * TODO Refactor list show it shows also icons.
 * This is the main screen
 * minor change.
 */
public class Activity3ListActivityes extends Activity {
	private List<Map<String, String>> items = new ArrayList<Map<String, String>>();
	private String logTag = MyKeys.PROPERTY_LOGTAG;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_activity3_list_activityes);

		initList();
		ListView listView = (ListView) findViewById(R.id.listView);

		// React to user clicks on items
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parentAdapter, View view,
					int position, long id) {
				int myId = (int) id;
				Intent intent = new Intent();
				
				switch (myId) {
				case 0:
					intent.setClass(Activity3ListActivityes.this,
							Activity2ShowLines.class);
					startActivity(intent);
					break;
				case 1:
					deleteDeviceFromBackend();
					break;
				case 2:
					intent.setClass(Activity3ListActivityes.this,
							Activity7ShowDisplayPanel.class);
					startActivity(intent);
					break;
				case 3:
					intent.setClass(Activity3ListActivityes.this,
							Activity9ConfigurePrinter.class);
					startActivity(intent);
					/*
					 * case 3: intent.setClass(Activity3ListActivityes.this,
					 * Activity8TestPrint.class); startActivity(intent); break;
					 */
				}
			}
		});

		SimpleAdapter simpleAdpt = new SimpleAdapter(this, items,
				android.R.layout.simple_list_item_1, new String[] { "item" },
				new int[] { android.R.id.text1 });
		listView.setAdapter(simpleAdpt);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity3_list_activityes, menu);
		return true;
	}

	private HashMap<String, String> createItem(String key, String name) {
		HashMap<String, String> item = new HashMap<String, String>();
		item.put(key, name);
		return item;
	}

	private void initList() {
		String[] myMenu = getResources().getStringArray(
				R.array.activity3_nenu_items);
		items.add(createItem("item", myMenu[0]));//Show all lines
		items.add(createItem("item", myMenu[1]));//Reset device
		items.add(createItem("item", myMenu[2]));//Show display panel
		items.add(createItem("item", myMenu[3]));//configure printer
		/*
		 * items.add(createItem("item",
		 * "Menu 3: Test andoroid printing system(only for Bogdan)"));
		 */
	}

	private void deleteDeviceFromBackend() {
		DownloadWebPageTask accesWeb = new DownloadWebPageTask();
		SharedPreferences prefs = getSharedPreferences(
				MyKeys.MY_SMARTLINE_PREFS, Context.MODE_PRIVATE);
		String gcmRegID = prefs.getString(MyKeys.PROPERTY_REG_ID, "no id");
		String resetDeviceUrl = MyKeys.PROPERTY_HOME + "Api/resetDevice/";
		Log.i(logTag, resetDeviceUrl + gcmRegID);
		accesWeb.execute(new String[] { resetDeviceUrl + gcmRegID });
	}

	// http://www.mysmartline.eu/deviceEntryPoint/registerPost
	private class DownloadWebPageTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {
			String response = "";
			Log.i(logTag, "do in background start");
			for (String url : urls) {
				DefaultHttpClient client = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(url);
				try {
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
							1);

					httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

					HttpResponse execute = client.execute(httppost);
					InputStream content = execute.getEntity().getContent();

					BufferedReader buffer = new BufferedReader(
							new InputStreamReader(content));
					String s = "";
					while ((s = buffer.readLine()) != null) {
						response += s;
						Log.i(logTag, "s = " + s);
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
			 * TODO: decode response and then go to main menu decode response
			 */
			Gson gson = new Gson();
			Boolean responce = gson.fromJson(result, Boolean.class);
			Log.d("mysmartlineV3", "responce = " + responce.toString());
			Log.i(logTag, "responce receved = " + responce);
			if (responce == true) {
				// reset local variables
				SharedPreferences prefs = getSharedPreferences(
						MyKeys.MY_SMARTLINE_PREFS, Context.MODE_PRIVATE);
				SharedPreferences.Editor editor = prefs.edit();
				editor.putBoolean(MyKeys.PROPERTY_DEVICE_ACTIVATED, false);
				editor.putString(MyKeys.PROPERTY_CURRENT_NUMBER, ":)");
				editor.putString(MyKeys.PROPERTY_CURRENT_NUMBER_B, ":)");
				editor.putString(MyKeys.PROPERTY_CURRENT_NUMBER_C, ":)");
				editor.commit();
			}

			// open MainActivity
			Intent intent = new Intent();
			intent.setClass(Activity3ListActivityes.this, MainActivity.class);
			startActivity(intent);
		}
	}

}
