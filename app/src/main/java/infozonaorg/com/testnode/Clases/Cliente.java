package infozonaorg.com.testnode.Clases;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by jvolpe on 30/11/2016.
 */

public class Cliente extends Usuario {

    private float rating;

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public Cliente(int id,String usuario, String urlFoto, LatLng ubicacion, int edad, Boolean isOnline, float rating, float costo, String email, String password, String descripcion,ArrayList<String> imagenes) {
        super(id,usuario, urlFoto, ubicacion, edad, isOnline, email,password,descripcion,imagenes);

        setRating(rating);
    }

    public Cliente() {
        super();
        setRating(0.0f);
    }

    public Cliente(JSONObject obj){
        super(obj);
        try {

            setRating(Float.parseFloat(obj.getString("rating")));

        } catch (JSONException e)
        {
            Log.e("Error",e.getMessage());
        }
    }

    @Override
    public JSONObject toJSON()
    {
        try {
            JSONObject jsonObject= super.toJSON();

            jsonObject.put("rating", getRating());

            return jsonObject;
        } catch (JSONException e)
        {
            Log.e("ERROR",e.getMessage());
            return null;
        }
    }
}
