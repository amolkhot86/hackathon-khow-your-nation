package amolkhot.hackathon.knowyournation.Screens;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;

import amolkhot.hackathon.knowyournation.Controllers.DataController;
import amolkhot.hackathon.knowyournation.Controllers.ViewController;
import amolkhot.hackathon.knowyournation.R;

public class CountryListing extends AppCompatActivity {
    private static String TAG = "CountryListing";
    private ListView countryList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_country_listing);
        countryList = (ListView)findViewById(R.id.countryList);
        try {
            countryList.setAdapter(DataController.getInstance().getCountryListAdapter());
        }catch (NullPointerException e){
            Log.e(TAG,"ERROR Loading Country List : " + e.getMessage());
        }
        countryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String title = (String) countryList.getAdapter().getItem(position);
                title = title.substring(0,title.indexOf('|'));
                ViewController.getInstance().launchCountryDetails(CountryListing.this,title);
            }
        });
    }
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);
        MenuItem item = menu.findItem(R.id.menuSearch);
        final SearchView searchView = (SearchView)item.getActionView();
        searchView.setQueryHint("Name,Country Code,Capital,Region,Currency ....");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                DataController.getInstance().getCountryListAdapter().getFilter().filter(newText);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }
}
