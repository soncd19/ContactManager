package com.caodinhson.contactactivity;

import android.Manifest;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.database.BlackListManager;
import com.util.PermissionUtil;

import java.util.ArrayList;
import java.util.List;

public class ContactsActivity extends AppCompatActivity {

    private Context mContext;

    private DrawerLayout mDrawerLayout;

    protected static final int NAV_DRAWER_ITEM_INVALID = -1;

    private static final String TAG = "ContactsActivity";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "SonCD: Vao ContactsActivity dau tien");
        setContentView(R.layout.activity_contacts);
        mContext = getApplicationContext();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //SonCD: Set view pager
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        //SonCD: Set Tabs inside Toolbar
        TabLayout tabs = (TabLayout) findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        //SonCD: Adding menu icon to Toolbar
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            VectorDrawableCompat indicator
                    = VectorDrawableCompat.create(getResources(), R.drawable.ic_menu, getTheme());
            indicator.setTint(ResourcesCompat.getColor(getResources(),R.color.white,getTheme()));
            supportActionBar.setHomeAsUpIndicator(indicator);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
        //SonCD: Set behavior of Navigation drawer
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        mDrawerLayout.closeDrawers();
                        onNavigationItemClicked(menuItem.getItemId());
                        return true;
                    }
                });
    }
    //SonCD click Navigation item
    private void onNavigationItemClicked(final int itemId) {
        if (itemId == getSelfNavDrawerItem()) {
            closeDrawer();
            return;
        }
        goToNavDrawerItem(itemId);
    }

    protected int getSelfNavDrawerItem() {
        return NAV_DRAWER_ITEM_INVALID;
    }

    //SonCD: action click navigation
    private void goToNavDrawerItem(int item) {
        switch (item) {
            case R.id.list_contacts_block:
                // TODO: 11/13/2016
                break;
            case  R.id.syn_contacts:
                // TODO: 11/13/2016   
                break;
            case R.id.delete_all_contacts:
                // TODO: 11/13/2016  
                break;
            case R.id.about_contact_app:
                closeDrawer();
                showDialogAbout();
                break;
        }
    }
     //SonCD: Show dialog about
    private void showDialogAbout() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.about_fragment,
                (ViewGroup) findViewById(R.id.layout_about_contact));
        TextView textVersion = (TextView) dialogLayout.findViewById(R.id.about_version);
        TextView textEmail = (TextView) dialogLayout.findViewById(R.id.email);
        TextView textNumber = (TextView) dialogLayout.findViewById(R.id.about_number);
        PackageInfo packageInfo = null;
        String version = null;
        try {
            packageInfo = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0);
            version = packageInfo.versionName;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(getApplicationContext().getResources().getString(R.string.version_name_app)).append(": ").append(version);
            version = stringBuilder.toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        textVersion.setText(version);
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.myDialog))
                .setView(dialogLayout)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        builder.show();
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //SonCD: reCreate activity if permission ok
        if (requestCode == PermissionUtil.REQUEST_CONTACTS) {
            Log.i(TAG, "Received response for contact permissions request.");
            // We have requested multiple permissions for contacts, so all of them need to be
            // checked.
            if (PermissionUtil.verifyPermissions(grantResults)) {
                super.recreate();
            } else {
               finish();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /*getMenuInflater().inflate(R.menu.option_menu_contacts, menu);
        MenuItem searchMenuItem = menu.findItem(R.id.item_menu_search_contact);
        SearchView menuSearchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        menuSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        menuSearchView.setOnQueryTextListener(onQueryTextListener);*/
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                openDrawer();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    //SonCD: OPEN NAVIGATION
    protected void openDrawer() {
        if (mDrawerLayout == null)
            return;

        mDrawerLayout.openDrawer(GravityCompat.START);
    }
    //SonCD: CLOSE NAVIGATION
    protected void closeDrawer() {
        if (mDrawerLayout == null)
            return;

        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    //SonCD: On query text change
    private SearchView.OnQueryTextListener onQueryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            return false;
        }
    };

    //SonCD: Add Fragments to Tabs
    private void setupViewPager(ViewPager viewPager) {
        Adapter adapter = new Adapter(getSupportFragmentManager());
        adapter.addFragment(new ListContactFragment(),
                mContext.getResources().getString(R.string.title_name_list_contacts));
        adapter.addFragment(new ListFavoriteFragment(),
                mContext.getResources().getString(R.string.title_name_list_contacts_star));
        adapter.addFragment(new InformationFragment(),
                mContext.getResources().getString(R.string.title_name_infomation));
        viewPager.setAdapter(adapter);
    }
    //SonCD: Adapter add Fragment
    static class Adapter extends FragmentPagerAdapter{

        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();


        public Adapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

        public void addFragment(Fragment fragment, String title){
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }
    }
}
