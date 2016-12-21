package infozonaorg.com.testnode.Clases;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by jvolpe on 08/12/2016.
 */

public class Session
{
    private SharedPreferences prefs;

    public Session(Context cntx) {

        prefs = PreferenceManager.getDefaultSharedPreferences(cntx);
    }

    public void setId(int id)
    {
            prefs.edit().putInt("id", id).apply();
    }

    public int getId() {
        int id = prefs.getInt("id",0);
        return id;
    }

    public void setUsuario(String usuario) {
        prefs.edit().putString("usuario", usuario).apply();
    }



    public String getUsuario() {
        String usuario = prefs.getString("usuario","");
        return usuario;
    }
    public void setEmail(String email) {
        prefs.edit().putString("email", email).apply();
    }

    public String getEmail() {
        String email = prefs.getString("email","");
        return email;
    }
    public void setEdad(int edad) {
        prefs.edit().putInt("edad", edad).apply();
    }

    public int getEdad() {
        int edad = prefs.getInt("edad",0);
        return edad;
    }
    public void setDescripcion(String descrip) {
        prefs.edit().putString("descrip", descrip).apply();
    }

    public String getDescripcion() {
        String descrip = prefs.getString("descrip","");
        return descrip;
    }

    public void clearAll()
    {
        prefs.edit().clear().commit();
    }
}
