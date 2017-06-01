package com.example.peopleloader;

import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PeopleViewerActivity extends AppCompatActivity {
    @BindView (R.id.people_pager) protected ViewPager mPeoplePager;
    private PageAdapter mAdapter;
    private DatabaseHelper mDatabaseHelper;

    private int mResumePosition = 0;

    private DeletePersonAsync mDelete = null;
    private LoadCursorAsync mLoad = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_people_viewer);
        ButterKnife.bind(this);

        mDatabaseHelper = new DatabaseHelper(this);
        mAdapter = new PageAdapter(getSupportFragmentManager());

        mLoad = new LoadCursorAsync();
        mLoad.execute(mDatabaseHelper);

        mPeoplePager.setAdapter(mAdapter);

        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException e) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDelete != null) {
            mDelete.cancel(false);
        }
        if (mLoad != null) {
            mLoad.cancel(false);
        }
        if (mAdapter != null) {
            mAdapter.closeCursor();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                Intent upIntent = NavUtils.getParentActivityIntent(this);
                upIntent.putExtra(LoadActivity.DB_STATUS_EXTRA, true);
                NavUtils.navigateUpTo(this, upIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Callback for the search button
     *
     * Launches a web browser
     */
    @OnClick(R.id.search_person_button)
    public void searchCurrentPerson() {
        if (mDelete == null) {
            String name = mAdapter.getPersonName(mPeoplePager.getCurrentItem());
            Intent launchBrowser = new Intent(Intent.ACTION_VIEW);
            launchBrowser.setData(Uri.parse(formattedSearch(name)));
            startActivity(launchBrowser);
        }
    }

    @OnClick(R.id.delete_button)
    public void deleteCurrentPerson() {
        if (mDelete == null) {
            int currentPosition = mPeoplePager.getCurrentItem();
            mDelete = new DeletePersonAsync(mAdapter.getPersonName(currentPosition));
            if (currentPosition > 0) {
                mResumePosition = currentPosition - 1;
            } else if (mAdapter.getCount() > 1) {
                mResumePosition = currentPosition;
            } // Last case is that this is the only remaining item

            mDelete.execute(mDatabaseHelper);
        }
    }

    private String formattedSearch(String name) {
        String baseQuery = getString(R.string.base_query);
        for (String nameSegment: name.split(" ")) {
            baseQuery += nameSegment + "+";
        }
        //Use substring to remove the last '+' character
        return baseQuery.substring(0, baseQuery.length() - 1);
    }

    private class PageAdapter extends FragmentStatePagerAdapter {
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

        public String getPersonName(int position) {
            mPeopleCursor.moveToPosition(position);
            return mPeopleCursor
                    .getString(mPeopleCursor.getColumnIndex(DatabaseHelper.COLUMN_PERSON));
        }

        public void closeCursor() {
            mPeopleCursor.close();
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
                if (cursor.getCount() > 0) {
                    return cursor;
                } else {
                    cursor.close();
                    return null;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            if (cursor != null) {
                mAdapter.setPeopleCursor(cursor);
                mAdapter.notifyDataSetChanged();
                mPeoplePager.setCurrentItem(mResumePosition, false);
            } else {
                Intent upIntent = NavUtils.getParentActivityIntent(PeopleViewerActivity.this);
                upIntent.putExtra(LoadActivity.DB_STATUS_EXTRA, false);
                NavUtils.navigateUpTo(PeopleViewerActivity.this, upIntent);
            }
            mLoad = null;
        }
    }

    /**
     * Subclass of (@link AsyncTask) to delete a person from the database
     * Sets the adapter for the ViewPager to null to force it to release fragments
     * and remake them.
     *
     * There is probably some inherited internal state from FragmentStatePagerAdapter
     * that could be overwritten to get the same effect.
     */
    private class DeletePersonAsync extends AsyncTask<DatabaseHelper,Void,Void> {
        private String mName;

        public DeletePersonAsync(String name) {
            mName = name;
        }

        @Override
        public void onPreExecute() {
            mPeoplePager.setAdapter(null);
            mAdapter.closeCursor();
            mAdapter = new PageAdapter(getSupportFragmentManager());
        }

        @Override
        protected Void doInBackground(DatabaseHelper... params) {
            if (params.length > 0) {
                SQLiteDatabase db = params[0].getWritableDatabase();
                db.delete(DatabaseHelper.TABLE,
                        getString(R.string.delete_format, DatabaseHelper.COLUMN_PERSON, mName),
                        null);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void ignore) {
            mPeoplePager.setAdapter(mAdapter);
            mLoad = new LoadCursorAsync();
            mLoad.execute(mDatabaseHelper);
            mDelete = null;
        }

    }

}
