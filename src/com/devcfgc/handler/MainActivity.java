package com.devcfgc.handler;

import java.io.InputStream;
import java.lang.ref.WeakReference;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends Activity {

	private static ProgressDialog progressDialog;
	private static ImageView imageView;
	private String url = "http://www.devcfgc.com/wp-content/uploads/2014/06/Android_Handler.jpg";
	private static Bitmap bitmap = null;
	private final ImageHandler mHandler = new ImageHandler(this);

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		imageView = (ImageView) findViewById(R.id.showImage);

		Button start = (Button) findViewById(R.id.btnStartHandler);
		start.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				progressDialog = ProgressDialog.show(MainActivity.this, "",
						"Loading..");
				new Thread() {
					public void run() {
						bitmap = downloadBitmap(url);
						mHandler.sendEmptyMessage(0);
					}
				}.start();
			}
		});
	}

	/**
	 * Instances of static inner classes do not hold an implicit reference to
	 * their outer class.
	 * 
	 * To fix possible memory leaks in the MainActivity we need to use a static inner class for the Handler. 
	 * Static inner classes do not hold an implicit reference to their outer class, so the activity 
	 * will not be leaked. If you need to invoke the outer activity's methods from within the Handler, 
	 * have the Handler hold a WeakReference to the activity so you don't accidentally leak a context. 
	 * More info in http://stackoverflow.com/questions/11278875/handlers-and-memory-leaks-in-android
	 */
	private static class ImageHandler extends Handler {
		private final WeakReference<MainActivity> mActivity;

		public ImageHandler(MainActivity activity) {
			mActivity = new WeakReference<MainActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			if (mActivity == null) {
				throw new RuntimeException("Something goes wrong.");
			} else {
				imageView.setImageBitmap(bitmap);
				progressDialog.dismiss();
			}
		}
	}

	private Bitmap downloadBitmap(String url) {
		// Initialize the default HTTP client object
		final DefaultHttpClient client = new DefaultHttpClient();

		// forming a HttoGet request
		final HttpGet getRequest = new HttpGet(url);
		try {

			HttpResponse response = client.execute(getRequest);

			// check 200 OK for success
			final int statusCode = response.getStatusLine().getStatusCode();

			if (statusCode != HttpStatus.SC_OK) {
				Log.w("ImageHandler", "Error " + statusCode
						+ " retrieving bitmap from " + url);
				return null;
			}

			final HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream inputStream = null;
				try {
					// getting contents from the stream
					inputStream = entity.getContent();

					// decoding stream data back into image Bitmap
					Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

					return bitmap;
				} finally {
					if (inputStream != null) {
						inputStream.close();
					}
					entity.consumeContent();
				}
			}
		} catch (Exception e) {
			getRequest.abort();
		}

		return null;
	}
}