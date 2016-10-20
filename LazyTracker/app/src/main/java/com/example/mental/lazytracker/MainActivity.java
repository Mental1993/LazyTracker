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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity implements OnClickListener {



    //Class objects
    DatabaseHelper myDb;

    //Number formats
    private static DecimalFormat coordinatesDf = new DecimalFormat(".####");
    private static DecimalFormat distanceDf = new DecimalFormat("###,###.##");

    ArrayList<Location> listLoc;

    //Widgets
    private TextView currLocTextView;
    private TextView closestMarketTextView;
    private Button updateLocButton;
    private Button findClosestButton;
    private Button showMapButton;

    //Location Manager and Listener
    private LocationManager locationManager = null;
    private LocationListener locationListener = null;

    //Launch Location variables
    private Location launchLoc = null;
    private double launchLong = 0;
    private double launchLat = 0;

    private Boolean flag = false;
    private Boolean networkAvaliableFlag = false;
    private int index;
    private float min;


    ConnectionDetector cd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //locationObject = new LocationClass();
        myDb = new DatabaseHelper(this);

       // myDb.onCreate(myDb.getWritableDatabase());
        myDb.deleteTableData(myDb.getWritableDatabase());
        insertLocation(myDb, "Super Market 1", 23.549635, 41.090289);
        insertLocation(myDb, "Super Market 2", 23.547200, 41.090798);
        insertLocation(myDb, "Super Market 3", 23.552884, 41.092136);


        //Lock screen for always Portrait mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //Initializations
        currLocTextView = (TextView) findViewById(R.id.currLocTextView);
        closestMarketTextView = (TextView) findViewById(R.id.closestMarketTextView);
        updateLocButton = (Button) findViewById(R.id.updateLocButton);
        updateLocButton.setOnClickListener(this);
        findClosestButton = (Button) findViewById(R.id.findClosestButton);
        showMapButton = (Button) findViewById(R.id.showMapButton);

        //Internet connection detector
        cd = new ConnectionDetector(this);
        if (cd.isConnected())
        { }
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Warning");
            builder.setCancelable(false);
            builder.setMessage("Internet Connection Required");
            builder.setNegativeButton("OK", new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.dismiss();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();

        }

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
                        "διεύθυνση " + getName(launchLoc, "address") + " πόλη " + getName(launchLoc, "city") + " χώρα " + getName(launchLoc, "country"));
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
            listLoc = new ArrayList<>();
            min = 1000000000;
            int i = 0;
            index = -1;
            while(res.moveToNext()) {
                Location loc = new Location(res.getString(1));
                loc.setLongitude(Double.parseDouble(res.getString(2)));
                loc.setLatitude(Double.parseDouble(res.getString(3)));
                listLoc.add(loc);
                if(launchLoc.distanceTo(listLoc.get(i)) < min) {
                    min = launchLoc.distanceTo(listLoc.get(i));
                    index = i;
                }
                i++;
            }
            closestMarketTextView.setText("Κοντινότερη απόσταση είναι " + distanceDf.format(min) + "m \n "+listLoc.get(index).getProvider()+" με διευθυνση " + getName(listLoc.get(index), "address"));
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
        switch(v.getId()) {
            case R.id.updateLocButton:
            flag = displayGpsStatus();
                if (flag) {
                    locationListener = new MyLocationListener();

                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 2, locationListener);
                }
                updateLocButton.setText("Η τοποθεσία θα ανανεώνεται αυτόματα");
                updateLocButton.setEnabled(false);
                break;
        }
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
        return "Δεν υπάρχουν πληροφορίες";
    }




    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            launchLoc = location;
            Toast.makeText(getBaseContext(),"Η τοποθεσία άλλαξε : Lat: " +
                            coordinatesDf.format(location.getLatitude())+ " Lng: " + coordinatesDf.format(location.getLongitude()),
                    Toast.LENGTH_SHORT).show();
            currLocTextView.setText("Τελευταία γνωστή τοποθεσία: Longtitude = " + coordinatesDf.format(location.getLongitude()) + " και latitude = " + coordinatesDf.format(location.getLatitude()) + "\n" +
                    "διεύθυνση " + getName(location, "address") + " πόλη " + getName(location, "city") + " χώρα " + getName(location, "country"));
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
