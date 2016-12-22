package infozonaorg.com.testnode.Clases;

import com.google.ads.mediation.EmptyNetworkExtras;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jvolpe on 30/11/2016.
 */

public class Empleado extends Usuario {

    private float rating;
    private String descripcion;
    private float costo;

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public float getCosto() {
        return costo;
    }

    public void setCosto(float costo) {
        this.costo = costo;
    }




    public Empleado(int id,String usuario, String urlFoto, LatLng ubicacion, int edad, Boolean isOnline, float rating, String descripcion, float costo, String email, String password) {
        super(id,usuario, urlFoto, ubicacion, edad,isOnline,email,password);

        setRating(rating);
        setDescripcion(descripcion);
        setCosto(costo);
    }

    public Empleado(JSONObject obj)
    {
        super(obj);
        try {

        setDescripcion(obj.getString("descripcion"));
        setCosto(Float.parseFloat(obj.getString("costo")));
        setRating(Float.parseFloat(obj.getString("rating")));

        } catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public Empleado() {
        super();

        setRating(0.0f);
        setDescripcion("Default");
        setCosto(0);

    }

    @Override
    public JSONObject toJSON()
    {
        try {
        JSONObject jsonObject= super.toJSON();

        jsonObject.put("rating", getRating());
        jsonObject.put("descripcion", getDescripcion());
        jsonObject.put("costo", getCosto());

        return jsonObject;
        } catch (JSONException e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
