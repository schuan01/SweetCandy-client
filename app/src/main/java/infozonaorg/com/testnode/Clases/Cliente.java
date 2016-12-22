package infozonaorg.com.testnode.Clases;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

/**
 * Created by jvolpe on 30/11/2016.
 */

public class Cliente extends Usuario {

    //TODO IGUAL AL USUARIO NORMAL

    @Override
    public String getUrlFoto() {
        return super.getUrlFoto();
    }

    @Override
    public void setUrlFoto(String urlFoto) {
        super.setUrlFoto(urlFoto);
    }

    @Override
    public LatLng getUbicacion() {
        return super.getUbicacion();
    }

    @Override
    public void setUbicacion(LatLng ubicacion) {
        super.setUbicacion(ubicacion);
    }

    @Override
    public int getEdad() {
        return super.getEdad();
    }

    @Override
    public void setEdad(int edad) {
        super.setEdad(edad);
    }

    @Override
    public String getUsuario() {
        return super.getUsuario();
    }

    @Override
    public void setUsuario(String usu) {
        super.setUsuario(usu);
    }

    public Cliente(int id,String usuario, String urlFoto, LatLng ubicacion, int edad, Boolean isOnline, String email,String password) {
        super(id,usuario, urlFoto, ubicacion, edad, isOnline, email,password);
    }

    public Cliente() {
        super();
    }

    public Cliente(JSONObject obj){super(obj);}
}
