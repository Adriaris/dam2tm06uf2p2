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
	    	Configuration configuration = new Configuration().configure("hibernate.cfg.xml");
			org.hibernate.SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
			session = sessionFactory.openSession();
	    	
	    	
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
		session.close();
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
            // Inicia la transacción
            tx = session.beginTransaction();

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
                        vid.setBodega(bodega);
                        session.update(vid);
                    }
                }
            }
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private List<Campo> recuperarTodosLosCampos() {
    	// recupero todos los objetos Campo de la base de datos y los devuelvo como una lista.
        return session.createQuery("FROM Campo").list();
    }

	private void addVid(String[] split) {
		Vid v = new Vid(TipoVid.valueOf(split[1].toUpperCase()), Integer.parseInt(split[2]));
		tx = session.beginTransaction();
		session.save(v);

		c.addVid(v);
		session.save(c);

		tx.commit();

	}

	private void addCampo(String[] split) {
		c = new Campo(b);
		tx = session.beginTransaction();

		int id = (Integer) session.save(c);
		c = session.get(Campo.class, id);

		tx.commit();
	}

	private void addBodega(String[] split) {
		b = new Bodega(split[1]);
		tx = session.beginTransaction();

		int id = (Integer) session.save(b);
		b = session.get(Bodega.class, id);

		tx.commit();

	}

    private void getEntrada() {
  
        MongoCollection<Document> collection = database.getCollection("Entrada"); // Obtener la colección aquí

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
		tx = session.beginTransaction();
		Query q = session.createQuery("select c from Campo c");
		List<Campo> list = q.list();
		for (Campo c : list) {
			System.out.println(c);
		}
		tx.commit();
	}
	
	public void asignarPreciosAleatorios() {
	    Random random = new Random();
	    session.beginTransaction(); // Inicia la transacción

	    List<Vid> vids = session.createQuery("FROM Vid", Vid.class).getResultList(); // Recupera todas las Vids

	    for (Vid vid : vids) {
	        double precioAleatorio = 10.0 + (90.0 * random.nextDouble()); // Genera precio entre 10 y 100
	        double precioRedondeado = Math.round(precioAleatorio * 100.0) / 100.0; // Redondea a 2 decimales
	        vid.setPrecio(precioRedondeado); // Asigna el precio redondeado
	        session.update(vid); // Actualiza la entidad Vid
	    }

	    session.getTransaction().commit(); // Confirma la transacción
	}

	
	public void showTotalPrice() {
	    // Consulta para sumar el precio de todas las Vids
	    Double totalPrice = (Double) session.createQuery("SELECT SUM(v.precio) FROM Vid v").uniqueResult();

	    // manejar el caso en que no haya Vids y totalPrice sea null
	    if (totalPrice == null) {
	        System.out.println("El precio total es: 0");
	    } else {
	        System.out.println("El precio total es: " + totalPrice);
	    }
	}


}
