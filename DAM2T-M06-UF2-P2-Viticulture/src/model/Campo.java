package model;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class Campo {

    private ObjectId id_campo; // Usar ObjectId para MongoDB
    private List<Vid> vids;
    private Bodega bodega;

    public Campo() {
        this.vids = new ArrayList<>();
    }

    public Campo(Bodega bodega) {
        this();
        this.bodega = bodega;
    }

    public ObjectId getId_campo() {
        return id_campo;
    }

    public void setId_campo(ObjectId id_campo) {
        this.id_campo = id_campo;
    }

    public List<Vid> getVids() {
        return new ArrayList<>(vids);
    }

    public void setVids(List<Vid> vids) {
        this.vids = vids;
    }

    public Bodega getBodega() {
        return bodega;
    }

    public void setBodega(Bodega bodega) {
        this.bodega = bodega;
    }

    @Override
    public String toString() {
        return "Campo [id_campo=" + id_campo + ", vids=" + vids + ", bodega=" + bodega + "]";
    }

    public void addVid(Vid v) {
        this.vids.add(v);
    }

    // Método para convertir un documento MongoDB a una instancia de Campo
    public static Campo fromDocument(Document doc) {
        Campo campo = new Campo();
        campo.setId_campo(doc.getObjectId("_id"));
        campo.setBodega(Bodega.fromDocument((Document) doc.get("bodega")));
        
        return campo;
    }

    
    public Document toDocument() {
        Document doc = new Document("_id", id_campo)
            .append("bodega", bodega.toDocument());
        
        return doc;
    }
}
