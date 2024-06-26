package manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bson.Document;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import model.Bodega;
import model.Campo;
import model.Entrada;
import model.Vid;
import utils.TipoVid;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;



public class Manager {
	private static Manager manager;
	private ArrayList<Entrada> entradas;
	private Session session;
	private Transaction tx;
	private Bodega b;
	private Campo c;
	
    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<org.bson.Document> collection;

	private Manager() {
		this.entradas = new ArrayList<>();
	}

	public static Manager getInstance() {
		if (manager == null) {
			manager = new Manager();
		}
		return manager;
	}
	 
	    private void createSession() {

	        // Establece la conexión a la base de datos
	        String uri = "mongodb://localhost:27017";
	        mongoClient = MongoClients.create(uri);
	        database = mongoClient.getDatabase("dam2tm06uf2p2");
	    }

	    public void closeSession() {
	        if (mongoClient != null) {
	            mongoClient.close();
	            System.out.println("MongoDB connection closed.");
	        }
	    }

	public void init() {
		createSession();
		getEntrada();
		manageActions();
		showAllCampos();
		asignarPreciosAleatorios();
		showTotalPrice();
		closeSession();
	}

	private void manageActions() {
		for (Entrada entrada : this.entradas) {
			try {
				System.out.println(entrada.getInstruccion());
				switch (entrada.getInstruccion().toUpperCase().split(" ")[0]) {
				case "B":
					addBodega(entrada.getInstruccion().split(" "));
					break;
				case "C":
					addCampo(entrada.getInstruccion().split(" "));
					break;
				case "V":
					addVid(entrada.getInstruccion().split(" "));
					break;
				case "#":
					vendimia();
					break;
				default:
					System.out.println("Instruccion incorrecta");
				}
			} catch (HibernateException e) {
				e.printStackTrace();
				if (tx != null) {
					tx.rollback();
				}
			}
		}
	}
	
	
	public void vendimia() {
	    try {

	        // Recupera todos los campos
	        List<Campo> campos = recuperarTodosLosCampos();

	        for (Campo campo : campos) {
	            Bodega bodega = campo.getBodega(); 
	            if (bodega != null) {
	                // Asociar las Vid de Campo a Bodega
	                List<Vid> vidsDelCampo = campo.getVids();
	                for (Vid vid : vidsDelCampo) {
	                    // Añade las Vids del campo a la Bodega
	                    bodega.getVids().add(vid);
	                    // Actualiza la referencia de Vid a su nueva Bodega
	                    
	                   
	                }
	            }
	        }
	      
	    } catch (HibernateException e) {
	        if (tx != null) tx.rollback();
	        e.printStackTrace();
	    }
	}


	private List<Campo> recuperarTodosLosCampos() {
	    List<Campo> campos = new ArrayList<>();

	    // Obtener la colección de campos en MongoDB
	    MongoCollection<Document> campoCollection = database.getCollection("Campo");

	    // Recuperar todos los documentos de la colección
	    try (MongoCursor<Document> cursor = campoCollection.find().iterator()) {
	        while (cursor.hasNext()) {
	            Document campoDoc = cursor.next();
	            Campo campo = convertirDocumentoACampo(campoDoc);
	            campos.add(campo);
	        }
	    }

	    return campos;
	}
	
	private Campo convertirDocumentoACampo(Document campoDoc) {
	    // Obtener los datos del documento
	    String nombreBodega = campoDoc.getString("bodega");

	    // Crear una instancia de Bodega con el nombre obtenido
	    Bodega bodega = new Bodega(nombreBodega);

	    // Crear una instancia de Campo con la Bodega creada
	    Campo campo = new Campo(bodega);

	    return campo;
	}



    private void addVid(String[] split) {
        Vid v = new Vid(TipoVid.valueOf(split[1].toUpperCase()), Integer.parseInt(split[2]));
        
        // Insertar el Vid en MongoDB
        MongoCollection<Document> vidCollection = database.getCollection("Vid");
        Document vidDoc = new Document("tipo_vid", split[1].toUpperCase())
                        .append("cantidad", Integer.parseInt(split[2]));
        vidCollection.insertOne(vidDoc);
        
        // Asociar el Vid al Campo
        c.addVid(v);
     
    }


	private void addCampo(String[] split) {
	    c = new Campo(b);
	    
	    // Insertar el campo en MongoDB
	    MongoCollection<Document> campoCollection = database.getCollection("Campo");
	    Document campoDoc = new Document("bodega", b.getNombre());
	    campoCollection.insertOne(campoDoc);
	    

	}

	private void addBodega(String[] split) {
	    b = new Bodega(split[1]);
	    
	    // Insertar la bodega en MongoDB
	    MongoCollection<Document> bodegaCollection = database.getCollection("Bodega");
	    Document bodegaDoc = new Document("nombre", b.getNombre());
	    bodegaCollection.insertOne(bodegaDoc);
	    


	}


    private void getEntrada() {
  
        MongoCollection<Document> collection = database.getCollection("Entrada"); 
        try (MongoCursor<Document> cursor = collection.find().iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                Entrada entrada = Entrada.fromDocument(doc); // Convertir el documento a un objeto Entrada
                entradas.add(entrada);
            }
        }

        // Mostrar las entradas leídas
        for (Entrada entrada : entradas) {
            System.out.println("ID: " + entrada.getId() + ", Instrucción: " + entrada.getInstruccion());
        }

      
    }

    private void showAllCampos() {
        MongoCollection<Document> campoCollection = database.getCollection("Campo");

        try (MongoCursor<Document> cursor = campoCollection.find().iterator()) {
            while (cursor.hasNext()) {
                Document campoDoc = cursor.next();
                Campo campo = convertirDocumentoACampo(campoDoc);
                System.out.println(campo);
            }
        }
    }

    // Método para asignar precios aleatorios a las Vids
    public void asignarPreciosAleatorios() {
        Random random = new Random();
        MongoCollection<Document> vidCollection = database.getCollection("Vid");

        try (MongoCursor<Document> cursor = vidCollection.find().iterator()) {
            while (cursor.hasNext()) {
                Document vidDoc = cursor.next();
                double precioAleatorio = 10.0 + (90.0 * random.nextDouble());
                double precioRedondeado = Math.round(precioAleatorio * 100.0) / 100.0;

                // Actualizar el documento de la Vid con el nuevo precio
                vidDoc.put("precio", precioRedondeado);
                vidCollection.replaceOne(Filters.eq("_id", vidDoc.get("_id")), vidDoc);
            }
        }
    }

 // Método para mostrar el precio total de todas las Vids
    public void showTotalPrice() {
        MongoCollection<Document> vidCollection = database.getCollection("Vid");

        List<Document> pipeline = new ArrayList<>();
        pipeline.add(new Document("$group", new Document("_id", null).append("totalPrice", new Document("$sum", "$precio"))));

        Document result = vidCollection.aggregate(pipeline).first();
        double totalPrice = (result != null) ? result.getDouble("totalPrice") : 0.0;
        System.out.println("El precio total es: " + totalPrice);
    }


}
