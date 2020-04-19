package com.prezrohit.calllater.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.prezrohit.calllater.R;
import com.prezrohit.calllater.dao.ContactDao;
import com.prezrohit.calllater.database.AppDatabase;
import com.prezrohit.calllater.helper.DateTimeConverter;
import com.prezrohit.calllater.helper.DateTimeHelper;
import com.prezrohit.calllater.model.Contact;
import com.prezrohit.calllater.service.CallService;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TimerActivity extends AppCompatActivity {
	//region Class Member Declaration
	@BindView(R.id.seek_bar)
	SeekBar seekBar;

	@BindView(R.id.lbl_hang_up_time)
	TextView lblHangUpTime;

	@BindView(R.id.lbl_timer_selected_time)
	TextView lblSelectedTime;

	@BindView(R.id.lbl_timer_selected_date)
	TextView lblSelectedDate;

	@BindView(R.id.lbl_contact_name)
	TextView lblContactName;

	@BindView(R.id.edt_contact_number)
	EditText edtContactNumber;

	private ContactDao contactDao;
	private CallService callReceiverService;

	private int timerHour;
	private int timerMinute;
	private int timerDay;
	private int timerMonth;
	private int timerYear;

	private String timeScheduled;
	private String dateScheduled;
	private int hangUpChangedValue = 6;
	private static final int PICK_CONTACT = 100;
	private boolean receiverIsRegistered = false;
	private static final String TAG = "TimerActivity";
	private static final int CONTACT_PERMISSION_REQUEST_CODE = 200;
	private static final int PHONE_STATE_PERMISSION_REQUEST_CODE = 1;
	//endregion

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timer);

		ButterKnife.bind(this);

		Calendar calendar = Calendar.getInstance();
		timerYear = calendar.get(Calendar.YEAR) - 1900;
		timerMonth = calendar.get(Calendar.MONTH);
		timerDay = calendar.get(Calendar.DATE);

		timerHour = calendar.get(Calendar.HOUR_OF_DAY);
		timerMinute = calendar.get(Calendar.MINUTE);

		Log.d(TAG, "onCreate: ");
		Log.d(TAG, "year: " + timerYear);
		Log.d(TAG, "monthOfYear: " + timerMonth);
		Log.d(TAG, "dayOfMonth: " + timerDay);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission_group.CALL_LOG, Manifest.permission.CALL_PHONE,
					Manifest.permission.READ_PHONE_STATE, Manifest.permission.ANSWER_PHONE_CALLS}, PHONE_STATE_PERMISSION_REQUEST_CODE);
		else
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.PROCESS_OUTGOING_CALLS, Manifest.permission.CALL_PHONE,
					Manifest.permission.READ_PHONE_STATE}, PHONE_STATE_PERMISSION_REQUEST_CODE);

		contactDao = AppDatabase.getInstance(getApplicationContext()).contactDao();
		callReceiverService = new CallService();

		setDateTimeAtStart();

		seekBar.setOnSeekBarChangeListener(seekBarChangeListener);
	}

	SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			// updated continuously as the user slides the thumb
			hangUpChangedValue = progress;

			lblHangUpTime.setText("Hang Up In: " + hangUpChangedValue + " sec");
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// called when the user first touches the SeekBar
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// called after the user finished  moving the SeekBar
		}
	};

	@OnClick(R.id.btn_browse)
	public void onClickBrowseContact() {
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
			Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
			startActivityForResult(intent, PICK_CONTACT);
		} else {
			requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, CONTACT_PERMISSION_REQUEST_CODE);
		}
	}

	//TODO: show suggestions while typing in the phone number
	//TODO: slide to delete
	//TODO: app crashes on selecting file manager on add contact

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == CONTACT_PERMISSION_REQUEST_CODE) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
				startActivityForResult(intent, PICK_CONTACT);
			} else {
				Toast.makeText(this, "We need Permission to read contacts", Toast.LENGTH_LONG).show();
			}
		}
	}

	@OnClick(R.id.const_layout_date)
	public void onClickSetDate() {

		Calendar c = Calendar.getInstance();
		int mYear = c.get(Calendar.YEAR);
		int mMonth = c.get(Calendar.MONTH);
		int mDay = c.get(Calendar.DATE);

		DatePickerDialog datePickerDialog = new DatePickerDialog(this,
				(view, year, monthOfYear, dayOfMonth) -> {
					timerYear = year - 1900;
					timerMonth = monthOfYear;
					timerDay = dayOfMonth;

					Log.d(TAG, "year: " + year);
					Log.d(TAG, "monthOfYear: " + monthOfYear);
					Log.d(TAG, "dayOfMonth: " + dayOfMonth);

					Log.d(TAG, "onClickSetDate: date listener");
					dateScheduled = DateTimeHelper.formatDate(dayOfMonth + " " + (monthOfYear + 1) + " " + year);
					lblSelectedDate.setText(dateScheduled);
				},
				mYear,
				mMonth,
				mDay);

		datePickerDialog.show();
	}

	@OnClick(R.id.const_layout_time)
	public void onClickSetStartTime() {

		Calendar calendar = Calendar.getInstance();
		int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
		int currentMinute = calendar.get(Calendar.MINUTE);

		TimePickerDialog timePickerDialog = new TimePickerDialog(
				this,
				(view, hourOfDay, minute) -> {
					timerHour = hourOfDay;
					timerMinute = minute;

					Log.d(TAG, "onClickSetStartTime: time listener");
					timeScheduled = DateTimeConverter.convertTo12Hour(hourOfDay, minute);
					lblSelectedTime.setText(timeScheduled);
				},
				currentHour,
				currentMinute,
				false);

		timePickerDialog.show();
	}

	@OnClick(R.id.fab_add_to_schedule)
	void onClickAddContact() {

		String name = lblContactName.getText().toString();
		String number = edtContactNumber.getText().toString();

		if (isContactValid(number)) {
			validateDateTime();

			Contact contact = new Contact(name, dateScheduled + " - " + timeScheduled, number, 0, hangUpChangedValue);
			long id = contactDao.insert(contact);

			IntentFilter filter = new IntentFilter();
			filter.addAction("android.intent.action.NEW_OUTGOING_CALL");
			filter.addAction("android.intent.action.PHONE_STATE");
			filter.addAction("android.intent.action.READ_CALL_LOG");
			registerReceiver(callReceiverService, filter);
			receiverIsRegistered = true;

            /*Intent alarmIntent = new Intent(this, AlarmReceiver.class);
            alarmIntent.putExtra("id", id);
            alarmIntent.putExtra("hang_up_value", hangUpChangedValue);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC_WAKEUP, date.getTime(), pendingIntent);*/

			Date date = new Date(timerYear, timerMonth, timerDay, timerHour, timerMinute);
			Timer t = new Timer();
			t.schedule(new TimerTask() {
				public void run() {
					if (contactDao.getContactById(id) != null) {
						callReceiverService.prepareCall(getApplication(), contactDao.getContactById(id));
					}
				}
			}, date);

			startActivity(new Intent(getApplicationContext(), ContactsActivity.class));
			finish();

		} else
			Toast.makeText(this, "Contact is Invalid. Please Recheck it.", Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == PICK_CONTACT) {
			if (resultCode == Activity.RESULT_OK) {
				Uri contactData = data.getData();
				Cursor c = managedQuery(contactData, null, null, null, null);
				if (c.moveToFirst()) {

					String id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));

					String hasPhone = c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

					if (hasPhone.equalsIgnoreCase("1")) {
						Cursor phones = getContentResolver().query(
								ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
								ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
								null, null);
						phones.moveToFirst();

						String cNumber = phones.getString(phones.getColumnIndex("data1"));
						cNumber = cNumber.replace(" ", "");
						String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

						Log.d(TAG, "Number is: " + cNumber);
						Log.d(TAG, "Name is: " + name);
						edtContactNumber.setText(cNumber);
						lblContactName.setText(name);
						lblContactName.setVisibility(View.VISIBLE);
					}
				}
			}
		}
	}

	@Override
	public void onBackPressed() {
		new AlertDialog.Builder(this)
				.setTitle(getString(R.string.string_exit))
				.setMessage(getString(R.string.string_exit_message))
				.setPositiveButton(getString(R.string.string_yes), (dialog, which) -> {
					startActivity(new Intent(TimerActivity.this, ContactsActivity.class));
					finish();
					Log.d(TAG, "onClick: exit");
				})
				.setNegativeButton(getString(R.string.string_no), null)
				.show();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (receiverIsRegistered) {
			unregisterReceiver(callReceiverService);
			receiverIsRegistered = false;
		}
	}

	private void setDateTimeAtStart() {
		Calendar c = Calendar.getInstance();
		int mYear = c.get(Calendar.YEAR);
		int mMonth = c.get(Calendar.MONTH);
		++mMonth;
		int mDay = c.get(Calendar.DATE);
		lblSelectedDate.setText(DateTimeHelper.formatDate(mDay + " " + mMonth + " " + mYear));

		int currentHour = c.get(Calendar.HOUR_OF_DAY);
		int currentMinute = c.get(Calendar.MINUTE);
		lblSelectedTime.setText(DateTimeConverter.convertTo12Hour(currentHour, currentMinute));
	}

	private void validateDateTime() {
		if (timeScheduled == null)
			timeScheduled = lblSelectedTime.getText().toString();

		if (dateScheduled == null)
			dateScheduled = lblSelectedDate.getText().toString();
	}

	private boolean isContactValid(String contactNumber) {
		if (contactNumber == null)
			return false;
		else if (contactNumber.isEmpty())
			return false;
		else return contactNumber.length() >= 10;
	}
}
