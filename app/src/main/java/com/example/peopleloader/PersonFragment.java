package com.example.peopleloader;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.net.MalformedURLException;
import java.net.URL;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


/**
 * A {@link Fragment} to hold basic information about different people.
 */
public class PersonFragment extends Fragment {
    private static final String NAME_KEY = "NAME_KEY";
    private static final String BLURB_KEY = "BLURB_KEY";
    private static final String LOC_KEY = "LOC_KEY";

    @BindView (R.id.picture) protected ImageView mPicture;
    @BindView (R.id.name) protected TextView mName;
    @BindView (R.id.blurb) protected TextView mBlurb;
    private Unbinder unbinder;


    public PersonFragment() {
        // Required empty public constructor
    }

    static PersonFragment newInstance(String name, String blurb, String pictureLoc) {
        Bundle args = new Bundle();
        args.putString(NAME_KEY, name);
        args.putString(BLURB_KEY, blurb);
        args.putString(LOC_KEY, pictureLoc);

        PersonFragment instance = new PersonFragment();
        instance.setArguments(args);
        return instance;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_person, container, false);
        unbinder = ButterKnife.bind(this, rootView);

        Bundle args = getArguments();
        mName.setText(args.getString(NAME_KEY));
        mBlurb.setText(args.getString(BLURB_KEY));

        //Now start loading the image
        try {
            URL pictureURL = new URL(args.getString(LOC_KEY));

            RequestOptions baseRequest = new RequestOptions();
            baseRequest.placeholder(R.drawable.placeholder_picture)
                    .optionalCenterInside();

            Glide.with(this)
                    .load(pictureURL)
                    .apply(baseRequest)
                    .into(mPicture);
        } catch (MalformedURLException e) {
            //Load the default image if the URL is invalid
            RequestOptions centerRequest= new RequestOptions();
            centerRequest.optionalCenterInside();

            Glide.with(this)
                    .load(R.drawable.placeholder_picture)
                    .apply(centerRequest)
                    .into(mPicture);
        }

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Glide.with(this).clear(mPicture);
        unbinder.unbind();
    }

}
