package amolkhot.hackathon.knowyournation.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by amol.khot on 30-Nov-17.
 */
public class CustomArrayAdapter<String> extends ArrayAdapter<String> {

    private int mResource;
    private int mFieldId = 0;
    private final LayoutInflater mInflater;
    private int mDropDownResource;

    public CustomArrayAdapter(Context context, int resource) {
        super(context, resource);
        mInflater = LayoutInflater.from(context);
        mResource = mDropDownResource = resource;
    }

    public CustomArrayAdapter(Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
        mInflater = LayoutInflater.from(context);
        mResource = mDropDownResource = resource;
        mFieldId = textViewResourceId;
    }

    public CustomArrayAdapter(Context context, int resource, String[] objects) {
        super(context, resource, objects);
        mInflater = LayoutInflater.from(context);
        mResource = mDropDownResource = resource;
    }

    public CustomArrayAdapter(Context context, int resource, int textViewResourceId, String[] objects) {
        super(context, resource, textViewResourceId, objects);
        mInflater = LayoutInflater.from(context);
        mResource = mDropDownResource = resource;
    }

    public CustomArrayAdapter(Context context, int resource, List<String> objects) {
        super(context, resource, objects);
        mInflater = LayoutInflater.from(context);
        mResource = mDropDownResource = resource;
    }

    public CustomArrayAdapter(Context context, int resource, int textViewResourceId, List<String> objects) {
        super(context, resource, textViewResourceId, objects);
        mInflater = LayoutInflater.from(context);
        mResource = mDropDownResource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(mInflater, position, convertView, parent, mResource);
    }
    private View createViewFromResource(LayoutInflater inflater, int position, View convertView,
                                        ViewGroup parent, int resource) {
        View view;
        TextView text;

        if (convertView == null) {
            view = inflater.inflate(resource, parent, false);
        } else {
            view = convertView;
        }

        try {
            if (mFieldId == 0) {
                //  If no custom field is assigned, assume the whole resource is a TextView
                text = (TextView) view;
            } else {
                //  Otherwise, find the TextView field within the layout
                text = (TextView) view.findViewById(mFieldId);
            }
        } catch (ClassCastException e) {
            Log.e("ArrayAdapter", "You must supply a resource ID for a TextView");
            throw new IllegalStateException(
                    "ArrayAdapter requires the resource ID to be a TextView", e);
        }

        String item = getItem(position);
        if (item instanceof CharSequence) {
            text.setText(((CharSequence)item).toString().substring(0,item.toString().indexOf("|")));
        } else {
            text.setText(item.toString().substring(0,10));
        }

        return view;
    }
}
