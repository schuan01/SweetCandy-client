package infozonaorg.com.testnode;

import android.app.Application;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import infozonaorg.com.testnode.Clases.Cliente;
import infozonaorg.com.testnode.Clases.Empleado;
import infozonaorg.com.testnode.Clases.Usuario;
import io.socket.client.IO;
import io.socket.client.Socket;


public class TestApplication extends Application {

    static List<Empleado> lstEmpleadosConectados = new ArrayList<>();
    static List<Cliente> lstClientesConectoads = new ArrayList<>();


    private Socket mSocket;
    {
        try {
            mSocket = IO.socket(Constantes.SERVER_URL);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public Socket getSocket() {
        return mSocket;
    }

    public static void addUserToList(Usuario u)
    {
        if(u instanceof Empleado) {
            lstEmpleadosConectados.add((Empleado) u);
        }

        if(u instanceof Cliente) {
            lstClientesConectoads.add((Cliente) u);
        }
    }

    public static Usuario getUserById(int id, char tipo)
    {
        if(tipo == 'E')//Si es empleado
        {
            for (Empleado e : lstEmpleadosConectados)
            {
                if(e.getId() == id)
                {
                    return e;
                }
            }

        }

        if(tipo == 'C')//SI es cliente
        {
            for (Cliente c : lstClientesConectoads)
            {
                if(c.getId() == id)
                {
                    return c;
                }
            }

        }

        return null;

    }

}
