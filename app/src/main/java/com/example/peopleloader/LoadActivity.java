package com.example.peopleloader;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/*
 *
 */

public class LoadActivity extends AppCompatActivity {
    @BindView (R.id.url_entry) protected EditText mUrlEntry;
    @BindView (R.id.progressBar) protected ProgressBar mProgressBar;
    @BindView (R.id.view_button) protected Button mViewButton;
    @BindView (R.id.add_button) protected Button mAddButton;
    private DatabaseHelper mDatabaseHelper;
    private LoadPeopleTask mLoadPeopleTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load);
        ButterKnife.bind(this);
        mDatabaseHelper = new DatabaseHelper(this);
    }

    @OnClick (R.id.add_button)
    public void add() {
        //If we are not already loading people
        if (mLoadPeopleTask == null ) {
            try {
                URL url = new URL(mUrlEntry.getText().toString());
                mLoadPeopleTask = new LoadPeopleTask();
                mLoadPeopleTask.execute(url);
            } catch (MalformedURLException e) {
                Toast.makeText(this, R.string.url_invalid, Toast.LENGTH_SHORT).show();
            }
        }

    }

    @OnClick (R.id.view_button)
    public void viewPeople() {
        //Go to a new activity to load people from the database
    }

    class LoadPeopleTask extends AsyncTask<URL, Void, Boolean> {
        LoadPeopleTask() {
            //Empty Package Private Constructor
        }

        @Override
        protected void onPreExecute() {
            mProgressBar.setVisibility(View.VISIBLE);
            mAddButton.setEnabled(false);
            mViewButton.setEnabled(false);

        }

        @Override
        protected Boolean doInBackground(URL... loadFrom) {

            //Attempt to load people from the specified URL
            boolean successStatus = true;
            try {
                URLConnection connection = loadFrom[0].openConnection();
                connection.setAllowUserInteraction(true);
                connection.connect();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                String urlPath = loadFrom[0].toString();
                String localPath = urlPath.substring(0, urlPath.lastIndexOf(File.separator) + 1);

                SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
                ContentValues cv = new ContentValues();
                int lineNumber = 0;
                String line = reader.readLine();
                while (line != null && !isCancelled()) {
                    //Data in files expected to be in format:
                    //<Person Name><Newline>
                    //<Descriptive Blurb><Newline>
                    //<Local address of Picture><Newline>
                    //etc...
                    switch (lineNumber % 3) {
                        case 0:
                            cv.put(DatabaseHelper.COLUMN_PERSON, line);
                            break;
                        case 1:
                            cv.put(DatabaseHelper.COLUMN_BLURB, line);
                            break;
                        case 2:
                            cv.put(DatabaseHelper.COLUMN_PICTURE_LOC, localPath + line);
                            db.insertWithOnConflict(DatabaseHelper.TABLE,
                                    DatabaseHelper.COLUMN_PICTURE_LOC, cv,
                                    SQLiteDatabase.CONFLICT_IGNORE);
                            break;
                    }
                    lineNumber++;
                    line = reader.readLine();
                }
                if (lineNumber == 0) {
                    //If there was not data to read this was unsuccessful
                    successStatus = false;
                }
            } catch(IOException e) {
                //Attempt was unsuccessful
                successStatus = false;
            }

            return successStatus;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(LoadActivity.this, R.string.download_complete, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(LoadActivity.this, R.string.download_failed, Toast.LENGTH_SHORT).show();
            }
            //Now Update UI
            mProgressBar.setVisibility(View.INVISIBLE);
            mAddButton.setEnabled(true);
            mViewButton.setEnabled(true);
            mLoadPeopleTask = null;

        }

    }

}
