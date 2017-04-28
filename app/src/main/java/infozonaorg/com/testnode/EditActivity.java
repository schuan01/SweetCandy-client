package infozonaorg.com.testnode;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import infozonaorg.com.testnode.Clases.Session;
import infozonaorg.com.testnode.Fragments.GeneralEditFragment;
import infozonaorg.com.testnode.Fragments.PhotoEditFragment;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class EditActivity extends AppCompatActivity {

    private Socket mSocket;
    private Boolean isConnected = true;
    private Snackbar snackbarConectado = null;
    private Snackbar snackbarDesconectado = null;
    private Snackbar snackbarFallo = null;
    private Session session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        //SOCKETS ----------------------------------------------------------------
        TestApplication app = (TestApplication) getApplication();
        mSocket = app.getSocket();
        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.on("fotosobtenidas", onFotosObtenidas);//obtiene el usuario logeado
        session = new Session(EditActivity.this, true);
        mSocket.emit("getfotosusuario", session.toJSON());
        //FIN SOCKETS ----------------------------------------------------------------


        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new GeneralEditFragment(), "Mi Perfil");
        adapter.addFragment(new PhotoEditFragment(), "Fotos");
        viewPager.setAdapter(adapter);

    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }



        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    //Obtiene las fotos
    private Emitter.Listener onFotosObtenidas = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    try {

                        ArrayList<String> fotos = new ArrayList<String>();
                        JSONArray lista = (JSONArray) args[0];//Obtenemos el array del servidor
                        for (int con = 0; con < lista.length(); con++) {
                            JSONObject data = (JSONObject) lista.get(con);//Obtenemos cada elemento

                            String urlFoto = data.getString("urlFoto");
                            fotos.add(urlFoto);

                        }

                        session.setFotos(fotos);

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
