package infozonaorg.com.testnode.Fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import infozonaorg.com.testnode.Clases.Session;
import infozonaorg.com.testnode.R;


public class GeneralEditFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "EditActivity";

    @BindView(R.id.input_edad) EditText _edadNumber;
    @BindView(R.id.txtTituloEditar) TextView _tituloEditar;
    @BindView(R.id.btn_save)
    Button _btnGuardar;
    private Session session;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public GeneralEditFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_general_edit, container, false);
        ButterKnife.bind(this,v);

        session = new Session(getActivity());
        if(session.getTipoUsuario().equals("Empleado")) {

            _tituloEditar.setText(_tituloEditar.getText() + " " + session.getUsuario());
            int test = session.getEdad();
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

    private void guardarCambios()
    {
        Log.d(TAG, "Guardar ejecutado");
        _btnGuardar.setEnabled(false);

        if (!validate()) {
            onSaveFailed();
            return;
        }

        onSaveSuccess();

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
            _edadNumber.setError("Debes ser mayor a 18 años y menor a 100");
            valid = false;
        }

        return valid;
    }
}
