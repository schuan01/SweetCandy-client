package infozonaorg.com.testnode.Clases;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by jvolpe on 29/11/2016.
 */

public class Usuario
{

    private int id;
    private String usuario;
    private String urlFoto;
    private LatLng ubicacion;
    private int edad;
    private Boolean isOnline;
    private String email;
    private String password;


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

    public Usuario(int id, String usuario, String urlFoto, LatLng ubicacion, int edad, Boolean isOnline, String email, String password) {
        setId(id);
        setUsuario(usuario);
        setUrlFoto(urlFoto);
        setUbicacion(ubicacion);
        setEdad(edad);
        setOnline(isOnline);
        setEmail(email);
        setPassword(password);

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

    }






}
