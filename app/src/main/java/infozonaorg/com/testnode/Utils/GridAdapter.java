package infozonaorg.com.testnode.Utils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.squareup.picasso.Picasso;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static android.widget.ImageView.ScaleType.CENTER_CROP;

import infozonaorg.com.testnode.Clases.Session;



public final class GridAdapter  extends BaseAdapter {
    private final Context context;
    private final List<String> urls = new ArrayList<>();

    public GridAdapter(Context context) {
        this.context = context;
        Session session = new Session(context, true);
        String[] fotos = session.getFotos().toArray(new String[session.getFotos().size()]);


        // Ensure we get a different ordering of images on each run.
        Collections.addAll(urls,fotos);
    }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        SquaredImageView view = (SquaredImageView) convertView;
        if (view == null) {
            view = new SquaredImageView(context);
            view.setScaleType(CENTER_CROP);
        }

        // Get the image URL for the current position.
        String url = getItem(position);

        // Trigger the download of the URL asynchronously into the image view.
        Picasso.with(context) //
                .load(url) //
                .fit() //
                .tag(context) //
                .into(view);

        return view;
    }

    @Override public int getCount() {
        return urls.size();
    }

    @Override public String getItem(int position) {
        return urls.get(position);
    }

    @Override public long getItemId(int position) {
        return position;
    }

}
