package infozonaorg.com.testnode.Utils;

import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import infozonaorg.com.testnode.Clases.Cliente;
import infozonaorg.com.testnode.Clases.Empleado;
import infozonaorg.com.testnode.Clases.Session;
import infozonaorg.com.testnode.Clases.Transaccion;
import infozonaorg.com.testnode.R;
import infozonaorg.com.testnode.TestApplication;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class FullscreenViewActivity extends AppCompatActivity {


    private FullScreenImageAdapter adapter;
    private Socket mSocket;
    private Boolean isConnected = true;
    private Snackbar snackbarConectado = null;
    private Snackbar snackbarDesconectado = null;
    private Snackbar snackbarFallo = null;
    private Empleado empleadoEncontrado = null;
    private Cliente clienteConectado = null;
    private Session session = null;

    @BindView(R.id.txtUsuario) TextView _txtUsuario;
    @BindView(R.id.pager) ViewPager viewPager;
    @BindView(R.id.btnMandarSolicitud) Button _btnMandarSolicitud;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_view);
        ButterKnife.bind(this);

        //SOCKETS ----------------------------------------------------------------
        TestApplication app = (TestApplication) getApplication();
        mSocket = app.getSocket();
        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.on("usuarioencontrado", onUsuarioEncontrado);//obtiene el usuario pasado por parametro
        mSocket.on("transaccioniniciada", onTransaccionIniciada);//comieza la transaccion
        //mSocket.emit("getfotosusuario", session.toJSON());
        //FIN SOCKETS ----------------------------------------------------------------

        session = new Session(FullscreenViewActivity.this, true);

        Intent i = getIntent();
        String idMarker = i.getStringExtra("idMarker");
        int idClienteConectado = session.getId();
        clienteConectado = new Cliente();
        clienteConectado.setId(idClienteConectado);
        float[] ubi = session.getUltimaUbicacion();
        clienteConectado.setUbicacion(new LatLng(ubi[0],ubi[1]));


        empleadoEncontrado = new Empleado();
        empleadoEncontrado.setId(Integer.parseInt(idMarker));
        mSocket.emit("obtenerempleado",empleadoEncontrado.toJSON());

        //Session session = new Session(getBaseContext(), true);
        //ArrayList<String> fotos = session.getFotos();




        _btnMandarSolicitud.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(empleadoEncontrado != null)
                {

                    try
                    {

                        Transaccion t = new Transaccion();
                        t.setClienteTransaccion(clienteConectado);
                        t.setEmpleadoTransaccion(empleadoEncontrado);


                        mSocket.emit("enviarsolicitudausuario",t.toJSON());


                    } catch (Exception e) {

                        Log.e("Error", e.getMessage());
                    }


                }



            }
        });
    }

    //Obtiene el usuario pasado por parametro
    private Emitter.Listener onUsuarioEncontrado = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            FullscreenViewActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject data = (JSONObject) args[0];//Obtenemos el unico elemento
                        if(data != null) {
                            empleadoEncontrado = new Empleado();
                            empleadoEncontrado.setId(data.getInt("id"));
                            empleadoEncontrado.setUsuario(data.getString("usuario"));
                            ArrayList<String> fotos = new ArrayList<String>();
                            JSONArray lista = data.getJSONArray("fotos");//Obtenemos el array del servidor
                            for (int con = 0; con < lista.length(); con++) {
                                JSONObject foto = (JSONObject) lista.get(con);//Obtenemos cada elemento

                                String urlFoto = foto.getString("urlFoto");
                                fotos.add(urlFoto);

                            }

                            _txtUsuario.setText(empleadoEncontrado.getUsuario());

                            adapter = new FullScreenImageAdapter(FullscreenViewActivity.this, fotos);

                            viewPager.setAdapter(adapter);

                            // displaying selected image first
                            viewPager.setCurrentItem(0);//Que arranque del principio
                        }
                        else
                        {
                            //TODO
                            //Verificar si esta Online en el servidor y mostrar un mensaje de no disponible
                            //Deshabilitar botones
                            finish();//Si no hay usuario, adios
                        }

                    } catch (Exception e) {
                        Log.e("Error", e.getMessage());

                    }
                }
            });


        }
    };

    //Recibe la confirmacion de la transaccion iniciada
    private Emitter.Listener onTransaccionIniciada = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            FullscreenViewActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {

                        finish();
                       /* mSocket.off("empleadoscercanos", onCercanos);//Dejo de escuchar hasta que finalice
                        final JSONObject data = (JSONObject) args[0];//Obtenemos el unico elemento
                        transaccionActual = new Transaccion();
                        transaccionActual.setIdTransaccion(data.getInt("id"));
                        transaccionActual.setActiva(true);
                        transaccionActual.setFechaInicioTransaccion(data.getString("fechaInicioTransaccion"));
                        transaccionActual.setEmpleadoTransaccion(new Empleado(new JSONObject(data.getString("empleadoTransaccion"))));
                        transaccionActual.setClienteTransaccion(new Cliente(new JSONObject(data.getString("clienteTransaccion"))));
                        transaccionActual.setIdBusquedaTransaccion(data.getInt("idBusquedaTransaccion"));
                        transaccionActiva = true;
                        btnFinalizarTransaccion.setVisibility(View.VISIBLE);
                        btnCancelarBusqueda.setVisibility(View.GONE);
                        btnCancelarBusqueda.setProgress(0);
                        btnBuscarDisponible.setVisibility(View.GONE);
                        dibujarTransaccion();

                        Toast.makeText(getApplicationContext(),
                                "Transaccion Iniciada", Toast.LENGTH_LONG).show();*/


                    } catch (Exception e) {
                        Log.e("Error", e.getMessage());

                    }
                }
            });


        }
    };

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!isConnected) {
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
            runOnUiThread(new Runnable() {
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
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    desactivarBotones();
                    handleSnackBarConexion("fallo");
                }
            });
        }
    };

    private void desactivarBotones() {
        //_btnGuardar.setEnabled(false);

    }

    private void activarBotones() {
        //_btnGuardar.setEnabled(true);

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
                break;
            case "desconecto":
                if (snackbarFallo != null) {
                    snackbarFallo.dismiss();
                    snackbarFallo = null;
                }
                if (snackbarDesconectado == null) {
                    snackbarDesconectado = Snackbar.make(findViewById(android.R.id.content), R.string.disconnect, Snackbar.LENGTH_INDEFINITE);
                    View sbView = snackbarDesconectado.getView();
                    sbView.setBackgroundColor(Color.RED);
                    TextView tv = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                    tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    snackbarDesconectado.show();
                }
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
                    snackbarConectado = Snackbar.make(findViewById(android.R.id.content), R.string.connect, Snackbar.LENGTH_SHORT);
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
