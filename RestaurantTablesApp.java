
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;

/**
 * RestaurantTablesApp.java
 * - Modo Encadeado / Não Encadeado (botão no lado esquerdo)
 * - Mapa visual das mesas (estilo desenho do usuário)
 * - Setas mostram encadeamento apenas no modo "Encadeado"
 * - Botão "Ver Detalhes" abre detalhes da linha selecionada na tabela
 *
 * Compile:
 *   javac RestaurantTablesApp.java
 * Run:
 *   java RestaurantTablesApp
 */

public class RestaurantTablesApp extends JFrame {

    // ------------- MODELO -------------
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
        Node(Table t) { this.table = t; next = null; }
    }

    static class TableList {
        Node head;
        int nextId = 1;

        public Table createTable(int capacity) {
            Table t = new Table(nextId++, capacity);
            Node n = new Node(t);
            if (head == null) head = n;
            else {
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

        public Table[] toArray() {
            int count = 0;
            Node cur = head;
            while (cur != null) { count++; cur = cur.next; }
            Table[] arr = new Table[count];
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

    // ------------- PAINEL MAPA -------------
    static class TableMapPanel extends JPanel {
        private TableList list;
        private boolean encadeado;
        // fixed positions for the first 5 tables to match user's sketch
        private final int[][] fixedPositions = {
            {180, 90},  // 1 (center top)
            {90, 220},  // 2 (left bottom)
            {80, 40},   // 3 (left top)
            {260, 260}, // 4 (right bottom)
            {280, 60}   // 5 (right top)
        };

        public TableMapPanel(TableList list, boolean encadeado) {
            this.list = list;
            this.encadeado = encadeado;
            setPreferredSize(new Dimension(600, 420)); // larger canvas like the sketch
            setBackground(Color.white);
        }

        public void setEncadeado(boolean encadeado) {
            this.encadeado = encadeado;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Table[] tables = list.toArray();

            // Draw background grid lightly for alignment (optional)
            // g.setColor(new Color(240,240,240));
            // for (int i=0;i<getWidth();i+=20) g.drawLine(i,0,i,getHeight());
            // for (int i=0;i<getHeight();i+=20) g.drawLine(0,i,getWidth(),i);

            // If there are <= fixedPositions.length mesas, use fixed positions like sketch.
            // Otherwise arrange remaining in a grid below.
            int n = tables.length;
            Point[] positions = new Point[Math.max(n,0)];

            for (int i = 0; i < Math.min(n, fixedPositions.length); i++) {
                positions[i] = new Point(fixedPositions[i][0], fixedPositions[i][1]);
            }
            if (n > fixedPositions.length) {
                // place extras in a grid on bottom area
                int cols = 4;
                int startY = 330;
                int gapX = 120;
                int gapY = 70;
                for (int i = fixedPositions.length; i < n; i++) {
                    int idx = i - fixedPositions.length;
                    int col = idx % cols;
                    int row = idx / cols;
                    int x = 40 + col * gapX;
                    int y = startY + row * gapY;
                    positions[i] = new Point(x, y);
                }
            }

            // draw tables
            for (int i = 0; i < n; i++) {
                Table t = tables[i];
                Point p = positions[i];
                if (p == null) continue;
                int x = p.x, y = p.y;
                int w = 80, h = 60;
                // shape: slightly rounded rectangle like sketch
                if (t.occupied) g.setColor(new Color(220, 70, 70));
                else g.setColor(new Color(110, 200, 120));
                g.fillRoundRect(x, y, w, h, 16, 16);

                // border
                g.setColor(Color.DARK_GRAY);
                g.drawRoundRect(x, y, w, h, 16, 16);

                // ID text center-ish
                g.setColor(Color.BLACK);
                Font f = g.getFont().deriveFont(Font.BOLD, 14f);
                g.setFont(f);
                String idText = String.valueOf(t.id);
                // draw id bigger
                g.drawString(idText, x + w/2 - (g.getFontMetrics().stringWidth(idText)/2), y + 22);

                // capacity small under id
                Font f2 = g.getFont().deriveFont(Font.PLAIN, 11f);
                g.setFont(f2);
                String capText = "cap:" + t.capacity;
                g.drawString(capText, x + 6, y + h - 18);

                // party name if occupied
                if (t.occupied) {
                    String name = t.partyName;
                    String shortName = name.length() > 12 ? name.substring(0, 11) + "…" : name;
                    g.drawString(shortName, x + 6, y + h - 6);
                }
            }

            // draw arrows if encadeado
            if (encadeado) {
                g.setColor(Color.BLACK);
                for (int i = 0; i < n - 1; i++) {
                    Point a = positions[i];
                    Point b = positions[i+1];
                    if (a == null || b == null) continue;
                    // draw line from right-center of a to left-center of b (or a->b)
                    int ax = a.x + 80;
                    int ay = a.y + 30;
                    int bx = b.x;
                    int by = b.y + 30;
                    // slightly curved or straight line
                    drawArrow(g, ax, ay, bx, by);
                }
            }
        }

        // utility to draw arrow head
        private void drawArrow(Graphics g, int x1, int y1, int x2, int y2) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setStroke(new BasicStroke(2));
            // draw line
            g2.drawLine(x1, y1, x2, y2);
            // arrow head
            double phi = Math.toRadians(20);
            double barb = 12;
            double dy = y2 - y1;
            double dx = x2 - x1;
            double theta = Math.atan2(dy, dx);
            double x, y;
            x = x2 - barb * Math.cos(theta + phi);
            y = y2 - barb * Math.sin(theta + phi);
            g2.drawLine(x2, y2, (int)x, (int)y);
            x = x2 - barb * Math.cos(theta - phi);
            y = y2 - barb * Math.sin(theta - phi);
            g2.drawLine(x2, y2, (int)x, (int)y);
            g2.dispose();
        }
    }

    // ------------- UI / CONTROLS -------------
    private TableList tableList = new TableList();
    private DefaultTableModel tableModel;
    private JTable table;
    private TableMapPanel mapPanel;

    private boolean isEncadeado = true;

    // controls fields
    private JTextField tfCapacity = new JTextField();
    private JTextField tfRemove = new JTextField();
    private JTextField tfSeatId = new JTextField();
    private JTextField tfSeatName = new JTextField();
    private JTextField tfFreeId = new JTextField();
    private JLabel lblMode = new JLabel("Modo: Encadeado");
    private JLabel lblStatus = new JLabel("Pronto");

    public RestaurantTablesApp() {
        super("Gerenciador de Mesas");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 720);
        setLocationRelativeTo(null);
        initUI();
    }

    private void initUI() {
        // Left panel (controls) - requested to be on the left
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        left.setPreferredSize(new Dimension(220, 0));

        // Mode toggle and explicit label
        JButton btnToggle = new JButton("Alternar Modo");
        btnToggle.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnToggle.addActionListener(e -> {
            isEncadeado = !isEncadeado;
            lblMode.setText("Modo: " + (isEncadeado ? "Encadeado" : "Não Encadeado"));
            mapPanel.setEncadeado(isEncadeado);
            mapPanel.repaint();
        });

        lblMode.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblMode.setFont(lblMode.getFont().deriveFont(Font.BOLD, 14f));
        left.add(lblMode);
        left.add(Box.createRigidArea(new Dimension(0,6)));
        left.add(btnToggle);
        left.add(Box.createRigidArea(new Dimension(0,10)));

        // Details button (shows selected table in the table)
        JButton btnDetails = new JButton("Ver Detalhes");
        btnDetails.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnDetails.addActionListener(e -> showSelectedDetails());
        left.add(btnDetails);
        left.add(Box.createRigidArea(new Dimension(0,10)));

        // Add controls (create/remove/occupy/free)
        left.add(new JLabel("Criar mesa - Capacidade"));
        tfCapacity.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        left.add(tfCapacity);
        JButton btnAdd = new JButton("Adicionar Mesa");
        btnAdd.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnAdd.addActionListener(e -> {
            try {
                int cap = Integer.parseInt(tfCapacity.getText().trim());
                if (cap <= 0) throw new NumberFormatException();
                tableList.createTable(cap);
                tfCapacity.setText("");
                refreshAll("Mesa adicionada");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Capacidade inválida");
            }
        });
        left.add(btnAdd);
        left.add(Box.createRigidArea(new Dimension(0,8)));

        left.add(new JLabel("Remover mesa - ID"));
        tfRemove.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        left.add(tfRemove);
        JButton btnRemove = new JButton("Remover");
        btnRemove.addActionListener(e -> {
            try {
                int id = Integer.parseInt(tfRemove.getText().trim());
                boolean ok = tableList.removeTableById(id);
                tfRemove.setText("");
                refreshAll(ok ? "Mesa removida" : "Mesa não encontrada");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "ID inválido");
            }
        });
        left.add(btnRemove);
        left.add(Box.createRigidArea(new Dimension(0,8)));

        left.add(new JLabel("Ocupar - ID"));
        tfSeatId.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        left.add(tfSeatId);
        left.add(new JLabel("Nome do grupo"));
        tfSeatName.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        left.add(tfSeatName);
        JButton btnSeat = new JButton("Ocupar Mesa");
        btnSeat.addActionListener(e -> {
            try {
                int id = Integer.parseInt(tfSeatId.getText().trim());
                Table t = tableList.findById(id);
                if (t == null) {
                    JOptionPane.showMessageDialog(this, "Mesa não encontrada");
                    return;
                }
                if (t.occupied) {
                    JOptionPane.showMessageDialog(this, "Mesa já ocupada");
                    return;
                }
                t.occupied = true;
                t.partyName = tfSeatName.getText().trim();
                tfSeatId.setText(""); tfSeatName.setText("");
                refreshAll("Mesa ocupada");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "ID inválido");
            }
        });
        left.add(btnSeat);
        left.add(Box.createRigidArea(new Dimension(0,8)));

        left.add(new JLabel("Liberar - ID"));
        tfFreeId.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        left.add(tfFreeId);
        JButton btnFree = new JButton("Liberar Mesa");
        btnFree.addActionListener(e -> {
            try {
                int id = Integer.parseInt(tfFreeId.getText().trim());
                Table t = tableList.findById(id);
                if (t == null) {
                    JOptionPane.showMessageDialog(this, "Mesa não encontrada");
                    return;
                }
                t.occupied = false;
                t.partyName = "";
                tfFreeId.setText("");
                refreshAll("Mesa liberada");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "ID inválido");
            }
        });
        left.add(btnFree);
        left.add(Box.createVerticalGlue());
        left.add(lblStatus);

        add(left, BorderLayout.WEST);

        // Center: Map (top) + Table (bottom) using JSplitPane vertical
        mapPanel = new TableMapPanel(tableList, isEncadeado);
        JPanel centerTop = new JPanel(new BorderLayout());
        centerTop.add(mapPanel, BorderLayout.CENTER);
        centerTop.setBorder(BorderFactory.createTitledBorder("Mapa das Mesas"));

        // Table area
        String[] cols = {"ID", "Capacidade", "Ocupada", "Grupo"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setPreferredSize(new Dimension(400, 150));
        JPanel centerBottom = new JPanel(new BorderLayout());
        centerBottom.add(tableScroll, BorderLayout.CENTER);
        centerBottom.setBorder(BorderFactory.createTitledBorder("Tabela de Mesas"));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, centerTop, centerBottom);
        split.setResizeWeight(0.7); // map gets more space
        add(split, BorderLayout.CENTER);

        // bottom: quick action buttons (optional) or status
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnClearAll = new JButton("Limpar todas mesas");
        btnClearAll.addActionListener(e -> {
            int op = JOptionPane.showConfirmDialog(this, "Remover todas as mesas?");
            if (op == JOptionPane.YES_OPTION) {
                tableList.clear();
                refreshAll("Todas as mesas removidas");
            }
        });
        bottom.add(btnClearAll);
        add(bottom, BorderLayout.SOUTH);

        // add a few demo tables
        tableList.createTable(2);
        tableList.createTable(4);
        tableList.createTable(6);
        tableList.createTable(4);
        tableList.createTable(2);

        refreshAll("Pronto");
    }

    private void refreshAll(String status) {
        // update table model
        tableModel.setRowCount(0);
        for (Table t : tableList.toArray()) {
            tableModel.addRow(new Object[]{ t.id, t.capacity, t.occupied ? "Sim" : "Não", t.partyName });
        }
        lblStatus.setText(status);
        lblModeUpdate();
        mapPanel.setEncadeado(isEncadeado);
        mapPanel.repaint();
    }

    private void lblModeUpdate() {
        // keep the left mode label consistent
        // find the label component (we have lblMode instance)
        lblMode.setText("Modo: " + (isEncadeado ? "Encadeado" : "Não Encadeado"));
    }

    private void showSelectedDetails() {
        int sel = table.getSelectedRow();
        if (sel == -1) {
            JOptionPane.showMessageDialog(this, "Selecione uma linha na tabela para ver detalhes.");
            return;
        }
        int id = (int) tableModel.getValueAt(sel, 0);
        Table t = tableList.findById(id);
        if (t == null) {
            JOptionPane.showMessageDialog(this, "Mesa não encontrada (inconsistência).");
            return;
        }
        String msg = String.format("ID: %d%nCapacidade: %d%nOcupada: %s%nNome do Grupo: %s",
                t.id, t.capacity, t.occupied ? "Sim" : "Não", t.partyName.isEmpty() ? "-" : t.partyName);
        JOptionPane.showMessageDialog(this, msg, "Detalhes da Mesa", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            RestaurantTablesApp app = new RestaurantTablesApp();
            app.setVisible(true);
        });
    }
}
