package tech.greedylabs.crowspot;

import android.content.Context;
import android.location.Address;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class ListAdapter extends ArrayAdapter<Address> {

    private int resourceLayout;
    private Context mContext;

    private int selected=0;

    public int getSelected() {
        return selected;
    }

    public ListAdapter(Context context, int resource, List<Address> items) {
        super(context, resource, items);
        this.resourceLayout = resource;
        this.mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(mContext);
            v = vi.inflate(resourceLayout, null);
        }

        LinearLayout parentLl = (LinearLayout) v.findViewById(R.id.parentLl);
        parentLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selected = (int) view.findViewById(R.id.radioIv).getTag();
                notifyDataSetChanged();
            }
        });

        Address p = getItem(position);
        if(position==selected)
        {
            if (p != null) {
                TextView tt1 = (TextView) v.findViewById(R.id.addressTv);
                ImageView imageView = (ImageView) v.findViewById(R.id.radioIv);
                if (tt1 != null) {
                    tt1.setText(p.getAddressLine(0));
                    imageView.setVisibility(View.VISIBLE);
                    imageView.setTag(position);
                }
            }
        }
        else{
            if (p != null) {
                TextView tt1 = (TextView) v.findViewById(R.id.addressTv);
                ImageView imageView = (ImageView) v.findViewById(R.id.radioIv);
                if (tt1 != null) {
                    tt1.setText(p.getAddressLine(0));
                    imageView.setVisibility(View.INVISIBLE);
                    imageView.setTag(position);
                }
            }
        }

        return v;
    }

}