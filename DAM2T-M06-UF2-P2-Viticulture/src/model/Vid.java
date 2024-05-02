package model;

import org.bson.types.ObjectId;

import utils.TipoVid;

public class Vid {
    private ObjectId id;
    private TipoVid vid;
    private int cantidad;
    private double precio; 
    private ObjectId id_bodega; 

    public Vid() {}

    public Vid(TipoVid vid, int cantidad) {
        this.vid = vid;
        this.cantidad = cantidad;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public TipoVid getVid() {
        return vid;
    }

    public void setVid(TipoVid vid) {
        this.vid = vid;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public ObjectId getId_bodega() {
        return id_bodega;
    }

    public void setId_bodega(ObjectId id_bodega) {
        this.id_bodega = id_bodega;
    }

    @Override
    public String toString() {
        return "Vid [vid=" + (vid.equals(TipoVid.BLANCA) ? "blanca" : "negra")  + ", cantidad=" + cantidad + "]";
    }
}
