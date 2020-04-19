package com.prezrohit.calllater.service;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.prezrohit.calllater.dao.ContactDao;
import com.prezrohit.calllater.database.AppDatabase;
import com.prezrohit.calllater.model.Contact;

import java.lang.reflect.Method;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class CallService extends BroadcastReceiver {
	private int duration = 6;
	private static boolean isIncoming;
	private CountDownTimer countDownTimer;

	private static int lastState = TelephonyManager.CALL_STATE_IDLE;
	private static final String TAG = "CallReceiverService";

	/***
	 * this function get number from database and pass it to dialing function
	 *
	 * @param context - context of the activity calling the service
	 */
	public void prepareCall(Context context, Contact contact) {
		if(contact.getHangupValue() > 0)
			this.duration = contact.getHangupValue();

		ContactDao contactDao = AppDatabase.getInstance(context).contactDao();
		if (contact.getStatus() == 0) {
			startCall(context, contact);
			contactDao.updateStatus(contact.getId(), 1);

		} else {
			Log.d(TAG, "call: contact already dialed");
			contactDao.updateStatus(contact.getId(), 1);
		}
	}

	private void startCall(Context context, Contact contact) {
		if (contact.getNumber() != null)
			Log.d(TAG, "startCall: " + contact.getNumber());
		Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + contact.getNumber()));
		callIntent.addFlags(FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(callIntent);
	}

	@Override
	public void onReceive(Context context, Intent intent) {

		Log.v(TAG, "Receiving Intent");

		if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {

		} else {
			String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
			String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
			int state = 0;
			if (stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
				state = TelephonyManager.CALL_STATE_IDLE;

			} else if (stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
				state = TelephonyManager.CALL_STATE_OFFHOOK;

			} else if (stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
				state = TelephonyManager.CALL_STATE_RINGING;
			}

			onCallStateChanged(context, state);
		}
	}

	private void onCallStateChanged(Context context, int state) {
		final Context context1 = context;
		switch (state) {

			case TelephonyManager.CALL_STATE_RINGING:
				Log.d(TAG, "onCallStateChanged: ringing");
				isIncoming = true;
				break;

			case TelephonyManager.CALL_STATE_OFFHOOK:
				Log.d(TAG, "onCallStateChanged: offHook");

				isIncoming = false;
				Log.d(TAG, "Hangup Duration: " + duration);
				countDownTimer = new CountDownTimer(duration * 1000, 1000) {
					public void onTick(long millisUntilFinished) {
						Log.v(TAG, "onTick: " + millisUntilFinished);
					}

					public void onFinish() {
						Log.d(TAG, "onFinish: reject method called");
						rejectCall(context1);
					}
				};
				countDownTimer.start();
				break;

			case TelephonyManager.CALL_STATE_IDLE:
				isIncoming = false;
				Log.d(TAG, "onCallStateChanged: idle");
				break;
		}
		lastState = state;
	}

	private void rejectCall(Context context) {

		((Activity) context).finish();

		Log.d(TAG, "rejectCall: method call received");

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
			final TelecomManager telecomManager = (TelecomManager) context.getSystemService( Context.TELECOM_SERVICE );
			if (telecomManager != null && ContextCompat.checkSelfPermission( context, Manifest.permission.ANSWER_PHONE_CALLS ) == PackageManager.PERMISSION_GRANTED) {
				telecomManager.endCall( );
				Log.d(TAG, "rejectCall: disconnected in pie");
				countDownTimer.cancel();
				context.unregisterReceiver(this);
			}
		}
		else {
			try {

				String serviceManagerName = "android.os.ServiceManager";
				String serviceManagerNativeName = "android.os.ServiceManagerNative";
				String telephonyName = "com.android.internal.telephony.ITelephony";
				Class<?> telephonyClass;
				Class<?> telephonyStubClass;
				Class<?> serviceManagerClass;
				Class<?> serviceManagerNativeClass;
				Method telephonyEndCall;
				Object telephonyObject;
				Object serviceManagerObject;
				telephonyClass = Class.forName(telephonyName);
				telephonyStubClass = telephonyClass.getClasses()[0];
				serviceManagerClass = Class.forName(serviceManagerName);
				serviceManagerNativeClass = Class.forName(serviceManagerNativeName);
				Method getService = // getDefaults[29];
						serviceManagerClass.getMethod("getService", String.class);
				Method tempInterfaceMethod = serviceManagerNativeClass.getMethod("asInterface", IBinder.class);
				Binder tmpBinder = new Binder();
				tmpBinder.attachInterface(null, "fake");
				serviceManagerObject = tempInterfaceMethod.invoke(null, tmpBinder);
				IBinder retBinder = (IBinder) getService.invoke(serviceManagerObject, "phone");
				Method serviceMethod = telephonyStubClass.getMethod("asInterface", IBinder.class);
				telephonyObject = serviceMethod.invoke(null, retBinder);
				telephonyEndCall = telephonyClass.getMethod("endCall");
				telephonyEndCall.invoke(telephonyObject);
				Log.d(TAG, "rejectCall: disconnected");
				countDownTimer.cancel();
				context.unregisterReceiver(this);

			} catch (Exception e) {
				e.printStackTrace();
				Log.d("unable", "msg cant disconnect call....");
			}
		}
	}
}
