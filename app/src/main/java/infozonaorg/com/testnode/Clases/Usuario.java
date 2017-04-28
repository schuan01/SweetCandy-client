package infozonaorg.com.testnode.Clases;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by jvolpe on 29/11/2016.
 */

public abstract class Usuario
{

    private int id;
    private String usuario;
    private String urlFoto;
    private LatLng ubicacion;
    private int edad;
    private Boolean isOnline;
    private String email;
    private String password;
    private String descripcion;
    private ArrayList<String> fotos;//DEVUELVE LAS URL DE CADA DRAWABLE


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }



    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public Boolean getOnline() {
        return isOnline;
    }

    public void setOnline(Boolean online) {
        isOnline = online;
    }



    public String getUrlFoto() {
        return urlFoto;
    }

    public void setUrlFoto(String urlFoto) {
        this.urlFoto = urlFoto;
    }

    public LatLng getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(LatLng ubicacion)
    {
        this.ubicacion = ubicacion;
    }

    public int getEdad() {
        return edad;
    }

    public void setEdad(int edad) {
        this.edad = edad;
    }

    public String getUsuario()
    {
        return usuario;
    }

    public void setUsuario(String usu)
    {
        usuario = usu;
    }

    public ArrayList<String> getFotos() {
        return fotos;
    }

    public void setFotos(ArrayList<String> fotos) {
        this.fotos = fotos;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }


    public Usuario(int id, String usuario, String urlFoto, LatLng ubicacion, int edad, Boolean isOnline, String email, String password,String descripcion, ArrayList<String> imagenes) {
        setId(id);
        setUsuario(usuario);
        setUrlFoto(urlFoto);
        setUbicacion(ubicacion);
        setEdad(edad);
        setOnline(isOnline);
        setEmail(email);
        setPassword(password);
        setDescripcion(descripcion);
        setFotos(imagenes);

    }

    public Usuario(JSONObject obj){
        try
        {
            setId(obj.getInt("id"));
            setUsuario(obj.getString("usuario"));
            setUrlFoto(obj.getString("urlFoto"));
            setUbicacion(new LatLng(obj.getDouble("latitud"),obj.getDouble("longitud")));
            setEdad(obj.getInt("edad"));
            setOnline(obj.getBoolean("isOnline"));
            setEmail(obj.getString("email"));
            setDescripcion(obj.getString("descripcion"));
        }
        catch (JSONException e)
        {
            Log.e("Error",e.getMessage());
        }

    }



    public Usuario(){

        setId(0);
        setUsuario("Default");
        setUrlFoto("Default_Foto");
        setUbicacion(new LatLng(0.00,0.00));
        setEdad(1);
        setOnline(false);
        setEmail("test@test.com");
        setPassword("1234");
        setDescripcion("Default");
        setFotos(new ArrayList<String>());

    }

    public JSONObject toJSON(){

        JSONObject jsonObject= new JSONObject();
        try {
            jsonObject.put("id", getId());
            jsonObject.put("usuario", getUsuario());
            jsonObject.put("edad", getEdad());
            jsonObject.put("email", getEmail());
            jsonObject.put("isOnline", getOnline());
            jsonObject.put("latitud", getUbicacion().latitude);
            jsonObject.put("longitud", getUbicacion().longitude);
            jsonObject.put("urlFoto", getUrlFoto());
            jsonObject.put("descripcion", getDescripcion());

            return jsonObject;
        } catch (JSONException e) {
            Log.e("ERROR",e.getMessage());
            return null;
        }

    }






}
