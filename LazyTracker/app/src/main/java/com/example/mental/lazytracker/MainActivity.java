package com.example.mental.lazytracker;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.view.View.OnClickListener;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity implements OnClickListener {

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

    private Boolean flag = false;
    private Boolean networkAvaliableFlag = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

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
        updateLocButton.setOnClickListener(this);
        findClosestButton = (Button) findViewById(R.id.findClosestButton);
        showMapButton = (Button) findViewById(R.id.showMapButton);

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if(checkNetwork(networkAvaliableFlag)) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            launchLoc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (launchLoc != null) {
                launchLong = launchLoc.getLongitude();
                launchLat = launchLoc.getLatitude();
                currLocTextView.setText("Τελευταία γνωστή τοποθεσία: Longtitude = " + coordinatesDf.format(launchLong) + " και latitude = " + coordinatesDf.format(launchLat) + "\n" +
                        "dieuthinsi " + getName(launchLoc, "address") + " poli " + getName(launchLoc, "city") + " xwra " + getName(launchLoc, "country"));
            }
        }
    }

    //checks if the network is active
    public boolean checkNetwork(boolean networkAvaliableFlag) {
        networkAvaliableFlag = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if(networkAvaliableFlag) {
            return true;
        }else {
            WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            wifi.setWifiEnabled(true); // true or false to activate/deactivate wifi
            if(wifi.isWifiEnabled()) {
                return true;
            }else {
                return false;
            }
        }
    }

    /*----Method to Check GPS is enable or disable ----- */
    private Boolean displayGpsStatus() {
        ContentResolver contentResolver = getBaseContext()
                .getContentResolver();
        boolean gpsStatus = Settings.Secure
                .isLocationProviderEnabled(contentResolver,
                        LocationManager.GPS_PROVIDER);
        if (gpsStatus) {
            return true;

        } else {
            Intent i = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(i);
            return true;
        }
    }

    public void onFindClosest(View v) {
        Cursor res = myDb.getData();
        if(res.getCount() == 0) {
            //empy database
            return;
        }else {
            StringBuffer buffer = new StringBuffer();
            while(res.moveToNext()) {
                buffer.append("ID " + res.getString(0) + "\nName " + res.getString(1) + "\nLong " + res.getString(2) + "\nLat " + res.getString(3) + "\n\n");
            }
            //closestMarketTextView.setText(buffer);
        }
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

    @Override
    public void onClick(View v) {
        flag = displayGpsStatus();
        if(flag) {
            locationListener = new MyLocationListener();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
        }
        updateLocButton.setEnabled(false);
    }

    public String getName(Location loc, String dataType) {
        String addressName = null;
        String postalCode = null;
        String cityName = null;
        String countryName = null;
        Geocoder gcd = new Geocoder(getBaseContext(),
                Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = gcd.getFromLocation(loc.getLatitude(), loc
                    .getLongitude(), 1);
            if (addresses.size() > 0) {
                if(dataType.equals("address"))
                    return addressName = addresses.get(0).getAddressLine(0);
                if(dataType.equals("city"))
                    return cityName = addresses.get(0).getLocality();
                if(dataType.equals("postal"))
                    return postalCode = addresses.get(0).getPostalCode();
                if(dataType.equals("country"))
                    return countryName = addresses.get(0).getCountryName();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            Toast.makeText(getBaseContext(),"Η τοποθεσία άλλαξε : Lat: " +
                            coordinatesDf.format(location.getLatitude())+ " Lng: " + coordinatesDf.format(location.getLongitude()),
                    Toast.LENGTH_SHORT).show();
            currLocTextView.setText("Τελευταία γνωστή τοποθεσία: Longtitude = " + coordinatesDf.format(location.getLongitude()) + " και latitude = " + coordinatesDf.format(location.getLatitude()) + "\n" +
                    "dieuthinsi " + getName(location, "address") + " poli " + getName(location, "city") + " xwra " + getName(location, "country"));

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
