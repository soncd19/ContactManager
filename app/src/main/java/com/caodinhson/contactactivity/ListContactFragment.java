package com.caodinhson.contactactivity;

import android.Manifest;
import android.app.SearchManager;
import android.content.AsyncQueryHandler;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.adapter.ContactsAdapter;
import com.adapter.DividerItemDecoration;
import com.adapter.RecyclerTouchListener;
import com.database.BlackListManager;
import com.melnykov.fab.FloatingActionButton;
import com.util.Contact;
import com.util.PermissionUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import static android.content.Context.SEARCH_SERVICE;

/**
 * Created by Cao Dinh Son on 11/13/2016.
 */

public class ListContactFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "ListContactFragment";

    private RecyclerView mRecyclerView;

    private TextView mTextEmpty;

    private ContactsAdapter mContactAdapter;

    private static final int CONTACTS_LIST_QUERY_TOKEN = 1701;

    private Context mContext;

    private View mView;

    private String mQueryString;

    private ArrayList<Contact> mListContact = new ArrayList<Contact>();

    private ContactContentObserver mContentObserver;

    private QueryContactHandler mQueryHandler;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
        mContactAdapter = new ContactsAdapter(mContext, mListContact);
        mContentObserver = new ContactContentObserver(this, new Handler());
        //SonCD: Fragment want to se OptionMenu
        setHasOptionsMenu(true);
        //SonCD: Check permission befor query contact from db
        if (!(ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_CONTACTS)
                != PackageManager.PERMISSION_GRANTED)) {
            mQueryHandler = new QueryContactHandler(this, mContext);
        }
    }


    //SonCD: show Snackbar request contact phone permission
    private void requestContactsPhonePermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                Manifest.permission.READ_CONTACTS)
                || ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                Manifest.permission.WRITE_CONTACTS)
                || ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                Manifest.permission.CALL_PHONE)
                || ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                Manifest.permission.ACCESS_NETWORK_STATE)) {

            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example, if the request has been denied previously.
            Log.i("SonCD",
                    "Displaying contacts permission rationale to provide additional context.");
            // Display a SnackBar with an explanation and a button to trigger the request.
            Snackbar snackbar = Snackbar.make(mView, R.string.permission_contacts_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat
                                    .requestPermissions(getActivity(), PermissionUtil.PERMISSIONS_CONTACT_PHONE,
                                            PermissionUtil.REQUEST_CONTACTS);
                        }
                    });
            snackbar.show();
            snackbar.setActionTextColor(Color.WHITE);
            View v = snackbar.getView();
            TextView txtv = (TextView) v.findViewById(android.support.design.R.id.snackbar_text);
            txtv.setTextColor(Color.WHITE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                v.setBackgroundColor(getContext().getColor(R.color.colorPrimaryDark));
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    v.setBackground(getContext().getResources().getDrawable(R.color.colorPrimaryDark));
                }
            }
        } else {
            // Contact permissions have not been granted yet. Request them directly.
            ActivityCompat.requestPermissions(getActivity(), PermissionUtil.PERMISSIONS_CONTACT_PHONE,
                    PermissionUtil.REQUEST_CONTACTS);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //SonCD: Check permission in android 6.0
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_CONTACTS)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_NETWORK_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            requestContactsPhonePermissions();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_contact_fragment, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        //mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //mRecyclerView.addOnItemTouchListener(onTouchListener);
        mRecyclerView.setAdapter(mContactAdapter);
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab1);
        fab.attachToRecyclerView(mRecyclerView);
        fab.setOnClickListener(this);
        mTextEmpty = (TextView) view.findViewById(R.id.list_contacts_empty);
        mView = view;
        return view;
    }

    private RecyclerTouchListener onTouchListener = new RecyclerTouchListener(mContext, mRecyclerView, new RecyclerTouchListener.ClickListener() {
        @Override
        public void onClick(View view, int position) {

        }

        @Override
        public void onLongClick(View view, int position) {

        }
    });

    @Override
    public void onResume() {
        super.onResume();
        mContext.getContentResolver()
                .registerContentObserver(ContactsContract.Contacts.CONTENT_URI, true, mContentObserver);
        startQueryContact();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab1:
                showDialogAddContact();
                break;
        }
    }

    private void showOrHideListContact(boolean show){
        if (show){
            //mRecyclerViewFavorite.setVisibility(View.VISIBLE);
            mTextEmpty.setVisibility(View.GONE);
        }else {
            //mRecyclerViewFavorite.setVisibility(View.GONE);
            mTextEmpty.setVisibility(View.VISIBLE);
        }
    }

    //SonCD: Query Handler
    public static class QueryContactHandler extends AsyncQueryHandler {

        private WeakReference<ListContactFragment> weakReference;

        public QueryContactHandler(ListContactFragment listContactFragment, Context context) {
            super(context.getContentResolver());
            weakReference = new WeakReference<ListContactFragment>(listContactFragment);

        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            super.onQueryComplete(token, cookie, cursor);
            try {
                switch (token) {
                    case CONTACTS_LIST_QUERY_TOKEN:
                        if (cursor == null || cursor.getCount() == 0){
                            weakReference.get().showOrHideListContact(false);
                        }else {
                            weakReference.get().showOrHideListContact(true);
                            try {
                                weakReference.get().mContactAdapter.appendCusor(cursor,
                                        weakReference.get().mQueryString, BlackListManager.getInstance().getListContactsBlock());
                            }finally {
                                cursor.close();
                            }
                        }
                }
            } catch (Exception e) {
                Log.d(TAG, "ATTT NullPointerException " + e.toString());
            }
        }
    }

    private void startQueryContact(){
        String sortOrder = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC";
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        mQueryHandler.startQuery(CONTACTS_LIST_QUERY_TOKEN, null, uri, null, null, null, sortOrder);
    }

    public ArrayList<Contact> loadContact() {
        if (TextUtils.isEmpty(mQueryString)) {
            mQueryString = "";
        }
        ArrayList<Contact> listContact;
        //Uri simUri = Uri.parse("content://icc/adn/");
        String sortOrder = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC";
        Cursor cursor = mContext.getContentResolver()
                .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, sortOrder);
        if (cursor != null && cursor.getCount() > 0){
            showOrHideListContact(true);
        }else {
            showOrHideListContact(false);
        }
        listContact = mContactAdapter.appendCusor(cursor, mQueryString, BlackListManager.getInstance().getListContactsBlock());
        return listContact;
    }

    //SonCD: get list contact from cursor
//    public ArrayList<Contact> appendCusor(Cursor cursor, String query) {
//        ArrayList<Contact> listContacts = new ArrayList<Contact>();
//        String name;
//        String number;
//        int id;
//        Contact contact;
//        if (cursor != null && cursor.getCount() > 0) {
//            cursor.moveToLast();
//            do {
//                contact = getContactFromCursor(cursor, mQueryString);
//                if (contact != null) {
//                    listContacts.add(contact);
//                }
//            } while (cursor.moveToPrevious());
//        }
//        //SonCD: SORT NAME
//        Collections.sort(listContacts, new Comparator<Contact>() {
//            @Override
//            public int compare(Contact o1, Contact o2) {
//                return o1.getNameContact().compareTo(o2.getNameContact());
//            }
//        });
//        return listContacts;
//    }
//
//    private Contact getContactFromCursor(Cursor cursor, String query){
//        String name;
//        String number;
//        int id;
//        id = cursor.getInt(cursor.getColumnIndex(ContactSimManager.CONTACT_ID));
//        name = cursor.getString(cursor.getColumnIndex(ContactSimManager.CONTACT_NAME));
//        number = cursor.getString(cursor.getColumnIndex(ContactSimManager.CONTACT_NUMBER));
//        if (TextUtils.isEmpty(name)){
//            name = "";
//        }
//        if (TextUtils.isEmpty(number)) {
//            number = "";
//        }
//        if (!TextUtils.isEmpty(query)){
//            boolean nameMatcher = name.toUpperCase().contains(query.toUpperCase());
//            boolean numberMatcher = number.contains(query);
//            if (nameMatcher){
//                Contact contact = new Contact(id, name, number);
//                return contact;
//            }
//        }else {
//            Contact contact = new Contact(id, name, number);
//            return contact;
//        }
//        return null;
//    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        //SonCD: inflater optionMenu
        inflater.inflate(R.menu.option_menu_contacts, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        //SonCD: Create SearchView with optionMenu
        MenuItem searchMenuItem = menu.findItem(R.id.item_menu_search_contact);
        SearchView menuSearchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
        SearchManager searchManager = (SearchManager) getContext().getSystemService(SEARCH_SERVICE);
        menuSearchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        menuSearchView.setOnQueryTextListener(onQueryTextListener);
    }

    //SonCD: On query text change
    private SearchView.OnQueryTextListener onQueryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            mQueryString = newText;
            ArrayList<Contact> contact = loadContact();
            mContactAdapter = new ContactsAdapter(mContext, contact);
            mRecyclerView.setAdapter(mContactAdapter);
            mContactAdapter.notifyDataSetChanged();
            return false;
        }
    };
    //SonCD: show dialog add new contact
    private void showDialogAddContact() {
        LayoutInflater inflater = getLayoutInflater(getArguments());
        View dialogLayout = inflater.inflate(R.layout.add_contact,
                (ViewGroup) getActivity().findViewById(R.id.layout_dialog_add_contact));
        final EditText addName = (EditText) dialogLayout.findViewById(R.id.textAddName);
        final EditText addNumber = (EditText) dialogLayout.findViewById(R.id.textAddNumber);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.add_contact).setView(dialogLayout)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = addName.getText().toString();
                        String number = addNumber.getText().toString();
                        if (!TextUtils.isEmpty(name) || !TextUtils.isEmpty(number)) {
                            ContentResolver cr = mContext.getContentResolver();
                            insertContact(cr, name, number);
                            Snackbar.make(mView, R.string.insert_contact_sucess, Snackbar.LENGTH_SHORT).show();
                            loadContact();
                        }
                    }
                }).setNegativeButton(android.R.string.cancel, null);
        builder.show();
    }

    //SonCD: Insert Contacts
    public static boolean insertContact(ContentResolver contactAdder, String firstName, String mobileNumber) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null).build());

        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, firstName).build());

        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI
        ).withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, mobileNumber)
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE).build());
        try {
            contactAdder.applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (Exception e) {
            Log.d(TAG, "SonCD: Insert contact error = " + e.toString());
            return false;
        }
        return true;
    }

    //Listener change database
    public class ContactContentObserver extends ContentObserver {
        private WeakReference<ListContactFragment> weakReference;

        public ContactContentObserver(ListContactFragment listContactFragment, Handler handler) {
            super(handler);
            weakReference = new WeakReference<ListContactFragment>(listContactFragment);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            Log.d(TAG, "SonCD: ContactContentObserver in ListFavorite");
            loadContact();
            mContactAdapter.notifyDataSetChanged();
        }
    }
}
