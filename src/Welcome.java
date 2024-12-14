import com.formdev.flatlaf.themes.FlatMacDarkLaf;

import javax.swing.*;
import java.awt.*;

public class Welcome extends JFrame {

    public Welcome() {
        try {
            // Apply the FlatLaf theme
            UIManager.setLookAndFeel(new FlatMacDarkLaf());
        } catch (Exception e) {
            System.out.println("Failed to load theme.");
        }

        // Customize FlatLaf colors
        UIManager.put("Label.foreground", Color.decode("#ffffff")); // White text
        UIManager.put("Button.foreground", Color.WHITE);            // White button text
        UIManager.put("Button.background", Color.decode("#709dff"));// Blue button background
        UIManager.put("Label.font", new Font("Poppins", Font.BOLD, 20)); // Smaller text size

        // Set up the frame
        setTitle("Welcome to Aptech Budget Tracker");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Welcome Label
        JLabel welcomeLabel = new JLabel("Welcome to Aptech Budget Tracker", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Poppins", Font.BOLD, 24)); // Smaller font size for title
        welcomeLabel.setForeground(Color.decode("#709dff"));
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));

        // Description Label
        JLabel descriptionLabel = new JLabel("Track your budget with ease and elegance.", SwingConstants.CENTER);
        descriptionLabel.setFont(new Font("Poppins", Font.PLAIN, 16)); // Smaller font for subtitle
        descriptionLabel.setForeground(Color.LIGHT_GRAY);

        // Button to open BudgetTracker
        JButton startButton = new JButton("Start Budget Tracker");
        startButton.setFont(new Font("Poppins", Font.PLAIN, 18));
        startButton.setFocusPainted(false); // Remove focus highlight
        startButton.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Hand cursor for the button

        // Add ActionListener to open the main app
        startButton.addActionListener(e -> {
            new BudgetTracker().setLocationRelativeTo(null); // Open BudgetTracker
            dispose(); // Close WelcomePage
        });

        // Layout components
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(startButton);

        add(welcomeLabel, BorderLayout.NORTH);
        add(descriptionLabel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Center the frame
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
