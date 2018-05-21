package com.example.karantiwana.gpsencryption;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.LocationProvider;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.DecimalFormat;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;


public class MainActivity extends ActionBarActivity implements LocationListener {
    Button btcusloc;
    ImageButton imgbt;
    ImageButton help;
    Button btenc;
    TextView tvmsg;
    private LocationManager locationManager;
    private TextView latitudeField;
    private TextView longitudeField;
    private TextView filepath;
    private String FilePath;
    private String provider;
    public static float longitude;
    public static float latitude;
    public static String path;
    private static final int PICKFILE_RESULT_CODE = 1;
    public static String ky;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        latitudeField = (TextView) findViewById(R.id.tvlat);
        longitudeField = (TextView) findViewById(R.id.tvlong);
        filepath = (TextView) findViewById(R.id.tvfilepath);
        btcusloc = (Button) findViewById(R.id.btenccusloc);
        btenc = (Button) findViewById(R.id.btencgpsloc);
        tvmsg = (TextView) findViewById(R.id.tvmsg);


        //Checks if GPS is on if not then redirects to location settings
        String GpsProvider = Settings.Secure.getString(getContentResolver(),Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if(GpsProvider.equals("")){
            //GPS Disabled
            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();

            // Setting Dialog Title
            alertDialog.setTitle("GPS disabled");

            // Setting Dialog Message
            alertDialog.setMessage("Please enable location to use this app.");

            // Setting Icon to Dialog
            alertDialog.setIcon(R.drawable.ic_launcher);

            // Setting OK Button
            alertDialog.setButton("Go To Settings", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS );
                    startActivity(intent);
                }
            });
            alertDialog.show();

        }else{
            //GPS Enabled

        }

        // Encrypt this location button event
        btenc.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                tvmsg.setText("");
                if(String.valueOf(latitudeField.getText()) == "Location not available") {
                    tvmsg.setText("Please wait for the location to be found, check if you have enabled GPS location and/or connected to the internet for better precision");
                }else{
                    tvmsg.setText("");
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("file/*");
                    startActivityForResult(intent, PICKFILE_RESULT_CODE);
                } 
                
            }});

      // custom loc button event
     btcusloc.setOnClickListener(new View.OnClickListener() {
         public void onClick(View v) {

             startActivity(new Intent(MainActivity.this, CuslocActivity.class));
         }
     });

    imgbt = (ImageButton) findViewById(R.id.imgbtdec);
    imgbt.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {

            startActivity(new Intent(MainActivity.this, DecryptActivity.class));
        }
    });

    help = (ImageButton) findViewById(R.id.help);
    help.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                startActivity(new Intent(MainActivity.this, Help.class));
         }
        });


        // Get the location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Define the criteria how to select the locatioin provider -> use default
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        Location location = locationManager.getLastKnownLocation(provider);

        // Initialize the location fields
        if (location != null) {
            System.out.println("Provider " + provider + " has been selected.");
            onLocationChanged(location);
        } else {
            latitudeField.setText("Location not available");
            longitudeField.setText("Location not available");
        }
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                makeUseOfNewLocation(location);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }

    /* Request updates at startup */
    protected void onResume() {
        super.onResume();
        locationManager.requestLocationUpdates(provider, 400, 1, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);

    }

    /* Remove the locationlistener updates when Activity is paused */
    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {

        float lat = (float) (location.getLatitude());
        float lng = (float) (location.getLongitude());
        // to trunc it to 4dp
        String lat2 = new DecimalFormat("##.###").format(lat);
        String lng2 = new DecimalFormat("##.###").format(lng);
        // display in textbox
        latitudeField.setText(String.valueOf(lat2));
        longitudeField.setText(String.valueOf(lng2));



    }
    //@Override
    public void makeUseOfNewLocation(Location location) {

        float lat = (float) (location.getLatitude());
        float lng = (float) (location.getLongitude());
        // to trunc it to 4dp
        String lat2 = new DecimalFormat("##.###").format(lat);
        String lng2 = new DecimalFormat("##.###").format(lng);
        latitudeField.setText(String.valueOf(lat2));
        longitudeField.setText(String.valueOf(lng2));

    }


    // Returns the file path
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub

        if (requestCode == PICKFILE_RESULT_CODE) {
            if (resultCode == RESULT_OK) {
                FilePath = data.getData().getPath();
                filepath.setText(String.valueOf(FilePath));
                path = filepath.getText().toString();

                String a = longitudeField.getText().toString();
                longitude = Float.parseFloat(a);
                String b = latitudeField.getText().toString();
                latitude = Float.parseFloat(b);

                ky = keygen(longitude,latitude);


                final ProgressDialog ringProgressDialog = ProgressDialog.show(MainActivity.this, "Please wait ...", "Encrypting File ...", true);
                ringProgressDialog.setCancelable(true);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {

                        FileEncryption.main(null);
                        tvmsg.setText("Encryption Complete");
                        Thread.sleep(10000);
                        } catch (Exception e) {

                        }
                        ringProgressDialog.dismiss();
                    }
                }).start();

                if (tvmsg.getText().toString().equals("")){
                    tvmsg.setText("An error occurred while decrypting, please try again!");
                }
            }

            }}


    public String keygen(float log, float lat)
    {
        float key =  log*10000 + lat*10000;
        String k = String.valueOf(key);
        return k;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(this, "Enabled new provider " + provider,
                Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, "Disabled provider " + provider,
                Toast.LENGTH_SHORT).show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



}

class FileEncryption
{

    private String algorithm;
    private File file;
    private File file2;

    public FileEncryption(String algorithm,String path)
    {
        this.algorithm=algorithm;
        this.file=new File(path);
        this.file2=new File(path);

    }

    public void encrypt() throws Exception
    {
        FileInputStream fis =new FileInputStream(file);
        file=new File(file.getAbsolutePath()+".enc");
        FileOutputStream fos =new FileOutputStream(file);

        //generating key
        byte k[] = MainActivity.ky.getBytes();
        SecretKeySpec key = new SecretKeySpec(k,"DES");

        //creating and initialising cipher and cipher streams
        Cipher encrypt =  Cipher.getInstance(algorithm);
        encrypt.init(Cipher.ENCRYPT_MODE, key);
        CipherOutputStream cout=new CipherOutputStream(fos, encrypt);

        byte[] buf = new byte[1024];
        int read;

        while((read=fis.read(buf))!=-1)  //reading data
            cout.write(buf,0,read);      //writing encrypted data

        //closing streams
        fis.close();
        cout.flush();
        cout.close();

        file2.delete();

    }


    public static void main (String[] args)throws Exception
    {
        //create a file name called test.txt then execute it else Excpetion occurs


        new FileEncryption("DES/ECB/PKCS5Padding", MainActivity.path).encrypt();


    }
}
