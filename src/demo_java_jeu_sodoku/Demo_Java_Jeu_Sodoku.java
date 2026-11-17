package demo_java_jeu_sodoku;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;

/** Application autonome : Java 8, Swing, Nimbus, sans dépendance externe. */
public final class Demo_Java_Jeu_Sodoku extends JFrame {
    private static final Color BACKGROUND = new Color(214, 218, 224);
    private static final Color BLUE = new Color(20, 90, 165);
    private static final Color INK = new Color(28, 43, 57);
    private static final Color LINE = new Color(63, 83, 91);
    private final JComboBox<String> sizes = new JComboBox<String>(
            new String[] {"4 × 4", "6 × 6", "9 × 9", "12 × 12", "16 × 16"});
    private final JComboBox<String> levels = new JComboBox<String>(
            new String[] {"Facile", "Moyen", "Difficile"});
    private final int[] sizeValues = {4, 6, 9, 12, 16};
    private final JPanel board = new JPanel();
    private final JPanel numberPad = new JPanel();
    private final JLabel status = new JLabel("Préparation du jeu…");
    private final JLabel progress = new JLabel(" ");
    private final JLabel clock = new JLabel("00:00", SwingConstants.RIGHT);
    private final JTextArea history = new JTextArea();
    private final JButton newGame = new JButton("Nouvelle partie");
    private final JButton verify = new JButton("Vérifier la grille");
    private final JButton hint = new JButton("Donner un indice");
    private final JButton undo = new JButton("Annuler la saisie");
    private final JButton clear = new JButton("Effacer la case");
    private final JButton restart = new JButton("Recommencer");
    private final JButton solve = new JButton("Afficher la solution");
    private final Deque<Move> moves = new ArrayDeque<Move>();
    private final Random random = new Random();
    private SudokuModel model;
    private JTextField[][] cells;
    private int[][] values;
    private boolean[][] marked;
    private int selectedR = -1, selectedC = -1;
    private boolean updating, finished, busy;
    private long startTime, frozenSeconds;
    private int hintsUsed;
    private final Timer timer;

    public Demo_Java_Jeu_Sodoku() {
        super("Demo_Java_Jeu_Sodoku — Java 8 / Nimbus");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        sizes.setSelectedIndex(2);
        levels.setSelectedIndex(1);
        JPanel root = new JPanel(new BorderLayout(18, 16));
        root.setBackground(BACKGROUND);
        root.setBorder(BorderFactory.createEmptyBorder(18, 20, 12, 20));
        setContentPane(root);

        JPanel header = transparent(new BorderLayout());
        JLabel title = new JLabel("SUDOKU  /  KALEMA DANIEL");
        title.setFont(new Font("SansSerif", Font.BOLD, 25));
        title.setForeground(INK);
        header.add(title, BorderLayout.WEST);
        clock.setFont(new Font("SansSerif", Font.BOLD, 20));
        clock.setForeground(BLUE);
        header.add(clock, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);

        JPanel left = transparent(new BorderLayout(0, 14));
        JPanel surround = transparent(new GridBagLayout());
        board.setBackground(LINE);
        board.setBorder(BorderFactory.createLineBorder(LINE, 4));
        surround.add(board);
        JScrollPane scroll = new JScrollPane(surround);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BACKGROUND);
        left.add(scroll, BorderLayout.CENTER);
        numberPad.setOpaque(false);
        left.add(numberPad, BorderLayout.SOUTH);
        root.add(left, BorderLayout.CENTER);

        JPanel side = transparent(new GridBagLayout());
        side.setPreferredSize(new Dimension(245, 590));
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = 0; g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(0, 0, 8, 0);
        addSide(side, new JLabel("Taille de la grille :"), g);
        addSide(side, sizes, g);
        addSide(side, new JLabel("Difficulté :"), g);
        addSide(side, levels, g);
        addSide(side, newGame, g);
        progress.setForeground(BLUE);
        progress.setFont(new Font("SansSerif", Font.BOLD, 14));
        addSide(side, progress, g);
        history.setEditable(false);
        history.setFont(new Font("SansSerif", Font.PLAIN, 13));
        history.setLineWrap(true);
        history.setWrapStyleWord(true);
        history.setMargin(new Insets(10, 10, 10, 10));
        history.setForeground(INK);
        JScrollPane log = new JScrollPane(history);
        g.weighty = 1; g.fill = GridBagConstraints.BOTH;
        addSide(side, log, g);
        g.weighty = 0; g.fill = GridBagConstraints.HORIZONTAL;
        for (JButton button : new JButton[] {verify, hint, undo, clear, restart, solve}) {
            addSide(side, button, g);
        }
        JButton help = new JButton("Règles et aide");
        addSide(side, help, g);
        root.add(side, BorderLayout.EAST);
        status.setFont(new Font("SansSerif", Font.PLAIN, 12));
        root.add(status, BorderLayout.SOUTH);

        newGame.addActionListener(e -> generateGame());
        verify.addActionListener(e -> verifyBoard());
        hint.addActionListener(e -> giveHint());
        clear.addActionListener(e -> enterValue(0));
        undo.addActionListener(e -> undoMove());
        restart.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this,
                    "Effacer toutes vos réponses et recommencer cette grille ?",
                    "Recommencer", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                loadPuzzle();
            }
        });
        solve.addActionListener(e -> revealSolution());
        help.addActionListener(e -> showHelp());
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), "undo");
        getRootPane().getActionMap().put("undo", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { undoMove(); }
        });
        timer = new Timer(1000, e -> updateClock());
        timer.start();
        setMinimumSize(new Dimension(850, 730));
        setSize(1050, 850);
        setLocationRelativeTo(null);
        generateGame();
    }

    private static JPanel transparent(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setOpaque(false);
        return panel;
    }

    private static void addSide(JPanel side, Component item, GridBagConstraints g) {
        side.add(item, g);
        g.gridy++;
    }

    private void generateGame() {
        if (busy) return;
        final int size = sizeValues[sizes.getSelectedIndex()];
        final int difficulty = levels.getSelectedIndex();
        final String level = (String) levels.getSelectedItem();
        busy = true;
        setActionsEnabled(false);
        status.setText("Création d'une grille à solution unique…");
        new SwingWorker<SudokuModel, Void>() {
            @Override protected SudokuModel doInBackground() {
                return SudokuModel.generate(size, difficulty, random);
            }
            @Override protected void done() {
                try {
                    model = get();
                    buildBoard();
                    loadPuzzle();
                    append("Nouvelle partie : " + size + " × " + size + " — " + level + ".");
                    append("Blocs : " + model.boxRows + " lignes × " + model.boxCols + " colonnes.");
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (CancellationException ex) {
                    status.setText("Création annulée.");
                } catch (ExecutionException ex) {
                    JOptionPane.showMessageDialog(Demo_Java_Jeu_Sodoku.this,
                            "Impossible de créer la grille : " + ex.getCause(),
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                } finally {
                    busy = false;
                    setActionsEnabled(true);
                    refresh();
                }
            }
        }.execute();
    }

    private void buildBoard() {
        board.removeAll();
        board.setLayout(new GridLayout(model.size, model.size, 0, 0));
        int side = model.size >= 12 ? 640 : 600;
        board.setPreferredSize(new Dimension(side, side));
        cells = new JTextField[model.size][model.size];
        int fontSize = model.size >= 12 ? 21 : model.size == 9 ? 28 : 36;
        for (int r = 0; r < model.size; r++) {
            for (int c = 0; c < model.size; c++) {
                final int row = r, col = c;
                JTextField cell = new JTextField();
                cell.setHorizontalAlignment(SwingConstants.CENTER);
                cell.setFont(new Font("SansSerif", Font.BOLD, fontSize));
                cell.setOpaque(true);
                // Nimbus : fond contrôlé par l'application pour les surbrillances.
                cell.putClientProperty("Nimbus.Overrides.InheritDefaults", true);
                cell.setBorder(BorderFactory.createMatteBorder(
                        r % model.boxRows == 0 ? 2 : 1,
                        c % model.boxCols == 0 ? 2 : 1,
                        r == model.size - 1 ? 2 : 0,
                        c == model.size - 1 ? 2 : 0, LINE));
                ((AbstractDocument) cell.getDocument()).setDocumentFilter(new NumberFilter(model.size));
                cell.getDocument().addDocumentListener(new DocumentListener() {
                    @Override public void insertUpdate(DocumentEvent e) { edited(row, col); }
                    @Override public void removeUpdate(DocumentEvent e) { edited(row, col); }
                    @Override public void changedUpdate(DocumentEvent e) { }
                });
                cell.addFocusListener(new FocusAdapter() {
                    @Override public void focusGained(FocusEvent e) {
                        selectedR = row; selectedC = col;
                        cell.selectAll();
                        refresh();
                    }
                });
                cell.addKeyListener(new KeyAdapter() {
                    @Override public void keyPressed(KeyEvent e) {
                        int nr = row, nc = col;
                        switch (e.getKeyCode()) {
                            case KeyEvent.VK_UP: nr--; break;
                            case KeyEvent.VK_DOWN: nr++; break;
                            case KeyEvent.VK_LEFT: nc--; break;
                            case KeyEvent.VK_RIGHT: nc++; break;
                            default: return;
                        }
                        cells[(nr + model.size) % model.size][(nc + model.size) % model.size].requestFocusInWindow();
                        e.consume();
                    }
                });
                cells[r][c] = cell;
                board.add(cell);
            }
        }
        numberPad.removeAll();
        numberPad.setLayout(new GridLayout(model.size > 9 ? 2 : 1,
                model.size > 9 ? model.size / 2 : model.size, 5, 5));
        for (int value = 1; value <= model.size; value++) {
            final int number = value;
            JButton button = new JButton(Integer.toString(value));
            button.setFont(new Font("SansSerif", Font.BOLD, 17));
            button.setBackground(new Color(255, 216, 116));
            button.setFocusable(false);
            button.setPreferredSize(new Dimension(38, 38));
            button.addActionListener(e -> enterValue(number));
            numberPad.add(button);
        }
        board.revalidate();
        board.repaint();
        numberPad.revalidate();
        numberPad.repaint();
    }

    private void loadPuzzle() {
        updating = true;
        values = SudokuModel.copy(model.puzzle);
        marked = new boolean[model.size][model.size];
        for (int r = 0; r < model.size; r++) {
            for (int c = 0; c < model.size; c++) {
                cells[r][c].setText(values[r][c] == 0 ? "" : Integer.toString(values[r][c]));
            }
        }
        updating = false;
        moves.clear();
        finished = false;
        selectedR = selectedC = -1;
        hintsUsed = 0;
        frozenSeconds = 0;
        startTime = System.nanoTime();
        history.setText("");
        status.setText("Sélectionnez une case puis un nombre. Les cases bleues sont les valeurs de départ.");
        setActionsEnabled(true);
        updateClock();
        refresh();
    }

    private void edited(int row, int col) {
        if (updating || model == null) return;
        String text = cells[row][col].getText();
        int value = text.isEmpty() ? 0 : Integer.parseInt(text);
        if (values[row][col] == value) return;
        moves.push(new Move(row, col, values[row][col], value));
        values[row][col] = value;
        marked[row][col] = false;
        refresh();
        checkVictory();
    }

    private void enterValue(int value) {
        if (busy || finished || model == null) return;
        if (selectedR < 0 || model.puzzle[selectedR][selectedC] != 0) {
            status.setText("Sélectionnez d'abord une case vide à compléter.");
            return;
        }
        JTextField cell = cells[selectedR][selectedC];
        // Une action du pavé correspond à une seule entrée dans l'historique.
        setValue(selectedR, selectedC, value, true);
        cell.requestFocusInWindow();
    }

    private void setValue(int row, int col, int value, boolean record) {
        if (values[row][col] == value) return;
        if (record) moves.push(new Move(row, col, values[row][col], value));
        updating = true;
        values[row][col] = value;
        marked[row][col] = false;
        cells[row][col].setText(value == 0 ? "" : Integer.toString(value));
        updating = false;
        refresh();
        checkVictory();
    }

    private void undoMove() {
        if (busy || finished || moves.isEmpty()) return;
        Move move = moves.pop();
        selectedR = move.row; selectedC = move.col;
        setValue(move.row, move.col, move.before, false);
        cells[move.row][move.col].requestFocusInWindow();
        status.setText("Dernière saisie annulée.");
    }

    private void verifyBoard() {
        if (model == null || busy || finished) return;
        int errors = 0, empty = 0;
        for (int r = 0; r < model.size; r++) {
            for (int c = 0; c < model.size; c++) {
                marked[r][c] = values[r][c] != 0 && values[r][c] != model.solution[r][c];
                if (marked[r][c]) errors++;
                if (values[r][c] == 0) empty++;
            }
        }
        String message = errors == 0 ? "Aucune erreur dans les valeurs saisies. " + empty + " case(s) à compléter."
                : errors + " erreur(s) signalée(s) en rouge. " + empty + " case(s) vide(s).";
        append(message);
        status.setText(message);
        refresh();
    }

    private void giveHint() {
        if (model == null || busy || finished) return;
        int r = selectedR, c = selectedC;
        if (r < 0 || model.puzzle[r][c] != 0 || values[r][c] == model.solution[r][c]) {
            r = -1;
            for (int i = 0; i < model.size && r < 0; i++) {
                for (int j = 0; j < model.size; j++) {
                    if (values[i][j] != model.solution[i][j]) { r = i; c = j; break; }
                }
            }
        }
        if (r < 0) return;
        hintsUsed++;
        selectedR = r; selectedC = c;
        setValue(r, c, model.solution[r][c], true);
        append("Indice : ligne " + (r + 1) + ", colonne " + (c + 1) + " = " + model.solution[r][c] + ".");
        cells[r][c].requestFocusInWindow();
        if (!finished) status.setText("Indice ajouté. Vous pouvez l'annuler.");
    }

    private void revealSolution() {
        if (model == null || busy || finished) return;
        if (JOptionPane.showConfirmDialog(this, "Afficher la solution et terminer cette partie ?",
                "Solution", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        updating = true;
        values = SudokuModel.copy(model.solution);
        marked = new boolean[model.size][model.size];
        for (int r = 0; r < model.size; r++) {
            for (int c = 0; c < model.size; c++) cells[r][c].setText(Integer.toString(values[r][c]));
        }
        updating = false;
        finish("Solution affichée. Vous pouvez recommencer ou créer une nouvelle partie.");
    }

    private void checkVictory() {
        if (finished || busy) return;
        for (int r = 0; r < model.size; r++) {
            for (int c = 0; c < model.size; c++) {
                if (values[r][c] != model.solution[r][c]) return;
            }
        }
        finish("Bravo ! Grille résolue en " + timeText(elapsedSeconds()) + " — " + hintsUsed + " indice(s).");
    }

    private void finish(String message) {
        frozenSeconds = elapsedSeconds();
        finished = true;
        append(message);
        status.setText(message);
        setActionsEnabled(true);
        refresh();
    }

    private void refresh() {
        if (model == null || values == null) return;
        int filled = 0, total = 0;
        int selectedValue = selectedR < 0 ? 0 : values[selectedR][selectedC];
        for (int r = 0; r < model.size; r++) {
            for (int c = 0; c < model.size; c++) {
                boolean fixed = model.puzzle[r][c] != 0;
                if (!fixed) { total++; if (values[r][c] != 0) filled++; }
                Color background = fixed ? new Color(234, 239, 244) : Color.WHITE;
                if (selectedR >= 0 && (r == selectedR || c == selectedC ||
                        (r / model.boxRows == selectedR / model.boxRows &&
                         c / model.boxCols == selectedC / model.boxCols))) {
                    background = new Color(224, 241, 249);
                }
                if (selectedValue != 0 && values[r][c] == selectedValue) background = new Color(255, 230, 158);
                if (r == selectedR && c == selectedC) background = new Color(255, 216, 116);
                boolean conflict = values[r][c] != 0 && !SudokuModel.canPlace(
                        values, model.boxRows, model.boxCols, r, c, values[r][c]);
                if (conflict || marked[r][c]) background = new Color(255, 195, 202);
                cells[r][c].setBackground(background);
                cells[r][c].setForeground(fixed ? BLUE : INK);
                cells[r][c].setEditable(!fixed && !busy && !finished);
            }
        }
        progress.setText("Cases saisies : " + filled + " / " + total);
        undo.setEnabled(!busy && !finished && !moves.isEmpty());
    }

    private void setActionsEnabled(boolean enabled) {
        newGame.setEnabled(enabled);
        sizes.setEnabled(enabled);
        levels.setEnabled(enabled);
        boolean playing = enabled && model != null && !finished;
        for (JButton button : new JButton[] {verify, hint, clear, solve}) button.setEnabled(playing);
        restart.setEnabled(enabled && model != null);
        undo.setEnabled(playing && !moves.isEmpty());
        for (Component button : numberPad.getComponents()) button.setEnabled(playing);
        if (cells != null) refresh();
    }

    private void append(String text) {
        history.append(text + "\n\n");
        history.setCaretPosition(history.getDocument().getLength());
    }

    private long elapsedSeconds() {
        return finished ? frozenSeconds : Math.max(0, (System.nanoTime() - startTime) / 1000000000L);
    }

    private static String timeText(long seconds) {
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }

    private void updateClock() {
        clock.setText(model == null ? "00:00" : timeText(elapsedSeconds()));
    }

    private void showHelp() {
        JOptionPane.showMessageDialog(this,
                "Complétez chaque ligne, colonne et bloc avec les nombres de 1 à N,\n"
                + "sans répétition. N est la taille choisie (4, 6, 9, 12 ou 16).\n\n"
                + "4 × 4 : blocs 2 × 2     |     6 × 6 : blocs 2 × 3\n"
                + "9 × 9 : blocs 3 × 3     |     12 × 12 : blocs 3 × 4\n"
                + "16 × 16 : blocs 4 × 4, nombres de 1 à 16.\n\n"
                + "Choisissez la taille et la difficulté puis cliquez sur Nouvelle partie.\n"
                + "Les changements de taille s'appliquent à la prochaine partie.\n"
                + "Cliquez sur une case, puis tapez un nombre ou utilisez le pavé jaune.\n"
                + "Flèches : navigation. Retour arrière / Suppr : effacement.\n"
                + "Ctrl+Z : annuler une saisie. Les nombres bleus sont fixes.\n"
                + "Rouge : répétition détectée, ou erreur après vérification.\n"
                + "Chaque grille possède une solution unique. La difficulté règle\n"
                + "le nombre de cases retirées ; elle ne mesure pas les techniques requises.",
                "Règles et aide — Sudoku", JOptionPane.INFORMATION_MESSAGE);
    }

    private static final class NumberFilter extends DocumentFilter {
        private final int max;
        NumberFilter(int max) { this.max = max; }
        @Override public void insertString(FilterBypass fb, int offset, String text,
                AttributeSet attrs) throws BadLocationException {
            replace(fb, offset, 0, text, attrs);
        }
        @Override public void replace(FilterBypass fb, int offset, int length, String text,
                AttributeSet attrs) throws BadLocationException {
            String old = fb.getDocument().getText(0, fb.getDocument().getLength());
            String next = old.substring(0, offset) + (text == null ? "" : text) + old.substring(offset + length);
            if (next.isEmpty() || (next.matches("[1-9][0-9]?") && Integer.parseInt(next) <= max)) {
                fb.replace(offset, length, text, attrs);
            } else Toolkit.getDefaultToolkit().beep();
        }
    }

    private static final class Move {
        final int row, col, before, after;
        Move(int row, int col, int before, int after) {
            this.row = row; this.col = col; this.before = before; this.after = after;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception ex) {
                System.err.println("Nimbus indisponible : " + ex.getMessage());
            }
            new Demo_Java_Jeu_Sodoku().setVisible(true);
        });
    }
}
