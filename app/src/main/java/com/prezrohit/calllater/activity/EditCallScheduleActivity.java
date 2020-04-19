package com.prezrohit.calllater.activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.prezrohit.calllater.R;
import com.prezrohit.calllater.dao.ContactDao;
import com.prezrohit.calllater.database.AppDatabase;
import com.prezrohit.calllater.helper.DateTimeConverter;
import com.prezrohit.calllater.helper.DateTimeHelper;
import com.prezrohit.calllater.service.CallService;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class EditCallScheduleActivity extends AppCompatActivity {

	@BindView(R.id.seek_bar)
	SeekBar seekBar;

	@BindView(R.id.lbl_hang_up_time)
	TextView lblHangUpTime;

	@BindView(R.id.lbl_contact_name)
	TextView lblContactName;

	@BindView(R.id.lbl_timer_selected_time)
	TextView lblSelectedTime;

	@BindView(R.id.lbl_timer_selected_date)
	TextView lblSelectedDate;

	private int id;
	private int hangupValue;

	private int timerHour;
	private int timerMinute;
	private int timerDay;
	private int timerMonth;
	private int timerYear;

	private Date scheduleDate;
	private String timeScheduled;
	private String dateScheduled;

	private boolean receiverIsRegistered = false;
	private CallService callReceiverService;

	private static final String TAG = "EditScheduleActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timer);

		ButterKnife.bind(this);

		id = getIntent().getIntExtra("id", -1);
		String name = getIntent().getStringExtra("name");
		String number = getIntent().getStringExtra("number");
		String dateTime = getIntent().getStringExtra("date_time");
		hangupValue = getIntent().getIntExtra("hangup_value", 6);

		if (name != null) {
			lblContactName.setVisibility(View.VISIBLE);
			lblContactName.setText(name);
		}

		EditText edtContactNumber = findViewById(R.id.edt_contact_number);
		edtContactNumber.setKeyListener(null);

		lblHangUpTime.setText("Hang Up In: " + hangupValue + " sec");
		seekBar.setProgress(hangupValue);
		edtContactNumber.setText(number);
		scheduleDate = DateTimeHelper.getDateFromString(dateTime);

		Calendar calendar = Calendar.getInstance();
		timerYear = calendar.get(Calendar.YEAR) - 1900;
		timerMonth = calendar.get(Calendar.MONTH);
		timerDay = calendar.get(Calendar.DATE);

		timerHour = calendar.get(Calendar.HOUR_OF_DAY);
		timerMinute = calendar.get(Calendar.MINUTE);

		dateScheduled = DateTimeHelper.formatDate(scheduleDate.getDate() + " " + (scheduleDate.getMonth() + 1) + " " + (scheduleDate.getYear() + 1900));
		timeScheduled = DateTimeConverter.convertTo12Hour(scheduleDate.getHours(), scheduleDate.getMinutes());

		callReceiverService = new CallService();

		setDateAtStart(scheduleDate.getYear() + 1900, scheduleDate.getMonth() + 1, scheduleDate.getDate());
		setTimeAtStart(scheduleDate.getHours(), scheduleDate.getMinutes());

		seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);

		Button btnBrowse = findViewById(R.id.btn_browse);
		btnBrowse.setVisibility(View.GONE);
	}

	SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			hangupValue = progress;
			lblHangUpTime.setText("Hang Up In: " + hangupValue + " sec");
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {

		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {

		}
	};

	@OnClick(R.id.const_layout_date)
	public void onClickSetDate() {

		int setYear = scheduleDate.getYear() + 1900;
		int setMonth = scheduleDate.getMonth();
		int setDate = scheduleDate.getDate();

		DatePickerDialog datePickerDialog = new DatePickerDialog(this,
				(view, year, monthOfYear, dayOfMonth) -> {
					timerYear = year - 1900;
					timerMonth = monthOfYear;
					timerDay = dayOfMonth;

					dateScheduled = DateTimeHelper.formatDate(dayOfMonth + " " + (monthOfYear + 1) + " " + year);
					lblSelectedDate.setText(dateScheduled);
				},
				setYear,
				setMonth,
				setDate);

		datePickerDialog.show();
	}

	@OnClick(R.id.const_layout_time)
	public void onClickSetStartTime() {

		int setHour = scheduleDate.getHours();
		int setMinute = scheduleDate.getMinutes();

		TimePickerDialog timePickerDialog = new TimePickerDialog(
				this,
				(view, hourOfDay, minute) -> {
					timerHour = hourOfDay;
					timerMinute = minute;

					timeScheduled = DateTimeConverter.convertTo12Hour(hourOfDay, minute);
					lblSelectedTime.setText(timeScheduled);
				},
				setHour,
				setMinute,
				false);

		timePickerDialog.show();
	}

	@OnClick(R.id.fab_add_to_schedule)
	void onClickAddContact() {
		ContactDao contactDao = AppDatabase.getInstance(getApplicationContext()).contactDao();
		contactDao.updateScheduleAndHangupTime(id, hangupValue, dateScheduled + " - " + timeScheduled);

		IntentFilter filter = new IntentFilter();
		filter.addAction("android.intent.action.NEW_OUTGOING_CALL");
		filter.addAction("android.intent.action.PHONE_STATE");
		filter.addAction("android.intent.action.READ_CALL_LOG");
		registerReceiver(callReceiverService, filter);
		receiverIsRegistered = true;

		Date date = new Date(timerYear, timerMonth, timerDay, timerHour, timerMinute);
		Log.d(TAG, "date: " + date);
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
	}

	@Override
	public void onBackPressed() {
		new AlertDialog.Builder(this)
				.setTitle(getString(R.string.string_exit))
				.setMessage(getString(R.string.string_exit_message))
				.setPositiveButton(getString(R.string.string_yes), (dialog, which) -> {
					startActivity(new Intent(EditCallScheduleActivity.this, ContactsActivity.class));
					finish();
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

	private void setDateAtStart(int mYear, int mMonth, int mDay) {
		lblSelectedDate.setText(DateTimeHelper.formatDate(mDay + " " + mMonth + " " + mYear));
	}

	private void setTimeAtStart(int hour, int minute) {
		lblSelectedTime.setText(DateTimeConverter.convertTo12Hour(hour, minute));
	}
}
