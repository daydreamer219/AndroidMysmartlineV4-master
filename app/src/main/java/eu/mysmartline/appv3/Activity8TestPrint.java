package eu.mysmartline.appv3;

import com.google.gson.Gson;

import eu.mysmartline.appv3.models.ApiPrintNumberRequest;
import eu.mysmartline.appv3.models.MyKeys;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;

public class Activity8TestPrint extends Activity {
	// printing parameters
	private WebView mWebView;
	private String TAG = "mysmartlineV3";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_activity8_test_print);
		// Show the Up button in the action bar.
		setupActionBar();
		Log.d(TAG, "debug: initial debug message ");
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity8_test_print, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void printSomething(View view) {
		doWebViewPrint();
	}

	private void doWebViewPrint() {
		// Create a WebView object specifically for printing
		WebView webView = new WebView(this);
		webView.setWebViewClient(new WebViewClient() {

			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				return false;
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				Log.i(TAG, "debug: page finished loading " + url);
				createWebPrintJob(view);
				mWebView = null;
			}
		});
		
		String generatePrintNumber = MyKeys.PROPERTY_HOME+"lineRegistration/generatePrintNumber/";
		String webAddress = generatePrintNumber;
		String validRequest = generatePrintNumberRequest();
		webView.loadUrl(webAddress + validRequest);

		// Keep a reference to WebView object until you pass the
		// PrintDocumentAdapter
		// to the PrintManager
		mWebView = webView;
	}

	private void createWebPrintJob(WebView webView) {
		
		// Get a PrintManager instance
		// Get a PrintManager instance
		PrintManager printManager = (PrintManager) this
				.getSystemService(Context.PRINT_SERVICE);

		// Get a print adapter instance
		PrintDocumentAdapter printAdapter = webView
				.createPrintDocumentAdapter();

		// Create a print job with name and adapter instance
		String jobName = getString(R.string.app_name) + " Document";
		@SuppressWarnings("unused")
		PrintJob printJob = printManager.print(jobName, printAdapter,
				new PrintAttributes.Builder().build());

	}

	private String generatePrintNumberRequest() {
		SharedPreferences prefs = getSharedPreferences(
				MyKeys.MY_SMARTLINE_PREFS, Context.MODE_PRIVATE);
		String deviceId = prefs.getString(MyKeys.PROPERTY_REG_ID, "no value");
		String lineId = "1";
		
		ApiPrintNumberRequest apiPrintNumberRequest = new ApiPrintNumberRequest();
		apiPrintNumberRequest.setLineId(lineId);
		apiPrintNumberRequest.setPropertyRegId(deviceId);
		
		Gson gson = new Gson();
		
		
		return gson.toJson(apiPrintNumberRequest);
	}
}
