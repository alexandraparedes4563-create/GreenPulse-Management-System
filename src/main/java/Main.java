import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import java.io.FileInputStream;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        try {
            // 1. Configuración de la ruta al archivo JSON
            // Usamos la ruta que te funcionó anteriormente
            FileInputStream serviceAccount =
                    new FileInputStream("src/resources/serviceAccountKey.json");

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://greenpulse-76e6e-default-rtdb.firebaseio.com/")
                    .build();

            // 2. Inicializar Firebase si no se ha hecho antes
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }

            System.out.println("=========================================");
            System.out.println("¡CONEXIÓN EXITOSA CON FIREBASE!");
            System.out.println("El proyecto GreenPulse está listo.");
            System.out.println("=========================================");

            // 3. Abrir la interfaz gráfica (Dashboard)
            // Usamos SwingUtilities para asegurarnos que la ventana abra correctamente
            SwingUtilities.invokeLater(() -> {
                Dashboard ventana = new Dashboard();
                ventana.mostrar();
            });

        } catch (Exception e) {
            System.err.println("Error al conectar o abrir ventana: " + e.getMessage());
            e.printStackTrace();
        }
    }
}