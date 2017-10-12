package com.example.android.habittrackerapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.habittrackerapp.MainActivity.TaskDbHelper.TaskContract.TaskEntry;

public class MainActivity extends AppCompatActivity {

    private TaskDbHelper taskDbHelper;

    private EditText tNameEditText;
    private EditText tDateEditText;
    private CheckBox tDoneCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tNameEditText = (EditText) findViewById(R.id.edit_name_task);
        tDateEditText = (EditText) findViewById(R.id.edit_date_task);
        tDoneCheckBox = (CheckBox) findViewById(R.id.check_done_task);

        final TextView displayTextView = (TextView) findViewById(R.id.displayTextView);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                insertTask();
                displayDatabaseInfo();
            }
        });

        taskDbHelper= new TaskDbHelper(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        insertDefaultTask();
        displayDatabaseInfo();
    }

    @Override
    protected void onDestroy() {
        taskDbHelper.close();
        super.onDestroy();
    }

    private void displayDatabaseInfo() {

        SQLiteDatabase db = taskDbHelper.getReadableDatabase();

        String[] projection = {
                TaskEntry._ID,
                TaskEntry.COLUMN_TASK_NAME,
                TaskEntry.COLUMN_DATE,
                TaskEntry.COLUMN_DONE};

        // Perform a query on the tasks table
        Cursor cursor = db.query(
                TaskEntry.TABLE_NAME,   // The table to query
                projection,            // The columns to return
                null,                  // The columns for the WHERE clause
                null,                  // The values for the WHERE clause
                null,                  // Don't group the rows
                null,                  // Don't filter by row groups
                null);                   // The sort order

        TextView displayView = (TextView) findViewById(R.id.displayTextView);

        try {
            displayView.setText("The tasks table contains " + cursor.getCount() + " tasks.\n\n");
            displayView.append(TaskEntry._ID + " | " +
                    TaskEntry.COLUMN_TASK_NAME + " | " +
                    TaskEntry.COLUMN_DATE + " | " +
                    TaskEntry.COLUMN_DONE + " | " + "\n");

            // Figure out the index of each column
            int idColumnIndex = cursor.getColumnIndex(TaskEntry._ID);
            int nameColumnIndex = cursor.getColumnIndex(TaskEntry.COLUMN_TASK_NAME);
            int dateColumnIndex = cursor.getColumnIndex(TaskEntry.COLUMN_DATE);
            int doneColumnIndex = cursor.getColumnIndex(TaskEntry.COLUMN_DONE);

            while (cursor.moveToNext()) {

                int currentID = cursor.getInt(idColumnIndex);
                String currentName = cursor.getString(nameColumnIndex);
                String currentBreed = cursor.getString(dateColumnIndex);
                int currentGender = cursor.getInt(doneColumnIndex);

                displayView.append(("\n" + currentID + " | " +
                        currentName + " | " +
                        currentBreed + " | " +
                        currentGender));
            }
        } finally {
            cursor.close();
        }
    }

    private void insertTask(){
        String nameString = tNameEditText.getText().toString().trim();
        String dateString = tDateEditText.getText().toString().trim();
        int tDone ;
        if(tDoneCheckBox.isChecked())
           tDone = TaskEntry.DONE_TRUE;
        else
            tDone = TaskEntry.DONE_FALSE;

        SQLiteDatabase db = taskDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TaskEntry.COLUMN_TASK_NAME, nameString);
        values.put(TaskEntry.COLUMN_DATE, dateString);
        values.put(TaskEntry.COLUMN_DONE, tDone);

        long newRowId = db.insert(TaskEntry.TABLE_NAME, null, values);

        // Show a toast message depending on whether or not the insertion was successful
        if (newRowId == -1) {
            // If the row ID is -1, then there was an error with insertion.
            Toast.makeText(this, "Error with saving task", Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the insertion was successful and we can display a toast with the row ID.
            Toast.makeText(this, "Task saved with row id: " + newRowId, Toast.LENGTH_SHORT).show();
        }
    }

    private void insertDefaultTask() {
        // Gets the database in write mode
        SQLiteDatabase db = taskDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TaskEntry.COLUMN_TASK_NAME, "Go to the gum");
        values.put(TaskEntry.COLUMN_DATE, "15/10/2017");
        values.put(TaskEntry.COLUMN_DONE, TaskEntry.DONE_FALSE);

        long newRowId = db.insert(TaskEntry.TABLE_NAME, null, values);
    }


    public class TaskDbHelper extends SQLiteOpenHelper {

        private final String LOG_TAG = TaskDbHelper.class.getSimpleName();

        /** Name of the database file */
        private static final String DATABASE_NAME = "Task.db";

        private static final int DATABASE_VERSION = 1;

        private static final String SQL_DELETE_TASKS_TABLE =
                "DROP TABLE IF EXISTS " + TaskEntry.TABLE_NAME;

        // Create a String that contains the SQL statement to create the tasks table
        private static final String SQL_CREATE_TASKS_TABLE =  "CREATE TABLE "
                + TaskEntry.TABLE_NAME + " ("
                + TaskEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + TaskEntry.COLUMN_TASK_NAME + " TEXT NOT NULL, "
                + TaskEntry.COLUMN_DATE + " TEXT, "
                + TaskEntry.COLUMN_DONE + " INTEGER NOT NULL DEFAULT 0);";

        /**
         * Constructs a new instance of {@link TaskDbHelper}.
         *
         * @param context of the app
         */
        private TaskDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        /**
         * This is called when the database is created for the first time.
         */
        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.v(LOG_TAG,SQL_CREATE_TASKS_TABLE);
            Log.v(LOG_TAG,"onCreate");
            // Execute the SQL statement
            db.execSQL(SQL_CREATE_TASKS_TABLE);
        }

        /**
         * This is called when the database needs to be upgraded.
         */
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.v(LOG_TAG,SQL_DELETE_TASKS_TABLE);
            Log.v(LOG_TAG,"onUpgrade");
            db.execSQL(SQL_DELETE_TASKS_TABLE);
            onCreate(db);
        }
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.v(LOG_TAG,"onDowngrade");
            onUpgrade(db, oldVersion, newVersion);
        }

        public final class TaskContract {

            private TaskContract() {}

            public final class TaskEntry implements BaseColumns {

                private final static String TABLE_NAME = "tasks";

                private final static String _ID = BaseColumns._ID;

                private final static String COLUMN_TASK_NAME ="task";
                private final static String COLUMN_DATE = "date";
                private final static String COLUMN_DONE = "done";

                /**
                 * Possible values for the done column.
                 */
                private static final int DONE_FALSE = 0;
                private static final int DONE_TRUE = 1;
            }

        }
    }

}
