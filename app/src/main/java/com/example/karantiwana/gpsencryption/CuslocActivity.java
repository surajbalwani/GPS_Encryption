package com.example.karantiwana.gpsencryption;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;


public class CuslocActivity extends ActionBarActivity {
    EditText etlog; 
    EditText etlat; 
    Button btenc; 
    public static String path;
    private static final int PICKFILE_RESULT_CODE = 1;
    public static float longitude;
    public static float latitude;
    private TextView filepath;
    private String FilePath;
    public static String ky;
    TextView tvmsg;
     double latval;
     double logval;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cusloc);
        etlog = (EditText) findViewById(R.id.Longitude);
        etlat = (EditText) findViewById(R.id.Latitude);
        btenc = (Button) findViewById(R.id.btcuslocfileselect);
        tvmsg = (TextView) findViewById(R.id.tvmsg);
        etlat.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(5,3)});
        etlog.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(6,3)});
        filepath = (TextView) findViewById(R.id.path);



        btenc.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if(String.valueOf(etlat.getText()).equals("") || String.valueOf(etlog.getText()).equals("")) {
                    etlat.setError("This field must be filled");
                    etlog.setError("This field must be filled");

                }else{
                    String a = etlat.getText().toString();
                    latval = Double.parseDouble(a);
                    String b = etlog.getText().toString();
                    logval = Double.parseDouble(b);

                    if(latval > 90 || logval < -90 || logval > 180 || logval < -180){
                        etlat.setError("Latitude must be between +90 and -90");
                        etlog.setError("Longitude must be between +180 and -180");
                    }
                    else {
                        tvmsg.setText("");
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("file/*");
                        startActivityForResult(intent, PICKFILE_RESULT_CODE);
                    }
                }




            }});
    }

    // Returns the file path
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub


        if (requestCode == PICKFILE_RESULT_CODE){
            if (resultCode == RESULT_OK) {
            FilePath = data.getData().getPath();
            filepath.setText(String.valueOf(FilePath));
            path = filepath.getText().toString();


            String a = etlog.getText().toString().trim();
            longitude = Float.parseFloat(a);

            String b = etlat.getText().toString().trim();
            latitude = Float.parseFloat(b);

           ky = keygen(longitude,latitude);

                final ProgressDialog ringProgressDialog = ProgressDialog.show(CuslocActivity.this, "Please wait ...", "Encrypting File ...", true);
                ringProgressDialog.setCancelable(true);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {

                        CustomFileEncryption.main(null);
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


        }
     }


    public String keygen(float log, float lat)
    {
        float key =  log*10000 + lat*10000;
        String k = String.valueOf(key);
        return k;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_cusloc, menu);
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

class CustomFileEncryption
{

    private String algorithm;
    private File file;
    private File file2;

    public CustomFileEncryption(String algorithm,String path)
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
        byte k[] = CuslocActivity.ky.getBytes();
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
        new CustomFileEncryption("DES/ECB/PKCS5Padding", CuslocActivity.path).encrypt();


    }
}

class DecimalDigitsInputFilter implements InputFilter {

    Pattern mPattern;

    public DecimalDigitsInputFilter(int digitsBeforeZero,int digitsAfterZero) {
        mPattern=Pattern.compile("[0-9]{0," + (digitsBeforeZero-1) + "}+((\\.[0-9]{0," + (digitsAfterZero-1) + "})?)||(\\.)?");
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

        Matcher matcher=mPattern.matcher(dest);
        if(!matcher.matches())
            return "";
        return null;
    }

}
