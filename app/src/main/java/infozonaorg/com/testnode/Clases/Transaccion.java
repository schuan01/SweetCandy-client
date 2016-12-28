package infozonaorg.com.testnode.Clases;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by jvolpe on 15/12/2016.
 */

public class Transaccion
{


    private int idTransaccion;
    private Empleado empleadoTransaccion = null;
    private Cliente clienteTransaccion = null;
    private Date fechaInicioTransaccion;
    private Date fechaFinTransaccion;
    private boolean isActiva = false;
    private float totalTransaccion;

    public int getIdTransaccion() {
        return idTransaccion;
    }

    public void setIdTransaccion(int idTransaccion) {
        this.idTransaccion = idTransaccion;
    }

    public Empleado getEmpleadoTransaccion() {
        return empleadoTransaccion;
    }

    public void setEmpleadoTransaccion(Empleado empleadoTransaccion) {
        this.empleadoTransaccion = empleadoTransaccion;
    }

    public Cliente getClienteTransaccion() {
        return clienteTransaccion;
    }

    public void setClienteTransaccion(Cliente clienteTransaccion) {
        this.clienteTransaccion = clienteTransaccion;
    }

    public Date getFechaInicioTransaccion() {
        return fechaInicioTransaccion;
    }

    public void setFechaInicioTransaccion(Date fechaInicioTransaccion) {
        this.fechaInicioTransaccion = fechaInicioTransaccion;
    }

    public Date getFechaFinTransaccion() {
        return fechaFinTransaccion;
    }

    public void setFechaFinTransaccion(Date fechaFinTransaccion) {
        this.fechaFinTransaccion = fechaFinTransaccion;
    }

    public boolean isActiva() {
        return isActiva;
    }

    public void setActiva(boolean activa) {
        isActiva = activa;
    }

    public float getTotalTransaccion() {
        return totalTransaccion;
    }

    public void setTotalTransaccion(float totalTransaccion) {
        this.totalTransaccion = totalTransaccion;
    }

    public Transaccion(int id, Empleado empleadoTransaccion, Cliente clienteTransaccion, Date fechaInicioTransaccion, Date fechaFinTransaccion, boolean isActiva, float totalTransaccion) {
        setIdTransaccion(id);
        setEmpleadoTransaccion(empleadoTransaccion);
        setClienteTransaccion(clienteTransaccion);
        setFechaInicioTransaccion(new Date());//Actual siempre
        setFechaFinTransaccion(fechaFinTransaccion);
        setActiva(isActiva);
        setTotalTransaccion(totalTransaccion);
    }

    public Transaccion()
    {
        setIdTransaccion(0);
        setEmpleadoTransaccion(new Empleado());
        setClienteTransaccion(new Cliente());
        setFechaInicioTransaccion(new Date());//Actual siempre
        setFechaFinTransaccion(new Date());
        setActiva(false);
        setTotalTransaccion(0.00f);
    }

    public JSONObject toJSON(){

        JSONObject jsonObject= new JSONObject();
        try {
            jsonObject.put("id", getIdTransaccion());
            jsonObject.put("totalTransaccion", getTotalTransaccion());
            jsonObject.put("fechaInicioTransaccion", getFechaInicioTransaccion());
            jsonObject.put("fechaFinTransaccion", getFechaFinTransaccion());
            jsonObject.put("isActiva", isActiva());
            jsonObject.put("clienteTransaccion", getClienteTransaccion().toJSON());
            jsonObject.put("empleadoTransaccion", getEmpleadoTransaccion().toJSON());

            return jsonObject;
        } catch (JSONException e) {
            Log.e("ERROR",e.getMessage());
            return null;
        }

    }
}
