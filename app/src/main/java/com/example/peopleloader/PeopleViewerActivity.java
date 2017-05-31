package com.example.peopleloader;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PeopleViewerActivity extends AppCompatActivity {
    @BindView (R.id.people_pager) protected ViewPager mPeoplePager;
    private pageAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_people_viewer);
        ButterKnife.bind(this);

        mAdapter = new pageAdapter(getSupportFragmentManager());

        mPeoplePager.setAdapter(mAdapter);


    }

    class pageAdapter extends FragmentPagerAdapter {
        public pageAdapter(FragmentManager fm) {
            super(fm);
        }

        public Fragment getItem(int position) {
            return new PersonFragment();
        }

        public int getCount() {
            return 0;
        }
    }
}
