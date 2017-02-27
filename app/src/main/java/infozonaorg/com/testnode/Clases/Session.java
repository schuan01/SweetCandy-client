package infozonaorg.com.testnode.Clases;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.securepreferences.SecurePreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by jvolpe on 08/12/2016.
 */

public class Session
{
    private SharedPreferences prefs;

    public Session(Context cntx, boolean seguro) {


        if(!seguro)//TODO
            prefs = new SecurePreferences(cntx);
        else
            prefs =  PreferenceManager.getDefaultSharedPreferences(cntx);

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

    public void setTipoUsuario(String tipo) {
        prefs.edit().putString("tipousuario", tipo).apply();
    }

    public String getTipoUsuario() {
        String tipo = prefs.getString("tipousuario","");
        return tipo;
    }

    public void setPwd(String pwd) {
        prefs.edit().putString("pwd", pwd).apply();
    }

    public String getPwd() {
        String pwd = prefs.getString("pwd","");
        return pwd;
    }

    public void setFotos(ArrayList<String> fotos)
    {
        if(fotos.size() > 0) {
            Set<String> datos = new HashSet<String>(fotos);
            prefs.edit().putStringSet("fotos", datos).apply();
        }
        else
        {
            Set<String> datos = new HashSet<String>();
            prefs.edit().putStringSet("fotos", datos).apply();
        }
    }

    public ArrayList<String> getFotos()
    {
        Set<String> fotos = prefs.getStringSet("fotos", new HashSet<String>());
        ArrayList<String> col = new ArrayList<>();
        col.addAll(fotos);
        return col;
    }

    public void clearAll()
    {
        prefs.edit().clear().commit();
    }

    public JSONObject toJSON(){

        JSONObject jsonObject= new JSONObject();
        try {
            jsonObject.put("id", getId());
            jsonObject.put("usuario", getUsuario());
            jsonObject.put("edad", getEdad());
            jsonObject.put("email", getEmail());
            JSONArray fotos = new JSONArray();
            for (String url: getFotos())
            {
                JSONObject j = new JSONObject();
                j.put("urlFoto",url);
                fotos.put(j);
            }

            jsonObject.put("Fotos", fotos);

            return jsonObject;
        } catch (JSONException e) {
            Log.e("ERROR",e.getMessage());
            return null;
        }

    }
}
