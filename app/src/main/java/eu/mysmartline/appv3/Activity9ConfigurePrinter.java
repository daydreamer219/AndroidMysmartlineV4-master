package eu.mysmartline.appv3;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.epson.eposprint.Builder;
import com.epson.eposprint.EposException;
import com.epson.eposprint.Print;
import com.epson.epsonio.DevType;
import com.epson.epsonio.EpsonIo;
import com.epson.epsonio.EpsonIoException;
import com.epson.epsonio.Finder;
import com.epson.epsonio.IoStatus;

import eu.mysmartline.appv3.models.MyKeys;

public class Activity9ConfigurePrinter extends ActionBarActivity{
	private List<Map<String, String>> items = new ArrayList<Map<String, String>>();
	static final byte[] CMD_ESC_ENABLE_PRINTER = { 0x1b, 0x3d, 0x01, // ESC =
	// 1(Enables
	// printer)
	};
	static final byte[] CMD_GS_I_PRINTER_NAME = { 0x1d, 0x49, 0x43, // GS I
	// 67(Printer
	// name)
	};
	static final int SEND_RESPONSE_TIMEOUT = 1000;
	static final int RESPONSE_MAXBYTE = 128;
	static final int RESPONSE_HEADER = 0x5f;
	static final int RESPONSE_TERMINAL = 0x00;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_activity9_configure_printer);
        actionbarInit();
		initList();
		ListView listView = (ListView) findViewById(R.id.listView);
		// React to user clicks on items
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parentAdapter, View view,
					int position, long id) {
				int myId = (int) id;
				//Intent intent = new Intent();
				Context context = getApplicationContext();
				CharSequence tostText = "Hello!";
				int duration = Toast.LENGTH_SHORT;
				SharedPreferences prefs = getSharedPreferences(
						MyKeys.MY_SMARTLINE_PREFS, Context.MODE_PRIVATE);
				switch (myId) {
				case 0:
					new ConfigureEpson()
							.execute(new String[] { "nothing to do" });
					break;
				case 1:
					// Set printer values:
					
					SharedPreferences.Editor editor = prefs.edit();
					editor.putString(MyKeys.PROPERTY_PRINTER_SERVICE,
							"googleCloud");
					editor.commit();
					// show toast
					tostText = "Using google cloud print";
					Toast toast2 = Toast.makeText(context, tostText, duration);
					toast2.show();
					// Go to main screen
					goToActivity3();
					break;
				case 2:
					// Set printer values:
					
					SharedPreferences.Editor editor2 = prefs.edit();
					editor2.putString(MyKeys.PROPERTY_PRINTER_SERVICE,
							"no print service");
					editor2.commit();
					// Go to main screen
					tostText = "Printer disabled";
					Toast toast = Toast.makeText(context, tostText, duration);
					toast.show();
					goToActivity3();

					break;

				/*
				 * case 3: intent.setClass(Activity3ListActivityes.this,
				 * Activity8TestPrint.class); startActivity(intent); break;
				 */
				}
			}
		});
		SimpleAdapter simpleAdpt = new SimpleAdapter(this, items,
				R.layout.row_item, new String[] { "item" },
				new int[] { R.id.txt_name });
		listView.setAdapter(simpleAdpt);
	}

    private void actionbarInit() {
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.defaultThemeColor)));
        getSupportActionBar().setTitle(R.string.title_activity_activity9_configure_printer);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity9_configure_printer, menu);
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
		}else if (id == android.R.id.home) {
            finish();
            return true;
        }
		return super.onOptionsItemSelected(item);
	}

	private HashMap<String, String> createItem(String key, String name) {
		HashMap<String, String> item = new HashMap<String, String>();
		item.put(key, name);
		return item;
	}

	private void initList() {
		String [] myMenu = getResources().getStringArray(R.array.activity9_menu_items);
		items.add(createItem("item", myMenu[0]));//use Epson
		items.add(createItem("item", myMenu[1]));//Use Google cloud print
		items.add(createItem("item", myMenu[2]));//Disable printing
	}

	private class ConfigureEpson extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
			/*
			 * try { Thread.sleep(1000L); } catch (InterruptedException e) {
			 * 
			 * }
			 */
            Log.d("epson", "start of debug");
            Print printer = new Print();
            int[] status = new int[1];
            status[0] = 0;
            try {

                Finder.start(getBaseContext(), DevType.TCP, "255.255.255.255");
                String[] mList = null;

                boolean noPrinter = true;
                // search printer loop

                int i = 0;
                while (noPrinter) {
                    Log.d("epson", "value i = " + i);
                    if (i > 60000) {
                        break;
                    }
                    mList = Finder.getResult();
                    i++;
                    if (mList == null) {
                    } else {
                        noPrinter = false;
                    }
                }
                Finder.stop();
                if (noPrinter == false) {

                    Log.d("epson", "printers found = " + mList.length);
                    String printerIp = mList[0];
                    String printerName = null;
                    // get pringer name
                    EpsonIo port = new EpsonIo();
                    port.open(DevType.TCP, printerIp, null, null);
                    port.write(CMD_ESC_ENABLE_PRINTER, 0,
                            CMD_ESC_ENABLE_PRINTER.length,
                            SEND_RESPONSE_TIMEOUT);

                    // printe name
                    byte[] receiveBuffer = new byte[RESPONSE_MAXBYTE];
                    int[] receiveSize = new int[1];
                    Boolean ret = false;
                    String[] value = new String[1];

                    port.write(CMD_GS_I_PRINTER_NAME, 0,
                            CMD_GS_I_PRINTER_NAME.length, SEND_RESPONSE_TIMEOUT);

                    Arrays.fill(receiveBuffer, (byte) 0);

                    ret = receiveResponse(port, receiveBuffer, receiveSize);
                    if ((false != ret) && (0 < receiveSize[0])) {
                        byte[] response = Arrays.copyOf(receiveBuffer,
                                receiveSize[0]);

                        analyzeResponse(response, value);

                        printerName = value[0];
                    }

                    port.close();

                    if (printerName != null) {
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
                        builder.addText("Congratulations,\n");
                        builder.addText("You have successfully configured\n this printer!\n\t");
                        builder.addTextSize(2, 2);

                        // builder.addSymbol(data, type, level, width, height,
                        // size)

                        builder.addSymbol(
                                "Congratulations \nhttp://mysmartline.eu/",
                                Builder.SYMBOL_QRCODE_MODEL_2, Builder.LEVEL_L,
                                5, Builder.PARAM_UNSPECIFIED,
                                Builder.PARAM_UNSPECIFIED);
                        builder.addText("mysmartline.eu");
                        builder.addFeedLine(1);
                        builder.addCut(Builder.CUT_FEED);

                        // Send a print document
                        // <Start communication with the printer>
                        printer.openPrinter(Print.DEVTYPE_TCP, printerIp);
                        // <Send data>
                        printer.sendData(builder, 10000, status);
                        // <End communication with the printer>
                        printer.closePrinter();

                        // Set printer values:
                        SharedPreferences prefs = getSharedPreferences(
                                MyKeys.MY_SMARTLINE_PREFS, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString(MyKeys.PROPERTY_PRINTER_SERVICE,
                                "epson");
                        editor.putString(MyKeys.PROPERTY_PRINTER_NAME,
                                printerName);
                        editor.putString(MyKeys.PROPERTY_PRINTER_IP,
                                printerIp);
                        editor.commit();
                        // show toast

                        // Go to main screen
                        goToActivity3();
                        return "epsonFound";
                    }
                }

            } catch (EpsonIoException e) {
                Log.d("epson", "EpsonIoException: Something went wrong");
            } catch (EposException e) {
                Log.d("epson", "EposException");
            }
            return "noEpson";
        }

        @Override
        protected void onPostExecute(String result) {
            findViewById(R.id.progressBar).setVisibility(View.GONE);
            Context context = getApplicationContext();
            CharSequence text;
            if (result.equals("epsonFound")) {
                text = "Using Epson printer";
            } else {
                text = "No printer found";
            }
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }

        @Override
        protected void onPreExecute() {
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        }

	}

	// receive response
	private Boolean receiveResponse(EpsonIo port, byte[] receiveBuffer,
			int[] readSize) throws EpsonIoException {
		if ((null == receiveBuffer) || (0 >= receiveBuffer.length)) {
			return false;
		}

		if ((null == readSize) || (0 >= readSize.length)) {
			return false;
		}

		readSize[0] = 0;

		// receive
		try {
			readSize[0] = port.read(receiveBuffer, 0, receiveBuffer.length,
					SEND_RESPONSE_TIMEOUT);
		} catch (EpsonIoException e) {
			if (e.getStatus() == IoStatus.ERR_TIMEOUT) {
				return false;
			} else {
				throw e;
			}
		}

		return true;
	}

	private boolean analyzeResponse(byte[] response, String[] value) {
		int currentPos = 0;

		if ((null == value) || (0 >= value.length)) {
			return false;
		}
		value[0] = "";

		// search 5f header
		for (currentPos = 0; currentPos < response.length; currentPos++) {
			if (response[currentPos] == RESPONSE_HEADER) {
				currentPos++;
				break;
			}
		}

		if (currentPos >= response.length) {
			return false;
		}

		// Terminator check
		int endPos = 0;
		for (endPos = currentPos; endPos < response.length; endPos++) {
			if (response[endPos] == RESPONSE_TERMINAL) {
				break;
			}
		}

		if (endPos == currentPos) {
			return true;
		}

		// get response string
		String responseString = null;
		try {
			responseString = new String(response, currentPos, endPos
					- currentPos, "US-ASCII");
		} catch (UnsupportedEncodingException e) {
			return false;
		}

		value[0] = responseString;

		return true;
	}

	private void goToActivity3() {
        /*
		Intent intent = new Intent();
		intent.setClass(Activity9ConfigurePrinter.this,
				Activity3ListActivityes.class);
		startActivity(intent);
		*/
        finish();
	}
}
