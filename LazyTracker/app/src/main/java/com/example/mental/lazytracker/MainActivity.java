package com.example.mental.lazytracker;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;

public class MainActivity extends Activity {

    DatabaseHelper myDb;

    //Number formats
    private static DecimalFormat coordinatesDf = new DecimalFormat(".####");
    private static DecimalFormat distanceDf = new DecimalFormat("###,###.##");

    //Widgets
    private TextView currLocTextView;
    private TextView closestMarketTextView;
    private Button updateLocButton;
    private Button findClosestButton;
    private Button showMapButton;

    //Location Manager and Listener
    private LocationManager locationManager = null;
    private LocationListener locationListener = null;

    //Current Location variables
    private Location launchLoc = null;
    private double launchLong = 0;
    private double launchLat = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myDb = new DatabaseHelper(this);
        insertLocation(myDb, "Super Market 1", 41.090289, 23.549635);
        insertLocation(myDb, "Super Market 2", 41.090798, 23.547200);
        insertLocation(myDb, "Super Market 3", 41.092136, 23.552884);

        //Lock screen for always Portrait mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //Initializations
        currLocTextView = (TextView) findViewById(R.id.currLocTextView);
        closestMarketTextView = (TextView) findViewById(R.id.closestMarketTextView);
        updateLocButton = (Button) findViewById(R.id.updateLocButton);
        findClosestButton = (Button) findViewById(R.id.findClosestButton);
        showMapButton = (Button) findViewById(R.id.showMapButton);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //----------MISSING --- CHECK IS THERE IS NETWORK CONNECTION----------//
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        launchLoc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (launchLoc != null) {
            launchLong = launchLoc.getLongitude();
            launchLat = launchLoc.getLatitude();
            currLocTextView.setText("Τελευταία γνωστή τοποθεσία: Longtitude = " + coordinatesDf.format(launchLong) + " και latitude = " + coordinatesDf.format(launchLat));
        }

    }

    public void onUpdateLocation(View v) {
        //---------MISSING --- CHECK IF GPS IS ENABLED ---------//
        locationListener = new MyLocationListener();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
    }

    public void insertLocation(DatabaseHelper db, String name, double longt, double lat) {
        boolean isInserted = db.insertData(name, longt, lat);
        if(isInserted) {
            Toast.makeText(MainActivity.this, "Data inserted", Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(MainActivity.this, "Data not inserted", Toast.LENGTH_LONG).show();
        }

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.my_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            Toast.makeText(getBaseContext(),"Η τοποθεσία άλλαξε : Lat: " +
                            coordinatesDf.format(location.getLatitude())+ " Lng: " + coordinatesDf.format(location.getLongitude()),
                    Toast.LENGTH_SHORT).show();
            String longitude = "Longitude: " +coordinatesDf.format(location.getLongitude());
            //Log.v(TAG, longitude);
            String latitude = "Latitude: " +coordinatesDf.format(location.getLatitude());
            //Log.v(TAG, latitude);
            currLocTextView.setText("Τελευταία γνωστή τοποθεσία: Longtitude = " + coordinatesDf.format(location.getLongitude()) + " και latitude = " + coordinatesDf.format(location.getLatitude()));
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }

}
