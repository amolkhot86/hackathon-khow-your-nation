package amolkhot.hackathon.knowyournation.Screens;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import amolkhot.hackathon.knowyournation.Controllers.DataController;
import amolkhot.hackathon.knowyournation.Controllers.ViewController;
import amolkhot.hackathon.knowyournation.R;

public class LoadingActivity extends AppCompatActivity{

    private static String TAG = "LoadingActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        DataController.saveInstance(new DataController(this));
        ViewController.saveInstance(new ViewController(this));
        new StartupTask().execute();
    }
    private class StartupTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                DataController.getInstance().loadCountryDataFromFile("CountryData.json");
            } catch (Exception e) {
                Log.v(TAG, "StartupTask | doInBackground() | ERROR : " + e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Intent country_screen_intent = new Intent(LoadingActivity.this, CountryListing.class);
            country_screen_intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(country_screen_intent);
        }
    }
}
