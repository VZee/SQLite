package edu.osu.locationaware;

// Source: http://classes.engr.oregonstate.edu/eecs/spring2018/cs496/module-7/sqlite.html,
// Other CS 496 code from lectures

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import static com.google.android.gms.common.GooglePlayServicesUtil.getErrorDialog;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private TextView mLatText;
    private TextView mLonText;
    private TextView sampleLat;
    private TextView sampleLon;
    private Location mLastLocation;
    static double lon = -123.2;
    static double lat = 44.5;
    private LocationListener mLocationListener;
    private static final int LOCATION_PERMISSON_RESULT = 17;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mLatText = (TextView) findViewById(R.id.lat_output);
        mLonText = (TextView) findViewById(R.id.lon_output);
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(5000);
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (location != null) {
                    mLonText.setText(String.valueOf(location.getLongitude()));
                    mLatText.setText(String.valueOf(location.getLatitude()));

                    double myLat = location.getLatitude();
                    double myLon = location.getLongitude();

                    lat = myLat;
                    lon = myLon;

                } else {
                    mLonText.setText("No Location Avaliable");
                }
            }
        };

        Button horizontalViewItem = (Button) findViewById(R.id.view_db);
        horizontalViewItem.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NavigationActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        mLonText.setText("-123.2");
        mLatText.setText("44.5");

        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSON_RESULT);
            return;
        }
        updateLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Dialog errDialog = GoogleApiAvailability.getInstance().getErrorDialog(this, connectionResult.getErrorCode(), 0);
        errDialog.show();
        return;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        if(requestCode == LOCATION_PERMISSON_RESULT){
            if(grantResults.length > 0){
                updateLocation();
            }
        }
    }

    public void updateLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,mLocationListener);
        if(mLastLocation != null){
            // after location permissions are granted, display the correct lat / lon
            double myLat = mLastLocation.getLatitude();
            double myLon = mLastLocation.getLongitude();
            mLonText.setText(String.valueOf(myLon));
            mLatText.setText(String.valueOf(myLat));

            lon = mLastLocation.getLongitude();;
            lat = mLastLocation.getLatitude();;

        } else {
            mLonText.setText("-123.2");
            mLatText.setText("44.5");
            lat = 44.5;
            lon = -123.2;

            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,mLocationListener);
        }
    }

    public double getLat(){
        return lat;
    }

    public double getLon(){
        return lon;
    }
}

class NavigationActivity extends AppCompatActivity {
    // Variable setup
    edu.osu.locationaware.SQLiteExample mSQLiteExample;
    Button mSQLSubmitButton;
    Button navButton;
    Cursor mSQLCursor;
    SimpleCursorAdapter mSQLCursorAdapter;
    private static final String TAG = "SQLActivity";
    SQLiteDatabase mSQLDB;
    MainActivity myData = new MainActivity();
    TextView sampleText;
    TextView sampleLat;
    TextView sampleLon;

    // On Create
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sqlite);

        mSQLiteExample = new edu.osu.locationaware.SQLiteExample(this);
        mSQLDB = mSQLiteExample.getWritableDatabase();

        mSQLSubmitButton = (Button) findViewById(R.id.sql_add_row_button);
        /*
        Intent intent = new Intent(NavigationActivity.this, MainActivity.class);
        startActivity(intent);
        */

        getLocation();
        mSQLSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //getLocation();

                if(mSQLDB != null){
                    // get the values and insert into the database
                    //getLocation();
                    ContentValues vals = new ContentValues();
                    vals.put(edu.osu.locationaware.DBContract.DemoTable.COLUMN_NAME_DEMO_INT, ((EditText)findViewById(R.id.sql_text_input)).getText().toString() + " " + ((TextView)findViewById(R.id.textView9)).getText().toString() + " " + ((TextView)findViewById(R.id.textView10)).getText().toString());
                    mSQLDB.insert(edu.osu.locationaware.DBContract.DemoTable.TABLE_NAME,null,vals);
                    populateTable();
                } else {
                    Log.d(TAG, "Unable to access database for writing.");
                }
            }
        });

        // show all current values in the db
        populateTable();
    }

    private void populateTable(){
        // pull from the database to populate the table
        if(mSQLDB != null) {
            try {
                if(mSQLCursorAdapter != null && mSQLCursorAdapter.getCursor() != null){
                    if(!mSQLCursorAdapter.getCursor().isClosed()){
                        mSQLCursorAdapter.getCursor().close();
                    }
                }

                mSQLCursor = mSQLDB.query(edu.osu.locationaware.DBContract.DemoTable.TABLE_NAME,
                        new String[]{edu.osu.locationaware.DBContract.DemoTable._ID, edu.osu.locationaware.DBContract.DemoTable.COLUMN_NAME_DEMO_STRING,
                                edu.osu.locationaware.DBContract.DemoTable.COLUMN_NAME_DEMO_INT}, edu.osu.locationaware.DBContract.DemoTable.COLUMN_NAME_DEMO_INT + " > ?", new String[]{"100"}, null, null, null);
                ListView SQLListView = (ListView) findViewById(R.id.sql_list_view);
                mSQLCursorAdapter = new SimpleCursorAdapter(this,
                        R.layout.sql_item,
                        mSQLCursor,
                        new String[]{edu.osu.locationaware.DBContract.DemoTable.COLUMN_NAME_DEMO_STRING, edu.osu.locationaware.DBContract.DemoTable.COLUMN_NAME_DEMO_INT},
                        new int[]{R.id.sql_listview_string, R.id.sql_listview_int},
                        0);
                SQLListView.setAdapter(mSQLCursorAdapter);
            } catch (Exception e) {
                Log.d(TAG, "Error loading data from database");
            }
        }
        getLocation();
    }

    private void getLocation(){
        // set the lat and lon on our main view
        sampleLat = (TextView) findViewById(R.id.textView9);
        sampleLon = (TextView) findViewById(R.id.textView10);
        sampleLat.setText(String.valueOf(myData.getLat()));
        sampleLon.setText(String.valueOf(myData.getLon()));
    }
}

class SQLiteExample extends SQLiteOpenHelper {

    public SQLiteExample(Context context) {
        super(context, edu.osu.locationaware.DBContract.DemoTable.DB_NAME, null, edu.osu.locationaware.DBContract.DemoTable.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(edu.osu.locationaware.DBContract.DemoTable.SQL_CREATE_DEMO_TABLE);

        ContentValues testValues = new ContentValues();
        testValues.put(edu.osu.locationaware.DBContract.DemoTable.COLUMN_NAME_DEMO_INT, 42);
        testValues.put(edu.osu.locationaware.DBContract.DemoTable.COLUMN_NAME_DEMO_STRING, "Hello SQLite");
        db.insert(edu.osu.locationaware.DBContract.DemoTable.TABLE_NAME,null,testValues);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(edu.osu.locationaware.DBContract.DemoTable.SQL_DROP_DEMO_TABLE);
        onCreate(db);
    }
}

//final class DBContract {
class DBContract{
    private DBContract(){};

    public final class DemoTable implements BaseColumns {
        public static final String DB_NAME = "demo_db";
        public static final String TABLE_NAME = "demo";
        public static final String COLUMN_NAME_DEMO_STRING = "demo_string";
        public static final String COLUMN_NAME_DEMO_INT = "demo_int";
        public static final String COLUMN_NAME_LAT = "lat";
        public static final String COLUMN_NAME_LON = "lon";
        public static final int DB_VERSION = 4;


        public static final String SQL_CREATE_DEMO_TABLE = "CREATE TABLE " +
                edu.osu.locationaware.DBContract.DemoTable.TABLE_NAME + "(" + edu.osu.locationaware.DBContract.DemoTable._ID + " INTEGER PRIMARY KEY NOT NULL," +
                edu.osu.locationaware.DBContract.DemoTable.COLUMN_NAME_DEMO_STRING + " VARCHAR(255)," +
                edu.osu.locationaware.DBContract.DemoTable.COLUMN_NAME_DEMO_INT + " INTEGER);";

        public static final String SQL_TEST_DEMO_TABLE_INSERT = "INSERT INTO " + TABLE_NAME +
                " (" + COLUMN_NAME_DEMO_STRING + "," + COLUMN_NAME_DEMO_INT + ") VALUES ('test', 123);";

        public  static final String SQL_DROP_DEMO_TABLE = "DROP TABLE IF EXISTS " + edu.osu.locationaware.DBContract.DemoTable.TABLE_NAME;
    }
}
