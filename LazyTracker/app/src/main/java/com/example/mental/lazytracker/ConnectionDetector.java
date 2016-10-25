package com.example.mental.lazytracker;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;


public class ConnectionDetector
{
    private Context context;

    public ConnectionDetector(Context context)
    {
        this.context = context;
    }

    public boolean isConnected()
    {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Service.CONNECTIVITY_SERVICE);
        if(cm != null)
        {
            NetworkInfo info = cm.getActiveNetworkInfo();
            if(info != null)
            {
                if(info.getState() == NetworkInfo.State.CONNECTED)
                {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isNotConnected()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Warning");
        builder.setCancelable(false);
        builder.setMessage("Internet Connection Required");
        builder.setNegativeButton("OK", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
                Intent i = new Intent(Settings.ACTION_WIFI_SETTINGS);
                context.startActivity(i);


            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

        return true;
    }

}
