package amolkhot.hackathon.knowyournation.Controllers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.ArrayAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import amolkhot.hackathon.knowyournation.Adapters.CustomArrayAdapter;

/**
 * Created by amol.khot on 22-Nov-17.
 */
public class DataController {
    private static String TAG = "DataController";
    private Context context;
    static DataController instance;

    public JSONArray countryData;
    public ArrayList<String> countryTitleArray = new ArrayList<>();
    ArrayAdapter<String> countryNamesAdapter;


    SQLiteOpenHelper sqLiteOpenHelper;
    SQLiteDatabase flagDb;

    public DataController(Context thisContext) {
        context=thisContext;
        try {
            sqLiteOpenHelper = new SQLiteOpenHelper(context, "country.db", null, 1) {
                @Override
                public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                }

                @Override
                public void onCreate(SQLiteDatabase db) {
                }
            };
            flagDb = sqLiteOpenHelper.getWritableDatabase();
            flagDb.execSQL("CREATE TEMP TABLE Country (country_name TEXT,country_code TEXT,capital TEXT,search_string TEXT,region_name TEXT,subregion_name TEXT)");
        }catch (Exception e){
            Log.e(TAG,"ERROR : " + e.getMessage());
        }
    }

    public static void saveInstance(DataController e){ instance = e; }
    public static DataController getInstance(){return instance;}

    public ArrayAdapter<String> getCountryListAdapter(){return countryNamesAdapter;}

    public void loadCountryDataFromFile(String filename){
        Log.i(TAG, "loadCountryDataFromFile() | filename : " + filename);
        String data="";
        try {
            InputStream file = context.getAssets().open(filename);
            byte[] dataArray = new byte[file.available()];
            file.read(dataArray); file.close();
            data=new String(dataArray);
        }catch (IOException e){
            Log.e(TAG,"Error Reading File ("+e.getMessage()+")");
        }
        loadCountryData(data);
    }

    public void loadCountryData(String data){
        Log.i(TAG, "loadCountryData()");
        try {
            countryData = new JSONArray(data);
            Log.i(TAG, "Countries Found : " + countryData.length());
            generateAdapter();
        }catch (JSONException e){
            Log.e(TAG,"Error Loading JSON Object ("+e.getMessage()+")");
        }
    }

    public int getCountryIndex(String title){
        for(int i=0;i<countryTitleArray.size();i++) {
            if(countryTitleArray.get(i).indexOf(title+"|") != -1) return i;
        }
        return -1;
    }

    public void generateAdapter(){
        countryTitleArray.clear();
        flagDb.rawQuery("delete from Country", null);
        try {
            for (int i = 0; i < countryData.length(); i++) {
                JSONObject tmpCountryObj = (new JSONObject(countryData.getString(i)));
                String country_name = tmpCountryObj.getString("name");
                country_name = country_name.replaceAll("[\']+", "");
                String nativeName = tmpCountryObj.getString("nativeName");
                nativeName = nativeName.replaceAll("[\']+", "");
                String country_code = tmpCountryObj.getString("alpha3Code");
                String capital = tmpCountryObj.getString("capital");
                capital = capital.replaceAll("[\']+", "");
                String region = tmpCountryObj.getString("region");
                region = region.replaceAll("[\']+", "");
                String subregion = tmpCountryObj.getString("subregion");
                subregion = subregion.replaceAll("[\']+", "");
                JSONArray currency_obj = new JSONArray(tmpCountryObj.getString("currencies"));
                String currency_text;
                currency_text="";

                for(int c=0;c<currency_obj.length();c++) {
                    currency_text = currency_text + " " + currency_obj.getJSONObject(c).getString("name");
                }
                currency_text = currency_text.replaceAll("[\']+", "");
                String search_string = country_name + "|" +country_name + " " + country_code + " " + capital + " " + tmpCountryObj.getString("region") + " " + tmpCountryObj.getString("subregion") + " " + nativeName + currency_text;
                String query = "INSERT INTO Country (country_name,country_code,capital,search_string,region_name,subregion_name) Values('"+country_name+"','"+country_code+"','"+capital+"','"+search_string+"','"+region+"','"+subregion+"')";
                flagDb.execSQL(query);
                countryTitleArray.add(search_string);
            }
            Cursor countryCursor = flagDb.rawQuery("Select * from Country",null);
            for(int i=0;i<countryCursor.getCount();i++) {
                countryCursor.moveToPosition(i);
            }
        }catch (Exception e){
            Log.e(TAG,"generateAdapter() | Error Generating Adapter ("+e.getMessage()+")");
        }
        countryNamesAdapter= new CustomArrayAdapter<>(context,android.R.layout.simple_list_item_1,countryTitleArray);
    }

    public String getCountryNameFromCode(String code_list){
        try {
            Log.i(TAG,"getCountryNameFromCode("+code_list+")");
            String final_string="";
            code_list = code_list.replaceAll("[\\]]+", "").replaceAll("[\\[]+","");
            String query = "Select country_name from Country where country_code IN (" + code_list + ")";
            Log.i(TAG,query);
            Cursor countryListCursor = flagDb.rawQuery(query,null);
            Log.i(TAG,"____________COUNT : " + countryListCursor.getCount());
            for(int i=0;i<countryListCursor.getCount();i++) {
                countryListCursor.moveToPosition(i);
                if(i!=0) final_string=final_string+"<BR/>";
                final_string = final_string + countryListCursor.getString(0);
            }
            Log.i(TAG,"__________FINAL STRING : " +final_string);
            return final_string;
        }catch (Exception e){
            Log.e(TAG,"getCountryNameFromCode() | Error : " + e.getMessage());
        }
        return "";
    }
    public String getCountryNameFromRegion(String region){
        try {
            Log.i(TAG,"getCountryNameFromRegion("+region+")");
            String final_string="";
            String query = "Select country_name from Country where region_name = '"+region+"'";
            Log.i(TAG,query);
            Cursor countryListCursor = flagDb.rawQuery(query,null);
            Log.i(TAG,"____________COUNT : " + countryListCursor.getCount());
            for(int i=0;i<countryListCursor.getCount();i++) {
                countryListCursor.moveToPosition(i);
                if(i!=0) final_string=final_string+",";
                final_string = final_string + countryListCursor.getString(0);
            }
            Log.i(TAG,"__________FINAL STRING : " +final_string);
            return final_string;
        }catch (Exception e){
            Log.e(TAG,"getCountryNameFromCode() | Error : " + e.getMessage());
        }
        return "";
    }
    public String getCountryNameFromSubRegion(String subRegion){
        try {
            Log.i(TAG,"getCountryNameFromRegion("+subRegion+")");
            String final_string="";
            String query = "Select country_name from Country where subregion_name = '"+subRegion+"'";
            Log.i(TAG,query);
            Cursor countryListCursor = flagDb.rawQuery(query,null);
            Log.i(TAG,"____________COUNT : " + countryListCursor.getCount());
            for(int i=0;i<countryListCursor.getCount();i++) {
                countryListCursor.moveToPosition(i);
                if(i!=0) final_string=final_string+",";
                final_string = final_string + countryListCursor.getString(0);
            }
            Log.i(TAG,"__________FINAL STRING : " +final_string);
            return final_string;
        }catch (Exception e){
            Log.e(TAG,"getCountryNameFromCode() | Error : " + e.getMessage());
        }
        return "";
    }
}
