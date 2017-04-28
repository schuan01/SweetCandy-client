package infozonaorg.com.testnode.Clases;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by jvolpe on 24/04/2017.
 */

public class Anonimo extends Usuario {

    //IGUAL AL USUARIO

    public Anonimo(int id, String usuario, String urlFoto, LatLng ubicacion, int edad, Boolean isOnline, String email, String password, String descripcion,ArrayList<String> imagenes) {
        super(id,usuario, urlFoto, ubicacion, edad, isOnline, email,password,descripcion,imagenes);
    }

    public Anonimo() {
        super();
    }

    public Anonimo(JSONObject obj){super(obj);}
}
