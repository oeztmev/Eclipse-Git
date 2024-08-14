package gruppen;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import javax.swing.*;

public class PowerShellGUI {

    private static JButton button1;
    private static JButton button2;
    private static JButton button3;
    private static JLabel infoLabel1;
    private static JLabel statusLabel1;
    private static JLabel infoLabel2;
    private static JLabel statusLabel2;
    private static JLabel statusLabel3;
    private static JLabel infoLabel3;

    public static void main(String[] args) {
        // Erstelle das GUI-Frame
        JFrame frame = new JFrame("PowerShell Skript Executor");
        frame.setSize(600, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Erstelle das Haupt-Panel für die Buttons und Statusanzeigen
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        // Erstelle ein Panel für die Buttons und Statusanzeigen
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Erstelle die Buttons
        button1 = new JButton("Meine Gruppen ermitteln");
        button2 = new JButton("Gruppendetails ermitteln");
        button3 = new JButton("Details");
        button2.setEnabled(false);
        button3.setEnabled(false);

        // Setze die gleiche Größe für alle Buttons
        Dimension buttonSize = new Dimension(200, 30);
        button1.setPreferredSize(buttonSize);
        button2.setPreferredSize(buttonSize);
        button3.setPreferredSize(buttonSize);

        // Erstelle die Labels
        infoLabel1 = new JLabel(" ");
        infoLabel1.setForeground(Color.BLUE);
        statusLabel1 = new JLabel(" ");
        statusLabel1.setForeground(Color.RED);

        infoLabel2 = new JLabel(" ");
        infoLabel2.setForeground(Color.BLUE);
        statusLabel2 = new JLabel(" ");
        statusLabel2.setForeground(Color.RED);

        infoLabel3 = new JLabel(" ");
        infoLabel3.setForeground(Color.BLUE);
        statusLabel3 = new JLabel(" ");
        statusLabel3.setForeground(Color.RED);

        // Füge die Buttons und Labels zum Button-Panel hinzu
        gbc.gridx = 0;
        gbc.gridy = 0;
        buttonPanel.add(button1, gbc);

        gbc.gridx = 1;
        buttonPanel.add(statusLabel1, gbc);

        gbc.gridx = 2;
        buttonPanel.add(infoLabel1, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        buttonPanel.add(button2, gbc);

        gbc.gridx = 1;
        buttonPanel.add(statusLabel2, gbc);

        gbc.gridx = 2;
        buttonPanel.add(infoLabel2, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        buttonPanel.add(button3, gbc);

        gbc.gridx = 1;
        buttonPanel.add(statusLabel3, gbc);

        gbc.gridx = 2;
        buttonPanel.add(infoLabel3, gbc);

        // Füge das Button-Panel zum Haupt-Panel hinzu
        mainPanel.add(buttonPanel, BorderLayout.CENTER);

        // Füge ActionListener zu den Buttons hinzu
        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                executePowerShellScript1();
            }
        });

        button2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                executePowerShellScript2();
            }
        });

        button3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                executePowerShellScript3();
            }
        });

        // Füge das Panel zum Frame hinzu
        frame.add(mainPanel);

        // Setze das GUI sichtbar
        frame.setVisible(true);
    }

    private static void executePowerShellScript1() {
        String powerShellScript1 = 
            "$groupsOutput = whoami /groups\n" +
            "$emeraGroups = $groupsOutput | Where-Object { $_ -match \"^EMEA\\\\\" } | ForEach-Object {\n" +
            "    $_ -replace \"^EMEA\\\\\", \"\" -split '\\\\s+' | Select-Object -First 1\n" +
            "}\n" +
            "$emeraGroups | Out-File -FilePath \"C:\\\\Temp\\\\my_emea_groups.txt\" -Encoding UTF8\n";

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                String scriptPath = createTempScript(powerShellScript1);
                String command = "powershell.exe -ExecutionPolicy Bypass -File " + scriptPath;

                ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", command);

                try {
                    SwingUtilities.invokeLater(() -> {
                        statusLabel1.setText("Bitte warten, die Abfrage läuft...");
                        infoLabel1.setText("");
                    });

                    Process process = processBuilder.start();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }

                    while ((line = errorReader.readLine()) != null) {
                        System.err.println("ERROR: " + line);
                    }

                    int exitCode = process.waitFor();
                    System.out.println("Befehl beendete mit Fehlercode: " + exitCode);
                    
                    new File(scriptPath).delete();

                    File outputFile = new File("C:\\Temp\\my_emea_groups.txt");
                    if (outputFile.exists()) {
                        SwingUtilities.invokeLater(() -> {
                            statusLabel1.setText("");
                            infoLabel1.setText("Datei erstellt.");
                            button2.setEnabled(true);
                        });
                    } else {
                        SwingUtilities.invokeLater(() -> {
                            statusLabel1.setText("Fehler: Datei wurde nicht erstellt.");
                            infoLabel1.setText("");
                        });
                    }

                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }

                return null;
            }
        };

        worker.execute();
    }

    private static void executePowerShellScript2() {
        String powerShellScript2 = 
            "Add-Type -AssemblyName System.DirectoryServices\n" +
            "$domain = \"LDAP://emea.corpdir.net\"\n" +
            "$inputFilePath = \"C:\\\\Temp\\\\my_emea_groups.txt\"\n" +
            "$outputFilePath = \"C:\\\\Temp\\\\GroupOwners.csv\"\n" +
            "$groupNames = Get-Content -Path $inputFilePath\n" +
            "$resultList = @()\n" +
            "foreach ($groupName in $groupNames) {\n" +
            "    $groupName = $groupName.Trim()\n" +
            "    $searcher = New-Object System.DirectoryServices.DirectorySearcher\n" +
            "    $searcher.SearchRoot = $domain\n" +
            "    $searcher.Filter = \"(cn=$groupName)\"\n" +
            "    $searcher.PropertiesToLoad.Add(\"dcxobjectowner\") | Out-Null\n" +
            "    try {\n" +
            "        $result = $searcher.FindOne()\n" +
            "        if ($result -ne $null) {\n" +
            "            $dcxobjectowner = $result.Properties[\"dcxobjectowner\"] -join ', '\n" +
            "            $resultList += [pscustomobject]@{\n" +
            "                GroupName = $groupName\n" +
            "                DCXObjectOwner = $dcxobjectowner\n" +
            "            }\n" +
            "        } else {\n" +
            "            $resultList += [pscustomobject]@{\n" +
            "                GroupName = $groupName\n" +
            "                DCXObjectOwner = \"Gruppe nicht gefunden\"\n" +
            "            }\n" +
            "        }\n" +
            "    } catch {\n" +
            "        $resultList += [pscustomobject]@{\n" +
            "            GroupName = $groupName\n" +
            "            DCXObjectOwner = \"Fehler: $($_.Exception.Message)\"\n" +
            "        }\n" +
            "    }\n" +
            "}\n" +
            "$resultList | Export-Csv -Path $outputFilePath -NoTypeInformation\n" +
            "Write-Output \"Die Ergebnisse wurden in der Datei gespeichert: $outputFilePath\"\n";

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                String scriptPath = createTempScript(powerShellScript2);
                String command = "powershell.exe -ExecutionPolicy Bypass -File " + scriptPath;

                ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", command);

                try {
                    SwingUtilities.invokeLater(() -> {
                        statusLabel2.setText("Bitte warten, die Abfrage läuft...");
                        infoLabel2.setText("");
                    });

                    Process process = processBuilder.start();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }

                    while ((line = errorReader.readLine()) != null) {
                        System.err.println("ERROR: " + line);
                    }

                    int exitCode = process.waitFor();
                    System.out.println("Befehl beendete mit Fehlercode: " + exitCode);
                    
                    new File(scriptPath).delete();

                    File outputFile = new File("C:\\Temp\\GroupOwners.csv");
                    if (outputFile.exists()) {
                        SwingUtilities.invokeLater(() -> {
                            statusLabel2.setText("");
                            infoLabel2.setText("Datei erstellt.");
                            button3.setEnabled(true);
                        });
                    } else {
                        SwingUtilities.invokeLater(() -> {
                            statusLabel2.setText("Fehler: Datei wurde nicht erstellt.");
                            infoLabel2.setText("");
                        });
                    }

                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }

                return null;
            }
        };

        worker.execute();
    }

    private static void executePowerShellScript3() {
        String powerShellScript3 = 
            "# Definiere den Pfad zur Eingabedatei und zur Ausgabedatei\n" +
            "$inputFilePath = \"C:\\\\Temp\\\\GroupOwners.csv\"\n" +
            "$outputFilePath = \"C:\\\\Temp\\\\GroupOwnersmitNamen.csv\"\n" +
            "\n" +
            "# Lese die CSV-Datei ein\n" +
            "$data = Import-Csv -Path $inputFilePath -Delimiter ',' -Header \"GroupName\", \"RolesXml\"\n" +
            "\n" +
            "# Erstelle eine Liste für die bereinigten Daten\n" +
            "$formattedData = @()\n" +
            "\n" +
            "foreach ($row in $data) {\n" +
            "    # Extrahiere die Gruppendaten und die XML-Daten\n" +
            "    $groupName = $row.GroupName.Trim('\"')\n" +
            "    $rolesXml = $row.RolesXml.Trim('\"')\n" +
            "\n" +
            "    # Initialisiere leere Variablen für die sAMAccountNames\n" +
            "    $owner = \"\"\n" +
            "    $supervisor = \"\"\n" +
            "    $delegate = \"\"\n" +
            "\n" +
            "    # Falls die XML-Daten vorhanden sind, extrahiere die sAMAccountNames\n" +
            "    if ($rolesXml) {\n" +
            "        # Lade die XML-Daten in ein XmlDocument\n" +
            "        $xmlDoc = New-Object -TypeName System.Xml.XmlDocument\n" +
            "        $xmlDoc.LoadXml($rolesXml)\n" +
            "\n" +
            "        # Extrahiere die sAMAccountNames für jede Rolle\n" +
            "        $roles = $xmlDoc.SelectNodes(\"//role\")\n" +
            "\n" +
            "        foreach ($role in $roles) {\n" +
            "            $type = $role.GetAttribute(\"type\")\n" +
            "            $sAMAccountName = $role.GetAttribute(\"sAMAccountName\")\n" +
            "\n" +
            "            switch ($type) {\n" +
            "                \"OWNER\" {\n" +
            "                    if (-not $owner) {\n" +
            "                        $owner = $sAMAccountName\n" +
            "                    }\n" +
            "                }\n" +
            "                \"SUPERVISOR\" {\n" +
            "                    if (-not $supervisor) {\n" +
            "                        $supervisor = $sAMAccountName\n" +
            "                    }\n" +
            "                }\n" +
            "                \"DELEGATE\" {\n" +
            "                    if (-not $delegate) {\n" +
            "                        $delegate = $sAMAccountName\n" +
            "                    }\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "\n" +
            "    # Füge die bereinigten Daten zur Liste hinzu\n" +
            "    $formattedData += [pscustomobject]@{\n" +
            "        GroupName                  = $groupName\n" +
            "        OWNER_sAMAccountName       = $owner\n" +
            "        SUPERVISOR_sAMAccountName  = $supervisor\n" +
            "        DELEGATE_sAMAccountName    = $delegate\n" +
            "    }\n" +
            "}\n" +
            "\n" +
            "# Exportiere die bereinigten Daten in eine neue CSV-Datei\n" +
            "$formattedData | Export-Csv -Path $outputFilePath -NoTypeInformation -Delimiter ','\n" +
            "\n" +
            "# Bestätigung ausgeben\n" +
            "Write-Output \"Die bereinigten Daten wurden in der Datei gespeichert: $outputFilePath\"\n";

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                String scriptPath = createTempScript(powerShellScript3);
                String command = "powershell.exe -ExecutionPolicy Bypass -File " + scriptPath;

                ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", command);

                try {
                    SwingUtilities.invokeLater(() -> {
                        statusLabel3.setText("Bitte warten, die Abfrage läuft...");
                        infoLabel3.setText("");
                        infoLabel2.setText("");
                        infoLabel2.setText("Datei erstellt."); // Aktualisiere Status für `button2`
                    });

                    Process process = processBuilder.start();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }

                    while ((line = errorReader.readLine()) != null) {
                        System.err.println("ERROR: " + line);
                    }

                    int exitCode = process.waitFor();
                    System.out.println("Befehl beendete mit Fehlercode: " + exitCode);
                    
                    new File(scriptPath).delete();

                    File outputFile = new File("C:\\Temp\\GroupOwnersmitNamen.csv");
                    if (outputFile.exists()) {
                        SwingUtilities.invokeLater(() -> {
                            statusLabel3.setText("");
                            infoLabel3.setText("Datei erstellt.");
                        });
                    } else {
                        SwingUtilities.invokeLater(() -> {
                            statusLabel3.setText("Fehler: Datei wurde nicht erstellt.");
                            infoLabel3.setText("");
                        });
                    }

                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }

                return null;
            }
        };

        worker.execute();
    }

    private static String createTempScript(String scriptContent) throws IOException {
        File tempScriptFile = File.createTempFile("powershell_script", ".ps1");
        tempScriptFile.deleteOnExit();

        try (FileWriter writer = new FileWriter(tempScriptFile)) {
            writer.write(scriptContent);
        }

        return tempScriptFile.getAbsolutePath();
    }
}
