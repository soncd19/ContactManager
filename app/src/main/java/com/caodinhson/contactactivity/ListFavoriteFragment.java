package com.caodinhson.contactactivity;

import android.app.SearchManager;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
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
import com.database.BlackListManager;
import com.melnykov.fab.FloatingActionButton;
import com.util.Contact;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import static android.content.Context.SEARCH_SERVICE;

/**
 * Created by Cao Dinh Son on 11/13/2016.
 */

public class ListFavoriteFragment extends Fragment {

    public static final String TAG = "ListFavoriteFragment";

    private Context mContext;

    private RecyclerView mRecyclerViewFavorite;

    private TextView mTextEmpty;

    private ContactsAdapter mContactFavoriteAdapter;

    private String mQueryString;

    private ArrayList<Contact> listContact = new ArrayList<>();

    private ContactContentObserver mContentObserver;

    public static final int TOKEN_QUERY_LIST_CONTACT_FAVORITE = 1012;

    private QueryContactFavoriteHandler mQueryContactFavorite;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
        mContactFavoriteAdapter = new ContactsAdapter(mContext, listContact);
        mContentObserver = new ContactContentObserver(this, new Handler());
        mQueryContactFavorite = new QueryContactFavoriteHandler(mContext, this);
        setHasOptionsMenu(true);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_contact_fragment, container, false);
        mRecyclerViewFavorite = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerViewFavorite.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerViewFavorite.setLayoutManager(mLayoutManager);
        //mRecyclerViewFavorite.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
        mRecyclerViewFavorite.setItemAnimator(new DefaultItemAnimator());
        mRecyclerViewFavorite.setAdapter(mContactFavoriteAdapter);
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab1);
        fab.attachToRecyclerView(mRecyclerViewFavorite);
        mTextEmpty = (TextView) view.findViewById(R.id.list_contacts_empty);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mContext.getContentResolver()
                .registerContentObserver(ContactsContract.Contacts.CONTENT_URI, true, mContentObserver);
        startQueryContactFavorite();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void showOrHideListContact(boolean show){
        if (show){
            mRecyclerViewFavorite.setVisibility(View.VISIBLE);
            mTextEmpty.setVisibility(View.GONE);
        }else {
            mRecyclerViewFavorite.setVisibility(View.GONE);
            mTextEmpty.setVisibility(View.VISIBLE);
        }
    }

    public ArrayList<Contact> loadContact() {
        if (TextUtils.isEmpty(mQueryString)) {
            mQueryString = "";
        }
        ArrayList<Contact> listContact;
        String sortOrder = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC";
        String selection = ContactsContract.Contacts.STARRED + " != 0";
        Cursor cursor = mContext.getContentResolver()
                .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                        selection, null, sortOrder);
        if (cursor != null && cursor.getCount() > 0){
            showOrHideListContact(true);
        }else {
            showOrHideListContact(false);
        }
        listContact = mContactFavoriteAdapter.appendCusor(cursor, mQueryString, BlackListManager.getInstance().getListContactsBlock());
        return listContact;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
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
            mContactFavoriteAdapter = new ContactsAdapter(mContext, contact);
            mRecyclerViewFavorite.setAdapter(mContactFavoriteAdapter);
            mContactFavoriteAdapter.notifyDataSetChanged();
            return false;
        }
    };

    //Listener change database
    public class ContactContentObserver extends ContentObserver {
        private WeakReference<ListFavoriteFragment> weakReference;

        public ContactContentObserver(ListFavoriteFragment listFavoriteFragment, Handler handler) {
            super(handler);
            weakReference = new WeakReference<ListFavoriteFragment>(listFavoriteFragment);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            Log.d(TAG, "SonCD: ContactContentObserver in ListFavorite");
            loadContact();
            mContactFavoriteAdapter.notifyDataSetChanged();
        }
    }
    //SoCD: query contact favorite handler
    private void startQueryContactFavorite(){
        Uri uri =  ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String sortOrder = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC";
        String selection = ContactsContract.Contacts.STARRED + " != 0";
        mQueryContactFavorite.startQuery(TOKEN_QUERY_LIST_CONTACT_FAVORITE, null, uri, null, selection, null, sortOrder);
    }
    //SoCD: create QueryContactFavoriteHandler
    public static class QueryContactFavoriteHandler extends AsyncQueryHandler{

        private WeakReference<ListFavoriteFragment> weakReference;

        public QueryContactFavoriteHandler(Context context, ListFavoriteFragment listFavoriteFragment) {
            super(context.getContentResolver());
            weakReference = new WeakReference<ListFavoriteFragment>(listFavoriteFragment);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            super.onQueryComplete(token, cookie, cursor);
            switch (token){
                case TOKEN_QUERY_LIST_CONTACT_FAVORITE:
                    if (cursor == null || cursor.getCount() == 0){
                        weakReference.get().showOrHideListContact(false);
                    }else {
                        weakReference.get().showOrHideListContact(true);
                        weakReference.get().mContactFavoriteAdapter.appendCusor(cursor, weakReference.get().mQueryString, BlackListManager.getInstance().getListContactsBlock());
                    }
                    break;
            }
        }
    }
}
