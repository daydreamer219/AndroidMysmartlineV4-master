package eu.mysmartline.appv3;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.epson.eposprint.Builder;
import com.epson.eposprint.Print;
import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import eu.mysmartline.appv3.models.ApiPrintNumberRequest;
import eu.mysmartline.appv3.models.ApiPrintNumberResponce;
import eu.mysmartline.appv3.models.LineDetailsModel;
import eu.mysmartline.appv3.models.MyKeys;

public class Activity6ShowLineDetails extends ActionBarActivity {
	private ImageView imageQRCode;
	private LineDetailsModel lineDetails;
	private Bitmap image;
	private SleepAndThenReturn sleepAndThenReturn;
	@SuppressWarnings("unused")
	private WebView mWebView;

	@SuppressWarnings("unused")
	private String logTag = MyKeys.PROPERTY_LOGTAG;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_activity6_show_line_details);
        lineDetails = (LineDetailsModel) getIntent().getSerializableExtra(
                "LineDetailsModel");
        actionbarInit();
		TextView textView1 = (TextView) findViewById(R.id.textView1);

		textView1.setText( getResources().getString(R.string.activiti6_to_get_in_line_please_scan));
		imageQRCode = (ImageView) findViewById(R.id.imageView1);
		ShowQrCode showQrCode = new ShowQrCode();
		showQrCode.execute(new String[] {});
		
		togleButon();

		sleepAndThenReturn = new SleepAndThenReturn();
		sleepAndThenReturn.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

    private void actionbarInit() {
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.defaultThemeColor)));
        getSupportActionBar().setTitle(lineDetails.getName());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

	private void togleButon(){
		SharedPreferences preferences = getSharedPreferences(
				MyKeys.MY_SMARTLINE_PREFS, Context.MODE_PRIVATE);
		String service = preferences.getString(MyKeys.PROPERTY_PRINTER_SERVICE,
				"no_service");
		Button button = (Button) findViewById(R.id.button1);
		TextView textView = (TextView)findViewById(R.id.textView3);
		if (service.equals("no print service")){
			button.setVisibility(View.GONE);
			textView.setVisibility(View.GONE);
		}else{
			button.setVisibility(View.VISIBLE);
			textView.setVisibility(View.VISIBLE);
		}
		
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity6_show_line_details, menu);
		return true;
	}

	private class ShowQrCode extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			try {
				image = generateQRCode(lineDetails.getRegistrationUrl());
			} catch (WriterException e) {
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			if (image != null) {
				imageQRCode.setImageBitmap(image);
			}
		}
	}

	private Bitmap generateQRCode(String url) throws WriterException {
		QRCodeWriter myQRwriter = new QRCodeWriter();
		BitMatrix bitMatrix = myQRwriter.encode(url, BarcodeFormat.QR_CODE,
				300, 300);
		int matrixWidth = bitMatrix.getWidth();
		Bitmap bmp = Bitmap.createBitmap(matrixWidth, matrixWidth,
				Config.ARGB_8888);
		for (int i = 0; i < matrixWidth; i++) {
			for (int j = 0; j < matrixWidth; j++) {
				bmp.setPixel(i, j, bitMatrix.get(i, j) ? Color.BLACK
						: Color.WHITE);
			}
		}
		return bmp;
	}

	private class PrintEpsonAsyncTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {

			ApiPrintNumberResponce printResponce = generateNumber();
			// if no number just return and do nothing
			if (printResponce == null) {
				return null;
			}
			printToEpson(printResponce);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			setNavigationAndNavigate();
		}
	}

	private void printToEpson(ApiPrintNumberResponce printResponce) {
		SharedPreferences preferences = getSharedPreferences(
				MyKeys.MY_SMARTLINE_PREFS, Context.MODE_PRIVATE);
		String printerName = preferences.getString(MyKeys.PROPERTY_PRINTER_NAME,
				"no printer");
		String printerIp = preferences.getString(MyKeys.PROPERTY_PRINTER_IP,
				"no ip");
		
		try {
		// Initialize a Builder class instance
		Builder builder = new Builder(printerName,
				Builder.MODEL_ANK);
		// Create a print document
		// <Configure the print character settings>
		builder.addTextLang(Builder.LANG_EN);
		builder.addTextSmooth(Builder.TRUE);
		builder.addTextFont(Builder.FONT_A);
		builder.addTextSize(1, 1);
		builder.addTextStyle(Builder.FALSE, Builder.FALSE,
				Builder.TRUE, Builder.PARAM_UNSPECIFIED);
		// <Specify the print data>
		builder.addText(printResponce.getLineName()+"\n\t");
		builder.addTextSize(2, 2);
		builder.addText(printResponce.getClientNumber()+"\n\t");
		builder.addTextSize(1, 1);

		// builder.addSymbol(data, type, level, width, height,
		// size)

		builder.addSymbol(
				printResponce.getQrText(),
				Builder.SYMBOL_QRCODE_MODEL_2, Builder.LEVEL_L,
				5, Builder.PARAM_UNSPECIFIED,
				Builder.PARAM_UNSPECIFIED);
		builder.addText("You can convert this ticket into a virtual one by scanning the QR code\n");
		builder.addTextSize(2, 2);
		builder.addText("mysmartline.eu");
		builder.addFeedLine(1);
		builder.addCut(Builder.CUT_FEED);

		// Send a print document
		// <Start communication with the printer>
		Print printer = new Print();
		
		printer.openPrinter(Print.DEVTYPE_TCP, printerIp);
		// <Send data>
		int[] status = new int[1];
		status[0] = 0;
		printer.sendData(builder, 10000, status);
		// <End communication with the printer>
		printer.closePrinter();

		}catch (Exception e){
			
		}
	}

	/**
	 * connect to the server and generate a ticket
	 * 
	 * @return {@link ApiPrintNumberResponce}
	 */
	private ApiPrintNumberResponce generateNumber() {
		ApiPrintNumberResponce printResponce = null;
		try {
			String response = "";
			String encodedRequest = URLEncoder.encode(
					generatePrintNumberRequest(), "UTF-8");
			String websiteUrl = MyKeys.PROPERTY_HOME
					+ "Api/generatePrintNumberV2/" + encodedRequest;
			DefaultHttpClient client = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(websiteUrl);

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);

			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			HttpResponse execute = client.execute(httppost);
			InputStream content = execute.getEntity().getContent();

			BufferedReader buffer = new BufferedReader(new InputStreamReader(
					content));
			String s = "";
			while ((s = buffer.readLine()) != null) {
				response += s;
			}

			// decode json
			Gson gson = new Gson();
			printResponce = gson.fromJson(response,
					ApiPrintNumberResponce.class);
			// print the printResponce

		} catch (Exception e) {
			Writer writer = new StringWriter();
			PrintWriter printWriter = new PrintWriter(writer);
			e.printStackTrace(printWriter);
			@SuppressWarnings("unused")
			String myError = writer.toString();
		}
		return printResponce;
	}

	private class SleepAndThenReturn extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {

			try {
				Thread.sleep(20000);
			} catch (InterruptedException e) {
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			setNavigationAndNavigate();
		}
	}

	private void setNavigationAndNavigate() {
		/**
		 * check if current navigation intention is activity2 if corect chec
		 * navigation value if different then navigate to activity 2
		 */
		SharedPreferences prefs = getSharedPreferences(
				MyKeys.MY_SMARTLINE_PREFS, Context.MODE_PRIVATE);
		String navigationIntention = prefs.getString(
				MyKeys.PROPERTY_NAVIGATETO_INTENTION, "no value");
		if (navigationIntention.equals(MyKeys.PROPERTY_NAVIGATETO_SHOW_LINES)) {
			String navigationValue = prefs.getString(
					MyKeys.PROPERTY_NAVIGATETO_VALUE, "no value");
			if (!navigationValue.equals(MyKeys.PROPERTY_NAVIGATETO_SHOW_LINES)) {
                /*
				Intent intent = new Intent(Activity6ShowLineDetails.this,
						Activity2ShowLines.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
				*/
                finish();
			}
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		sleepAndThenReturn.cancel(true);
	}

	public void printTicket(View view) {
 
		SharedPreferences preferences = getSharedPreferences(
				MyKeys.MY_SMARTLINE_PREFS, Context.MODE_PRIVATE);
		String service = preferences.getString(MyKeys.PROPERTY_PRINTER_SERVICE,
				"no_service");
		// if not epson just return and do nothing
		if (service.equals("epson")) {
			new PrintEpsonAsyncTask()
					.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
		//TODO fix this because I'm missing google cloud print
		if (service.equals("googleCloud")){
			doWebViewPrint();
		}
		if (service.equals("no print service")){
			setNavigationAndNavigate();
		}
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
				createWebPrintJob(view);
				mWebView = null;
			}
		});

		String generatePrintNumber = MyKeys.PROPERTY_HOME
				+ "LineRegistration/generatePrintNumber/";
		String webAddress = generatePrintNumber;
		String validRequest = generatePrintNumberRequest();
		webView.loadUrl(webAddress + validRequest);

		// Keep a reference to WebView object until you pass the
		// PrintDocumentAdapter
		// to the PrintManager
		mWebView = webView;
	}

	private void createWebPrintJob(final WebView webView) {

		// Get a PrintManager instance
		// Get a PrintManager instance
		PrintManager printManager = (PrintManager) this
				.getSystemService(Context.PRINT_SERVICE);

		// Get a print adapter instance
		/*
		 * PrintDocumentAdapter printAdapter = webView
		 * .createPrintDocumentAdapter();
		 */

		// my experiment
		PrintDocumentAdapter printAdapter = new PrintDocumentAdapter() {
			private final PrintDocumentAdapter mWrappedInstance = webView
					.createPrintDocumentAdapter();

			@Override
			public void onLayout(PrintAttributes oldAttributes,
					PrintAttributes newAttributes,
					CancellationSignal cancellationSignal,
					LayoutResultCallback callback, Bundle extras) {
				// TODO Auto-generated method stub
				mWrappedInstance.onLayout(oldAttributes, newAttributes,
						cancellationSignal, callback, extras);
			}

			@Override
			public void onWrite(PageRange[] pages,
					ParcelFileDescriptor destination,
					CancellationSignal cancellationSignal,
					WriteResultCallback callback) {
				// TODO Auto-generated method stub
				mWrappedInstance.onWrite(pages, destination,
						cancellationSignal, callback);
			}

			@Override
			public void onFinish() {
				mWrappedInstance.onFinish();
				navigateToShowLines();
			}
		};

		// Create a print job with name and adapter instance
		String jobName = lineDetails.getName()
				+ lineDetails.getNextProbableNumber();
		@SuppressWarnings("unused")
		PrintJob printJob = printManager.print(jobName, printAdapter,
				new PrintAttributes.Builder().build());

	}

	private String generatePrintNumberRequest() {
		SharedPreferences prefs = getSharedPreferences(
				MyKeys.MY_SMARTLINE_PREFS, Context.MODE_PRIVATE);
		String deviceId = prefs.getString(MyKeys.PROPERTY_REG_ID, "no value");
		String lineId = lineDetails.getId().toString();

		ApiPrintNumberRequest apiPrintNumberRequest = new ApiPrintNumberRequest();
		apiPrintNumberRequest.setLineId(lineId);
		apiPrintNumberRequest.setPropertyRegId(deviceId);

		Gson gson = new Gson();

		return gson.toJson(apiPrintNumberRequest);
	}

	private void navigateToShowLines() {
		Intent intent = new Intent();
		intent.setClass(Activity6ShowLineDetails.this, Activity2ShowLines.class);
		startActivity(intent);
	}
}
