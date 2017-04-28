package infozonaorg.com.testnode.Capas;

import android.support.v7.app.AlertDialog;
import android.util.Log;

import org.json.JSONObject;

import infozonaorg.com.testnode.Clases.Cliente;
import infozonaorg.com.testnode.Clases.Empleado;
import infozonaorg.com.testnode.Clases.Transaccion;
import io.socket.client.Socket;

/**
 * Created by jvolpe on 25/04/2017.
 */

public abstract class LogicaTransaccion {

    //Cliente solicitud
    private static Cliente clienteSolicitud = null;
    private static Transaccion transaccionActual = null;


    public static Transaccion onSolicitudCliente(JSONObject datos)
    {

        if(LogicaUsuario.getTipoUsuario().equals("Empleado"))
        {
            JSONObject data = datos;//Obtenemos el unico elemento
            Transaccion tr = new Transaccion(data);
            setClienteSolicitud(tr.getClienteTransaccion());

            return tr;

        }
        return null;
    }

    public static boolean onTransaccionIniciada(JSONObject datos)
    {
        try {

            transaccionActual = new Transaccion(datos);
            /*transaccionActual = new Transaccion();
            transaccionActual.setIdTransaccion(datos.getInt("id"));
            transaccionActual.setActiva(true);
            transaccionActual.setFechaInicioTransaccion(datos.getString("fechaInicioTransaccion"));
            transaccionActual.setEmpleadoTransaccion(new Empleado(new JSONObject(datos.getString("empleadoTransaccion"))));
            transaccionActual.setClienteTransaccion(new Cliente(new JSONObject(datos.getString("clienteTransaccion"))));
            transaccionActual.setIdBusquedaTransaccion(datos.getInt("idBusquedaTransaccion"));*/
            return true;
        }
        catch (Exception ex)
        {
            Log.e("LogicaTransaccion",ex.getMessage());
            return false;
        }
    }

    public static void onTransaccionFinalizada()
    {
        transaccionActual = null;
    }

    public static void setClienteSolicitud(Cliente c)
    {
        clienteSolicitud = c;
    }

    public static Cliente getClienteSolicitud()
    {
        return clienteSolicitud;
    }

    public static Transaccion getTransaccionActual()
    {
        return transaccionActual;
    }
}
