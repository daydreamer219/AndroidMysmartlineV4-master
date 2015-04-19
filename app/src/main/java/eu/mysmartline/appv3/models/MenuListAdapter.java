package eu.mysmartline.appv3.models;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import java.util.List;

import eu.mysmartline.appv3.R;

/**
 * Created by daydreamer on 3/2/2015.
 */
public class MenuListAdapter extends ArrayAdapter<String> {
    private List<String> mItems;
    private Context mContext;
    private LayoutInflater inflater;

    public MenuListAdapter(Context context, List<String> items) {
        super(context, 0, items);
        mContext = context;
        mItems = items;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public String getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        String data = mItems.get(position);
        convertView = inflater.inflate(R.layout.row_menu, null);
        TextView txtName = (TextView) convertView.findViewById(R.id.txt_name);
        txtName.setText(data);
        return convertView;
    }

}
