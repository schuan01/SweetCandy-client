package infozonaorg.com.testnode;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

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
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.context.IconicsLayoutInflater;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mikepenz.materialdrawer.model.interfaces.Nameable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
    private List<Empleado> lstEmpleadosConectados = new ArrayList<Empleado>();
    private List<Empleado> lstEmpleadosCercanos = new ArrayList<Empleado>();
    private ArrayList<Integer> lstidConectados = new ArrayList<>();
    private Socket mSocket;
    private Boolean isConnected = true;
    private String tipoUsuario = "";
    private Session session;


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
    private Polyline mPolyline;

    //Para el drawer
    private Drawer result = null;
    private AccountHeader headerResult = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setTitle("App Test");
        Log.w("OnCreate Maps","Ejecutado!");

        //-------------DRAWER---------------------------
        new DrawerBuilder().withActivity(this).build();

        //if you want to update the items at a later time it is recommended to keep it in a variable
        PrimaryDrawerItem item1 = new PrimaryDrawerItem().withIdentifier(1).withName("Inicio");
        SecondaryDrawerItem item2 = new SecondaryDrawerItem().withIdentifier(2).withName("Configuracion");
        final IProfile profile3 = new ProfileDrawerItem().withName("Juan Volpe").withEmail("schuanv@gmail.com").withIcon(R.drawable.common_signin_btn_text_pressed_light).withIdentifier(102);


        // Create the AccountHeader
        headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withTranslucentStatusBar(true)
                .withSavedInstance(savedInstanceState)
                .addProfiles(
                        profile3
                )
                .build();

        result = new DrawerBuilder()
                .withActivity(this)
                .withHasStableIds(true)
                .withAccountHeader(headerResult) //set the AccountHeader we created earlier for the header
                .addDrawerItems(
                        new PrimaryDrawerItem().withName("Test1").withDescription("Desc1").withIcon(GoogleMaterial.Icon.gmd_build).withIdentifier(1).withSelectable(false)) // add the items we want to use with our Drawer
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
                            if (drawerItem.getIdentifier() == 1) {
                                intent = new Intent(MapsActivity.this, MainActivity.class);

                                if (intent != null) {
                                    MapsActivity.this.startActivity(intent);
                                }
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
        mSocket.on("transaccioniniciada", onTransaccionIniciada);//comieza la transaccion


        //BOTON INCIO LOGIN
        Button btnEnviar = (Button) findViewById(R.id.btnBuscarDisponible);
        btnEnviar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0)
            {

                buscarEmpleadoDisponible();

            }
        });





        //LO QUE VIENE DEL MAIN ACTIVITY
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            tipoUsuario = extras.getString("tipoBoton");
        }

        if(tipoUsuario.equals("Empleado"))
        {
            session = new Session(MapsActivity.this);
            empleadoConectado.setId(session.getId());
            empleadoConectado.setUsuario(session.getUsuario());
            empleadoConectado.setEmail(session.getEmail());
            empleadoConectado.setDescripcion(session.getDescripcion());

        }

        if(tipoUsuario.equals("Cliente"))
        {
            clienteConectado.setUsuario("Cliente nuevo");
            clienteConectado.setOnline(true);
            clienteConectado.setEdad(20);

        }


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
            mMap.setMyLocationEnabled(true);

        }

        if(mLastLocation == null)
        {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-34.893713,-56.171671),10.5f));//Montevideo
        }
        else {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()),13));//Mi ubicacion
        }

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates loc = result.getLocationSettingsStates();
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
                            mMap.addMarker(new MarkerOptions().position(e.getUbicacion()).title(e.getUsuario()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)));

                        }




                    } catch (JSONException e)
                    {
                        Log.e("Error", e.getMessage());

                    }
                }
            });


        }
    };

    //Obtiene los usuarios cercanos cuando el servidor lo manda
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
                                final Cliente c = new Cliente(data);

                                mSocket.off("solicitudcliente",onSolicitudCliente);//Dejo de escuchar hasta que el empleado acepte
                                alertaSolicitud = new AlertDialog.Builder(MapsActivity.this)
                                        .setTitle("Nueva solicitud: " + c.getUsuario())
                                        .setMessage("Quieres aceptar la solicitud?")
                                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {

                                                JSONObject cliente = c.toJSON();
                                                try {

                                                    cliente.put("tipo", "Cliente");

                                                } catch (JSONException e) {

                                                    e.printStackTrace();
                                                }

                                                JSONObject empleado = empleadoConectado.toJSON();
                                                try {

                                                    empleado.put("tipo", "Empleado");

                                                } catch (JSONException e) {

                                                    e.printStackTrace();
                                                }

                                                JSONObject transaccion = null;
                                                try
                                                {
                                                    Transaccion t = new Transaccion();
                                                    t.setActiva(true);//Empieza a estar activa
                                                    t.setFechaFinTransaccion(null);
                                                    t.setEmpleadoTransaccion(empleadoConectado);
                                                    t.setClienteTransaccion(c);
                                                    transaccion = t.toJSON();
                                                    transaccion.put("tipo", "Transaccion");

                                                } catch (JSONException e) {

                                                    e.printStackTrace();
                                                }

                                                JSONArray jsonArray = new JSONArray();

                                                jsonArray.put(cliente);
                                                jsonArray.put(empleado);
                                                jsonArray.put(transaccion);

                                                mSocket.emit("aceptarsolicitud", jsonArray);
                                                alertaSolicitud = null;
                                            }
                                        })
                                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                mSocket.on("solicitudcliente",onSolicitudCliente);
                                                alertaSolicitud = null;
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


                            final JSONObject data = (JSONObject) args[0];//Obtenemos el unico elemento
                            transaccionActual = new Transaccion();
                            transaccionActual.setIdTransaccion(data.getInt("id"));
                            transaccionActual.setActiva(true);
                            Date fechaIni = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(data.getString("fechaInicioTransaccion"));
                            transaccionActual.setFechaInicioTransaccion(fechaIni);
                            transaccionActual.setEmpleadoTransaccion(new Empleado(new JSONObject(data.getString("empleadoTransaccion"))));
                            transaccionActual.setClienteTransaccion(new Cliente(new JSONObject(data.getString("clienteTransaccion"))));
                            mSocket.on("solicitudcliente",onSolicitudCliente);
                            mMap.clear();
                            Marker clienteT = mMap.addMarker(new MarkerOptions().position(transaccionActual.getClienteTransaccion().getUbicacion()).title(transaccionActual.getClienteTransaccion().getUsuario())
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                            Marker empleadoT = mMap.addMarker(new MarkerOptions().position(transaccionActual.getEmpleadoTransaccion().getUbicacion()).title(transaccionActual.getEmpleadoTransaccion().getUsuario())
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                            PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
                            options.add(clienteT.getPosition());
                            options.add(empleadoT.getPosition());
                            mMap.addPolyline(options);
                            //mPolyline.setPoints(Arrays.asList(clienteT.getPosition(),empleadoT.getPosition()));


                            Toast.makeText(getApplicationContext(),
                                   "T:" + transaccionActual.getIdTransaccion() + " E:"
                                           + transaccionActual.getEmpleadoTransaccion().getId() + " C:"
                                           + transaccionActual.getClienteTransaccion().getId(), Toast.LENGTH_LONG).show();




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
                        Toast.makeText(getApplicationContext(),
                                R.string.connect, Toast.LENGTH_LONG).show();
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
                    Toast.makeText(getApplicationContext(),
                            R.string.disconnect, Toast.LENGTH_LONG).show();
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
                    Toast.makeText(getApplicationContext(),
                            R.string.error_connect, Toast.LENGTH_LONG).show();
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
        desconectarUsuario();
        //desconectarSocket();
        super.onDestroy();

    }

    private void desconectarUsuario()
    {
        if(tipoUsuario.equals("Empleado")) {
            JSONObject informacion = new JSONObject();
            try {
                informacion.put("id", empleadoConectado.getId());

            } catch (JSONException e) {

                e.printStackTrace();
            }

            mSocket.emit("desconectarusuario", informacion);
        }

    }

    private void desconectarSocket()
    {
        mSocket.disconnect();

        mSocket.off(Socket.EVENT_CONNECT, onConnect);
        mSocket.off(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.off("empleadoscercanos", onCercanos);//obtiene los cercanos
        mSocket.off("solicitudcliente", onSolicitudCliente);//maneja la respuesta del servidor a la solicitud cliente

    }

    private void conectarSocket()
    {
        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.on("empleadoscercanos", onCercanos);//obtiene los cercanos
        mSocket.on("solicitudcliente", onSolicitudCliente);//solicitud aceptada del servidor
        mSocket.on("transaccioniniciada", onTransaccionIniciada);//comieza la transaccion
        mSocket.connect();
    }

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
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    @Override
    public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null)
        {
            mMap.clear();
            if(tipoUsuario.equals("Cliente"))
            {
                marcadorYo = mMap.addMarker(new MarkerOptions().position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))
                        .title(clienteConectado.getUsuario())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

                clienteConectado.setUbicacion(marcadorYo.getPosition());

                JSONObject informacion = new JSONObject();
                try {
                    informacion.put("latitud", clienteConectado.getUbicacion().latitude);
                    informacion.put("longitud", clienteConectado.getUbicacion().longitude);
                    informacion.put("limite", Constantes.LIMITE_CERCANOS);

                } catch (JSONException e) {

                    e.printStackTrace();
                }

                //Manda la info necesaria para saber cercanos en el servidor
                mSocket.emit("getcercanos", informacion);
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
                    informacion.put("id", empleadoConectado.getId());
                    informacion.put("latitud", marcadorYo.getPosition().latitude);
                    informacion.put("longitud", marcadorYo.getPosition().longitude);

                } catch (JSONException e) {

                    e.printStackTrace();
                }

                //Manda la info necesaria del empleado en cuestion
                mSocket.emit("setubacionempleado", informacion);
            }

            Log.i("LATITUD onConnected",String.valueOf(mLastLocation.getLatitude()));
            Log.i("LONGITUD onConnected",String.valueOf(mLastLocation.getLongitude()));


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

                JSONObject informacion = new JSONObject();
                try {
                    informacion.put("latitud", clienteConectado.getUbicacion().latitude);
                    informacion.put("longitud", clienteConectado.getUbicacion().longitude);
                    informacion.put("limite", Constantes.LIMITE_CERCANOS);

                } catch (JSONException e) {

                    e.printStackTrace();
                }

                //Manda la info necesaria para saber cercanos en el servidor
                mSocket.emit("getcercanos", informacion);
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
                    informacion.put("id", empleadoConectado.getId());
                    informacion.put("latitud", marcadorYo.getPosition().latitude);
                    informacion.put("longitud", marcadorYo.getPosition().longitude);

                } catch (JSONException e) {

                    e.printStackTrace();
                }

                //Manda la info necesaria del empleado en cuestion
                mSocket.emit("setubacionempleado", informacion);
            }
            Log.i("LATITUD updateUI", String.valueOf(mLastLocation.getLatitude()));
            Log.i("LONGITUD updateUI", String.valueOf(mLastLocation.getLongitude()));


        }
    }

    private void buscarEmpleadoDisponible()
    {
        if(tipoUsuario.equals("Cliente"))
        {
            try
            {

                mSocket.emit("buscardisponible",clienteConectado.toJSON());


            } catch (Exception e) {

                e.printStackTrace();
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

                e.printStackTrace();
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

                e.printStackTrace();
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

}
