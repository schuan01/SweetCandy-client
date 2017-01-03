package infozonaorg.com.testnode;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;
import infozonaorg.com.testnode.Clases.Session;
import infozonaorg.com.testnode.Complementos.InputFilterMinMax;

public class EditActivity extends AppCompatActivity {

    private static final String TAG = "EditActivity";

    @InjectView(R.id.input_passwordanterior) EditText _passwordAnterior;
    @InjectView(R.id.input_password) EditText _passwordText;
    @InjectView(R.id.input_passwordConfirm) EditText _passwordConfirmText;
    @InjectView(R.id.input_edad) EditText _edadNumber;
    @InjectView(R.id.txtTituloEditar) TextView _tituloEditar;
    @InjectView(R.id.btn_save) Button _btnGuardar;
    private Session session;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        ButterKnife.inject(this);

        session = new Session(EditActivity.this);
        if(session.getTipoUsuario().equals("Empleado")) {

            _tituloEditar.setText(_tituloEditar.getText() + " " + session.getUsuario());
            int test = session.getEdad();
            _edadNumber.setText(""+session.getEdad());
            _edadNumber.setFilters(new InputFilter[]{new InputFilterMinMax("18", "100")});

            _btnGuardar.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    guardarCambios();
                }


            });
        }
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
        setResult(RESULT_OK, null);
        finish();

    }

    public void onSaveFailed() {
        Toast.makeText(getBaseContext(), "Error al guardar cambios", Toast.LENGTH_LONG).show();

        _btnGuardar.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String password = _passwordText.getText().toString();
        String passwordConfirm = _passwordConfirmText.getText().toString();

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("Contraseña deber ser entre 4 y 10 caracteres");
            valid = false;
        }
        else {
            _passwordText.setError(null);
        }

        if(passwordConfirm.isEmpty() || !passwordConfirm.equals(password))
        {
            _passwordConfirmText.setError("Ambas contraseñas no coinciden");
            valid = false;
        }
        else {
            _passwordConfirmText.setError(null);
        }

        return valid;
    }
}
