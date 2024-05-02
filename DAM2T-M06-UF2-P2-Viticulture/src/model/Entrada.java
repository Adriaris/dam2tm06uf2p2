package model;

import org.bson.Document;

public class Entrada {
    private int id;
    private String instruccion;

    // Constructor vacío necesario para mapear documentos
    public Entrada() {}

    public Entrada(int id, String instruccion) {
        this.id = id;
        this.instruccion = instruccion;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getInstruccion() {
        return instruccion;
    }

    public void setInstruccion(String instruccion) {
        this.instruccion = instruccion;
    }

    // Método para convertir un Document a una instancia de Entrada
    public static Entrada fromDocument(Document doc) {
        Entrada entrada = new Entrada();
        entrada.setId(doc.getInteger("id"));
        entrada.setInstruccion(doc.getString("instruccion"));
        return entrada;
    }
}
