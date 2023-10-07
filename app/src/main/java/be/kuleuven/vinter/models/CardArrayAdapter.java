package be.kuleuven.vinter.models;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import be.kuleuven.vinter.R;

public class CardArrayAdapter extends ArrayAdapter<ProductCard> {
    private List<ProductCard> mList;

    public CardArrayAdapter(Context context, int resourceId, List<ProductCard> items){
        super(context, resourceId, items);
        mList = items;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        ProductCard cartItem = getItem(position);

        if (convertView == null)
        {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item, parent, false);
        }


        TextView name = (TextView) convertView.findViewById(R.id.name);
        TextView price = (TextView) convertView.findViewById(R.id.price);
        ImageView image = (ImageView) convertView.findViewById(R.id.image);
        TextView size = (TextView) convertView.findViewById(R.id.textView2);

        name.setText(cartItem.getName());
        price.setText("€" + Integer.toString(cartItem.getPrice()));
        try {
            image.setImageBitmap(cartItem.getSpecImage());
        }
        catch (IndexOutOfBoundsException e) {
            //image.setImageResource(R.mipmap.ic_launcher);
        }
        size.setText(Integer.toString(cartItem.getSize()));

        return convertView;
    }


}
