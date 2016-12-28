package infozonaorg.com.testnode;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import infozonaorg.com.testnode.Clases.Empleado;
import infozonaorg.com.testnode.Clases.Session;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class LoginActivity extends AppCompatActivity
{
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;
    private Socket mSocket;
    private Boolean isConnected = true;
    private Session session;
    Snackbar snackbarConectado = null;
    Snackbar snackbarDesconectado = null;
    Snackbar snackbarFallo = null;


    @InjectView(R.id.input_email) EditText _emailText;
    @InjectView(R.id.input_password) EditText _passwordText;
    @InjectView(R.id.btn_login) Button _loginButton;
    @InjectView(R.id.link_signup) TextView _signupLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //SOCKETS ----------------------------------------------------------------
        TestApplication app = (TestApplication) getApplication();
        mSocket = app.getSocket();
        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.on("usuariologeado", onLoginResult);//obtiene el usuario logeado
        //FIN SOCKETS ----------------------------------------------------------------

        ButterKnife.inject(this);

        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });

        _signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
            }
        });
    }

    public void login() {
        Log.d(TAG, "Login");
        _loginButton.setEnabled(false);

        if (!validate()) {
            onLoginFailed();
            return;
        }



        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        email.toLowerCase();
        password.toLowerCase();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            password += Constantes.TEXTOFIJO;
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            String base64 = Base64.encodeToString(hash, Base64.DEFAULT);
            password = base64;
        }
        catch (Exception ex)
        {
            Log.e("ERROR",ex.getMessage());
        }

        JSONObject informacion = new JSONObject();
        try {
            informacion.put("email", email);
            informacion.put("password", password);

        } catch (JSONException e) {

            e.printStackTrace();
        }

        mSocket.emit("loginempleado", informacion);


    }

    //Obtiene el usuario logeado correctamente
    private Emitter.Listener onLoginResult = new Emitter.Listener() {
        @Override
        public void call(final Object... args)
        {
            LoginActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try
                    {

                        JSONObject usuario = (JSONObject) args[0];//Obtenemos el array del servidor
                        if(usuario != null)
                        {
                            session = new Session(LoginActivity.this);
                            session.setId(usuario.getInt("id"));
                            session.setUsuario(usuario.getString("usuario"));
                            session.setEmail(usuario.getString("email"));
                            session.setDescripcion(usuario.getString("descripcion"));
                            session.setEdad(usuario.getInt("edad"));
                            onLoginSuccess();
                        }
                        else
                        {
                            onLoginFailed();
                        }
                    } catch (Exception e)
                    {
                        Log.e("Error", e.getMessage());
                        onLoginFailed();
                    }
                }
            });


        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {

                // TODO: Implement successful signup logic here
                // By default we just finish the Activity and log them in automatically
                this.finish();
            }
        }
    }

    public void onLoginSuccess() {
        _loginButton.setEnabled(true);
        finish();
        Intent about = new Intent(LoginActivity.this, MapsActivity.class);
        about.putExtra("tipoBoton","Empleado");
        about.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        about.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(about);
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Error al ingresar", Toast.LENGTH_LONG).show();

        _loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("Ingrese un mail valido");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("Contraseña entre 4 y 10 caracteres");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

    }




    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            LoginActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(!isConnected) {
                        handleSnackBarConexion("conecto");
                        isConnected = true;
                    }
                }
            });
        }
    };

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            LoginActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    desactivarBotones();
                    isConnected = false;
                    handleSnackBarConexion("desconecto");
                }
            });
        }
    };

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            LoginActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    desactivarBotones();
                    handleSnackBarConexion("fallo");
                }
            });
        }
    };

    private void desactivarBotones()
    {
        _loginButton.setEnabled(false);

    }

    private void handleSnackBarConexion(String evento)
    {
        switch (evento) {
            case "fallo":
                if(snackbarDesconectado != null)
                {
                    snackbarDesconectado.dismiss();
                    snackbarDesconectado = null;
                }
                if(snackbarFallo == null) {
                    snackbarFallo = Snackbar.make(findViewById(android.R.id.content), "Fallo al conectar", Snackbar.LENGTH_INDEFINITE);
                    View sbView = snackbarFallo.getView();
                    sbView.setBackgroundColor(Color.RED);
                    TextView tv = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                    tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    snackbarFallo.show();
                }
                break;
            case "desconecto":
                if(snackbarFallo != null)
                {
                    snackbarFallo.dismiss();
                    snackbarFallo = null;
                }
                if(snackbarDesconectado == null) {
                    snackbarDesconectado = Snackbar.make(findViewById(android.R.id.content), "Desconectado", Snackbar.LENGTH_INDEFINITE);
                    View sbView = snackbarDesconectado.getView();
                    sbView.setBackgroundColor(Color.RED);
                    TextView tv = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                    tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    snackbarDesconectado.show();
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
                    snackbarConectado = Snackbar.make(findViewById(android.R.id.content),"Conectado", Snackbar.LENGTH_SHORT);
                    View sbView = snackbarConectado.getView();
                    sbView.setBackgroundColor(Color.GREEN);
                    TextView tv = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                    tv.setTextColor(Color.BLACK);
                    tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    snackbarConectado.show();
                }
                snackbarConectado = null;
                break;

            default:
                break;



        }
    }

}
