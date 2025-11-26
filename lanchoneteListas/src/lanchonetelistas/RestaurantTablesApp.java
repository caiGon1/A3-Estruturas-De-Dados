import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;

public class RestaurantTablesApp extends JFrame {

    // === MODELO DE DADOS (lista encadeada simples) ===
    static class Table {
        int id;
        int capacity;
        boolean occupied;
        String partyName;

        Table(int id, int capacity) {
            this.id = id;
            this.capacity = capacity;
            this.occupied = false;
            this.partyName = "";
        }
    }

    static class Node {
        Table table;
        Node next;
        Node(Table t) { table = t; next = null; }
    }

    static class TableList {
        private Node head;
        private int nextId = 1;

        public TableList() { head = null; }

        public Table createTable(int capacity) {
            Table t = new Table(nextId++, capacity);
            Node n = new Node(t);
            if (head == null) {
                head = n;
            } else {
                Node cur = head;
                while (cur.next != null) cur = cur.next;
                cur.next = n;
            }
            return t;
        }

        public boolean removeTableById(int id) {
            if (head == null) return false;
            if (head.table.id == id) {
                head = head.next;
                return true;
            }
            Node cur = head;
            while (cur.next != null && cur.next.table.id != id) cur = cur.next;
            if (cur.next == null) return false;
            cur.next = cur.next.next;
            return true;
        }

        public Table findById(int id) {
            Node cur = head;
            while (cur != null) {
                if (cur.table.id == id) return cur.table;
                cur = cur.next;
            }
            return null;
        }

        public boolean seatParty(int id, String partyName) {
            Table t = findById(id);
            if (t == null) return false;
            if (t.occupied) return false;
            t.occupied = true;
            t.partyName = partyName;
            return true;
        }

        public boolean freeTable(int id) {
            Table t = findById(id);
            if (t == null) return false;
            if (!t.occupied) return false;
            t.occupied = false;
            t.partyName = "";
            return true;
        }

        public Table[] toArray() {
            int size = 0;
            Node cur = head;
            while (cur != null) { size++; cur = cur.next; }
            Table[] arr = new Table[size];
            cur = head;
            int i = 0;
            while (cur != null) {
                arr[i++] = cur.table;
                cur = cur.next;
            }
            return arr;
        }

        public void clear() {
            head = null;
            nextId = 1;
        }
    }

    // === UI ===
    private TableList tableList = new TableList();
    private DefaultTableModel tableModel;
    private JTable jTable;

    private JTextField tfCapacity;
    private JTextField tfRemoveId;
    private JTextField tfSeatId;
    private JTextField tfPartyName;
    private JTextField tfFreeId;
    private JLabel lblStatus;

    private TableMapPanel mapPanel;

    public RestaurantTablesApp() {
        super("Gerenciador de Mesas - Lista Encadeada");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 520);
        setLocationRelativeTo(null);
        initComponents();
    }

    private void initComponents() {

        setLayout(new BorderLayout(8,8));

        // Painel de desenho das mesas
        mapPanel = new TableMapPanel(tableList);
        add(mapPanel, BorderLayout.WEST);

        // tabela
        String[] columns = {"ID", "Capacidade", "Ocupada", "Nome do Grupo"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        jTable = new JTable(tableModel);

        JScrollPane scroll = new JScrollPane(jTable);
        add(scroll, BorderLayout.CENTER);

        // painel de controles
        JPanel controls = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6,6,6,6);
        c.fill = GridBagConstraints.HORIZONTAL;

        // Adicionar mesa
        c.gridx = 0; c.gridy = 0;
        controls.add(new JLabel("Capacidade:"), c);
        tfCapacity = new JTextField(5);
        c.gridx = 1; controls.add(tfCapacity, c);
        JButton btnAdd = new JButton("Adicionar Mesa");
        c.gridx = 2; controls.add(btnAdd, c);

        btnAdd.addActionListener(e -> {
            try {
                int cap = Integer.parseInt(tfCapacity.getText().trim());
                if (cap <= 0) throw new NumberFormatException();
                Table t = tableList.createTable(cap);
                updateEverything();
                lblStatus.setText("Mesa adicionada (ID=" + t.id + ")");
                tfCapacity.setText("");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Capacidade inválida.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Remover
        c.gridx = 0; c.gridy = 1;
        controls.add(new JLabel("Remover ID:"), c);
        tfRemoveId = new JTextField(5);
        c.gridx = 1; controls.add(tfRemoveId, c);
        JButton btnRemove = new JButton("Remover Mesa");
        c.gridx = 2; controls.add(btnRemove, c);

        btnRemove.addActionListener(e -> {
            try {
                int id = Integer.parseInt(tfRemoveId.getText().trim());
                boolean ok = tableList.removeTableById(id);
                updateEverything();
                lblStatus.setText(ok ? "Mesa removida." : "Mesa não encontrada.");
                tfRemoveId.setText("");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "ID inválido.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Sentar clientes
        c.gridx = 0; c.gridy = 2;
        controls.add(new JLabel("Sentar ID:"), c);
        tfSeatId = new JTextField(5);
        c.gridx = 1; controls.add(tfSeatId, c);

        c.gridx = 0; c.gridy = 3;
        controls.add(new JLabel("Nome do Grupo:"), c);
        tfPartyName = new JTextField(10);
        c.gridx = 1; controls.add(tfPartyName, c);

        JButton btnSeat = new JButton("Sentar Clientes");
        c.gridx = 2; controls.add(btnSeat, c);

        btnSeat.addActionListener(e -> {
            try {
                int id = Integer.parseInt(tfSeatId.getText().trim());
                String party = tfPartyName.getText().trim();
                if (party.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Informe o nome do grupo.");
                    return;
                }
                boolean ok = tableList.seatParty(id, party);
                updateEverything();
                lblStatus.setText(ok ? "Clientes sentados." : "Mesa já ocupada ou não existe.");
                tfSeatId.setText(""); tfPartyName.setText("");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "ID inválido.");
            }
        });

        // Liberar mesa
        c.gridx = 0; c.gridy = 4;
        controls.add(new JLabel("Liberar ID:"), c);
        tfFreeId = new JTextField(5);
        c.gridx = 1; controls.add(tfFreeId, c);

        JButton btnFree = new JButton("Liberar Mesa");
        c.gridx = 2; controls.add(btnFree, c);

        btnFree.addActionListener(e -> {
            try {
                int id = Integer.parseInt(tfFreeId.getText().trim());
                boolean ok = tableList.freeTable(id);
                updateEverything();
                lblStatus.setText(ok ? "Mesa liberada." : "Mesa já está livre ou não existe.");
                tfFreeId.setText("");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "ID inválido.");
            }
        });

        // detalhes
        JButton btnDetails = new JButton("Ver Detalhes");
        c.gridx = 0; c.gridy = 5; c.gridwidth = 2;
        controls.add(btnDetails, c);

        btnDetails.addActionListener(e -> {
            int sel = jTable.getSelectedRow();
            if (sel == -1) {
                JOptionPane.showMessageDialog(this, "Selecione uma mesa.");
                return;
            }
            int id = (int) tableModel.getValueAt(sel, 0);
            Table t = tableList.findById(id);
            String msg = "ID: " + t.id + "\nCapacidade: " + t.capacity +
                         "\nOcupada: " + (t.occupied ? "Sim" : "Não") +
                         "\nGrupo: " + (t.partyName.isEmpty() ? "-" : t.partyName);
            JOptionPane.showMessageDialog(this, msg);
        });

        // limpar tudo
        JButton btnClear = new JButton("Limpar Tudo");
        c.gridx = 2; c.gridy = 5; c.gridwidth = 1;
        controls.add(btnClear, c);

        btnClear.addActionListener(e -> {
            int op = JOptionPane.showConfirmDialog(this, "Remover todas as mesas?");
            if (op == 0) {
                tableList.clear();
                updateEverything();
                lblStatus.setText("Tudo limpo.");
            }
        });

        // status
        lblStatus = new JLabel("Pronto.");
        c.gridx = 0; c.gridy = 6; c.gridwidth = 3;
        controls.add(lblStatus, c);

        add(controls, BorderLayout.EAST);

        addDemoTables();
        updateEverything();
    }

    private void addDemoTables() {
        tableList.createTable(2);
        tableList.createTable(4);
        tableList.createTable(6);
    }

    private void updateEverything() {
        updateTableView();
        mapPanel.repaint();
    }

    private void updateTableView() {
        tableModel.setRowCount(0);
        Table[] arr = tableList.toArray();
        for (Table t : arr) {
            tableModel.addRow(new Object[]{
                t.id,
                t.capacity,
                t.occupied ? "Sim" : "Não",
                t.partyName
            });
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            new RestaurantTablesApp().setVisible(true);
        });
    }
}

// =============================================
// PAINEL QUE DESENHA O MAPA VISUAL DAS MESAS
// =============================================
class TableMapPanel extends JPanel {

    private RestaurantTablesApp.TableList tableList;

    public TableMapPanel(RestaurantTablesApp.TableList list) {
        this.tableList = list;
        setPreferredSize(new Dimension(350, 350));
        setBackground(Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        RestaurantTablesApp.Table[] tables = tableList.toArray();

        int[][] positions = {
            {150, 140},
            {100, 220},
            {80, 60},
            {230, 230},
            {240, 70}
        };

        for (int i = 0; i < tables.length && i < positions.length; i++) {
            RestaurantTablesApp.Table t = tables[i];
            int x = positions[i][0];
            int y = positions[i][1];

            g.setColor(t.occupied ? Color.RED : Color.GREEN);
            g.fillRoundRect(x, y, 60, 60, 20, 20);

            g.setColor(Color.BLACK);
            g.drawRoundRect(x, y, 60, 60, 20, 20);

            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.drawString(String.valueOf(t.id), x + 25, y + 35);
        }
    }
}
