package com.example.peopleloader;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
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
    private PageAdapter mAdapter;
    private DatabaseHelper mDatabaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_people_viewer);
        ButterKnife.bind(this);

        mDatabaseHelper = new DatabaseHelper(this);
        mAdapter = new PageAdapter(getSupportFragmentManager());

        LoadCursorAsync loader = new LoadCursorAsync();
        loader.execute(mDatabaseHelper);

        mPeoplePager.setAdapter(mAdapter);

    }

    private class PageAdapter extends FragmentPagerAdapter {
        private Cursor mPeopleCursor = null;

        public PageAdapter(FragmentManager fm) {
            super(fm);
        }

        public Fragment getItem(int position) {
            if (mPeopleCursor != null) {
                mPeopleCursor.moveToPosition(position);

                String name = mPeopleCursor.getString(mPeopleCursor.getColumnIndex(DatabaseHelper.COLUMN_PERSON));
                String blurb = mPeopleCursor.getString(mPeopleCursor.getColumnIndex(DatabaseHelper.COLUMN_BLURB));
                String picture_loc = mPeopleCursor.getString(mPeopleCursor.getColumnIndex(DatabaseHelper.COLUMN_PICTURE_LOC));

                return PersonFragment.newInstance(name, blurb, picture_loc);
            }
            return null;
        }

        public int getCount() {
            if (mPeopleCursor == null) {
                return 0;
            } else {
                return mPeopleCursor.getCount();
            }
        }

        public void setPeopleCursor(Cursor cursor) {
            mPeopleCursor = cursor;
        }
    }


    /**
     * Subclass of (@link AsyncTask) to load a cursor to our database
     */
    private class LoadCursorAsync extends AsyncTask<DatabaseHelper,Void,Cursor> {
        @Override
        protected Cursor doInBackground(DatabaseHelper... params) {
            if (params.length > 0 ) {
                SQLiteDatabase db = params[0].getReadableDatabase();
                Cursor cursor = db.query(DatabaseHelper.TABLE,
                        new String[]{
                                DatabaseHelper.COLUMN_PERSON,
                                DatabaseHelper.COLUMN_BLURB,
                                DatabaseHelper.COLUMN_PICTURE_LOC},
                        null, null, null, null, DatabaseHelper.COLUMN_PERSON);

                //Perform an operation to force cursor to load now
                cursor.getCount();
                return cursor;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            mAdapter.setPeopleCursor(cursor);
            mAdapter.notifyDataSetChanged();
        }
    }

}
