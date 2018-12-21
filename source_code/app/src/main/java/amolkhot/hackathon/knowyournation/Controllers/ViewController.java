package amolkhot.hackathon.knowyournation.Controllers;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.util.Locale;

import amolkhot.hackathon.knowyournation.Screens.CountryDetails;
import amolkhot.hackathon.knowyournation.Screens.CountryListing;

/**
 * Created by amol.khot on 06-Dec-17.
 */
public class ViewController {
    private static String TAG = "ViewController";
    private Context context;
    static ViewController instance;

    public ViewController(Context thisContext) {
        context = thisContext;
    }

    public static void saveInstance(ViewController e){ instance = e; }
    public static ViewController getInstance(){return instance;}

    public void launchCountryDetails(Context context,String contry_name){
        Intent country_details_intent = new Intent(context, CountryDetails.class);
        country_details_intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        country_details_intent.putExtra("selected_country_title", contry_name);
        context.startActivity(country_details_intent);
    }
    public void launchMap(float latitude,float longitude,int zoom,String label){
        Log.i(TAG, "launchMap("+latitude+","+longitude+")");
        String uri = String.format(Locale.ENGLISH, "geo:%f,%f?z=%d", latitude, longitude, zoom);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        context.startActivity(intent);
    }

}
