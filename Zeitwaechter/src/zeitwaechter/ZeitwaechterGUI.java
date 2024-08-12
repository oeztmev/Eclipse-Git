package zeitwaechter;

import javax.swing.*;
import java.awt.*;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

public class ZeitwaechterGUI {
    private static Duration PAUSE = Duration.ofMinutes(45);
    private static Duration ARBEITSDAUER = Duration.ofHours(7);
    private static final Duration MAX_ARBEITSZEIT = Duration.ofHours(10);

    private static JLabel kommtZeitValue;
    private static JLabel endeArbeitszeitValue;
    private static JLabel maxArbeitszeitEndeValue;
    private static JLabel infoLabel;
    private static JLabel uhrLabel;
    private static JProgressBar progressBar;
    private static JTextField arbeitszeitField;
    private static JTextField pauseField;
    private static JButton bearbeitenButton;
    private static JButton okButton;

    public static void main(String[] args) {
        // Hauptfenster erstellen
        JFrame frame = new JFrame("Zeitwächter");
        frame.setSize(471, 283);
        frame.setLocationRelativeTo(null);  // Fenster in der Mitte des Bildschirms starten
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // TabbedPane erstellen
        JTabbedPane tabbedPane = new JTabbedPane();
        frame.getContentPane().add(tabbedPane);

        // Haupt-Tab
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(null);
        tabbedPane.addTab("Tool", mainPanel);

        // Kommt-Zeit Label
        JLabel kommtZeitLabel = new JLabel("Kommt-Zeit (HH:mm):");
        kommtZeitLabel.setBounds(20, 10, 150, 25);
        mainPanel.add(kommtZeitLabel);

        JTextField kommtZeitField = new JTextField();
        kommtZeitField.setBounds(200, 10, 60, 25);
        mainPanel.add(kommtZeitField);

        // Arbeitsbeginn Label
        JLabel arbeitsbeginnLabel = new JLabel("Arbeitsbeginn: ");
        arbeitsbeginnLabel.setBounds(20, 40, 119, 25);
        mainPanel.add(arbeitsbeginnLabel);

        // Kommt-Zeit Wert
        kommtZeitValue = new JLabel("08:25 Uhr");
        kommtZeitValue.setBounds(200, 40, 100, 25);
        mainPanel.add(kommtZeitValue);

        // Ende Arbeitszeit Label
        JLabel endeArbeitszeitLabel = new JLabel("Ende Arbeitszeit (IRTAZ):");
        endeArbeitszeitLabel.setBounds(20, 70, 180, 25);
        mainPanel.add(endeArbeitszeitLabel);

        endeArbeitszeitValue = new JLabel("16:10 Uhr");
        endeArbeitszeitValue.setBounds(200, 70, 100, 25);
        mainPanel.add(endeArbeitszeitValue);

        // Max. Arbeitszeit Label
        JLabel maxArbeitszeitEndeLabel = new JLabel("Erreichen Höchstarbeitszeit:");
        maxArbeitszeitEndeLabel.setBounds(20, 100, 180, 25);
        mainPanel.add(maxArbeitszeitEndeLabel);

        maxArbeitszeitEndeValue = new JLabel("19:10 Uhr");
        maxArbeitszeitEndeValue.setForeground(Color.RED);
        maxArbeitszeitEndeValue.setBounds(200, 100, 100, 25);
        mainPanel.add(maxArbeitszeitEndeValue);

        // Digitaluhr (Systemzeit)
        uhrLabel = new JLabel();
        uhrLabel.setFont(new Font("Arial", Font.BOLD, 20));
        uhrLabel.setBounds(351, 1, 89, 37);
        mainPanel.add(uhrLabel);

        // Fortschrittsbalken für Arbeitszeit
        progressBar = new JProgressBar(0, 100) {
            /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getValue() > 0) {
                    // Zeichne die Prozentzahl auf dem Fortschrittsbalken
                    String percent = getValue() + "%";
                    FontMetrics fm = g.getFontMetrics();
                    int width = getWidth();
                    int height = getHeight();
                    int x = (width - fm.stringWidth(percent)) / 2;
                    int y = (height + fm.getAscent() - fm.getDescent()) / 2;
                    g.setColor(Color.BLACK); // Textfarbe
                    g.drawString(percent, x, y);
                }
            }
        };
        progressBar.setBounds(20, 136, 424, 30);
        progressBar.setStringPainted(true);
        progressBar.setForeground(Color.RED);
        mainPanel.add(progressBar);

        // Info Label
        infoLabel = new JLabel("reguläre Arbeitszeit beendet");
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 14)); // Schriftgröße und -art anpassen
        infoLabel.setForeground(Color.BLACK); // Farbe auf Schwarz ändern
        infoLabel.setBackground(Color.WHITE); // Hintergrundfarbe auf Weiß ändern
        infoLabel.setOpaque(true); // Hintergrundfarbe sichtbar machen
        infoLabel.setBounds(20, 177, 424, 25);
        mainPanel.add(infoLabel);

     // System-Tray-Funktionalität hinzufügen
        if (SystemTray.isSupported()) {
           SystemTray tray = SystemTray.getSystemTray();
            Image image = Toolkit.getDefaultToolkit().getImage(ZeitwaechterGUI.class.getResource("/resources/icon.png"));

            TrayIcon trayIcon = new TrayIcon(image, "Zeitwächter");
            trayIcon.setImageAutoSize(true);

            // Kontextmenü für das Tray-Icon hinzufügen
            PopupMenu popup = new PopupMenu();
            MenuItem showItem = new MenuItem("Show");
            MenuItem exitItem = new MenuItem("Exit");

            showItem.addActionListener(e -> frame.setVisible(true));
            exitItem.addActionListener(e -> {
                tray.remove(trayIcon);
                System.exit(0);
            });

            popup.add(showItem);
            popup.add(exitItem);
            trayIcon.setPopupMenu(popup);

            trayIcon.addActionListener(e -> frame.setVisible(true));

            try {
                tray.add(trayIcon);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Optional: Schließe das Fenster und lasse die Anwendung im Tray laufen
            frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        } else {
            System.out.println("System Tray is not supported.");
        }
        
        
        // Benutzerdefinierte Einstellungen Tab
        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(null);
        tabbedPane.addTab("Settings", settingsPanel);

        // Arbeitszeit (Soll) Label und Feld
        JLabel arbeitszeitLabel = new JLabel("Arbeitszeit (Soll) (HH:mm):");
        arbeitszeitLabel.setBounds(20, 20, 180, 25);
        settingsPanel.add(arbeitszeitLabel);

        
        
        
        
        arbeitszeitField = new JTextField("07:00");
        arbeitszeitField.setBounds(200, 20, 60, 25);
        arbeitszeitField.setEnabled(false);  // Standardmäßig gesperrt
        settingsPanel.add(arbeitszeitField);

        // Pause Label und Feld
        JLabel pauseLabel = new JLabel("Pause (HH:mm):");
        pauseLabel.setBounds(20, 60, 180, 25);
        settingsPanel.add(pauseLabel);

        pauseField = new JTextField("00:45");
        pauseField.setBounds(200, 60, 60, 25);
        pauseField.setEnabled(false);  // Standardmäßig gesperrt
        settingsPanel.add(pauseField);

        // Bearbeiten Button
        bearbeitenButton = new JButton("Bearbeiten");
        bearbeitenButton.setBounds(20, 100, 100, 25);
        settingsPanel.add(bearbeitenButton);

        // OK Button
        okButton = new JButton("OK");
        okButton.setBounds(130, 100, 100, 25);
        okButton.setEnabled(false);  // OK Button deaktiviert, bis Bearbeiten gedrückt wird
        settingsPanel.add(okButton);
        
        JLabel lblNewLabel = new JLabel("Daimler Truck AG");
        lblNewLabel.setBounds(20, 179, 128, 14);
        settingsPanel.add(lblNewLabel);
        
        JLabel lblNewLabel_1 = new JLabel("T/IEG-A");
        lblNewLabel_1.setBounds(210, 179, 78, 14);
        settingsPanel.add(lblNewLabel_1);
        
        JLabel lblNewLabel_2 = new JLabel("OEZTMEV");
        lblNewLabel_2.setBounds(385, 179, 60, 14);
        settingsPanel.add(lblNewLabel_2);
        

        // ActionListener für Bearbeiten Button
        bearbeitenButton.addActionListener(e -> {
            arbeitszeitField.setEnabled(true);
            pauseField.setEnabled(true);
            okButton.setEnabled(true);
            bearbeitenButton.setEnabled(false);
            
            
           
        });

        // ActionListener für OK Button
        okButton.addActionListener(e -> {
            ARBEITSDAUER = Duration.ofHours(Long.parseLong(arbeitszeitField.getText().split(":")[0]))
                    .plusMinutes(Long.parseLong(arbeitszeitField.getText().split(":")[1]));
            PAUSE = Duration.ofMinutes(Long.parseLong(pauseField.getText().split(":")[1]));
            arbeitszeitField.setEnabled(false);
            pauseField.setEnabled(false);
            okButton.setEnabled(false);
            bearbeitenButton.setEnabled(true);
            
         
            updateStatus(kommtZeitField.getText());  // Werte sofort aktualisieren
        });

        // Timer zur automatischen Aktualisierung der Zeit
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> updateStatus(kommtZeitField.getText()));
            }
        }, 0, 1000);

        // Fenster anzeigen
        frame.setVisible(true);
    }

    private static void updateStatus(String kommtZeit) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime jetzt = LocalTime.now();
        uhrLabel.setText(jetzt.format(DateTimeFormatter.ofPattern("HH:mm:ss")));

        try {
            LocalTime kommtZeitParsed = LocalTime.parse(kommtZeit, formatter);
            LocalTime arbeitsende = kommtZeitParsed.plus(PAUSE).plus(ARBEITSDAUER);
            LocalTime maxArbeitszeitEnde = kommtZeitParsed.plus(MAX_ARBEITSZEIT);

            kommtZeitValue.setText(kommtZeitParsed.format(formatter) + " Uhr");
            endeArbeitszeitValue.setText(arbeitsende.format(formatter) + " Uhr");
            maxArbeitszeitEndeValue.setText(maxArbeitszeitEnde.format(formatter) + " Uhr");

            if (jetzt.isBefore(kommtZeitParsed)) {
                infoLabel.setText("Arbeitszeit hat noch nicht begonnen.");
                infoLabel.setForeground(Color.ORANGE); // Beispiel: Orange für besseren Kontrast
                progressBar.setValue(0);
                progressBar.setForeground(Color.RED);
            } else if (jetzt.isAfter(arbeitsende)) {
                infoLabel.setText("Erreichen Höchstarbeitszeit: " + maxArbeitszeitEnde.format(formatter) + " Uhr");
                infoLabel.setForeground(Color.RED); // Rot für dringendere Informationen
                progressBar.setValue(100);
                progressBar.setForeground(Color.GREEN);
            } else {
                infoLabel.setText("reguläre Arbeitszeit läuft.");
                infoLabel.setForeground(Color.BLACK); // Schwarz für gute Lesbarkeit

                // Berechne den Fortschritt und setze den Fortschrittsbalken
                long totalMinutes = Duration.between(kommtZeitParsed, arbeitsende).toMinutes();
                long workedMinutes = Duration.between(kommtZeitParsed, jetzt).toMinutes();
                int progress = (int) ((workedMinutes * 100) / totalMinutes);

                progressBar.setValue(progress);

                // Ändere die Farbe des Fortschrittsbalkens entsprechend dem Fortschritt
                if (progress < 50) {
                    progressBar.setForeground(Color.RED);
                } else if (progress < 80) {
                    progressBar.setForeground(Color.ORANGE);
                } else {
                    progressBar.setForeground(Color.GREEN);
                }
            }
        } catch (Exception ex) {
            kommtZeitValue.setText("Ungültige Zeit");
            endeArbeitszeitValue.setText("");
            maxArbeitszeitEndeValue.setText("");
            infoLabel.setText("Bitte gültige Kommt-Zeit eingeben");
            infoLabel.setForeground(Color.RED);
            progressBar.setValue(0);
            progressBar.setForeground(Color.RED);
        }
    }
}

//testdfasdfe
