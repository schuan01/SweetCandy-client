package infozonaorg.com.testnode.Capas;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import infozonaorg.com.testnode.Clases.Session;
import infozonaorg.com.testnode.Constantes;
import infozonaorg.com.testnode.R;
import infozonaorg.com.testnode.TestApplication;
import io.socket.client.Socket;

/**
 * Created by jvolpe on 21/04/2017.
 */

public abstract class LogicaUsuario {

    //PARA METODO handleSnackBarConexion()
    private static Snackbar snackbarConectado = null;
    private static Snackbar snackbarDesconectado = null;
    private static Snackbar snackbarFallo = null;
    private static String tipoUsuario = "";
    private static String passwordTmp = "";//para autologin

    public static JSONObject makeLogin(String email, String password)
    {
        email.toLowerCase();
        password.toLowerCase();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            password += Constantes.TEXTOFIJO;
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            password = Base64.encodeToString(hash, Base64.DEFAULT);
            passwordTmp = password;
        }
        catch (Exception ex)
        {

            Log.e("ERROR",ex.getMessage());
            return null;
        }

        JSONObject informacion = new JSONObject();
        try {
            informacion.put("email", email);
            informacion.put("password", password);

        } catch (JSONException e) {

            Log.e("Error",e.getMessage());
            return null;
        }

        return informacion;
    }

    public static boolean onLoginResult(Session session, JSONObject dato)
    {
        try
        {

            JSONObject usuario = dato;//Obtenemos el array del servidor
            if(usuario != null)
            {
                session.setId(usuario.getInt("id"));
                session.setUsuario(usuario.getString("usuario"));
                session.setEmail(usuario.getString("email"));
                session.setDescripcion(usuario.getString("descripcion"));
                session.setEdad(usuario.getInt("edad"));
                session.setTipoUsuario(usuario.getString("tipoUsuario"));
                if(!passwordTmp.equals("")) {
                    session.setPwd(passwordTmp);
                }

                return true;
            }
            else
            {
                return false;
            }
        } catch (Exception e)
        {
            Log.e("LogicaUsuario",e.getMessage());
            return false;
        }
    }

    public static Snackbar handleSnackBarConexion(String evento, Activity a)
    {

        Snackbar retorno = null;
        switch (evento) {
            case "fallo":
                if(snackbarDesconectado != null)
                {
                    snackbarDesconectado.dismiss();
                    snackbarDesconectado = null;
                }
                if(snackbarFallo == null) {
                    snackbarFallo = Snackbar.make(a.findViewById(android.R.id.content), R.string.error_connect, Snackbar.LENGTH_INDEFINITE);
                    View sbView = snackbarFallo.getView();
                    sbView.setBackgroundColor(Color.RED);
                    TextView tv = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                    tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    //snackbarFallo.show();
                    retorno = snackbarFallo;
                }
                break;
            case "desconecto":
                if(snackbarFallo != null)
                {
                    snackbarFallo.dismiss();
                    snackbarFallo = null;
                }
                if(snackbarDesconectado == null) {
                    snackbarDesconectado = Snackbar.make(a.findViewById(android.R.id.content), R.string.disconnect, Snackbar.LENGTH_INDEFINITE);
                    View sbView = snackbarDesconectado.getView();
                    sbView.setBackgroundColor(Color.RED);
                    TextView tv = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                    tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    //snackbarDesconectado.show();
                    retorno = snackbarDesconectado;
                }
                break;

            case "conecto":
                if(snackbarDesconectado != null)
                {
                    snackbarDesconectado.dismiss();
                    snackbarDesconectado = null;
                }

                if(snackbarFallo != null)
                {
                    snackbarFallo.dismiss();
                    snackbarFallo = null;
                }

                if(snackbarConectado == null) {
                    snackbarConectado = Snackbar.make(a.findViewById(android.R.id.content),R.string.connect, Snackbar.LENGTH_SHORT);
                    View sbView = snackbarConectado.getView();
                    sbView.setBackgroundColor(Color.GREEN);
                    TextView tv = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                    tv.setTextColor(Color.BLACK);
                    tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    //snackbarConectado.show();
                    retorno = snackbarConectado;
                }
                snackbarConectado = null;
                break;

            default:
                break;



        }

        return retorno;
    }

    public static void setTipoUsuario(String usu)
    {
        tipoUsuario = usu;
    }

    public static String getTipoUsuario()
    {
        return tipoUsuario;
    }



}
