package infozonaorg.com.testnode.Fragments;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import infozonaorg.com.testnode.Clases.Session;
import infozonaorg.com.testnode.R;
import infozonaorg.com.testnode.TestApplication;
import infozonaorg.com.testnode.Utils.GridAdapter;
import infozonaorg.com.testnode.Utils.PhotoScrollListener;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class PhotoEditFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private Socket mSocket;
    private Boolean isConnected = true;
    private Snackbar snackbarConectado = null;
    private Snackbar snackbarDesconectado = null;
    private Snackbar snackbarFallo = null;
    private static final int IMAGE_PICKER_SELECT = 0;
    @BindView(R.id.grid_view)
    GridView _gwvFotos;
    @BindView(R.id.btnSubirImagen)
    FloatingActionButton _btnSubirFoto;
    Cloudinary cloudinary = null;
    Session session;

    // TODO: Rename and change types of parameters
    public static final int MY_PERMISSIONS_READ_EXTERNAL_STORAGE = 1;
    InputStream inputStream;

    public PhotoEditFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Map config = new HashMap();
        config.put("cloud_name", "schuan-corp");
        config.put("api_key", "646314622931327");
        config.put("api_secret", "X2JgZu9Q4hN6KVoba5nddkkz_9g");
        cloudinary = new Cloudinary(config);

        //SOCKETS ----------------------------------------------------------------
        TestApplication app = (TestApplication) getActivity().getApplication();
        mSocket = app.getSocket();
        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        //FIN SOCKETS ----------------------------------------------------------------


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_PICKER_SELECT && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                //Display an error
                return;
            }
            try {
                inputStream = getActivity().getContentResolver().openInputStream(data.getData());

                Upload task = new Upload(cloudinary, getActivity());
                task.execute("");


            } catch (Exception e) {
                Log.e("Error", e.getMessage());
            }

            //Now you can do whatever you want with your inpustream, save it as file, upload to a server, decode a bitmap...
        }
    }

    private class Upload extends AsyncTask<String, Void, String> {

        private ProgressDialog dialog;
        private Activity activity;

        private Cloudinary mCloudinary;
        Map uploadResult;
        private Context context;

        public Upload(Cloudinary cloudinary, Activity activity) {
            super();
            mCloudinary = cloudinary;
            this.activity = activity;
            context = activity;
            dialog = new ProgressDialog(context);
        }

        protected void onPreExecute() {

            this.dialog.setMessage(getString(R.string.txtSubiendo));
            this.dialog.show();


        }

        @Override
        protected String doInBackground(String... urls) {
            String response = "";

            try {
                uploadResult = mCloudinary.uploader().upload(inputStream, ObjectUtils.emptyMap());

            } catch (Exception e) {
                Log.e("Error", e.getMessage());
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                String url = (String) uploadResult.get("url");
                session = new Session(getActivity(), true);
                ArrayList<String> lista = session.getFotos();
                lista.add(url);
                session.setFotos(lista);
                JSONObject informacion = new JSONObject();

                informacion.put("id", session.getId());
                informacion.put("urlFoto", url);
                mSocket.emit("guardarfoto", informacion);

            } catch (Exception ex) {

                Log.e("ERROR", ex.getMessage());
            }

            if (dialog.isShowing()) {
                dialog.dismiss();

            }

            //RECARGA EL FRAGMENT Y ACTUALIZA LAS FOTOS
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.detach(PhotoEditFragment.this).attach(PhotoEditFragment.this).commit();
        }
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_edit, container, false);
        ButterKnife.bind(this, v);
        _gwvFotos.setAdapter(new GridAdapter(getActivity()));
        _gwvFotos.setOnScrollListener(new PhotoScrollListener(getActivity()));

        _btnSubirFoto.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                // Start the Signup activity
                // Here, thisActivity is the current activity
                if (ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                            Manifest.permission.READ_EXTERNAL_STORAGE)) {

                        // Show an expanation to the user *asynchronously* -- don't block
                        // this thread waiting for the user's response! After the user
                        // sees the explanation, try again to request the permission.

                    } else {

                        // No explanation needed, we can request the permission.

                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                MY_PERMISSIONS_READ_EXTERNAL_STORAGE);

                        // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                        // app-defined int constant. The callback method gets the
                        // result of the request.
                    }
                }

                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, IMAGE_PICKER_SELECT);
            }
        });

        // Inflate the layout for this fragment


        return v;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    Intent i = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(i, IMAGE_PICKER_SELECT);

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }

            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            getActivity().runOnUiThread(new Runnable() {
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
            getActivity().runOnUiThread(new Runnable() {
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
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    desactivarBotones();
                    handleSnackBarConexion("fallo");
                }
            });
        }
    };

    private void desactivarBotones() {
        _btnSubirFoto.setEnabled(false);

    }

    private void activarBotones() {
        _btnSubirFoto.setEnabled(true);

    }

    private void handleSnackBarConexion(String evento) {
        switch (evento) {
            case "fallo":
                if (snackbarDesconectado != null) {
                    snackbarDesconectado.dismiss();
                    snackbarDesconectado = null;
                }
                if (snackbarFallo == null) {
                    snackbarFallo = Snackbar.make(getActivity().findViewById(android.R.id.content), R.string.error_connect, Snackbar.LENGTH_INDEFINITE);
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
                    snackbarDesconectado = Snackbar.make(getActivity().findViewById(android.R.id.content), R.string.disconnect, Snackbar.LENGTH_INDEFINITE);
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
                    snackbarConectado = Snackbar.make(getActivity().findViewById(android.R.id.content), R.string.connect, Snackbar.LENGTH_SHORT);
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


