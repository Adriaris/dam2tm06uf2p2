package model;

import org.bson.types.ObjectId;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class Bodega {

    private ObjectId id_bodega; 
    private String nombre;
    private List<Vid> vids;

    public Bodega() {
        this.vids = new ArrayList<>();
    }

    public Bodega(String nombre) {
        this();
        this.nombre = nombre;
    }

    public ObjectId getId_bodega() {
        return id_bodega;
    }

    public void setId_bodega(ObjectId id_bodega) {
        this.id_bodega = id_bodega;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<Vid> getVids() {
        return vids;
    }

    public void setVids(List<Vid> vids) {
        this.vids = vids;
    }

    @Override
    public String toString() {
        return "Bodega [id_bodega=" + id_bodega + ", nombre=" + nombre + ", vids=" + vids + "]";
    }

    // Método para convertir un documento MongoDB en una instancia de Bodega
    public static Bodega fromDocument(Document doc) {
        Bodega bodega = new Bodega();
        bodega.setId_bodega(doc.getObjectId("_id"));
        bodega.setNombre(doc.getString("nombre"));
       
        return bodega;
    }

    // Método para convertir una instancia de Bodega en un documento MongoDB
    public Document toDocument() {
        Document doc = new Document("_id", id_bodega)
            .append("nombre", nombre);
        
        return doc;
    }
}
