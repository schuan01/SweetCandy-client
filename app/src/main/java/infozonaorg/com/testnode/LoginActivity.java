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
import org.json.JSONException;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Objects;;
import butterknife.BindView;
import butterknife.ButterKnife;
import infozonaorg.com.testnode.Capas.LogicaUsuario;
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
    private ProgressDialog progress;


    @BindView(R.id.input_email) EditText _emailText;
    @BindView(R.id.input_password) EditText _passwordText;
    @BindView(R.id.btn_login) Button _loginButton;
    @BindView(R.id.link_signup) TextView _signupLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        session = new Session(LoginActivity.this,true);
        //SOCKETS ----------------------------------------------------------------
        TestApplication app = (TestApplication) getApplication();
        mSocket = app.getSocket();
        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.on("usuariologeado", onLoginResult);//obtiene el usuario logeado
        //FIN SOCKETS ----------------------------------------------------------------

        if(Objects.equals(session.getTipoUsuario(), "Empleado")) {//Si no esta como Empleado
            if (session.getId() != 0) {
                progress = ProgressDialog.show(this, getString(R.string.txt_cargando),
                        getString(R.string.txt_iniciandoSesion), true);

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        autoLogin();//Proceso el autologin

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                            }
                        });
                    }
                }).start();

            }
        }
        else
        {
            session.clearAll();
        }





        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                progress = ProgressDialog.show(LoginActivity.this, getString(R.string.txt_cargando),
                        getString(R.string.txt_iniciandoSesion), true);

                new Thread(new Runnable()
                {
                    @Override
                    public void run() {


                        runOnUiThread(new Runnable() {
                            @Override
                            public void run()
                            {
                                login();

                            }
                        });

                    }
                }).start();
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
                estadoInput();

                if (!validate()) {
                    onLoginFailed();
                    return;
                }

                String email = _emailText.getText().toString();
                String password = _passwordText.getText().toString();

                mSocket.emit("loginusuario", LogicaUsuario.makeLogin(email,password));
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

                        if(LogicaUsuario.onLoginResult(session,usuario))
                        {
                            onLoginSuccess();
                        }
                        else
                        {
                            onLoginFailed();
                        }


                        /*
                        if(usuario != null)
                        {
                            session = new Session(LoginActivity.this,true);
                            session.setId(usuario.getInt("id"));
                            session.setUsuario(usuario.getString("usuario"));
                            session.setEmail(usuario.getString("email"));
                            session.setDescripcion(usuario.getString("descripcion"));
                            session.setEdad(usuario.getInt("edad"));
                            session.setTipoUsuario("Empleado");
                            if(!passwordTmp.equals("")) {
                                session.setPwd(passwordTmp);
                            }

                            onLoginSuccess();
                        }
                        else
                        {
                            onLoginFailed();
                        }*/
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

    public void autoLogin()
    {
        JSONObject informacion = new JSONObject();
        try {
            informacion.put("email", session.getEmail());
            informacion.put("password", session.getPwd());

        } catch (JSONException e) {

            Log.e("Error",e.getMessage());
        }

        mSocket.emit("loginusuario", informacion);
    }

    public void onLoginSuccess() {
        _loginButton.setEnabled(true);
        progress.dismiss();
        finish();
        Intent about = new Intent(LoginActivity.this, MapsActivity.class);
        about.putExtra("tipoBoton",session.getTipoUsuario());//Viene de la logica Login
        about.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        about.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(about);

    }

    public void onLoginFailed() {
        estadoInput();
        LogicaUsuario.handleSnackBarConexion("fallo",LoginActivity.this);
        _loginButton.setEnabled(true);
        progress.dismiss();
    }

    public boolean validate() {
        boolean valid = true;

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError(getString(R.string.txt_mailInvalido));
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError(getString(R.string.txt_ControlPassword));
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
                        activarBotones();
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

    private void activarBotones()
    {
        _loginButton.setEnabled(true);

    }

    private void estadoInput()
    {
        if(_emailText.isEnabled())
            _emailText.setEnabled(false);
        else
            _emailText.setEnabled(true);

        if(_passwordText.isEnabled())
            _passwordText.setEnabled(false);
        else
            _passwordText.setEnabled(true);
    }

    private void handleSnackBarConexion(String evento)
    {

        Snackbar estado = LogicaUsuario.handleSnackBarConexion(evento,LoginActivity.this);
        if(estado != null)
        {
            estado.show();
            if(evento.equals("fallo") || evento.equals("desconecto") )
            {
                desactivarBotones();
            }
        }
        /*switch (evento) {
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



        }*/
    }


}
