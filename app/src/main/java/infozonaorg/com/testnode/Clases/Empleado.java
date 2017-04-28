package infozonaorg.com.testnode.Clases;

import android.graphics.drawable.Drawable;
import android.media.Image;
import android.util.Log;

import com.google.ads.mediation.EmptyNetworkExtras;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jvolpe on 30/11/2016.
 */

public class Empleado extends Usuario {

    private float rating;
    private float costo;

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }


    public float getCosto() {
        return costo;
    }

    public void setCosto(float costo) {
        this.costo = costo;
    }




    public Empleado(int id,String usuario, String urlFoto, LatLng ubicacion, int edad, Boolean isOnline, float rating, String descripcion, float costo, String email, String password, ArrayList<String> imagenes) {
        super(id,usuario, urlFoto, ubicacion, edad,isOnline,email,password,descripcion,imagenes);

        setRating(rating);
        setCosto(costo);
    }

    public Empleado(JSONObject obj)
    {
        super(obj);
        try {

        setCosto(Float.parseFloat(obj.getString("costo")));
        setRating(Float.parseFloat(obj.getString("rating")));

        } catch (JSONException e)
        {
            Log.e("Error",e.getMessage());
        }
    }

    public Empleado() {
        super();

        setRating(0.0f);
        setCosto(0);

    }

    @Override
    public JSONObject toJSON()
    {
        try {
        JSONObject jsonObject= super.toJSON();

        jsonObject.put("rating", getRating());
        jsonObject.put("costo", getCosto());

        return jsonObject;
        } catch (JSONException e)
        {
            Log.e("ERROR",e.getMessage());
            return null;
        }
    }
}
