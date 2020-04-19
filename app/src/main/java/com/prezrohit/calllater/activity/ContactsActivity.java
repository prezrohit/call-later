package com.prezrohit.calllater.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.prezrohit.calllater.R;
import com.prezrohit.calllater.adapter.ContactsAdapter;
import com.prezrohit.calllater.dao.ContactDao;
import com.prezrohit.calllater.database.AppDatabase;
import com.prezrohit.calllater.model.Contact;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ContactsActivity extends AppCompatActivity {

	//region MemberDeclaration
	@BindView(R.id.rv_contacts)
	RecyclerView rvContact;
	@BindView(R.id.fab_add_contact)
	FloatingActionButton fabAddContact;
	@BindView(R.id.lbl_add_contact_hint)
	TextView lblAddContactHint;

	private ContactDao contactDao;
	private static final int PERMISSION_REQUEST_CODE = 200;
	private static final String TAG = "ContactsActivity";
	//endregion

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_contacts);

		ButterKnife.bind( this );

		contactDao = AppDatabase.getInstance( getApplicationContext() ).contactDao();

		populateList();
	}

	private void populateList() {

		LinearLayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, true);
		rvContact.setLayoutManager(layoutManager);

		contactDao.getAllContact().observe(this, (List<Contact> list) -> {
			if (list == null || list.size() == 0) {
				rvContact.setVisibility( View.GONE );
				lblAddContactHint.setVisibility( View.VISIBLE );
			} else {
				rvContact.setVisibility( View.VISIBLE );
				lblAddContactHint.setVisibility( View.GONE );
				ContactsAdapter contactsAdapter = new ContactsAdapter(this, list);
				rvContact.setAdapter(contactsAdapter);
			}
		});
	}

	@OnClick(R.id.fab_add_contact)
	public void onClickAddContacts() {
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
			startActivity(new Intent(this, TimerActivity.class));
			finish();
		}
		else
			requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSION_REQUEST_CODE);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == PERMISSION_REQUEST_CODE) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				startActivity(new Intent(this, TimerActivity.class));
			} else {
				Toast.makeText(this, "We need Permission to read contacts", Toast.LENGTH_LONG).show();
			}
		}
	}
}
