package infozonaorg.com.testnode;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.CountDownTimer;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.dd.processbutton.iml.ActionProcessButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import infozonaorg.com.testnode.Clases.Cliente;
import infozonaorg.com.testnode.Clases.Empleado;
import infozonaorg.com.testnode.Clases.Session;
import infozonaorg.com.testnode.Clases.Transaccion;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener
{

    private static final String LOCATION_KEY = "";
    private static final String REQUESTING_LOCATION_UPDATES_KEY = "";
    private static final int MY_PERMISSION_REQUEST_READ_FINE_LOCATION = 1 ;
    private GoogleMap mMap;

    // The entry point to Google Play services, used by the Places API and Fused Location Provider.
    private GoogleApiClient mGoogleApiClient;

    private JSONArray lista;
    private List<Empleado> lstEmpleadosCercanos = new ArrayList<Empleado>();
    private Socket mSocket;
    private Boolean isConnected = true;
    private String tipoUsuario = "";
    private Session session;
    private Snackbar snackbarConectado = null;
    private Snackbar snackbarDesconectado = null;
    private Snackbar snackbarFallo = null;

    @InjectView(R.id.btnBuscarDisponible) Button btnBuscarDisponible;
    @InjectView(R.id.btnFinalizarTransaccion) Button btnFinalizarTransaccion;
    @InjectView(R.id.btnCancelarBusqueda) ActionProcessButton btnCancelarBusqueda;
    @InjectView(R.id.btnToggle)     ImageButton btnToggle;


    //AGREGAR MARCADOR NUEVO
    Marker marcadorYo = null;

    //Empleado actual conectado
    Empleado empleadoConectado = new Empleado();

    //Cliente actual conectado
    Cliente clienteConectado = new Cliente();

    //Ultima ubicacion
    Location mLastLocation = null;

    //Location Request
    LocationRequest  mLocationRequest = null;

    //Algo
    boolean mRequestingLocationUpdates = true;

    //Alerta Aceptar solicituds
    AlertDialog alertaSolicitud = null;

    //Transaccion Actual
    Transaccion transaccionActual = null;

    //Para dibujar en el mapa el trazo de distancia
    private PolylineOptions options = null;

    //Para saber si hay una transaccion activa actualmente
    boolean transaccionActiva = false;

    //Para el drawer
    //save our header or result
    private AccountHeader headerResult = null;
    private Drawer result = null;

    //Buscando empleado disponible
    boolean buscandoEmpleado = false;

    //Timer para cancelar
    CountDownTimer timer = null;

    //Cliente solicitud
    Cliente clienteSolicitud = null;

    //Procesando
    //ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);
        setTitle("App Test");
        Log.w("OnCreate Maps", "Ejecutado!");

        buildGoogleApiClient();
        mGoogleApiClient.connect();
        createLocationRequest();
        updateValuesFromBundle(savedInstanceState);

        TestApplication app = (TestApplication) getApplication();
        mSocket = app.getSocket();
        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.on("empleadoscercanos", onCercanos);//obtiene los cercanos
        mSocket.on("solicitudcliente", onSolicitudCliente);//solicitud aceptada del servidor
        mSocket.on("solicitudcancelada", onSolicitudCancelada);//solicitud cancelada del servidor
        mSocket.on("transaccioniniciada", onTransaccionIniciada);//comieza la transaccion
        mSocket.on("transaccionfinalizada", onTransaccionIniciada);//comieza la transaccion
        mSocket.on("clienteanonimoconectado", onClienteAnonimoConectado);//cuando conecta un cliente
        mSocket.on("transaccionfinalizada", onTransaccionFinalizada);//cuando finaliza la transaccion

        //LO QUE VIENE DEL MAIN ACTIVITY
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            tipoUsuario = extras.getString("tipoBoton");
        }

        if(tipoUsuario != null && tipoUsuario.equals("Empleado"))
        {
            session = new Session(MapsActivity.this);
            empleadoConectado.setId(session.getId());
            empleadoConectado.setUsuario(session.getUsuario());
            empleadoConectado.setEmail(session.getEmail());
            empleadoConectado.setDescripcion(session.getDescripcion());


        }

        if(tipoUsuario != null && tipoUsuario.equals("Cliente"))
        {
            clienteConectado.setUsuario("Cliente Anonimo");
            clienteConectado.setOnline(true);
            clienteConectado.setEdad(20);
            mSocket.emit("conectarclienteanonimo",clienteConectado.toJSON());

        }

        //BUTTER KNIFE
        ButterKnife.inject(this);

        //-------------DRAWER---------------------------
        //Remove line to test RTL support
        //getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        // Handle Toolbar


        // Create a few sample profile
        // NOTE you have to define the loader logic too. See the CustomApplication for more details


        IProfile profile3 = null;
        if(tipoUsuario.equals("Empleado"))
        {
            profile3 = new ProfileDrawerItem().withName(empleadoConectado.getUsuario()).withEmail(empleadoConectado.getEmail()).withIcon(Uri.parse("https://s-media-cache-ak0.pinimg.com/736x/4d/b2/4b/4db24b16ab66d4829fad06aa05c866b5.jpg")).withIdentifier(102);
        }
        else
        {
            profile3 = new ProfileDrawerItem().withName("Anonimo").withEmail("").withIcon(Uri.parse("https://s-media-cache-ak0.pinimg.com/736x/4d/b2/4b/4db24b16ab66d4829fad06aa05c866b5.jpg")).withIdentifier(102);
        }

        // Create the AccountHeader
        headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withTranslucentStatusBar(true)
                .withHeaderBackground(R.drawable.material_background3)
                //.addProfiles( profile3)
                .withSavedInstance(savedInstanceState)
                .build();

        //Create the drawer
        result = new DrawerBuilder()
                .withActionBarDrawerToggleAnimated(true)
                .withActivity(this)
                .withHasStableIds(true)
                .withAccountHeader(headerResult) //set the AccountHeader we created earlier for the header
                .addDrawerItems(
                        new PrimaryDrawerItem().withName("Configuracion").withDescription("Edita los datos de la cuenta").withIcon(FontAwesome.Icon.faw_cog).withIdentifier(1).withSelectable(false)

                ) // add the items we want to use with our Drawer
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        //check if the drawerItem is set.
                        //there are different reasons for the drawerItem to be null
                        //--> click on the header
                        //--> click on the footer
                        //those items don't contain a drawerItem

                        if (drawerItem != null) {
                            Intent intent = null;
                            if (drawerItem.getIdentifier() == 1) {//Configuracion
                                intent = new Intent(MapsActivity.this, EditActivity.class);
                            }

                            if (intent != null) {
                                MapsActivity.this.startActivity(intent);
                            }


                        }
                        return false;
                    }

                })
                .withSavedInstance(savedInstanceState)
                .withShowDrawerOnFirstLaunch(true)
                .build();

        //only set the active selection or active profile if we do not recreate the activity
        if (savedInstanceState == null) {

            //set the active profile
            headerResult.setActiveProfile(profile3);
        }


        //------------ FIN DRAWER ------------------------





        //BOTON BUSCAR DISPONIBLE
        btnBuscarDisponible.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0)
            {


                buscarEmpleadoDisponible();

            }
        });

        //BOTON FINALIZAR TRANSACCION
        btnFinalizarTransaccion.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0)
            {

                finalizarTransaccion();

            }
        });

        //BOTON CANCELAR BUSQUEDA
        btnCancelarBusqueda.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0)
            {

                cancelarBusqueda();

            }
        });

        //BOTON DRAWER TOGGLE
        btnToggle.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0)
            {

                if(result != null && !result.isDrawerOpen())
                    result.openDrawer();

            }
        });







        /*//BOTON CANCELAR
        btnCancelarBusqueda = (ActionProcessButton) findViewById(R.id.btnCancelarBusqueda);
        btnCancelarBusqueda.setMode(ActionProcessButton.Mode.PROGRESS);

        btnCancelarBusqueda.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    timer = new CountDownTimer(5000, 1000) {

                        public void onTick(long millisUntilFinished) {
                            if( millisUntilFinished / 1000 == 4) {
                                btnCancelarBusqueda.setProgress(25);
                            }

                            if( millisUntilFinished / 1000 == 3) {
                                btnCancelarBusqueda.setProgress(50);
                            }

                            if( millisUntilFinished / 1000 == 2) {
                                btnCancelarBusqueda.setProgress(75);
                            }

                            if( millisUntilFinished / 1000 == 1) {
                                btnCancelarBusqueda.setProgress(100);
                            }
                        }

                        public void onFinish() {

                        }
                    }.start();

                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    timer = null;

                }
                return false;
            }
        });*/


        // you can display endless google like progress indicator
        //btnCancelarBusqueda.setMode(ActionProcessButton.Mode.ENDLESS);
        // set progress > 0 to start progress indicator animation
        //btnCancelarBusqueda.setProgress(1);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        //mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(-34.893713,-56.171671)));//Montevideo
        if (ContextCompat.checkSelfPermission(MapsActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission( getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED)
        {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(MapsActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSION_REQUEST_READ_FINE_LOCATION);

                // MY_PERMISSION_REQUEST_READ_FINE_LOCATION is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        else
        {
            //Esto pone el marcador del punto azul y el boton de centrar en el mapa
            mMap.setMyLocationEnabled(true);

        }



        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                //final LocationSettingsStates loc = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location requests here.
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    MapsActivity.this,
                                    0);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        break;
                }
            }
        });

    }

    //Obtiene el cliente que se acaba de conectar
    private Emitter.Listener onClienteAnonimoConectado = new Emitter.Listener() {
        @Override
        public void call(final Object... args)
        {
            MapsActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try
                    {
                        JSONObject data = (JSONObject) args[0];//Obtenemos el unico elemento
                        clienteConectado = new Cliente(data);

                    } catch (Exception e)
                    {
                        Log.e("Error", e.getMessage());

                    }
                }
            });


        }
    };

    //Obtiene los usuarios cercanos cuando el servidor lo manda
    private Emitter.Listener onCercanos = new Emitter.Listener() {
        @Override
        public void call(final Object... args)
        {
            MapsActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try
                    {



                        lstEmpleadosCercanos = new ArrayList<Empleado>();
                        lista = (JSONArray) args[0];//Obtenemos el array del servidor
                        for (int con = 0; con < lista.length(); con++)
                        {
                            JSONObject data = (JSONObject) lista.get(con);//Obtenemos cada elemento

                            Empleado e = new Empleado(data);
                            lstEmpleadosCercanos.add(e);
                        }

                        for(Empleado e : lstEmpleadosCercanos)
                        {
                            if(e.getUbicacion() != null) {
                                mMap.addMarker(new MarkerOptions().position(e.getUbicacion()).title(e.getUsuario()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)));
                            }
                            else
                            {
                                Log.w("onCercanos","Ubicacion de " + e.getUsuario() + " no encontrada");
                            }

                        }

                    } catch (Exception e)
                    {
                        Log.e("Error", e.getMessage());

                    }
                }
            });


        }
    };

    //Obtiene la solicitud de un cliente
    private Emitter.Listener onSolicitudCliente = new Emitter.Listener() {
        @Override
        public void call(final Object... args)
        {
            MapsActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try
                    {
                        if(tipoUsuario.equals("Empleado"))
                        {
                            if(alertaSolicitud == null)
                            {
                                final JSONObject data = (JSONObject) args[0];//Obtenemos el unico elemento
                                clienteSolicitud = new Cliente(data);

                                mSocket.off("solicitudcliente",onSolicitudCliente);//Dejo de escuchar hasta que el empleado acepte
                                alertaSolicitud = new AlertDialog.Builder(MapsActivity.this)
                                        .setTitle("Nueva solicitud: " + clienteSolicitud.getUsuario())
                                        .setMessage("Quieres aceptar la solicitud?")
                                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {

                                                JSONObject cliente = clienteSolicitud.toJSON();
                                                try {

                                                    cliente.put("tipo", "Cliente");

                                                } catch (JSONException e) {

                                                    Log.e("Error",e.getMessage());
                                                }

                                                JSONObject empleado = empleadoConectado.toJSON();
                                                try {

                                                    empleado.put("tipo", "Empleado");

                                                } catch (JSONException e) {

                                                    Log.e("Error",e.getMessage());
                                                }

                                                JSONObject transaccion = null;
                                                try
                                                {
                                                    Transaccion t = new Transaccion();
                                                    t.setActiva(true);//Empieza a estar activa
                                                    t.setFechaFinTransaccion(null);
                                                    t.setEmpleadoTransaccion(empleadoConectado);
                                                    t.setClienteTransaccion(clienteSolicitud);
                                                    transaccion = t.toJSON();
                                                    transaccion.put("tipo", "Transaccion");

                                                } catch (JSONException e) {

                                                    Log.e("Error",e.getMessage());
                                                }

                                                JSONArray jsonArray = new JSONArray();

                                                jsonArray.put(cliente);
                                                jsonArray.put(empleado);
                                                jsonArray.put(transaccion);

                                                mSocket.emit("aceptarsolicitud", jsonArray);
                                                alertaSolicitud = null;
                                                clienteSolicitud = null;
                                            }
                                        })
                                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                mSocket.on("solicitudcliente",onSolicitudCliente);
                                                alertaSolicitud = null;
                                                clienteSolicitud = null;
                                            }
                                        })
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .show();
                            }
                        }

                    } catch (Exception e)
                    {
                        Log.e("Error", e.getMessage());

                    }
                }
            });


        }
    };

    //Cuando el cliente cancela la solicitud
    private Emitter.Listener onSolicitudCancelada = new Emitter.Listener() {
        @Override
        public void call(final Object... args)
        {
            MapsActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try
                    {
                        if(tipoUsuario.equals("Empleado"))
                        {
                            if(alertaSolicitud != null)//Si el mensaje esta mostrado en la pantalla
                            {
                                final JSONObject data = (JSONObject) args[0];//Obtenemos el unico elemento

                                if(clienteSolicitud != null)//Si hay un cliente mostrado
                                {
                                    if(clienteSolicitud.getId() == data.getInt("id"))//Si es el mismo cliente mostrado que el de la solicitud
                                    {
                                        alertaSolicitud.dismiss();
                                        alertaSolicitud = null;
                                        mSocket.on("solicitudcliente",onSolicitudCliente);//Empiezo a escuchar
                                    }
                                }
                            }




                        }

                    } catch (Exception e)
                    {
                        Log.e("Error", e.getMessage());

                    }
                }
            });


        }
    };

    //Obtiene los usuarios cercanos cuando el servidor lo manda
    private Emitter.Listener onTransaccionIniciada = new Emitter.Listener() {
        @Override
        public void call(final Object... args)
        {
            MapsActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try
                    {
                            mSocket.off("empleadoscercanos",onCercanos);//Dejo de escuchar hasta que finalice
                            final JSONObject data = (JSONObject) args[0];//Obtenemos el unico elemento
                            transaccionActual = new Transaccion();
                            transaccionActual.setIdTransaccion(data.getInt("id"));
                            transaccionActual.setActiva(true);
                            Date fechaIni = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(data.getString("fechaInicioTransaccion"));
                            transaccionActual.setFechaInicioTransaccion(fechaIni);
                            transaccionActual.setEmpleadoTransaccion(new Empleado(new JSONObject(data.getString("empleadoTransaccion"))));
                            transaccionActual.setClienteTransaccion(new Cliente(new JSONObject(data.getString("clienteTransaccion"))));
                            transaccionActiva = true;
                            btnFinalizarTransaccion.setVisibility(View.VISIBLE);
                            btnCancelarBusqueda.setVisibility(View.GONE);
                            btnCancelarBusqueda.setProgress(0);
                            btnBuscarDisponible.setVisibility(View.GONE);
                            dibujarTransaccion();

                            Toast.makeText(getApplicationContext(),
                                   "Transaccion Iniciada", Toast.LENGTH_LONG).show();



                    } catch (Exception e)
                    {
                        Log.e("Error", e.getMessage());

                    }
                }
            });


        }
    };

    //Obtiene los usuarios cercanos cuando el servidor lo manda
    private Emitter.Listener onTransaccionFinalizada = new Emitter.Listener() {
        @Override
        public void call(final Object... args)
        {
            MapsActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try
                    {
                        btnBuscarDisponible.setVisibility(View.VISIBLE);
                        btnCancelarBusqueda.setVisibility(View.GONE);
                        btnCancelarBusqueda.setMode(ActionProcessButton.Mode.ENDLESS);
                        btnCancelarBusqueda.setProgress(0);
                        btnFinalizarTransaccion.setVisibility(View.GONE);
                        buscandoEmpleado = false;
                        transaccionActiva = false;
                        transaccionActual = null;
                        btnFinalizarTransaccion.setVisibility(View.GONE);
                        btnCancelarBusqueda.setVisibility(View.GONE);
                        btnCancelarBusqueda.setProgress(0);
                        btnBuscarDisponible.setVisibility(View.VISIBLE);
                        mSocket.on("empleadoscercanos",onCercanos);//Escucho los empleados cercanos
                        mSocket.on("solicitudcliente",onSolicitudCliente);//Vuelvo a escuchar
                        dibujarTransaccion();


                    } catch (Exception e)
                    {
                        Log.e("Error", e.getMessage());

                    }
                }
            });


        }
    };

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            MapsActivity.this.runOnUiThread(new Runnable() {
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
            MapsActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    isConnected = false;
                    desactivarBotones();
                    handleSnackBarConexion("desconecto");
                }
            });
        }
    };

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            MapsActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    desactivarBotones();
                    handleSnackBarConexion("fallo");

                }
            });
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_READ_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    mMap.setMyLocationEnabled(true);

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }

            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onDestroy()
    {
        //TODO
        //Sacar el desconectar del onDestroy y manejarlo mejor
        Log.w("onDestroy() Maps","Ejecutado");
        if(tipoUsuario.equals("Empleado")) {
            desconectarUsuario();
        }
        else
        {
            desconectarClienteAnonimo();
        }
        //desconectarSocket();
        super.onDestroy();

    }

    private void desconectarUsuario()
    {
        if(tipoUsuario.equals("Empleado")) {
            JSONObject informacion = new JSONObject();
            try {
                informacion = empleadoConectado.toJSON();

            } catch (Exception e) {

                Log.e("Error",e.getMessage());
            }

            mSocket.emit("desconectarusuario", informacion);
        }

    }

    private void desconectarClienteAnonimo()
    {
        if(tipoUsuario.equals("Cliente")) {
            JSONObject informacion = new JSONObject();
            try {
                informacion = clienteConectado.toJSON();

            } catch (Exception e) {

                Log.e("Error",e.getMessage());
            }

            mSocket.emit("desconectarclienteanonimo", informacion);
        }

    }

    /*private void desconectarSocket()
    {
        mSocket.disconnect();

        mSocket.off(Socket.EVENT_CONNECT, onConnect);
        mSocket.off(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.off("empleadoscercanos", onCercanos);//obtiene los cercanos
        mSocket.off("solicitudcliente", onSolicitudCliente);//maneja la respuesta del servidor a la solicitud cliente

    }*/

    /*private void conectarSocket()
    {
        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.on("empleadoscercanos", onCercanos);//obtiene los cercanos
        mSocket.on("solicitudcliente", onSolicitudCliente);//solicitud aceptada del servidor
        mSocket.on("transaccioniniciada", onTransaccionIniciada);//comieza la transaccion
        mSocket.connect();
    }*/

    /**
     * Builds a GoogleApiClient.
     * Uses the addApi() method to request the Google Places API and the Fused Location Provider.
     */
    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(15000);
        mLocationRequest.setFastestInterval(10000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    @Override
    public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null)
        {

            //Cuando conecta, me manda a la ubicacion
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()),15.0f));//Mi ubicacion


            if(tipoUsuario.equals("Cliente"))
            {
                //TODO
                //Borrar el marcador YO ya que voy a usar el marcador por defecto del mapa(punto azul)

                marcadorYo = mMap.addMarker(new MarkerOptions().position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))
                        .title(clienteConectado.getUsuario())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

                clienteConectado.setUbicacion(marcadorYo.getPosition());

                JSONObject informacion = new JSONObject();
                try {

                    informacion = clienteConectado.toJSON();
                    informacion.put("limite", Constantes.LIMITE_CERCANOS);

                } catch (JSONException e) {

                    Log.e("Error",e.getMessage());
                }


                if(!transaccionActiva || transaccionActual == null) {
                    //Manda la info necesaria para saber cercanos en el servidor
                    mSocket.emit("getcercanos", informacion);
                }
            }

            if(tipoUsuario.equals("Empleado"))
            {
                marcadorYo = mMap.addMarker(new MarkerOptions().position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))
                        .title(empleadoConectado.getUsuario())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

                //Le seteamos la ubicacion marcada
                empleadoConectado.setUbicacion(marcadorYo.getPosition());


                JSONObject informacion = new JSONObject();
                try
                {
                    informacion = empleadoConectado.toJSON();

                } catch (Exception e) {

                    Log.e("Error",e.getMessage());
                }

                    //Manda la info necesaria del empleado en cuestion
                    mSocket.emit("setubacionempleado", informacion);

            }

            Log.i("LATITUD onConnected",String.valueOf(mLastLocation.getLatitude()));
            Log.i("LONGITUD onConnected",String.valueOf(mLastLocation.getLongitude()));


        }
        else
        {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-34.893713,-56.171671),15.0f));//Montevideo
        }

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        updateUI();
    }

    private void updateUI() {
        if (mLastLocation != null) {
            mMap.clear();
            if(tipoUsuario.equals("Cliente"))
            {
                marcadorYo = mMap.addMarker(new MarkerOptions().position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))
                        .title(clienteConectado.getUsuario())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

                clienteConectado.setUbicacion(marcadorYo.getPosition());

                if(transaccionActiva && transaccionActual != null)
                {
                    transaccionActual.getClienteTransaccion().setUbicacion(marcadorYo.getPosition());
                }

                JSONObject informacion = new JSONObject();
                try {
                    informacion = clienteConectado.toJSON();
                    informacion.put("limite", Constantes.LIMITE_CERCANOS);

                } catch (JSONException e) {

                    Log.e("Error",e.getMessage());
                }

                //Manda la info necesaria para saber cercanos en el servidor
                if(!transaccionActiva || transaccionActual == null) {
                    mSocket.emit("getcercanos", informacion);
                }
            }

            if(tipoUsuario.equals("Empleado"))
            {
                marcadorYo = mMap.addMarker(new MarkerOptions().position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))
                        .title(empleadoConectado.getUsuario())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

                //Le seteamos la ubicacion marcada
                empleadoConectado.setUbicacion(marcadorYo.getPosition());

                if(transaccionActiva && transaccionActual != null)
                {
                    transaccionActual.getEmpleadoTransaccion().setUbicacion(marcadorYo.getPosition());
                }


                JSONObject informacion = new JSONObject();
                try
                {
                    informacion = empleadoConectado.toJSON();

                } catch (Exception e) {

                    Log.e("Error",e.getMessage());
                }



                //Manda la info necesaria del empleado en cuestion
                mSocket.emit("setubacionempleado", informacion);
            }

            dibujarTransaccion();


            Log.i("LATITUD updateUI", String.valueOf(mLastLocation.getLatitude()));
            Log.i("LONGITUD updateUI", String.valueOf(mLastLocation.getLongitude()));


        }
    }

    private void dibujarTransaccion()
    {

        if(transaccionActiva && transaccionActual != null) {
            mMap.clear();

            Marker clienteT = mMap.addMarker(new MarkerOptions().position(transaccionActual.getClienteTransaccion().getUbicacion()).title(transaccionActual.getClienteTransaccion().getUsuario())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            Marker empleadoT = mMap.addMarker(new MarkerOptions().position(transaccionActual.getEmpleadoTransaccion().getUbicacion()).title(transaccionActual.getEmpleadoTransaccion().getUsuario())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
            options.add(clienteT.getPosition());
            options.add(empleadoT.getPosition());
            mMap.addPolyline(options);
        }
    }

    private void buscarEmpleadoDisponible()
    {
        if(tipoUsuario.equals("Cliente"))
        {
            try
            {
                btnBuscarDisponible.setVisibility(View.GONE);
                btnCancelarBusqueda.setVisibility(View.VISIBLE);
                btnCancelarBusqueda.setMode(ActionProcessButton.Mode.ENDLESS);
                btnCancelarBusqueda.setProgress(1);
                buscandoEmpleado = true;

                mSocket.emit("buscardisponible",clienteConectado.toJSON());


            } catch (Exception e) {

                Log.e("Error",e.getMessage());
            }





        }
        else if(tipoUsuario.equals("Empleado"))
        {
            //TODO
            //BORRAR
            Cliente c = new Cliente();
            c.setUbicacion(new LatLng(-34.8931971,-56.1616734));//Cerca del trabajo
            JSONObject cliente = null;
            JSONObject empleado = null;
            JSONObject transaccion = null;

            try {
                cliente =c.toJSON();
                cliente.put("tipo","Cliente");
                empleado = empleadoConectado.toJSON();
                empleado.put("tipo","Empleado");

            } catch (Exception e) {

                Log.e("Error",e.getMessage());
            }




            try
            {
                Transaccion t = new Transaccion();
                t.setFechaFinTransaccion(null);
                t.setEmpleadoTransaccion(empleadoConectado);
                t.setClienteTransaccion(c);
                transaccion = t.toJSON();
                transaccion.put("tipo","Transaccion");

            } catch (Exception e) {

                Log.e("Error",e.getMessage());
            }

            JSONArray jsonArray = new JSONArray();

            jsonArray.put(cliente);
            jsonArray.put(empleado);
            jsonArray.put(transaccion);

            mSocket.emit("aceptarsolicitud", jsonArray);
            //BORRAR------------------------------------------------------
        }


    }

    @Override
    protected void onPause() {
        Log.w("onPause() Maps","Ejecutado");
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    public void onResume() {
        Log.w("onResume() Maps","Ejecutado");
        super.onResume();
        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    protected void stopLocationUpdates() {
        if(mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
        }
    }

    protected void startLocationUpdates() {
        if(mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }
    }



    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY,
                mRequestingLocationUpdates);
        savedInstanceState.putParcelable(LOCATION_KEY, mLastLocation);
        super.onSaveInstanceState(savedInstanceState);
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Update the value of mRequestingLocationUpdates from the Bundle, and
            // make sure that the Start Updates and Stop Updates buttons are
            // correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        REQUESTING_LOCATION_UPDATES_KEY);
                //setButtonsEnabledState();
            }

            // Update the value of mCurrentLocation from the Bundle and update the
            // UI to show the correct latitude and longitude.
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that
                // mCurrentLocationis not null.
                mLastLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }


            updateUI();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        //handle the back press :D close the drawer first and if the drawer is closed close the activity
        if (result != null && result.isDrawerOpen()) {
            result.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    protected void onStart() {
        Log.w("onStart() Maps","Ejecutado");
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {

        Log.w("onStop() Maps","Ejecutado");

        mGoogleApiClient.disconnect();
        super.onStop();
    }

    private void desactivarBotones()
    {
        btnBuscarDisponible.setEnabled(false);
        btnCancelarBusqueda.setEnabled(false);

    }

    private void activarBotones()
    {
        btnBuscarDisponible.setEnabled(true);
        btnCancelarBusqueda.setEnabled(true);

    }

    private void handleSnackBarConexion(String evento) {
        switch (evento) {
            case "fallo":
                if (snackbarDesconectado != null) {
                    snackbarDesconectado.dismiss();
                    snackbarDesconectado = null;
                }
                if (snackbarFallo == null) {
                    snackbarFallo = Snackbar.make(findViewById(android.R.id.content), "Fallo al conectar", Snackbar.LENGTH_INDEFINITE);
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
                    snackbarDesconectado = Snackbar.make(findViewById(android.R.id.content), "Desconectado", Snackbar.LENGTH_INDEFINITE);
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
                    snackbarConectado = Snackbar.make(findViewById(android.R.id.content), "Conectado", Snackbar.LENGTH_SHORT);
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

    private void cancelarBusqueda()
    {
        if(buscandoEmpleado) {
            buscandoEmpleado = false;
            mSocket.emit("cancelarsolicitud", clienteConectado.toJSON());
            btnCancelarBusqueda.setVisibility(View.GONE);
            btnCancelarBusqueda.setProgress(0);
            btnBuscarDisponible.setVisibility(View.VISIBLE);
        }

    }

    private void finalizarTransaccion()
    {
        if(transaccionActiva) {

            mSocket.emit("finalizartransaccion", transaccionActual.toJSON());

        }
    }
}
