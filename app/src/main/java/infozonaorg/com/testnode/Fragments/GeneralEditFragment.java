package infozonaorg.com.testnode.Fragments;


import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import infozonaorg.com.testnode.Clases.Session;
import infozonaorg.com.testnode.R;
import infozonaorg.com.testnode.TestApplication;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;


public class GeneralEditFragment extends Fragment {

    private static final String TAG = "EditActivity";

    @BindView(R.id.input_edad) EditText _edadNumber;
    @BindView(R.id.txtTituloEditar) TextView _tituloEditar;
    @BindView(R.id.btn_save) Button _btnGuardar;
    private Session session;

    private Socket mSocket;
    private Boolean isConnected = true;
    private Snackbar snackbarConectado = null;
    private Snackbar snackbarDesconectado = null;
    private Snackbar snackbarFallo = null;



    public GeneralEditFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //SOCKETS ----------------------------------------------------------------
        TestApplication app = (TestApplication) getActivity().getApplication();
        mSocket = app.getSocket();
        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.on("usuarioeditado", onEditResult);//obtiene el usuario logeado
        //FIN SOCKETS ----------------------------------------------------------------


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_general_edit, container, false);
        ButterKnife.bind(this,v);

        session = new Session(getActivity(),true);
        if(session.getTipoUsuario().equals("Empleado")) {

            _tituloEditar.setText(_tituloEditar.getText() + " " + session.getUsuario());
            _edadNumber.setText(""+session.getEdad());


            _btnGuardar.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    guardarCambios();
                }


            });
        }

        return v;
    }



    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    //Informa si se guardo correctamente
    private Emitter.Listener onEditResult = new Emitter.Listener() {
        @Override
        public void call(final Object... args)
        {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try
                    {

                        boolean editSucces = (boolean) args[0];//Obtenemos el array del servidor
                        if(editSucces)
                        {
                            onSaveSuccess();
                        }
                        else
                        {
                            onSaveFailed();
                        }
                    } catch (Exception e)
                    {
                        Log.e("Error", e.getMessage());
                        onSaveFailed();
                    }
                }
            });


        }
    };

    private void guardarCambios()
    {
        _btnGuardar.setEnabled(false);

        if (!validate()) {
            onSaveFailed();
            return;
        }
        else
        {
            mSocket.emit("editarusuario",session.toJSON());
        }



    }

    public void onSaveSuccess() {
        _btnGuardar.setEnabled(true);
        session.setEdad(Integer.parseInt(_edadNumber.getText().toString()));
        getActivity().finish();

    }

    public void onSaveFailed() {
        _btnGuardar.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        int edad = Integer.parseInt(_edadNumber.getText().toString());

        if(edad < 18 || edad > 100)
        {
            _edadNumber.setError(getString(R.string.txtEdadMinima));
            valid = false;
        }

        return valid;
    }

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            getActivity().runOnUiThread(new Runnable() {
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

    private void desactivarBotones()
    {
        _btnGuardar.setEnabled(false);

    }

    private void activarBotones()
    {
        _btnGuardar.setEnabled(true);

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
                    snackbarFallo = Snackbar.make(getActivity().findViewById(android.R.id.content), R.string.error_connect, Snackbar.LENGTH_INDEFINITE);
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
                    snackbarDesconectado = Snackbar.make(getActivity().findViewById(android.R.id.content),  R.string.disconnect, Snackbar.LENGTH_INDEFINITE);
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
                    snackbarConectado = Snackbar.make(getActivity().findViewById(android.R.id.content),R.string.connect, Snackbar.LENGTH_SHORT);
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
