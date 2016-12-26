package com.caodinhson.contactactivity;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Cao Dinh Son on 11/13/2016.
 */

public class InformationFragment extends Fragment {

    private String mVersion;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PackageInfo packageInfo;
        try {
            packageInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            mVersion = packageInfo.versionName;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(getActivity().getResources().getString(R.string.version_name_app)).append(": ").append(mVersion);
            mVersion = stringBuilder.toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.about_fragment, container, false);
        TextView textVersion = (TextView) view.findViewById(R.id.about_version);
        TextView textEmail = (TextView) view.findViewById(R.id.email);
        TextView textNumber = (TextView) view.findViewById(R.id.about_number);
        //ImageView imageView = (ImageView) view.findViewById(R.id.about_image);
        textVersion.setText(mVersion);
        textVersion.setTypeface(Typeface.DEFAULT_BOLD);
        textEmail.setTypeface(Typeface.DEFAULT_BOLD);
        textNumber.setTypeface(Typeface.DEFAULT_BOLD);
        //imageView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.xinh));
        return view;

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
