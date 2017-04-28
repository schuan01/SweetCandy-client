package infozonaorg.com.testnode.Utils;

/**
 * Created by jvolpe on 12/04/2017.
 */

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import infozonaorg.com.testnode.R;

public class FullScreenImageAdapter extends PagerAdapter implements LoadImageTask.Listener {

    private Activity _activity;
    private ArrayList<String> _imagePaths;
    private ImageView imgDisplay;

    // constructor
    public FullScreenImageAdapter(Activity activity,
                                  ArrayList<String> imagePaths) {
        this._activity = activity;
        this._imagePaths = imagePaths;
    }

    @Override
    public int getCount() {
        return this._imagePaths.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((RelativeLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        LayoutInflater inflater = (LayoutInflater) _activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewLayout = inflater.inflate(R.layout.layout_fullscreen_image, container,
                false);

        imgDisplay = (ImageView) viewLayout.findViewById(R.id.imgDisplay);


        new LoadImageTask(this).execute(_imagePaths.get(position));



        ((ViewPager) container).addView(viewLayout);

        return viewLayout;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((RelativeLayout) object);

    }

    @Override
    public void onImageLoaded(Bitmap bitmap) {

        imgDisplay.setImageBitmap(bitmap);

    }

    @Override
    public void onError() {
        Log.e("IMAGENES","ESTALLO");

    }
}