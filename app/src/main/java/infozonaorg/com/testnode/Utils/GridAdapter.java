package infozonaorg.com.testnode.Utils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.squareup.picasso.Picasso;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static android.widget.ImageView.ScaleType.CENTER_CROP;

import infozonaorg.com.testnode.Clases.Session;
import infozonaorg.com.testnode.R;

/**
 * Created by jvolpe on 12/01/2017.
 */

public final class GridAdapter  extends BaseAdapter {
    private final Context context;
    private final List<String> urls = new ArrayList<>();
    private Session session;

    public GridAdapter(Context context) {
        this.context = context;

        final String BASE = "http://i.imgur.com/";
        final String EXT = ".jpg";
        final String[] URLS = {
                BASE + "CqmBjo5" + EXT, BASE + "zkaAooq" + EXT, BASE + "0gqnEaY" + EXT,
                BASE + "9gbQ7YR" + EXT, BASE + "aFhEEby" + EXT, BASE + "0E2tgV7" + EXT,
                BASE + "P5JLfjk" + EXT, BASE + "nz67a4F" + EXT, BASE + "dFH34N5" + EXT,
                BASE + "FI49ftb" + EXT, BASE + "DvpvklR" + EXT, BASE + "DNKnbG8" + EXT,
                BASE + "yAdbrLp" + EXT, BASE + "55w5Km7" + EXT
        };


        session = new Session(context);
        ArrayList<String> fotos = session.getFotos();


        // Ensure we get a different ordering of images on each run.
        Collections.addAll(fotos);
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
