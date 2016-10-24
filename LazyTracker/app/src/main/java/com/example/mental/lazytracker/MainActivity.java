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
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends  Activity implements OnClickListener {



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
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my_menu, menu);

        return true;
    }

    //Εμφανιση Overflow menu στο ActionBar
    private void getOverflowMenu() {

        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if(menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.help:

                return true;

            case R.id.about:

                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        int bgColor = Color.parseColor("#b3ffff");
        getWindow().getDecorView().setBackgroundColor(bgColor);
        this.getOverflowMenu();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //locationObject = new LocationClass();
        myDb = new DatabaseHelper(this);

       // myDb.onCreate(myDb.getWritableDatabase());
        myDb.deleteTableData(myDb.getWritableDatabase());
        insertLocation(myDb, "Σουπερ Μαρκετ ΜΑΣΟΥΤΗΣ", 23.550997, 41.082586);
        insertLocation(myDb, "Express Market", 23.543846, 41.080730);
        insertLocation(myDb, "ΑΒ Βασιλοπουλος", 23.539560, 41.079535);
        insertLocation(myDb, "LIDL", 23.541276, 41.073916);
        insertLocation(myDb, "Listamarket.gr", 23.547875, 41.081870);
        insertLocation(myDb, "Σουπερ Μαρκετ ΚΑΝΤΖΑΣ", 23.554258, 41.091485);
        insertLocation(myDb, "DISCOUNT Market", 23.540027, 41.087345);
        insertLocation(myDb, "Carrefour Μαρινοπουλος", 23.547199, 41.090786);
        insertLocation(myDb, "Smile Markets", 23.552825, 41.083949);
        insertLocation(myDb, "DIA Market", 23.549402, 41.085429);
        insertLocation(myDb, "ΠΡΟΟΔΟΣ Μαρκετ", 23.541903, 41.081967);
        insertLocation(myDb, "METRO CASH & CARRY", 23.539825, 41.063589);


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

    //Exit DialogBox
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        System.exit(0);
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
            //Toast.makeText(MainActivity.this, "Data inserted", Toast.LENGTH_LONG).show();
        }else {
            //Toast.makeText(MainActivity.this, "Data not inserted", Toast.LENGTH_LONG).show();
        }

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

    public void onShowMapPressed(View v) {
        try {
            //----------MISSING------------
            //CHECK FOR NETWORK AND GPS AVALIABILITY
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/dir/" + launchLoc.getLatitude() + "," + launchLoc.getLongitude() + "/" + listLoc.get(index).getLatitude() + "," + listLoc.get(index).getLongitude()));
            startActivity(intent);
        }catch (NullPointerException e) {
            Toast.makeText(MainActivity.this, "Δεν έχει βρεθεί η κοντινότερη απόσταση ακόμα.", Toast.LENGTH_LONG).show();
        }
    }

    //Dialog for "About" button
    public void showAlertAbout(MenuItem item) {
        AlertDialog.Builder myAlert = new AlertDialog.Builder(this);
        myAlert.setMessage("Version 1.0\n" +
        "\n" +
        "CREATED WITH\n" +
                "\n" +
                "ANDROID STUDIO VERSION 2.2.2 | MINIMUM SDK 15 | Gradle - Version 2.14.1")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(R.mipmap.ic_launcher_48)
                .setTitle("About")
                .create();
        myAlert.show();
    }

    //Dialog For "Help" button
    public void showAlertHelp(MenuItem item) {
        AlertDialog.Builder myAlert = new AlertDialog.Builder(this);
        myAlert.setMessage("H εφαρμογη Lazy Tracker χρησιμοποιει την τρεχουσα τοποθεσια σας για να υπολογισει την αποσταση σας απο το κοντινοτερο super market\n" +
                "\n" +
                "Απαιτειται συνδεση στο Internet (WiFi ή δεδομενα) και ενεργοποιημενο GPS")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(R.mipmap.ic_launcher_48)
                .setTitle("Help")
                .create();
        myAlert.show();
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
