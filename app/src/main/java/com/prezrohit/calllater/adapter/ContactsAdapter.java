package com.prezrohit.calllater.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.prezrohit.calllater.R;
import com.prezrohit.calllater.activity.EditCallScheduleActivity;
import com.prezrohit.calllater.dao.ContactDao;
import com.prezrohit.calllater.database.AppDatabase;
import com.prezrohit.calllater.model.Contact;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactViewHolder> {

	//region Class Member Declaration
	private Context context;
	private List<Contact> contactsArrayList;
	private ContactDao contactDao;
	private static final int TYPE_LIST_EMPTY = 0;
	private static final int TYPE_LIST_POPULATED = 1;

	private static final String TAG = "ContactsAdapter";
	//endregion

	public ContactsAdapter(Context context, List<Contact> contactsArrayList) {
		this.context = context;
		this.contactsArrayList = contactsArrayList;
		contactDao = AppDatabase.getInstance(context).contactDao();
	}

	@NonNull
	@Override
	public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new ContactViewHolder(LayoutInflater.from(context).inflate(R.layout.row_contact, parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
		Contact contact = contactsArrayList.get(position);

		if (contact.getName() == null || contact.getName().isEmpty())
			holder.lblContactName.setVisibility(View.GONE);
		else
			holder.lblContactName.setText(contact.getName());

		holder.lblContactNumber.setText(contact.getNumber());
		holder.lblSchedule.setText(contact.getScheduleTime());

		if (contact.getStatus() == 0) {
			holder.lblStatus.setText("Not Dialed");
			holder.lblStatus.setTextColor(Color.RED);

		} else {
			holder.lblStatus.setText("Dialed");
			holder.lblStatus.setTextColor(Color.GREEN);
		}

		holder.imgDelete.setOnClickListener(v -> contactDao.deleteContact(contact));

		holder.imgEdit.setOnClickListener(v -> {
			Intent intent = new Intent(context, EditCallScheduleActivity.class);
			intent.putExtra("id", contact.getId());
			intent.putExtra("name", contact.getName());
			intent.putExtra("number", contact.getNumber());
			intent.putExtra("date_time", contact.getScheduleTime());
			intent.putExtra("hangup_value", contact.getHangupValue());
			context.startActivity(intent);
		});
	}

	@Override
	public int getItemCount() {
		if (contactsArrayList == null)
			return TYPE_LIST_EMPTY;
		else if (contactsArrayList.size() == 0)
			return TYPE_LIST_EMPTY;
		else
			return contactsArrayList.size();
	}

	@Override
	public int getItemViewType(int position) {
		if (getItemCount() == 0)
			return TYPE_LIST_EMPTY;
		else
			return TYPE_LIST_POPULATED;
	}

	static class ContactViewHolder extends RecyclerView.ViewHolder {

		@BindView(R.id.lbl_contact_name)
		TextView lblContactName;

		@BindView(R.id.lbl_contact_number)
		TextView lblContactNumber;

		@BindView(R.id.lbl_status)
		TextView lblStatus;

		@BindView(R.id.lbl_schedule)
		TextView lblSchedule;

		@BindView(R.id.img_delete)
		ImageView imgDelete;

		@BindView(R.id.img_contact)
		ImageView imgContact;

		@BindView(R.id.img_edit)
		ImageView imgEdit;

		ContactViewHolder(@NonNull View itemView) {
			super(itemView);

			ButterKnife.bind(this, itemView);
		}
	}
}

