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



    public Cliente(int id,String usuario, String urlFoto, LatLng ubicacion, int edad, Boolean isOnline, float rating, float costo, String email, String password, String descripcion,ArrayList<String> imagenes) {
        super(id,usuario, urlFoto, ubicacion, edad, isOnline, email,password,descripcion,imagenes,rating);


    }

    public Cliente() {
        super();

    }

    public Cliente(JSONObject obj){
        super(obj);
    }

    @Override
    public JSONObject toJSON()
    {
        try {
            JSONObject jsonObject= super.toJSON();

            return jsonObject;
        } catch (Exception e)
        {
            Log.e("ERROR",e.getMessage());
            return null;
        }
    }
}
