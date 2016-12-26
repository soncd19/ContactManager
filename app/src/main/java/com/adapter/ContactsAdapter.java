package com.adapter;

import android.Manifest;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.caodinhson.contactactivity.R;
import com.database.BlackListManager;
import com.util.Contact;
import com.util.LetterTileDrawable;

import java.util.ArrayList;

/**
 * Created by Cao Dinh Son on 11/13/2016.
 */

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {

    private ArrayList<Contact> mListContacts = new ArrayList<Contact>();

    private ArrayList<Contact> mListContactSearch = new ArrayList<>();

    private Context mContext;

    public static final String TAG = "ContactsAdapter";

    public ContactsAdapter(Context context, ArrayList<Contact> listContact) {
        this.mContext = context;
        this.mListContacts = listContact;
        this.mListContactSearch = listContact;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_contact_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Contact contact = mListContacts.get(position);
        holder.mNameContact.setText(contact.getNameContact());
        holder.mNumberContact.setText(contact.getNumberContact());
        boolean isStarred = contact.getIsStart();
        boolean isBlock = contact.getIsBlock();
        if (isStarred){
            holder.mFavorite.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_favorite));
        }else {
            holder.mFavorite.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_un_favorite));
        }
        if (isBlock){
            holder.mBlock.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_lock));
        }else {
            holder.mBlock.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_lock_open));
        }
        //holder.mViewDetail.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_arrow_right));
        LetterTileDrawable letterTileDrawable = new LetterTileDrawable(mContext, null);
        letterTileDrawable.setContactDetails(contact.getNameContact(), String.valueOf(contact.getIdContact()));
        letterTileDrawable.setIsCircular(true);
        holder.mAvatarView.setImageDrawable(letterTileDrawable);
    }

    @Override
    public int getItemCount() {
        int count = 0;
        if (mListContacts != null) {
            count = mListContacts.size();
        }
        return count;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView mAvatarView;

        private TextView mNameContact;

        private TextView mNumberContact;

        private ImageButton mFavorite;

        private ImageButton mCall;

        private ImageButton mSms;

        private ImageButton mEdit;

        private ImageButton mDelete;

        private ImageButton mBlock;

        public ViewHolder(View itemView) {
            super(itemView);
            mAvatarView = (ImageView) itemView.findViewById(R.id.avatar);
            mNameContact = (TextView) itemView.findViewById(R.id.name_contact);
            mNumberContact = (TextView) itemView.findViewById(R.id.number_contact);
            mFavorite = (ImageButton) itemView.findViewById(R.id.action_favorite);
            mCall = (ImageButton) itemView.findViewById(R.id.action_call);
            mSms = (ImageButton) itemView.findViewById(R.id.action_sms);
            mEdit = (ImageButton) itemView.findViewById(R.id.action_edit);
            mDelete = (ImageButton) itemView.findViewById(R.id.action_delete);
            mBlock = (ImageButton) itemView.findViewById(R.id.action_block);
            mCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int positon = getAdapterPosition();
                    String number = mListContacts.get(positon).getNumberContact();
                    Intent callItent = new Intent(Intent.ACTION_CALL);
                    callItent.setData(Uri.parse("tel:" + number));
                    if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    mContext.startActivity(callItent);
                }
            });
            mSms.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int positon = getAdapterPosition();
                    String number = mListContacts.get(positon).getNumberContact();
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("sms:"));
                    intent.setType("vnd.android-dir/mms-sms");
                    intent.putExtra("address", number);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                }
            });

            mDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    String number = mListContacts.get(position).getNumberContact();
                    Long id = mListContacts.get(position).getIdContact();
                    showDialogDeleteContact(number, id, position);
                }
            });
            mFavorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Check add start or remove star
                    int position = getAdapterPosition();
                    Uri contactUri = getContactUri(position);
                    boolean isStart =  mListContacts.get(position).getIsStart();
                    if (isStart){
                        remveFromStar(mContext.getContentResolver(), contactUri);
                        mListContacts.get(position).setIsStar(false);
                    }else {
                        addToStar(mContext.getContentResolver(), contactUri);
                        mListContacts.get(position).setIsStar(true);
                    }
                    notifyDataSetChanged();
                }
            });

            mBlock.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    String number = mListContacts.get(position).getNumberContact();
                    boolean isBlock = mListContacts.get(position).getIsBlock();
                    if (isBlock){
                        showDialogUnBlockContac(number, false, position);
                    }else {
                        showDialogBlockContac(number, true, position);
                    }
                }
            });

            mEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    Uri contactUri = getContactUri(position);
                    Intent intent = new Intent(Intent.ACTION_EDIT);
                    intent.setData(contactUri);
                    mContext.startActivity(intent);
                }
            });
        }
    }

    //SonCD: append contact from cursor
    public ArrayList<Contact> appendCusor(Cursor cursor, String query, ArrayList<String> listBlock) {
        Contact contact;
        mListContacts.clear();
        ArrayList<Contact> listContact = new ArrayList<Contact>();
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                contact = getContactFromCursor(cursor, query, listBlock);
                if (contact != null) {
                    listContact.add(contact);
                }
            } while (cursor.moveToNext());
            //SonCD: SORT NAME
           /* Collections.sort(listContact, new Comparator<Contact>() {
                @Override
                public int compare(Contact o1, Contact o2) {
                    return o1.getNameContact().compareTo(o2.getNameContact());
                }
            });*/
            this.mListContacts.addAll(listContact);
        }
        return listContact;
    }

    public void queryContact(String query) {
        String name;
        String number;
        Long id;
        boolean isStar;
        boolean isBlock;
        Contact contact = null;
        if (!TextUtils.isEmpty(query)) {
            mListContacts.clear();
            ArrayList<Contact> listContact = new ArrayList<Contact>();
            for (int i = 0; i < mListContactSearch.size(); i++) {
                id = mListContactSearch.get(i).getIdContact();
                name = mListContactSearch.get(i).getNameContact();
                number = mListContactSearch.get(i).getNumberContact();
                isStar = mListContactSearch.get(i).getIsStart();
                if (TextUtils.isEmpty(name)) {
                    name = "";
                }
                if (TextUtils.isEmpty(number)) {
                    number = "";
                }
                isBlock = BlackListManager.getInstance().hasBlock(number);
                boolean nameMatcher = name.toUpperCase().contains(query.toUpperCase());
                boolean numberMatcher = number.contains(query);
                if (nameMatcher || numberMatcher) {
                    contact = new Contact(id, name, number, isStar, isBlock);
                    listContact.add(contact);
                }
            }
            mListContacts.addAll(listContact);
        } else {
            mListContacts = mListContactSearch;
        }
        notifyDataSetChanged();
    }

    //SonCD: get information contact from cursor
    public Contact getContactFromCursor(Cursor cursor, String query, ArrayList<String> listBlock) {
        String name;
        String number = "";
        int id;
        int star;
        boolean isStar = false;
        Contact contact = null;
        boolean isBlock = false;
        /*id = cursor.getInt(cursor.getColumnIndex(ContactSimManager.CONTACT_ID));
        name = cursor.getString(cursor.getColumnIndex(ContactSimManager.CONTACT_NAME));
        number = cursor.getString(cursor.getColumnIndex(ContactSimManager.CONTACT_NUMBER));*/
        id = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
        name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
        number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        star = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.STARRED));
        /*if (Integer.valueOf(phoneNumber) > 0){
            Cursor phones = mContext.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ id,
                    null, null);

            while (phones.moveToNext()) {
                number = phones.getString(
                        phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                Log.d("SonCD", "phone number = "+phoneNumber);
            }
            phones.close();
        }*/
        if (TextUtils.isEmpty(name)) {
            name = "";
        }
        if (TextUtils.isEmpty(number)) {
            number = "";
        }
        if (star == 1) {
            isStar = true;
        }
        isBlock = BlackListManager.getInstance().hasBlock(number);
        if (!TextUtils.isEmpty(query)) {
            boolean nameMatcher = name.toUpperCase().contains(query.toUpperCase());
            boolean numberMatcher = number.contains(query);
            if (nameMatcher || numberMatcher) {
                contact = new Contact(id, name, number, isStar, isBlock);
                return contact;
            }
        } else {
            contact = new Contact(id, name, number, isStar, isBlock);
            return contact;
        }
        return null;
    }

    private void deleteContact(ContentResolver contactHelper, String number, Long id) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        String[] args = new String[]{String.valueOf(/*getContactID(contactHelper, number)*/id)};
        ops.add(ContentProviderOperation.newDelete(ContactsContract.RawContacts.CONTENT_URI)
                .withSelection(ContactsContract.RawContacts.CONTACT_ID + "=?", args).build());
        try {
            contactHelper.applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (RemoteException e) {
            Log.d(TAG, "SonCD: RemoteException EROR = " + e.toString());
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            Log.d(TAG, "SonCD: OperationApplicationException EROR = " + e.toString());
            e.printStackTrace();
        }
    }
    //SonCD: show dialog delete contact
    private void showDialogDeleteContact(final String number, final Long id, final int position) {
        AlertDialog.Builder buider = new AlertDialog.Builder(mContext)
                .setTitle(R.string.title_delete_contact)
                .setMessage(R.string.do_you_want_delete_contact)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteContact(mContext.getContentResolver(), number, id);
                        mListContacts.remove(position);
                        notifyDataSetChanged();
                    }
                }).setNegativeButton(android.R.string.cancel, null);
        buider.show();
    }

    private void showDialogBlockContac(final String number, boolean block, final int position){
        AlertDialog.Builder buider = new AlertDialog.Builder(mContext)
                .setTitle(R.string.title_block_contact)
                .setMessage(R.string.do_you_want_block_number)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BlackListManager.getInstance().insertToBlockCall(number);
                        BlackListManager.getInstance().setListContactsBlock(number, true);
                        mListContacts.get(position).setIsBlock(true);
                        notifyDataSetChanged();
                    }
                }).setNegativeButton(android.R.string.cancel, null);
        buider.show();
    }

    private void showDialogUnBlockContac(final String number, final boolean block, final int position){
        AlertDialog.Builder buider = new AlertDialog.Builder(mContext)
                .setTitle(R.string.title_unblock_contact)
                .setMessage(R.string.do_you_want_un_block_number)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BlackListManager.getInstance().setListContactsBlock(number, block);
                        mListContacts.get(position).setIsBlock(false);
                        notifyDataSetChanged();
                    }
                }).setNegativeButton(android.R.string.cancel, null);
        buider.show();
    }
    //SonCD: get Uri Contact
    private Uri getContactUri(int positon) {
        long contactId = mListContacts.get(positon).getIdContact();
        Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
        return uri;
    }
    //SonCD: ADD contact to start
    private void addToStar(ContentResolver cr, Uri contactUri) {
        ContentValues values = new ContentValues(1);
        values.put(ContactsContract.Contacts.STARRED, 1);
        cr.update(contactUri, values, null, null);
    }
    //SonCD: remove contact star
    private void remveFromStar(ContentResolver cr, Uri contactUri){
        ContentValues values = new ContentValues(1);
        values.put(ContactsContract.Contacts.STARRED, 0);
        cr.update(contactUri, values, null, null);
    }
}
