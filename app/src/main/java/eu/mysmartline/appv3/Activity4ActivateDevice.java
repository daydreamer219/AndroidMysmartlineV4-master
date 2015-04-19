package eu.mysmartline.appv3;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import eu.mysmartline.appv3.models.MyKeys;

public class Activity4ActivateDevice extends Activity {

	ImageView viewShortIdQRCode;
	Bitmap bitmpQRCode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_activity4_activate_device);
		
		viewShortIdQRCode = (ImageView) findViewById(R.id.imageView1);
		String shortId = getShortId();
		AsynchronousQrCompute asynchronousQrCompute = new AsynchronousQrCompute();
		asynchronousQrCompute.execute(shortId);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity4_activate_device, menu);
		return true;
	}

	private String getShortId() {
		SharedPreferences prefs = 
				getSharedPreferences("MY_SMARTLINE_PREFS", Context.MODE_PRIVATE);

		return prefs.getString("PROPERTY_CLOUD_ACTIVATION_ID", "nothing found");
	}
	private Bitmap generateQRCode(String url) throws WriterException{
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
	private class AsynchronousQrCompute extends AsyncTask<String, Void, String>{

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			String activationCode = params[0];
			String activateAutomatic = MyKeys.PROPERTY_HOME + "DeviceManagement/activateAutomatic/";
			String url = activateAutomatic;
			try {
				bitmpQRCode = generateQRCode(url + activationCode);
			} catch (WriterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		@Override
		protected void onPostExecute (String result){
			if (bitmpQRCode != null){
				viewShortIdQRCode.setImageBitmap(bitmpQRCode);
			}
		}
	}
}
