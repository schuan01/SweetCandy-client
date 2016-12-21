package infozonaorg.com.testnode;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import infozonaorg.com.testnode.Clases.Empleado;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
{
    private Socket mSocket;
    private Boolean isConnected = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try
        {
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
        //FIN SOCKETS ----------------------------------------------------------------
    }

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(!isConnected) {
                        Toast.makeText(getApplicationContext(),
                                R.string.connect, Toast.LENGTH_SHORT).show();
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
                    /*Toast.makeText(getApplicationContext(),
                            R.string.disconnect, Toast.LENGTH_LONG).show();*/
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
                    Toast.makeText(getApplicationContext(),
                            R.string.error_connect, Toast.LENGTH_SHORT).show();
                }
            });
        }
    };
}


