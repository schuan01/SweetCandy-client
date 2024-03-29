package infozonaorg.com.testnode;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import infozonaorg.com.testnode.Capas.LogicaUsuario;
import infozonaorg.com.testnode.Clases.Cliente;
import infozonaorg.com.testnode.Clases.Empleado;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class SignUpActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener
{
    private static final String TAG = "SignUpActivity";

    @BindView(R.id.input_usuario) EditText _usuarioText;
    @BindView(R.id.input_email) EditText _emailText;
    @BindView(R.id.input_password) EditText _passwordText;
    @BindView(R.id.input_passwordConfirm) EditText _passwordConfirmText;
    @BindView(R.id.btn_signup) Button _signupButton;
    @BindView(R.id.link_login) TextView _loginLink;
    @BindView(R.id.spn_tiposUsuario) Spinner _spinnerTipos;

    private Socket mSocket;
    private Boolean isConnected = true;
    private Boolean creadoCorrectamente = false;
    private String seleccionTipoUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        ButterKnife.bind(this);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.tiposUsuario, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        _spinnerTipos.setAdapter(adapter);
        _spinnerTipos.setOnItemSelectedListener(this);


        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                Intent about = new Intent(SignUpActivity.this, MapsActivity.class);
                finish();
                startActivity(about);
            }
        });

        //SOCKETS ----------------------------------------------------------------
        TestApplication app = (TestApplication) getApplication();
        mSocket = app.getSocket();
        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.on("empleadocreado", onEmpleadoCreado);
        mSocket.on("clientecreado", onClienteCreado);
        //FIN SOCKETS ----------------------------------------------------------------




    }

    public void signup() {
        Log.d(TAG, "Signup");

        if (!validate()) {
            onSignupFailed();
            return;
        }

        _signupButton.setEnabled(false);
        final ProgressDialog progressDialog = new ProgressDialog(SignUpActivity.this,
                R.style.AppTheme);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.txt_EnviandoServidor));
        progressDialog.show();

        String usuario = _usuarioText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        usuario = usuario.toLowerCase();
        email = email.toLowerCase();
        password = password.toLowerCase();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            password += Constantes.TEXTOFIJO;
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            password = Base64.encodeToString(hash, Base64.DEFAULT);
        }
        catch (Exception ex)
        {
            Log.e("ERROR",ex.getMessage());
        }

        if(seleccionTipoUsuario.equals("Servicio"))
        {
            try {
                Empleado e = new Empleado();
                e.setUsuario(usuario);
                e.setEmail(email);
                e.setPassword(password);
                JSONObject info = e.toJSON();
                info.put("password", password);

                //Manda la info necesaria para agregar un nuevo empleado conectado al servidor
                mSocket.emit("nuevoempleado", info);
            }
            catch (Exception ex)
            {
                Log.e(TAG,ex.getMessage());
            }

        }
        else if(seleccionTipoUsuario.equals("Cliente"))
        {
            try
            {
                Cliente c = new Cliente();
                c.setUsuario(usuario);
                c.setEmail(email);
                c.setPassword(password);
                JSONObject info = c.toJSON();
                info.put("password",password);


                //Manda la info necesaria para agregar un nuevo empleado conectado al servidor
                mSocket.emit("nuevocliente",info);
            }
            catch (Exception ex)
            {
                Log.e(TAG,ex.getMessage());
            }
        }


        progressDialog.dismiss();
    }

    public void onSignupSuccess() {
        _signupButton.setEnabled(true);
        setResult(RESULT_OK, null);
        Intent about = new Intent(SignUpActivity.this, MapsActivity.class);
        if(seleccionTipoUsuario.equals("Servicio")) {
            about.putExtra("tipoBoton", "Empleado");
        }
        else if(seleccionTipoUsuario.equals("Cliente")) {
            about.putExtra("tipoBoton", "Cliente");
        }
        finish();
        startActivity(about);

    }

    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), R.string.txtCreacionFallo, Toast.LENGTH_LONG).show();

        _signupButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String usuario = _usuarioText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();
        String passwordConfirm = _passwordConfirmText.getText().toString();

        if (usuario.isEmpty() || usuario.length() < 3) {
            _usuarioText.setError(getString(R.string.txtUsuarioTresCaracteres));
            valid = false;
        } else {
            _usuarioText.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError(getString(R.string.txt_mailInvalido));
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError(getString(R.string.txt_ControlPassword));
            valid = false;
        }
        else {
            _passwordText.setError(null);
        }

        if(passwordConfirm.isEmpty() || !passwordConfirm.equals(password))
        {
            _passwordConfirmText.setError(getString(R.string.txt_ControlRepetirPass));
            valid = false;
        }
        else {
            _passwordConfirmText.setError(null);
        }

        if(Objects.equals(seleccionTipoUsuario, ""))
        {
            valid = false;
        }

        return valid;
    }

    //Obtiene los usuarios conectados cuando el servidor lo manda
    private Emitter.Listener onEmpleadoCreado = new Emitter.Listener() {
        @Override
        public void call(final Object... args)
        {
            SignUpActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final ProgressDialog progressDialog = new ProgressDialog(SignUpActivity.this,
                            R.style.AppTheme);
                    try
                    {

                        progressDialog.setIndeterminate(true);
                        progressDialog.setMessage(getString(R.string.txtCreandoUsuario));
                        progressDialog.show();
                        JSONObject data = (JSONObject) args[0];//Obtenemos el array del servidor

                        if(data.getString("id") != null)
                            {
                                int idEmpleado = data.getInt("id");
                                creadoCorrectamente = idEmpleado > 0;

                                if (creadoCorrectamente) {
                                    progressDialog.dismiss();
                                    onSignupSuccess();

                                } else {
                                    progressDialog.dismiss();
                                    onSignupFailed();
                                }
                            }

                    } catch (JSONException e)
                    {
                        Log.e(TAG, e.getMessage());
                        progressDialog.dismiss();
                    }
                }
            });


        }
    };

    private Emitter.Listener onClienteCreado = new Emitter.Listener() {
        @Override
        public void call(final Object... args)
        {
            SignUpActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final ProgressDialog progressDialog = new ProgressDialog(SignUpActivity.this,
                            R.style.AppTheme);
                    try
                    {

                        progressDialog.setIndeterminate(true);
                        progressDialog.setMessage(getString(R.string.txtCreandoUsuario));
                        progressDialog.show();
                        JSONObject data = (JSONObject) args[0];//Obtenemos el array del servidor

                        if(data.getString("id") != null)
                        {
                            int idCliente = data.getInt("id");
                            creadoCorrectamente = idCliente > 0;

                            if (creadoCorrectamente) {
                                progressDialog.dismiss();
                                onSignupSuccess();

                            } else {
                                progressDialog.dismiss();
                                onSignupFailed();
                            }
                        }

                    } catch (JSONException e)
                    {
                        Log.e(TAG, e.getMessage());
                        progressDialog.dismiss();
                    }
                }
            });


        }
    };

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            SignUpActivity.this.runOnUiThread(new Runnable() {
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
            SignUpActivity.this.runOnUiThread(new Runnable() {
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
            SignUpActivity.this.runOnUiThread(new Runnable() {
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
        _signupButton.setEnabled(false);

    }

    private void activarBotones()
    {
        _signupButton.setEnabled(true);

    }
    private void handleSnackBarConexion(String evento)
    {
        Snackbar estado = LogicaUsuario.handleSnackBarConexion(evento,SignUpActivity.this);
        if(estado != null)
        {
            estado.show();
            if(evento.equals("fallo") || evento.equals("desconecto") )
            {
                desactivarBotones();
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        // An item was selected. You can retrieve the selected item using
        String test = parent.getItemAtPosition(position).toString();
        seleccionTipoUsuario = test;

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        seleccionTipoUsuario = "";

    }
}
