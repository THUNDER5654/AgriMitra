import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*; 
import java.util.List;

public class AgriMitra {

    static HashMap<String, String[]> userDB = new HashMap<>(); // KCC -> [Name, City]
    static final String FILE_PATH = "users.txt";
    static final String WEATHER_API_KEY = "eea754f4664127d197ce774d9a1a6519";
    private static final String AI_API_KEY = ""; // leave empty for now
    private static final String[] COMMODITIES_TO_TRACK = {"Wheat", "Rice", "Maize", "Potato", "Onion", "Tomato"};

    // Modern color palette
    private static final Color PRIMARY_COLOR = new Color(46, 125, 50); // Deep green
    private static final Color SECONDARY_COLOR = new Color(129, 199, 132); // Light green
    private static final Color ACCENT_COLOR = new Color(255, 183, 77); // Warm orange
    private static final Color BACKGROUND_COLOR = new Color(248, 252, 248); // Light green background
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color TEXT_PRIMARY = new Color(33, 33, 33); // Dark gray
    private static final Color TEXT_SECONDARY = new Color(117, 117, 117); // Light gray

    // Image icons (we'll use text as fallback and try to create simple graphic icons)
    private static Map<String, Icon> iconCache = new HashMap<>();

    public static void main(String[] args) {
        // Set modern look and feel
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            // Set some UI defaults
            UIManager.put("nimbusBase", PRIMARY_COLOR);
            UIManager.put("nimbusBlueGrey", SECONDARY_COLOR);
            UIManager.put("control", BACKGROUND_COLOR);
        } catch (Exception e) {
            // Use default if Nimbus is not available
        }
        
        initializeIcons();
        loadUsers();
        SwingUtilities.invokeLater(AgriMitra::showLoginUI);
    }

    // ===== ICON INITIALIZATION =====
    private static void initializeIcons() {
        // Use system-recognized emojis and symbols for better relevance
        iconCache.put("app", createColoredIcon(PRIMARY_COLOR, "🌾")); // Wheat for agriculture
        iconCache.put("weather", createColoredIcon(new Color(66, 133, 244), "🌤️")); // Sun/cloud
        iconCache.put("market", createColoredIcon(new Color(251, 188, 5), "📊")); // Chart
        iconCache.put("soil", createColoredIcon(new Color(52, 168, 83), "🌱")); // Seedling
        iconCache.put("ai", createColoredIcon(new Color(234, 67, 53), "🤖")); // Robot for AI
        iconCache.put("user", createColoredIcon(new Color(101, 31, 255), "👨‍🌾")); // Farmer
        iconCache.put("farm", createColoredIcon(new Color(0, 150, 136), "🚜")); // Tractor
        iconCache.put("analysis", createColoredIcon(new Color(233, 30, 99), "🔍")); // Magnifying glass
        iconCache.put("settings", createColoredIcon(TEXT_SECONDARY, "⚙️")); // Gear
        iconCache.put("refresh", createColoredIcon(TEXT_SECONDARY, "🔄")); // Refresh arrows
        iconCache.put("search", createColoredIcon(TEXT_SECONDARY, "🔎")); // Search glass
        iconCache.put("send", createColoredIcon(PRIMARY_COLOR, "📤")); // Send/upload
        iconCache.put("upload", createColoredIcon(SECONDARY_COLOR, "📁")); // File folder
        
        // Commodity icons - using actual emojis for each crop
        iconCache.put("wheat", createColoredIcon(new Color(210, 180, 40), "🌾")); // Wheat
        iconCache.put("rice", createColoredIcon(new Color(240, 240, 240), "🍚")); // Cooked rice
        iconCache.put("maize", createColoredIcon(new Color(255, 204, 0), "🌽")); // Corn
        iconCache.put("potato", createColoredIcon(new Color(210, 180, 140), "🥔")); // Potato
        iconCache.put("onion", createColoredIcon(new Color(160, 120, 240), "🧅")); // Onion
        iconCache.put("tomato", createColoredIcon(new Color(255, 60, 60), "🍅")); // Tomato
    }

    private static Icon createColoredIcon(Color color, String emoji) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw background circle
                g2.setColor(color);
                g2.fillOval(x, y, getIconWidth(), getIconHeight());
                
                // Draw emoji/text
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16)); // Use emoji font
                FontMetrics fm = g2.getFontMetrics();
                int textX = x + (getIconWidth() - fm.stringWidth(emoji)) / 2;
                int textY = y + (getIconHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(emoji, textX, textY);
                
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return 32;
            }

            @Override
            public int getIconHeight() {
                return 32;
            }
        };
    }

    private static JLabel createIconLabel(String iconKey, String text, int iconSize) {
        JLabel label = new JLabel(text);
        label.setIcon(iconCache.get(iconKey));
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setVerticalTextPosition(SwingConstants.BOTTOM);
        label.setHorizontalTextPosition(SwingConstants.CENTER);
        return label;
    }

    // ===== USER DB =====
    private static void loadUsers() {
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) userDB.put(parts[0], new String[]{parts[1], parts[2]});
            }
        } catch (IOException e) {
            System.out.println("No existing users found.");
        }
    }

    private static void saveUsers() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Map.Entry<String, String[]> entry : userDB.entrySet()) {
                String kcc = entry.getKey();
                String name = entry.getValue()[0];
                String city = entry.getValue()[1];
                bw.write(kcc + "," + name + "," + city);
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ===== ENHANCED STYLED COMPONENTS =====
    private static JButton createStyledButton(String text, Color bgColor, Color textColor, int fontSize) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        button.setFont(new Font("Segoe UI", Font.BOLD, fontSize));
        button.setBackground(bgColor);
        button.setForeground(textColor);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        
        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.darker());
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(bgColor.darker().darker(), 2),
                    BorderFactory.createEmptyBorder(10, 23, 10, 23)
                ));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
                button.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));
            }
        });
        
        return button;
    }
    
    private static JTextField createStyledTextField(int columns) {
        JTextField field = new JTextField(columns) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        field.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        field.setBackground(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(SECONDARY_COLOR, 2),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        return field;
    }
    
    private static JPanel createCardPanel() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(240, 240, 240), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        return card;
    }

    // ===== ENHANCED LOGIN UI =====
    private static void showLoginUI() {
        JFrame frame = new JFrame("AgriMitra - Login");
        frame.setSize(450, 700);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(BACKGROUND_COLOR);

        // Main container with background
        JPanel mainContainer = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw background gradient
                GradientPaint gradient = new GradientPaint(0, 0, new Color(232, 245, 233), 0, getHeight(), BACKGROUND_COLOR);
                g2.setPaint(gradient);
                g2.fillRect(0, 0, getWidth(), getHeight());
                
                // Draw decorative elements
                g2.setColor(new Color(129, 199, 132, 30));
                g2.fillOval(-50, -50, 200, 200);
                g2.fillOval(getWidth() - 100, getHeight() - 150, 250, 250);
            }
        };
        mainContainer.setBackground(BACKGROUND_COLOR);
        frame.setContentPane(mainContainer);

        // Content panel with centered form
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);
        mainContainer.add(contentPanel, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 30, 10, 30);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // App logo/title with better styling
        JLabel title = new JLabel("AgriMitra", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 36));
        title.setForeground(PRIMARY_COLOR);
        title.setIcon(iconCache.get("app"));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(40, 30, 30, 30);
        contentPanel.add(title, gbc);

        // Subtitle
        JLabel subtitle = new JLabel("Smart Farming Assistant", SwingConstants.CENTER);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitle.setForeground(TEXT_SECONDARY);
        subtitle.setIcon(iconCache.get("farm"));
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 30, 40, 30);
        contentPanel.add(subtitle, gbc);

        // Enhanced form panel with shadow effect
        JPanel formPanel = createCardPanel();
        formPanel.setLayout(new GridBagLayout());
        GridBagConstraints formGbc = new GridBagConstraints();
        formGbc.insets = new Insets(12, 10, 12, 10);
        formGbc.fill = GridBagConstraints.HORIZONTAL;
        formGbc.gridwidth = 2;

        JLabel formTitle = new JLabel("Farmer Login", SwingConstants.CENTER);
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        formTitle.setForeground(TEXT_PRIMARY);
        formTitle.setIcon(iconCache.get("user"));
        formGbc.gridx = 0; formGbc.gridy = 0;
        formGbc.insets = new Insets(0, 10, 20, 10);
        formPanel.add(formTitle, formGbc);

        formGbc.gridy++;
        formGbc.insets = new Insets(5, 10, 5, 10);
        JLabel nameLabel = new JLabel("Name");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nameLabel.setForeground(TEXT_SECONDARY);
        formPanel.add(nameLabel, formGbc);

        formGbc.gridy++;
        JTextField nameField = createStyledTextField(20);
        formPanel.add(nameField, formGbc);

        formGbc.gridy++;
        JLabel kccLabel = new JLabel("KCC Number");
        kccLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        kccLabel.setForeground(TEXT_SECONDARY);
        formPanel.add(kccLabel, formGbc);

        formGbc.gridy++;
        JTextField kccField = createStyledTextField(20);
        formPanel.add(kccField, formGbc);

        gbc.gridy = 2; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 30, 30, 30);
        contentPanel.add(formPanel, gbc);

        // Buttons panel with better spacing
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 15, 15));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 30, 30, 30));

        JButton loginBtn = createStyledButton("LOGIN", PRIMARY_COLOR, Color.WHITE, 16);
        JButton signupBtn = createStyledButton("SIGN UP", SECONDARY_COLOR, Color.WHITE, 16);

        buttonPanel.add(loginBtn);
        buttonPanel.add(signupBtn);

        gbc.gridy = 3; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 30, 20, 30);
        contentPanel.add(buttonPanel, gbc);

        // Footer
        JLabel footer = new JLabel("Empowering Farmers with Technology", SwingConstants.CENTER);
        footer.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        footer.setForeground(TEXT_SECONDARY);
        gbc.gridy = 4;
        gbc.insets = new Insets(20, 30, 20, 30);
        contentPanel.add(footer, gbc);

        frame.setVisible(true);

        // Action listeners
        loginBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String kcc = kccField.getText().trim();
            if (name.isEmpty() || kcc.isEmpty()) {
                showErrorDialog(frame, "Please fill in all fields");
                return;
            }
            
            if (userDB.containsKey(kcc) && userDB.get(kcc)[0].equalsIgnoreCase(name)) {
                String city = userDB.get(kcc)[1];
                showSuccessDialog(frame, "Welcome back, " + name + "!");
                frame.dispose();
                showDashboard(name, kcc, city);
            } else {
                showErrorDialog(frame, "User not found! Please check your credentials or sign up.");
            }
        });

        signupBtn.addActionListener(e -> showSignupDialog(frame));
    }

    private static void showSignupDialog(JFrame parent) {
        JTextField nameF = createStyledTextField(20);
        JTextField kccF = createStyledTextField(20);
        JTextField cityF = createStyledTextField(20);
        JTextArea weatherPreview = new JTextArea(4, 25);
        weatherPreview.setEditable(false);
        weatherPreview.setBackground(new Color(245, 245, 245));
        weatherPreview.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        weatherPreview.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(SECONDARY_COLOR, 1),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        cityF.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent ke) {
                String city = cityF.getText().trim();
                if (!city.isEmpty()) fetchWeatherPreview(city, weatherPreview);
                else weatherPreview.setText("Enter city to see weather preview");
            }
        });

        JLabel nameLabel = new JLabel("Full Name:");
        nameLabel.setIcon(iconCache.get("user"));
        JLabel kccLabel = new JLabel("KCC Number:");
        kccLabel.setIcon(iconCache.get("user"));
        JLabel cityLabel = new JLabel("City:");
        cityLabel.setIcon(iconCache.get("weather"));
        JLabel weatherLabel = new JLabel("Weather Preview:");
        weatherLabel.setIcon(iconCache.get("weather"));

        Object[] fields = {
            nameLabel, nameF,
            kccLabel, kccF,
            cityLabel, cityF,
            weatherLabel, new JScrollPane(weatherPreview)
        };

        int option = JOptionPane.showConfirmDialog(parent, fields, "Farmer Registration", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (option == JOptionPane.OK_OPTION) {
            String nameVal = nameF.getText().trim();
            String kccVal = kccF.getText().trim();
            String cityVal = cityF.getText().trim();
            
            if (!nameVal.isEmpty() && !kccVal.isEmpty() && !cityVal.isEmpty()) {
                if (userDB.containsKey(kccVal)) {
                    showErrorDialog(parent, "KCC number already registered!");
                } else {
                    userDB.put(kccVal, new String[]{nameVal, cityVal});
                    saveUsers();
                    showSuccessDialog(parent, "Registration Successful!\nWelcome to AgriMitra, " + nameVal);
                    parent.dispose();
                    showDashboard(nameVal, kccVal, cityVal);
                }
            } else {
                showErrorDialog(parent, "Please fill all details!");
            }
        }
    }

    // ===== ENHANCED DIALOGS =====
    private static void showSuccessDialog(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Success", 
                JOptionPane.INFORMATION_MESSAGE);
    }

    private static void showErrorDialog(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", 
                JOptionPane.ERROR_MESSAGE);
    }

    // ===== ENHANCED DASHBOARD =====
    private static void showDashboard(String userName, String userKCC, String userCity) {
        JFrame frame = new JFrame("AgriMitra - Dashboard");
        frame.setSize(480, 850);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(BACKGROUND_COLOR);

        // TOP BAR with improved design
        JPanel topBar = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(0, 0, PRIMARY_COLOR, getWidth(), 0, PRIMARY_COLOR.darker());
                g2.setPaint(gradient);
                g2.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        topBar.setBackground(PRIMARY_COLOR);
        topBar.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        topBar.setPreferredSize(new Dimension(480, 70));
        
        // Left side with user info
        JPanel userInfoPanel = new JPanel(new BorderLayout());
        userInfoPanel.setOpaque(false);
        
        JLabel welcomeLabel = new JLabel("Welcome, " + userName + "!");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setIcon(iconCache.get("user"));
        
        JLabel locationLabel = new JLabel(userCity);
        locationLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        locationLabel.setForeground(new Color(255, 255, 255, 200));
        locationLabel.setIcon(iconCache.get("weather"));
        
        userInfoPanel.add(welcomeLabel, BorderLayout.NORTH);
        userInfoPanel.add(locationLabel, BorderLayout.SOUTH);
        
        // Right side with actions
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);
        
        JButton refreshBtn = createIconButton("refresh", "Refresh Data");
        JButton searchBtn = createIconButton("search", "Search");
        JButton menuBtn = createIconButton("settings", "Settings");
        
        actionPanel.add(refreshBtn);
        actionPanel.add(searchBtn);
        actionPanel.add(menuBtn);
        
        topBar.add(userInfoPanel, BorderLayout.WEST);
        topBar.add(actionPanel, BorderLayout.EAST);
        frame.add(topBar, BorderLayout.NORTH);

        // MAIN CONTENT with better organization
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(BACKGROUND_COLOR);
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Add padding
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // QUICK STATS PANEL
        JPanel statsPanel = createCardPanel();
        statsPanel.setLayout(new GridLayout(1, 3, 15, 0));
        
        // Weather Stat
        JPanel weatherStat = createStatCard("weather", "Weather", "Loading...", PRIMARY_COLOR);
        // Market Stat
        JPanel marketStat = createStatCard("market", "Market", "6 Commodities", ACCENT_COLOR);
        // Soil Stat
        JPanel soilStat = createStatCard("soil", "Soil Health", "Ready to Scan", SECONDARY_COLOR);
        
        statsPanel.add(weatherStat);
        statsPanel.add(marketStat);
        statsPanel.add(soilStat);
        mainPanel.add(statsPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // WEATHER CARD with more details
        JPanel weatherCard = createCardPanel();
        weatherCard.setLayout(new BorderLayout(10, 10));
        
        JPanel weatherHeader = new JPanel(new BorderLayout());
        weatherHeader.setOpaque(false);
        
        JLabel weatherTitle = new JLabel("Live Weather");
        weatherTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        weatherTitle.setForeground(TEXT_PRIMARY);
        weatherTitle.setIcon(iconCache.get("weather"));
        
        JLabel weatherLocation = new JLabel(userCity);
        weatherLocation.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        weatherLocation.setForeground(TEXT_SECONDARY);
        
        weatherHeader.add(weatherTitle, BorderLayout.WEST);
        weatherHeader.add(weatherLocation, BorderLayout.EAST);
        weatherCard.add(weatherHeader, BorderLayout.NORTH);
        
        JPanel weatherContent = new JPanel(new GridLayout(2, 2, 10, 10));
        weatherContent.setOpaque(false);
        
        JLabel tempLabel = createWeatherMetric("Temperature", "Loading...");
        JLabel humidityLabel = createWeatherMetric("Humidity", "Loading...");
        JLabel windLabel = createWeatherMetric("Wind Speed", "Loading...");
        JLabel conditionLabel = createWeatherMetric("Condition", "Loading...");
        
        weatherContent.add(tempLabel);
        weatherContent.add(humidityLabel);
        weatherContent.add(windLabel);
        weatherContent.add(conditionLabel);
        
        weatherCard.add(weatherContent, BorderLayout.CENTER);
        mainPanel.add(weatherCard);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // MARKET PRICES CARD
        JPanel marketCard = createCardPanel();
        marketCard.setLayout(new BorderLayout(10, 10));
        
        JLabel marketTitle = new JLabel("Market Prices");
        marketTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        marketTitle.setForeground(TEXT_PRIMARY);
        marketTitle.setIcon(iconCache.get("market"));
        marketCard.add(marketTitle, BorderLayout.NORTH);
        
        JPanel pricesPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        pricesPanel.setOpaque(false);
        
        // Add price items with commodity-specific icons
        for (String commodity : COMMODITIES_TO_TRACK) {
            JPanel priceItem = createPriceItem(commodity.toLowerCase(), commodity, "Loading...");
            pricesPanel.add(priceItem);
        }
        
        marketCard.add(pricesPanel, BorderLayout.CENTER);
        
        JButton viewAllPrices = createStyledButton("View All Market Data", ACCENT_COLOR, Color.WHITE, 14);
        marketCard.add(viewAllPrices, BorderLayout.SOUTH);
        mainPanel.add(marketCard);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // SOIL ANALYZER CARD
        JPanel soilCard = createCardPanel();
        soilCard.setLayout(new BorderLayout(10, 10));
        
        JLabel soilTitle = new JLabel("AI Soil Analyzer");
        soilTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        soilTitle.setForeground(TEXT_PRIMARY);
        soilTitle.setIcon(iconCache.get("analysis"));
        soilCard.add(soilTitle, BorderLayout.NORTH);
        
        JPanel soilContent = new JPanel();
        soilContent.setLayout(new BoxLayout(soilContent, BoxLayout.Y_AXIS));
        soilContent.setOpaque(false);
        
        JLabel soilDesc = new JLabel("<html><div style='text-align: center; color: #757575;'>Upload a soil image for AI analysis and get crop recommendations</div></html>");
        soilDesc.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        soilDesc.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        soilContent.add(Box.createRigidArea(new Dimension(0, 10)));
        soilContent.add(soilDesc);
        soilContent.add(Box.createRigidArea(new Dimension(0, 20)));
        
        JButton uploadBtn = createStyledButton("Upload Soil Image", SECONDARY_COLOR, Color.WHITE, 14);
        uploadBtn.setIcon(iconCache.get("upload"));
        uploadBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        soilContent.add(uploadBtn);
        
        soilContent.add(Box.createRigidArea(new Dimension(0, 15)));
        
        JLabel resultLabel = new JLabel("<html><div style='text-align: center; color: #757575; font-style: italic;'>No image selected</div></html>");
        resultLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        resultLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        soilContent.add(resultLabel);
        
        soilCard.add(soilContent, BorderLayout.CENTER);
        mainPanel.add(soilCard);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // AI ASSISTANT CARD
        JPanel assistantCard = createCardPanel();
        assistantCard.setLayout(new BorderLayout(10, 10));
        
        JLabel assistantTitle = new JLabel("Farming Assistant");
        assistantTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        assistantTitle.setForeground(TEXT_PRIMARY);
        assistantTitle.setIcon(iconCache.get("ai"));
        assistantCard.add(assistantTitle, BorderLayout.NORTH);
        
        JTextPane chatPane = new JTextPane();
        chatPane.setContentType("text/html");
        chatPane.setEditable(false);
        chatPane.setBackground(new Color(250, 250, 250));
        chatPane.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        chatPane.setText(getWelcomeMessage());
        
        JScrollPane chatScroll = new JScrollPane(chatPane);
        chatScroll.setPreferredSize(new Dimension(400, 200));
        chatScroll.setBorder(BorderFactory.createLineBorder(new Color(240, 240, 240), 1));
        assistantCard.add(chatScroll, BorderLayout.CENTER);
        
        JPanel inputPanel = new JPanel(new BorderLayout(10, 10));
        inputPanel.setBackground(Color.WHITE);
        
        JTextField inputField = createStyledTextField(20);
        inputField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(SECONDARY_COLOR, 1),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        JButton sendBtn = createStyledButton("Send", PRIMARY_COLOR, Color.WHITE, 14);
        sendBtn.setIcon(iconCache.get("send"));
        JButton voiceBtn = createStyledButton("🎤", SECONDARY_COLOR, Color.WHITE, 14); // Mic emoji
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(voiceBtn);
        buttonPanel.add(sendBtn);
        
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.EAST);
        assistantCard.add(inputPanel, BorderLayout.SOUTH);
        mainPanel.add(assistantCard);

        // Add bottom padding
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        frame.setVisible(true);

        // Initialize data
        initializeDashboard(frame, weatherStat, tempLabel, humidityLabel, windLabel, conditionLabel, 
                          pricesPanel, uploadBtn, resultLabel, sendBtn, voiceBtn, inputField, chatPane);
    }

    // ===== ENHANCED COMPONENT CREATORS =====
    private static JButton createIconButton(String iconKey, String tooltip) {
        JButton button = new JButton(iconCache.get(iconKey));
        button.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(255, 255, 255, 30));
        button.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setToolTipText(tooltip);
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(255, 255, 255, 60));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(255, 255, 255, 30));
            }
        });
        
        return button;
    }

    private static JPanel createStatCard(String iconKey, String title, String value, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue(), 30));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel iconLabel = new JLabel(iconCache.get(iconKey));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLabel.setForeground(TEXT_SECONDARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        valueLabel.setForeground(TEXT_PRIMARY);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        card.add(iconLabel);
        card.add(Box.createRigidArea(new Dimension(0, 5)));
        card.add(titleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 5)));
        card.add(valueLabel);
        
        return card;
    }

    private static JLabel createWeatherMetric(String metric, String value) {
        // Use different emojis based on the metric type - make it final
        final String emoji;
        switch (metric.toLowerCase()) {
            case "temperature": emoji = "🌡️"; break;
            case "humidity": emoji = "💧"; break;
            case "wind speed": emoji = "💨"; break;
            case "condition": emoji = "🌤️"; break;
            default: emoji = "📊";
        }
        
        JLabel label = new JLabel("<html><div style='text-align: center;'>" +
                                "<span style='color: #757575; font-size: 12px;'>" + metric + "</span><br/>" +
                                "<span style='font-weight: bold; font-size: 14px;'>" + value + "</span></div></html>");
        
        // Create a simple icon with the emoji
        label.setIcon(new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
                g2.drawString(emoji, x, y + 20);
                g2.dispose();
            }
            
            @Override public int getIconWidth() { return 24; }
            @Override public int getIconHeight() { return 24; }
        });
        
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalTextPosition(SwingConstants.BOTTOM);
        label.setHorizontalTextPosition(SwingConstants.CENTER);
        return label;
    }

    private static JPanel createPriceItem(String iconKey, String commodity, String price) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        
        JLabel nameLabel = new JLabel(commodity);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nameLabel.setIcon(iconCache.get(iconKey));
        
        JLabel priceLabel = new JLabel(price);
        priceLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        priceLabel.setForeground(PRIMARY_COLOR);
        
        panel.add(nameLabel, BorderLayout.WEST);
        panel.add(priceLabel, BorderLayout.EAST);
        
        return panel;
    }

    private static String getWelcomeMessage() {
        return "<html><body style='font-family: Segoe UI; font-size: 14px; line-height: 1.6;'>" +
               "<div style='color: #2E7D32; font-weight: bold; margin-bottom: 10px;'>AgriMitra Assistant</div>" +
               "<div style='color: #757575; margin-bottom: 15px;'>Hello! I'm your smart farming assistant. I can help you with:</div>" +
               "<div style='color: #424242;'>" +
               "• Crop recommendations based on soil type<br>" +
               "• Weather insights and forecasts<br>" +
               "• Market price trends<br>" +
               "• Pest control advice<br>" +
               "• Farming techniques and best practices<br>" +
               "</div>" +
               "<div style='color: #757575; margin-top: 15px; font-style: italic;'>How can I assist you today?</div>" +
               "</body></html>";
    }

    // ===== INITIALIZATION METHODS =====
    private static void initializeDashboard(JFrame frame, JPanel weatherStat, JLabel tempLabel, 
                                          JLabel humidityLabel, JLabel windLabel, JLabel conditionLabel,
                                          JPanel pricesPanel, JButton uploadBtn, JLabel resultLabel,
                                          JButton sendBtn, JButton voiceBtn, JTextField inputField, 
                                          JTextPane chatPane) {
        // Initialize weather data
        fetchDetailedWeather("Delhi", weatherStat, tempLabel, humidityLabel, windLabel, conditionLabel, frame);
        
        // Initialize market prices
        setupMarketPrices(pricesPanel, frame);
        
        // Setup soil analyzer
        setupSoilAnalyzer(uploadBtn, resultLabel, frame);
        
        // Setup AI assistant
        setupAIAssistant(sendBtn, voiceBtn, inputField, chatPane, frame);
    }

    // ===== ENHANCED WEATHER METHODS =====
    private static void fetchDetailedWeather(String city, JPanel weatherStat, JLabel tempLabel, 
                                           JLabel humidityLabel, JLabel windLabel, JLabel conditionLabel,
                                           JFrame parentFrame) {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            String temp = "", humidity = "", windSpeed = "", description = "";
            
            @Override
            protected Void doInBackground() {
                try {
                    String urlStr = "https://api.openweathermap.org/data/2.5/weather?q=" 
                            + URLEncoder.encode(city, "UTF-8") + "&appid=" + WEATHER_API_KEY + "&units=metric";
                    HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder(); 
                    String line;
                    while((line = br.readLine()) != null) sb.append(line);
                    br.close();
                    
                    String json = sb.toString();
                    temp = extractValue(json, "\"temp\":", ",") + "°C";
                    humidity = extractValue(json, "\"humidity\":", ",") + "%";
                    windSpeed = extractValue(json, "\"speed\":", ",") + " m/s";
                    description = extractValue(json, "\"description\":\"", "\"");
                    
                } catch(Exception e) {
                    temp = "N/A";
                    humidity = "N/A";
                    windSpeed = "N/A";
                    description = "Unable to fetch";
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    // Update weather stat
                    Component[] comps = weatherStat.getComponents();
                    if (comps.length >= 3 && comps[2] instanceof JLabel) {
                        ((JLabel)comps[2]).setText(temp.split("°")[0] + "°C");
                    }
                    
                    // Update detailed labels
                    tempLabel.setText("<html><div style='text-align: center;'>" +
                                    "<span style='color: #757575; font-size: 12px;'>Temperature</span><br/>" +
                                    "<span style='font-weight: bold; font-size: 14px;'>" + temp + "</span></div></html>");
                    
                    humidityLabel.setText("<html><div style='text-align: center;'>" +
                                        "<span style='color: #757575; font-size: 12px;'>Humidity</span><br/>" +
                                        "<span style='font-weight: bold; font-size: 14px;'>" + humidity + "</span></div></html>");
                    
                    windLabel.setText("<html><div style='text-align: center;'>" +
                                    "<span style='color: #757575; font-size: 12px;'>Wind Speed</span><br/>" +
                                    "<span style='font-weight: bold; font-size: 14px;'>" + windSpeed + "</span></div></html>");
                    
                    conditionLabel.setText("<html><div style='text-align: center;'>" +
                                         "<span style='color: #757575; font-size: 12px;'>Condition</span><br/>" +
                                         "<span style='font-weight: bold; font-size: 14px;'>" + description + "</span></div></html>");
                    
                } catch(Exception e) {
                    System.err.println("Error updating weather UI: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    // ===== ENHANCED MARKET PRICES =====
    private static void setupMarketPrices(JPanel pricesPanel, JFrame parentFrame) {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    Map<String, String> simulatedPrices = new HashMap<>();
                    simulatedPrices.put("Wheat", "₹2,100/qtl");
                    simulatedPrices.put("Rice", "₹2,800/qtl");
                    simulatedPrices.put("Maize", "₹1,850/qtl");
                    simulatedPrices.put("Potato", "₹2,500/qtl");
                    simulatedPrices.put("Onion", "₹3,000/qtl");
                    simulatedPrices.put("Tomato", "₹4,500/qtl");

                    // Simulate loading delay
                    Thread.sleep(1000);
                    
                    // Update UI on EDT
                    SwingUtilities.invokeLater(() -> {
                        Component[] components = pricesPanel.getComponents();
                        for (int i = 0; i < components.length && i < COMMODITIES_TO_TRACK.length; i++) {
                            if (components[i] instanceof JPanel) {
                                JPanel panel = (JPanel) components[i];
                                Component[] comps = panel.getComponents();
                                if (comps.length >= 2 && comps[1] instanceof JLabel) {
                                    String commodity = COMMODITIES_TO_TRACK[i];
                                    ((JLabel)comps[1]).setText(simulatedPrices.get(commodity));
                                }
                            }
                        }
                    });
                    
                } catch(Exception e) {
                    // Handle error silently
                }
                return null;
            }
        };
        worker.execute();
    }

    // ===== ENHANCED SOIL ANALYZER =====
    private static void setupSoilAnalyzer(JButton uploadBtn, JLabel resultLabel, JFrame parentFrame) {
        uploadBtn.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select Soil Image");
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Image files", "jpg", "jpeg", "png", "gif"));
            
            int result = fileChooser.showOpenDialog(parentFrame);
            if(result == JFileChooser.APPROVE_OPTION){
                File selectedFile = fileChooser.getSelectedFile();
                resultLabel.setText("<html><div style='text-align: center; color: #757575;'>Analyzing: " + 
                                  selectedFile.getName() + "</div></html>");
                
                SwingWorker<String, Void> worker = new SwingWorker<>() {
                    @Override
                    protected String doInBackground() throws Exception {
                        // Simulate AI processing
                        Thread.sleep(3000);
                        String[] soilTypes = {"Red Soil", "Alluvial Soil", "Black Soil", "Sandy Soil"};
                        String soilDetected = soilTypes[new Random().nextInt(soilTypes.length)];
                        return "<html><div style='text-align: center;'>" +
                               "<div style='color: #2E7D32; font-weight: bold; font-size: 16px;'>" + 
                               "Analysis Complete!</div>" +
                               "<div style='color: #424242; margin-top: 10px;'>" +
                               "Detected: <b>" + soilDetected + "</b><br/>" +
                               "Recommended: " + analyzeSoilAndRecommendCrops(soilDetected) +
                               "</div></div></html>";
                    }
                    
                    @Override
                    protected void done() {
                        try { 
                            resultLabel.setText(get()); 
                        } catch(Exception ex){ 
                            resultLabel.setText("<html><div style='text-align: center; color: #F44336;'>" + 
                                              "Analysis Failed</div></html>"); 
                        }
                    }
                };
                worker.execute();
            }
        });
    }

    private static String analyzeSoilAndRecommendCrops(String soilType){
        switch(soilType){
            case "Red Soil": return "Cotton, Wheat, Paddy";
            case "Alluvial Soil": return "Rice, Sugarcane, Wheat";
            case "Black Soil": return "Cotton, Soybean, Millets";
            case "Sandy Soil": return "Groundnut, Pulses, Millets";
            default: return "Consult agricultural expert";
        }
    }

    // ===== ENHANCED AI ASSISTANT =====
    private static void setupAIAssistant(JButton sendBtn, JButton voiceBtn, JTextField inputField, 
                                       JTextPane chatPane, JFrame parentFrame) {
        
        sendBtn.addActionListener(e -> sendMessage(inputField, chatPane));
        
        voiceBtn.addActionListener(e -> {
            // Simulate voice input
            String[] sampleQuestions = {
                "What crops are best for black soil?",
                "Tell me about today's weather forecast",
                "What are the current market prices for wheat?",
                "How to control pests in tomato crops?",
                "What organic fertilizers should I use?"
            };
            
            String randomQuestion = sampleQuestions[new Random().nextInt(sampleQuestions.length)];
            inputField.setText(randomQuestion);
        });
        
        // Allow pressing Enter to send message
        inputField.addActionListener(e -> sendMessage(inputField, chatPane));
    }

    private static void sendMessage(JTextField inputField, JTextPane chatPane) {
        String userMessage = inputField.getText().trim();
        if (!userMessage.isEmpty()) {
            appendChatMessage(chatPane, "You: " + userMessage, false);
            inputField.setText("");
            
            // Simulate AI thinking
            SwingWorker<String, Void> worker = new SwingWorker<>() {
                @Override
                protected String doInBackground() {
                    try {
                        Thread.sleep(1000 + new Random().nextInt(2000)); // Simulate processing time
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return generateAIResponse(userMessage);
                }
                
                @Override
                protected void done() {
                    try {
                        String response = get();
                        appendChatMessage(chatPane, "AgriMitra: " + response, true);
                    } catch (Exception ex) {
                        appendChatMessage(chatPane, "AgriMitra: Sorry, I encountered an error. Please try again.", true);
                    }
                }
            };
            worker.execute();
        }
    }

    private static void appendChatMessage(JTextPane pane, String message, boolean isAI) {
        try {
            String currentText = pane.getText();
            String newContent;
            
            if (currentText.contains("</body></html>")) {
                // Remove closing tags and append new message
                currentText = currentText.replace("</body></html>", "");
                String messageStyle = isAI ? "style='background: #f8f9fa; padding: 10px; border-radius: 10px; margin: 5px 0;'" : 
                                           "style='background: #e3f2fd; padding: 10px; border-radius: 10px; margin: 5px 0;'";
                newContent = currentText + "<div " + messageStyle + ">" + message + "</div></body></html>";
            } else {
                newContent = "<html><body style='font-family: Segoe UI; font-size: 14px; line-height: 1.4;'>" +
                           "<div style='background: #e3f2fd; padding: 10px; border-radius: 10px; margin: 5px 0;'>" + 
                           message + "</div></body></html>";
            }
            
            pane.setText(newContent);
            pane.setCaretPosition(pane.getDocument().getLength());
        } catch (Exception e) {
            // Fallback to plain text
            System.err.println("Error appending chat message: " + e.getMessage());
        }
    }

    private static String generateAIResponse(String userMessage) {
        String message = userMessage.toLowerCase();
        
        if (message.contains("weather") || message.contains("rain") || message.contains("temperature")) {
            return "Based on current conditions, I recommend checking the weather card above for real-time data. For farming, consider these tips: " +
                   "• Light rain is good for most crops\n" +
                   "• Temperature between 20-30°C is ideal\n" +
                   "• High humidity may require fungicide application";
        }
        else if (message.contains("price") || message.contains("market") || message.contains("cost")) {
            return "Current market trends show stable prices for essential commodities. Check the Market Prices section for detailed information. " +
                   "I recommend selling during peak demand seasons for better returns.";
        }
        else if (message.contains("crop") || message.contains("plant") || message.contains("grow")) {
            return "Crop selection depends on your soil type and season. Based on common practices:\n" +
                   "• Kharif season (June-Sept): Rice, Maize, Cotton\n" +
                   "• Rabi season (Oct-March): Wheat, Barley, Mustard\n" +
                   "• Zaid season (March-June): Watermelon, Cucumber\n" +
                   "Use our Soil Analyzer for personalized recommendations!";
        }
        else if (message.contains("soil") || message.contains("land")) {
            return "Soil health is crucial for good yield. I recommend:\n" +
                   "• Test soil pH regularly (ideal: 6.0-7.5)\n" +
                   "• Use organic compost to improve texture\n" +
                   "• Practice crop rotation\n" +
                   "• Upload a soil image in our analyzer for specific advice";
        }
        else if (message.contains("pest") || message.contains("insect") || message.contains("disease")) {
            return "For pest management, consider integrated approaches:\n" +
                   "• Use neem-based organic pesticides\n" +
                   "• Introduce beneficial insects\n" +
                   "• Practice crop rotation\n" +
                   "• Remove infected plants immediately\n" +
                   "Could you specify the crop and pest type for more targeted advice?";
        }
        else if (message.contains("fertilizer") || message.contains("nutrient")) {
            return "Fertilizer recommendations:\n" +
                   "• Nitrogen: For leafy growth (Urea, CAN)\n" +
                   "• Phosphorus: For root development (DAP, SSP)\n" +
                   "• Potassium: For overall health (MOP)\n" +
                   "• Organic: Compost, vermicompost, green manure\n" +
                   "Always conduct soil testing before application.";
        }
        else if (message.contains("hello") || message.contains("hi") || message.contains("hey")) {
            return "Hello! I'm your AgriMitra assistant. I can help you with weather information, market prices, crop recommendations, soil analysis, and farming best practices. What would you like to know today?";
        }
        else if (message.contains("thank")) {
            return "You're welcome! Remember, successful farming combines traditional wisdom with modern technology. Feel free to ask if you need more assistance!";
        }
        else {
            return "I understand you're asking about farming. I can help you with:\n" +
                   "• Crop planning and selection\n" +
                   "• Weather-based farming decisions\n" +
                   "• Market intelligence\n" +
                   "• Pest and disease management\n" +
                   "• Soil health and fertilization\n" +
                   "Could you please rephrase your question or ask about one of these topics?";
        }
    }

    // ===== UTILITY METHODS =====
    private static String extractValue(String json, String key, String endDelimiter){
        int index = json.indexOf(key);
        if(index == -1) return "N/A";
        index += key.length();
        int endIndex = json.indexOf(endDelimiter, index);
        if(endIndex == -1) endIndex = json.length();
        return json.substring(index, endIndex).replaceAll("[\"}]", "").trim();
    }

    private static void fetchWeatherPreview(String city, JTextArea previewArea) {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            String previewText = "";
            @Override
            protected Void doInBackground() {
                try {
                    String urlStr = "https://api.openweathermap.org/data/2.5/weather?q=" 
                            + URLEncoder.encode(city, "UTF-8") + "&appid=" + WEATHER_API_KEY + "&units=metric";
                    HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder(); String line;
                    while((line = br.readLine()) != null) sb.append(line);
                    br.close();
                    String json = sb.toString();
                    String temp = extractValue(json, "\"temp\":", ",");
                    String desc = extractValue(json, "\"description\":\"", "\"");
                    previewText = temp + "°C, " + desc;
                } catch(Exception e){ 
                    previewText = "Unable to fetch weather data"; 
                }
                return null;
            }
            @Override
            protected void done() { 
                previewArea.setText(previewText); 
            }
        };
        worker.execute();
    }
}
