package amolkhot.hackathon.knowyournation.Screens;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.net.URL;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import com.caverock.androidsvg.SVG;

import amolkhot.hackathon.knowyournation.Adapters.BorderCountriesAdapter;
import amolkhot.hackathon.knowyournation.Controllers.DataController;
import amolkhot.hackathon.knowyournation.Controllers.ViewController;
import amolkhot.hackathon.knowyournation.R;

public class CountryDetails extends AppCompatActivity {
    private static String TAG = "CountryDetails";
    private int selected_country_index=0;
    private int areaVal=0;
    private float latitude=0,longitude=0;
    private ImageView flagImage;
    private String imageUrl,border_countries;
    private TextView bordersText,regionText,subregionText,mapDataText;
    private LinearLayout regionContainer,subRegionContainer,mapDataContainer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_country_details);
        this.overridePendingTransition(R.anim.screen_enter_anim, R.anim.screen_exit_anim);
        flagImage = (ImageView) findViewById(R.id.flagImage);
        bordersText = (TextView) findViewById(R.id.bordersText);
        regionText = (TextView) findViewById(R.id.regionText);
        subregionText = (TextView) findViewById(R.id.subregionText);
        mapDataText = (TextView) findViewById(R.id.mapDataText);
        regionContainer = (LinearLayout) findViewById(R.id.regionContainer);
        subRegionContainer = (LinearLayout) findViewById(R.id.subRegionContainer);
        mapDataContainer = (LinearLayout) findViewById(R.id.mapDataContainer);
        setSelectedCountryIndex(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        this.overridePendingTransition(R.anim.screen_enter_anim, R.anim.screen_exit_anim);
        setSelectedCountryIndex(intent);
    }

    @Override
    public void finish() {
        super.finish();
        this.overridePendingTransition(0, R.anim.screen_exit2_anim);
    }

    private void setSelectedCountryIndex(Intent intent){
        String tmpTitle = intent.getStringExtra("selected_country_title");
        selected_country_index= DataController.getInstance().getCountryIndex(tmpTitle);
        if(selected_country_index == -1) finish();
        loadData();
    }
    private void loadData(){
        try {
            JSONObject countryData = (new JSONObject(DataController.getInstance().countryData.getString(selected_country_index)));
            final String countryName=countryData.getString("name");
            String countryNativeName = countryData.getString("nativeName");
            CountryDetails.this.setTitle(countryName);
            if(!countryName.equals(countryNativeName))
                ((TextView)findViewById(R.id.countryName)).setText(countryName);
            else
                ((TextView)findViewById(R.id.countryName)).setText("");
            ((TextView)findViewById(R.id.countryNativeName)).setText(countryNativeName);
            Log.i(TAG, "FLAG URL : " + countryData.get("flag"));
            imageUrl = (String) countryData.get("flag");
            new HttpImageRequestTask().execute();

            regionText.setText(countryData.getString("region"));
            regionText.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);

            JSONArray latlng = countryData.getJSONArray("latlng");
            latitude = Float.parseFloat(latlng.get(0).toString());
            longitude = Float.parseFloat(latlng.get(1).toString());
            mapDataText.setText(Html.fromHtml(latitude + "&#176;," + longitude+"&#176;"));
            mapDataText.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);

            regionContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    launchRegionCountries((String) regionText.getText());
                }
            });
            subRegionContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    launchSubRegionCountries((String) subregionText.getText());
                }
            });
            mapDataContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int zoom=5; // 2 - zoom out , 7-zoom in
                    zoom = 7-(Math.round(areaVal/2000000)+1);
                    if(zoom<=2) zoom=3;
                    ViewController.getInstance().launchMap(latitude,longitude,zoom,countryName);
                }
            });

            if((countryData.getString("subregion").equals(""))){
                subregionText.setText("-");
                subregionText.setPaintFlags(0);
                subregionText.setTextColor(getResources().getColor(R.color.details_color1));
            }else{
                subregionText.setText(countryData.getString("subregion"));
                subregionText.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
                subregionText.setTextColor(getResources().getColor(R.color.colorPrimary));
            }

            String areaText = countryData.getString("area") + " km<sup><small>2</small></sup>";
            areaVal = countryData.getInt("area");

            ((TextView) findViewById(R.id.areaText)).setText(Html.fromHtml(areaText));
            ((TextView) findViewById(R.id.areaText)).setHint("-");
            ((TextView) findViewById(R.id.capitalText)).setText((countryData.getString("capital").equals(""))?"-":countryData.getString("capital"));

            ((TextView) findViewById(R.id.populationText)).setText(countryData.getString("population"));

            String callingCodes=countryData.getJSONArray("callingCodes").getString(0);
            callingCodes = callingCodes.replaceAll("[\"]+","");
            ((TextView) findViewById(R.id.callingCodesText)).setText((callingCodes.equals(""))?"-":"+" + callingCodes);

            String timezoneText="";
            JSONArray timezones = countryData.getJSONArray("timezones");
            for(int t=0;t<timezones.length();t++){
                if(t!=0) timezoneText=timezoneText+"<BR>";
                String tmpTime = getTimeFromTimezone(timezones.getString(t));
                timezoneText = timezoneText  + "<B>Current Time : </B>"+ tmpTime + " <sup><small>[ " + timezones.getString(t)+" ]</small></sup>";
            }
            ((TextView) findViewById(R.id.timezoneText)).setText(Html.fromHtml(timezoneText));

            border_countries = countryData.getJSONArray("borders").toString();
            border_countries = DataController.getInstance().getCountryNameFromCode(border_countries);
            if(border_countries.equals("")){
                border_countries="-";
                bordersText.setPaintFlags(0);
                bordersText.setTextColor(getResources().getColor(R.color.details_color1));
            }else{
                bordersText.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
                bordersText.setTextColor(getResources().getColor(R.color.colorPrimary));
                bordersText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        launchCountrySelector();
                    }
                });
            }
            bordersText.setText(Html.fromHtml(border_countries));
            String currencies="";
            JSONArray currenciesArray = countryData.getJSONArray("currencies");
            for(int i=0;i<currenciesArray.length();i++) {
                if(i!=0) currencies = currencies + "<br/>";
                currencies = currencies + ""+(new JSONObject(currenciesArray.getString(i))).getString("name")+" ("+(new JSONObject(currenciesArray.getString(i))).getString("symbol")+")";
            }
            ((TextView) findViewById(R.id.currenciesText)).setText(Html.fromHtml(currencies));

            String languages="";
            JSONArray languagesArray = countryData.getJSONArray("languages");
            for(int i=0;i<languagesArray.length();i++) {
                if(i!=0) languages = languages + "<br/>";
                languages = languages + ""+(new JSONObject(languagesArray.getString(i))).getString("name")+" ("+(new JSONObject(languagesArray.getString(i))).getString("nativeName")+")";
            }
            ((TextView) findViewById(R.id.languagesText)).setText(Html.fromHtml(languages));


        }catch (Exception e){
            Log.e(TAG, "ERROR: " + e.getMessage());
        }
    }

    private String getTimeFromTimezone(String timezone){
        Log.i(TAG, "getTimeFromTimezone() : timezone : " + timezone);
        TimeZone tz = TimeZone.getTimeZone(timezone.replace("UTC","GMT"));
        Calendar c = Calendar.getInstance(tz);
        String time = String.format("%02d" , c.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d" , c.get(Calendar.MINUTE)) + ":" + String.format("%02d" , c.get(Calendar.SECOND));
        Log.i(TAG, "getTimeFromTimezone() : time : " + time);
        return time;
    }

    private boolean isNetworkAvailable() {
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private class HttpImageRequestTask extends AsyncTask<Void, Void, Drawable> {
        @Override
        protected Drawable doInBackground(Void... params) {
            Log.i(TAG,"HttpImageRequestTask | doInBackground | " + imageUrl);
            try {
                final URL url = new URL(imageUrl);
                if(isNetworkAvailable()) {
                    InputStream flagImageInputStream = url.openStream();
                    SVG svg = SVG.getFromInputStream(flagImageInputStream);
                    if (svg.getDocumentWidth() != -1) {
                        Bitmap newBM = Bitmap.createBitmap((int) Math.ceil(svg.getDocumentWidth()), (int) Math.ceil(svg.getDocumentHeight()), Bitmap.Config.ARGB_8888);
                        Canvas bmcanvas = new Canvas(newBM);
                        bmcanvas.drawRGB(255, 255, 255);
                        svg.renderToCanvas(bmcanvas);
                        return new BitmapDrawable(getResources(), newBM);
                    }
                }
                return null;
            } catch (Exception e) {
                Log.e(TAG," ERROR : "+ e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Drawable drawable) {
            updateImageView(drawable);
        }
    }
    private void updateImageView(Drawable drawable){
        Log.i(TAG,"updateImageView()");
        flagImage.setImageDrawable(null);
        if(drawable != null){
            // Try using your library and adding this layer type before switching your SVG parsing
            Log.i(TAG,"updateImageView() | Loading Image");
            flagImage.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            flagImage.setImageDrawable(drawable);
            flagImage.animate().setDuration(1000).alpha(1).setStartDelay(200);
        }else{
            Log.i(TAG, "updateImageView() | Loading Default FLAG");
            flagImage.setImageResource(R.drawable.flag_image_not_found);
            flagImage.animate().setDuration(1000).alpha(1).setStartDelay(200);
        }
    }

    private void launchRegionCountries(String region){
        Log.i(TAG, "launchRegionCountries('" + region + "')");
        AlertDialog.Builder popupBuilder = new AlertDialog.Builder(CountryDetails.this);
        popupBuilder.setTitle(region);
        String coutryList = DataController.getInstance().getCountryNameFromRegion(region);
        String[] borderCountryListArray=coutryList.split(",");
        final ArrayList<String> borCountryList = new ArrayList<>();
        for (int i = 0; i < borderCountryListArray.length; ++i) { borCountryList.add(borderCountryListArray[i]); }
        popupBuilder.setAdapter(new BorderCountriesAdapter(this, android.R.layout.simple_list_item_1, borCountryList), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String country_name = borCountryList.get(which);
                Log.i(TAG,"__________SELECTED ('"+country_name+"')");
                ViewController.getInstance().launchCountryDetails(CountryDetails.this,country_name);
            }
        });
        AlertDialog country_selector = popupBuilder.create();
        country_selector.show();
    }

    private void launchSubRegionCountries(String subRegion){
        Log.i(TAG, "launchSubRegionCountries('" + subRegion + "')");
        AlertDialog.Builder popupBuilder = new AlertDialog.Builder(CountryDetails.this);
        popupBuilder.setTitle(subRegion);
        String coutryList = DataController.getInstance().getCountryNameFromSubRegion(subRegion);
        String[] borderCountryListArray=coutryList.split(",");
        final ArrayList<String> borCountryList = new ArrayList<>();
        for (int i = 0; i < borderCountryListArray.length; ++i) { borCountryList.add(borderCountryListArray[i]); }
        popupBuilder.setAdapter(new BorderCountriesAdapter(this, android.R.layout.simple_list_item_1, borCountryList), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String country_name = borCountryList.get(which);
                Log.i(TAG,"__________SELECTED ('"+country_name+"')");
                ViewController.getInstance().launchCountryDetails(CountryDetails.this,country_name);
            }
        });
        AlertDialog country_selector = popupBuilder.create();
        country_selector.show();
    }

    private void launchCountrySelector(){
        Log.i(TAG, "launchCountrySelector()");
        AlertDialog.Builder popupBuilder = new AlertDialog.Builder(CountryDetails.this);
        popupBuilder.setTitle("Switch Country");
        String[] borderCountryListArray=((String)border_countries.replaceAll("<BR/>",":")).split(":");

        final ArrayList<String> borCountryList = new ArrayList<>();
        for (int i = 0; i < borderCountryListArray.length; ++i) { borCountryList.add(borderCountryListArray[i]); }
        popupBuilder.setAdapter(new BorderCountriesAdapter(this, android.R.layout.simple_list_item_1, borCountryList), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String country_name = borCountryList.get(which);
                Log.i(TAG,"__________SELECTED ('"+country_name+"')");
                ViewController.getInstance().launchCountryDetails(CountryDetails.this,country_name);
            }
        });
        AlertDialog country_selector = popupBuilder.create();
        country_selector.show();
    }
}
