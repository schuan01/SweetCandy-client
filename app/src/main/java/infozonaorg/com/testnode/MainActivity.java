package infozonaorg.com.testnode;

import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import infozonaorg.com.testnode.Clases.Session;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;


public class MainActivity extends AppCompatActivity
{
    private Socket mSocket;
    private Boolean isConnected = true;

    private Snackbar snackbarConectado = null;
    private Snackbar snackbarDesconectado = null;
    private Snackbar snackbarFallo = null;
    private Session session;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        try
        {
            /*//Borro la session si hay
            if(session != null) {
                session.clearAll();
            }*/

            //Conectamos el socket, el unico que conecta el socket es este
            //Ademas seria el unico que desconectaria el socket entero
            conectarSokect();


            //BOTON INCIO MAPA CLIENTE
            Button btnInicio = (Button) findViewById(R.id.btnMapa);
            btnInicio.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {

                    Intent about = new Intent(MainActivity.this, MapsActivity.class);
                    about.putExtra("tipoBoton","Cliente");
                    session = new Session(MainActivity.this,true);
                    session.setTipoUsuario("Cliente");
                    startActivity(about);

                }
            });

            //BOTON REGISTRO
            Button btnRegistro = (Button) findViewById(R.id.btnSignUp);
            btnRegistro.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {

                    Intent about = new Intent(MainActivity.this, SignUpActivity.class);
                    startActivity(about);

                }
            });

            //BOTON INCIO LOGIN
            Button btnLogin = (Button) findViewById(R.id.btnLogin);
            btnLogin.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {

                    Intent about = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(about);

                }
            });


        }
        catch (Exception ex)
        {
            Log.e("ERROR",ex.getMessage());
        }
    }

    private void conectarSokect()
    {
        //SOCKETS ----------------------------------------------------------------
        TestApplication app = (TestApplication) getApplication();
        mSocket = app.getSocket();
        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);

        mSocket.connect();//Devuelve el socket de la URL pasada, siempre obtiene el mismo Socket con Singleton
        Log.w("conectarSokect()", "Ejecutado");
        //FIN SOCKETS ----------------------------------------------------------------
    }

    private void desconectarSocket()
    {
        mSocket.disconnect();

        mSocket.off(Socket.EVENT_CONNECT, onConnect);
        mSocket.off(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
    }

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            MainActivity.this.runOnUiThread(new Runnable() {
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
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    isConnected = false;
                    handleSnackBarConexion("desconecto");



                }
            });
        }
    };

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    handleSnackBarConexion("fallo");



                }
            });
        }
    };

    protected void onStart() {
        Log.w("onStart() Main","Ejecutado");
        super.onStart();
    }

    protected void onStop() {

        Log.w("onStop() Main","Ejecutado");
        super.onStop();
    }

    @Override
    protected void onPause() {
        Log.w("onPause() Main","Ejecutado");
        super.onPause();
    }

    @Override
    public void onResume() {
        conectarSokect();
        Log.w("onResume() Main","Ejecutado");
        super.onResume();
    }

    @Override
    public void onDestroy()
    {
        desconectarSocket();
        Log.w("onDestroy() Main","Ejecutado");
        super.onDestroy();

    }

    private void desactivarBotones()
    {
        Button btnInicio = (Button) findViewById(R.id.btnMapa);
        btnInicio.setEnabled(false);

        Button btnRegistro = (Button) findViewById(R.id.btnSignUp);
        btnRegistro.setEnabled(false);

        Button btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setEnabled(false);

    }

    private void activarBotones()
    {
        Button btnInicio = (Button) findViewById(R.id.btnMapa);
        btnInicio.setEnabled(true);

        Button btnRegistro = (Button) findViewById(R.id.btnSignUp);
        btnRegistro.setEnabled(true);

        Button btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setEnabled(true);

    }

    private void handleSnackBarConexion(String evento) {
            switch (evento) {
                case "fallo":
                    if (snackbarDesconectado != null) {
                        snackbarDesconectado.dismiss();
                        snackbarDesconectado = null;
                    }
                    if (snackbarFallo == null) {
                        snackbarFallo = Snackbar.make(findViewById(android.R.id.content), R.string.error_connect, Snackbar.LENGTH_INDEFINITE);
                        View sbView = snackbarFallo.getView();
                        sbView.setBackgroundColor(Color.RED);
                        TextView tv = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        snackbarFallo.show();

                    }
                    desactivarBotones();
                    break;
                case "desconecto":
                    if (snackbarFallo != null) {
                        snackbarFallo.dismiss();
                        snackbarFallo = null;
                    }
                    if (snackbarDesconectado == null) {
                        snackbarDesconectado = Snackbar.make(findViewById(android.R.id.content), R.string.text_desconectado, Snackbar.LENGTH_INDEFINITE);
                        View sbView = snackbarDesconectado.getView();
                        sbView.setBackgroundColor(Color.RED);
                        TextView tv = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        snackbarDesconectado.show();

                    }
                    desactivarBotones();
                    break;

                case "conecto":
                    if (snackbarDesconectado != null) {
                        snackbarDesconectado.dismiss();
                        snackbarDesconectado = null;
                    }

                    if (snackbarFallo != null) {
                        snackbarFallo.dismiss();
                        snackbarFallo = null;
                    }

                    if (snackbarConectado == null) {
                        snackbarConectado = Snackbar.make(findViewById(android.R.id.content), R.string.text_conectado, Snackbar.LENGTH_SHORT);
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


