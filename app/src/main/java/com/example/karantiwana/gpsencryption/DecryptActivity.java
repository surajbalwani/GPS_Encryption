package com.example.karantiwana.gpsencryption;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
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
import javax.crypto.CipherInputStream;
import javax.crypto.spec.SecretKeySpec;


public class DecryptActivity extends ActionBarActivity implements LocationListener {

    ImageButton imgbt;
    ImageButton help;
    private LocationManager locationManager;
    Button btdec2;
    TextView tvmsg;
    private TextView latitudeField;
    private TextView longitudeField;
    private TextView filepath;
    private String FilePath;
    private String provider;
    public static float longitude;
    public static float latitude;
    public static String path;
    public static String ky;
    private static final int PICKFILE_RESULT_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decrypt);
        latitudeField = (TextView) findViewById(R.id.tvlat);
        longitudeField = (TextView) findViewById(R.id.tvlong);
        filepath = (TextView) findViewById(R.id.textView2);
        btdec2 = (Button) findViewById(R.id.btdec);
        imgbt = (ImageButton) findViewById(R.id.imgbtenc);
        tvmsg = (TextView) findViewById(R.id.tvmsg);

    /*    //Checks if GPS is on if not then redirects to location settings
        String GpsProvider = Settings.Secure.getString(getContentResolver(),Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if(GpsProvider.equals("")){
            //GPS Disabled
            AlertDialog alertDialog = new AlertDialog.Builder(DecryptActivity.this).create();

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

        } */


        imgbt.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {

                startActivity(new Intent(DecryptActivity.this, MainActivity.class));
            }
        });

        help = (ImageButton) findViewById(R.id.help);
        help.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                startActivity(new Intent(DecryptActivity.this, Help.class));
            }
        });

        // Decryption button event
        btdec2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                tvmsg.setText("");
                if(String.valueOf(latitudeField.getText()) == "Location not available") {
                    tvmsg.setText("Please wait for the location to be found, check if you have enabled GPS location and/or connected to the internet for better precision");
                }else{
                    tvmsg.setText("");
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("file/*");
                startActivityForResult(intent,PICKFILE_RESULT_CODE);

            }}
        });



        // Get the location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Define the criteria how to select the locatioin provider -> use
        // default
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

    //Returns File path
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        /*switch(requestCode){
            case PICKFILE_RESULT_CODE:
                if(resultCode==RESULT_OK){
                    String FilePath = data.getData().getPath();
                    filepath.setText(String.valueOf(FilePath));
                }
                break;

        }*/

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


                final ProgressDialog ringProgressDialog = ProgressDialog.show(DecryptActivity.this, "Please wait ...", "Decrypting File ...", true);
                ringProgressDialog.setCancelable(true);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            FileDecryption.main(null);
                            tvmsg.setText("Decryption Complete");
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
        float key = log*10000 + lat*10000;
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
        getMenuInflater().inflate(R.menu.menu_decrypt, menu);
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

class FileDecryption
{

    private String algorithm;
    private File file;
    private File file2;

    public FileDecryption(String algorithm,String path)
    {
        this.algorithm=algorithm;
        this.file=new File(path);
        this.file2=new File(path);

    }

    public void decrypt() throws Exception
    {
        //opening streams
        FileInputStream fis =new FileInputStream(file);
        String s = file.getAbsolutePath();
        int len = s.length();
        String s1 = s.substring(0, len-4);
        file=new File(s1);
        FileOutputStream fos =new FileOutputStream(file);

        //generating same key
        byte k[] = DecryptActivity.ky.getBytes();
        SecretKeySpec key = new SecretKeySpec(k,"DES");

        //creating and initialising cipher and cipher streams
        Cipher decrypt =  Cipher.getInstance(algorithm);
        decrypt.init(Cipher.DECRYPT_MODE, key);
        CipherInputStream cin=new CipherInputStream(fis, decrypt);

        byte[] buf = new byte[1024];
        int read=0;

        while((read=cin.read(buf))!=-1)  //reading encrypted data
        {
            fos.write(buf,0,read);       //writing decrypted data
        }

        //closing streams
        cin.close();
        fos.flush();
        fos.close();

        //file2.delete();
    }

    public static void main (String[] args)throws Exception
    {
        //create a file name called test.txt then execute it else Excpetion occurs
        //new FileDecryption("DES/ECB/PKCS5Padding", MainActivity.path).encrypt();
        new FileDecryption("DES/ECB/PKCS5Padding",DecryptActivity.path).decrypt();


    }
}
