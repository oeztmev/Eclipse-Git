package zeitwaechter;

import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DigitalClockPanel extends JPanel {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JLabel timeLabel;
    private Timer timer;

    public DigitalClockPanel() {
        timeLabel = new JLabel();
        timeLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        timeLabel.setHorizontalAlignment(JLabel.CENTER);
        setLayout(new BorderLayout());
        add(timeLabel, BorderLayout.CENTER);
        
        // Timer, der die Uhr jede Sekunde aktualisiert
        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateClock();
            }
        });
        timer.start();
        updateClock(); // Initiales Update
    }

    private void updateClock() {
        LocalTime now = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        timeLabel.setText(now.format(formatter));
    }
}


//test