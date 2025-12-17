import com.google.firebase.database.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Dashboard extends JFrame {
    private JTextField txtNombre, txtCorreo, txtBuscar;
    private JTable tablaUsuarios;
    private DefaultTableModel modeloTabla;
    private JLabel lblTotal, lblEstado;
    private TableRowSorter<DefaultTableModel> sorter;

    // Paleta de Colores Corporativa GreenPulse
    private Color verdePrimario = new Color(34, 197, 94);
    private Color azulOscuro = new Color(15, 23, 42);
    private Color fondoGris = new Color(248, 250, 252);
    private Color blanco = Color.WHITE;

    public Dashboard() {
        setTitle("GreenPulse OS - Environmental Management System");
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(fondoGris);
        setLayout(new BorderLayout(20, 20));

        // --- 1. BARRA SUPERIOR (HEADER) ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(blanco);
        header.setPreferredSize(new Dimension(1100, 80));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));

        JLabel logo = new JLabel("  GREENPULSE INNOVATION", SwingConstants.LEFT);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        logo.setForeground(verdePrimario);

        lblEstado = new JLabel("● Conectado a Firebase Realtime Database  ", SwingConstants.RIGHT);
        lblEstado.setForeground(new Color(100, 116, 139));

        header.add(logo, BorderLayout.WEST);
        header.add(lblEstado, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // --- 2. PANEL LATERAL (CONTROLES Y MÉTRICAS) ---
        JPanel lateral = new JPanel();
        lateral.setLayout(new BoxLayout(lateral, BoxLayout.Y_AXIS));
        lateral.setBackground(blanco);
        lateral.setPreferredSize(new Dimension(320, 600));
        lateral.setBorder(new EmptyBorder(30, 25, 30, 25));

        // Tarjeta de Métrica (Corregido: variable unificada como cardMetrica)
        JPanel cardMetrica = new JPanel(new GridBagLayout());
        cardMetrica.setBackground(new Color(240, 253, 244));
        cardMetrica.setBorder(BorderFactory.createLineBorder(verdePrimario, 1));
        cardMetrica.setMaximumSize(new Dimension(280, 80));

        lblTotal = new JLabel("0 Usuarios");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTotal.setForeground(verdePrimario);
        cardMetrica.add(lblTotal);

        lateral.add(new JLabel("ESTADÍSTICAS GLOBALES"));
        lateral.add(Box.createVerticalStrut(10));
        lateral.add(cardMetrica);
        lateral.add(Box.createVerticalStrut(30));

        // Campos de entrada
        txtNombre = crearCampoInput("Nombre del Usuario");
        txtCorreo = crearCampoInput("Correo Electrónico");
        txtBuscar = crearCampoInput("🔍 Filtrar Tabla...");

        JButton btnAdd = crearBotonEstilizado("REGISTRAR USUARIO", verdePrimario);
        JButton btnReporte = crearBotonEstilizado("GENERAR REPORTE TXT", azulOscuro);
        JButton btnDel = crearBotonEstilizado("ELIMINAR SELECCIÓN", new Color(239, 68, 68));

        lateral.add(new JLabel("ACCIONES DE GESTIÓN"));
        lateral.add(Box.createVerticalStrut(10));
        lateral.add(txtNombre); lateral.add(Box.createVerticalStrut(10));
        lateral.add(txtCorreo); lateral.add(Box.createVerticalStrut(20));
        lateral.add(btnAdd); lateral.add(Box.createVerticalStrut(10));
        lateral.add(btnReporte); lateral.add(Box.createVerticalStrut(40));
        lateral.add(new JLabel("HERRAMIENTAS DE BUSQUEDA"));
        lateral.add(Box.createVerticalStrut(10));
        lateral.add(txtBuscar); lateral.add(Box.createVerticalStrut(15));
        lateral.add(btnDel);

        add(lateral, BorderLayout.WEST);

        // --- 3. PANEL CENTRAL (TABLA DE DATOS) ---
        modeloTabla = new DefaultTableModel(new String[]{"ID ÚNICO (UUID)", "NOMBRE USUARIO", "CORREO ELECTRÓNICO"}, 0);
        tablaUsuarios = new JTable(modeloTabla);
        sorter = new TableRowSorter<>(modeloTabla);
        tablaUsuarios.setRowSorter(sorter);

        // Estilización de Tabla
        tablaUsuarios.setRowHeight(40);
        tablaUsuarios.setSelectionBackground(new Color(240, 253, 244));
        tablaUsuarios.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tablaUsuarios.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tablaUsuarios.getTableHeader().setBackground(blanco);

        JScrollPane scroll = new JScrollPane(tablaUsuarios);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(blanco);
        add(scroll, BorderLayout.CENTER);

        // --- LÓGICA DE EVENTOS ---
        btnAdd.addActionListener(e -> registrarEnFirebase());
        btnDel.addActionListener(e -> eliminarSeleccionado());
        btnReporte.addActionListener(e -> exportarReporteTxt());

        txtBuscar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) {
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + txtBuscar.getText()));
            }
        });

        conectarFirebaseListener();
    }

    // --- MÉTODOS DE SOPORTE ---

    private void registrarEnFirebase() {
        String n = txtNombre.getText();
        String c = txtCorreo.getText();

        String emailPattern = "^[A-Za-z0-9+_.-]+@(.+)$";
        if (n.isEmpty() || !Pattern.compile(emailPattern).matcher(c).matches()) {
            JOptionPane.showMessageDialog(this, "Datos inválidos. Verifique el formato del correo.");
            return;
        }

        DatabaseReference db = FirebaseDatabase.getInstance().getReference("Usuarios");
        Map<String, Object> u = new HashMap<>();
        u.put("nombre", n); u.put("email", c);
        db.push().setValueAsync(u);

        txtNombre.setText(""); txtCorreo.setText("");
    }

    private void eliminarSeleccionado() {
        int fila = tablaUsuarios.getSelectedRow();
        if (fila != -1) {
            int modelRow = tablaUsuarios.convertRowIndexToModel(fila);
            String id = modeloTabla.getValueAt(modelRow, 0).toString();
            FirebaseDatabase.getInstance().getReference("Usuarios").child(id).removeValueAsync();
            JOptionPane.showMessageDialog(this, "Registro eliminado de la nube.");
        }
    }

    private void exportarReporteTxt() {
        try (FileWriter fw = new FileWriter("GreenPulse_Report.txt")) {
            fw.write("REPORTES DE SISTEMA GREENPULSE\n");
            fw.write("==============================\n");
            for (int i = 0; i < modeloTabla.getRowCount(); i++) {
                fw.write("ID: " + modeloTabla.getValueAt(i, 0) + " | " +
                        "Nombre: " + modeloTabla.getValueAt(i, 1) + "\n");
            }
            JOptionPane.showMessageDialog(this, "Reporte exportado como GreenPulse_Report.txt");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void conectarFirebaseListener() {
        FirebaseDatabase.getInstance().getReference("Usuarios")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        modeloTabla.setRowCount(0);
                        lblTotal.setText(snapshot.getChildrenCount() + " Usuarios");
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            modeloTabla.addRow(new Object[]{
                                    ds.getKey(),
                                    ds.child("nombre").getValue(),
                                    ds.child("email").getValue()
                            });
                        }
                    }
                    @Override public void onCancelled(DatabaseError e) {}
                });
    }

    private JTextField crearCampoInput(String titulo) {
        JTextField f = new JTextField();
        f.setBorder(BorderFactory.createTitledBorder(titulo));
        f.setMaximumSize(new Dimension(280, 55));
        f.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return f;
    }

    private JButton crearBotonEstilizado(String texto, Color color) {
        JButton b = new JButton(texto);
        b.setBackground(color);
        b.setForeground(blanco);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setMaximumSize(new Dimension(280, 45));
        return b;
    }

    public void mostrar() { setVisible(true); }
}