import javax.swing.*;         // GUI
import javax.swing.border.*;  // borders
import java.awt.*;            // layouts, colors, fonts
import java.awt.event.*;      // for actions listeners 
import java.io.*;             // File reading and writing
import java.util.*;           // Import utility classes (ArrayList, Collections, Scanner)
// this is the one
public class newFlashcard extends JFrame { 

    // FLASHCARD CLASS 
    class Flashcard {
        private String question, answer; // Store question and answer text

        public Flashcard(String q, String a) { // Constructor
            question = q; 
            answer = a;   
        }
        // getters
        public String getQuestion() { return question; } 
        public String getAnswer() { return answer; }     
    }

    //  FLASHCARD MANAGER 
    class FlashcardManager {
        private ArrayList<Flashcard> flashcards = new ArrayList<>(); // Stores all flashcards
        private Flashcard currentFlashcard = null;                   // Tracks the current flashcard
        private final String SAVE_FILE = "flashcards.txt";           // File to save/load flashcards

        // Add a flashcard and save to file
        public void addFlashcard(String q, String a) {
            flashcards.add(new Flashcard(q, a));
            saveFlashcards(); // Save all flashcards to file
        }

        // Remove flashcard at a specific index and save
        public void removeFlashcard(int index) {
            if (index >= 0 && index < flashcards.size()) {
                flashcards.remove(index);
                saveFlashcards();
            }
        }

        // Get a random flashcard from list
        public Flashcard getRandomFlashcard() {
            if (flashcards.isEmpty()) return null; // Return null if no flashcards
            Collections.shuffle(flashcards);        // Shuffle to randomize
            currentFlashcard = flashcards.get(0);   // Set current flashcard
            return currentFlashcard;                // Return current flashcard
        }

        // Get 4 answers for quiz including the correct one
        public java.util.List<String> getRandomAnswers(String correct) {
            ArrayList<String> answers = new ArrayList<>();
            answers.add(correct); // Add correct answer first
            for (Flashcard fc : flashcards) { // Loop through all flashcards
                if (!fc.getAnswer().equals(correct)) {
                    answers.add(fc.getAnswer()); // Add incorrect answers
                }
                if (answers.size() == 4) break; // Stop after 4 answers B/C only 4 choices
            }
            
            Collections.shuffle(answers); // Randomize answer order
            return answers;
        }

        public Flashcard getCurrent() { return currentFlashcard; } // Getter for current flashcard
        public int size() { return flashcards.size(); }            // Get number of flashcards
        public ArrayList<Flashcard> getAll() { return flashcards; }// Get all flashcards

        // Save all flashcards to file
        public void saveFlashcards() {
            try (PrintWriter pw = new PrintWriter(new FileWriter(SAVE_FILE))) {
                for (Flashcard fc : flashcards)
                    pw.println(fc.getQuestion() + "%%" + fc.getAnswer()); // Separate question and answer with %%
            } catch (IOException e) { // error hadnling messahe if the file does not exist
                System.err.println("Error saving flashcards: " + e.getMessage());
            }
        }

        // Load flashcards from file
        public void loadFlashcards() {
            flashcards.clear(); // Clear current list, so there is not duplicates
            File f = new File(SAVE_FILE);
            if (!f.exists()) return; // Do nothing if file does not exist
            try (Scanner sc = new Scanner(f)) {
                while (sc.hasNextLine()) {      // Read each line
                    String line = sc.nextLine();
                    if (line.contains("%%")) {  // Only process lines with separator
                        String[] parts = line.split("%%"); // Split question and answer
                        if (parts.length == 2) {
                            flashcards.add(new Flashcard(parts[0], parts[1])); // Add flashcard
                        }
                    }
                }
            } catch (IOException e) { 
                System.err.println("Error loading flashcards: " + e.getMessage());
            }
        }
    }

    // MEMBER FIELDS 
    private FlashcardManager manager = new FlashcardManager(); 
    private int score = 0, total = 0;                          // Track quiz score and total attempts

    private JTabbedPane tabs = new JTabbedPane(); // Tabs for: Study, Quiz, Add/Remove

    // Study tab
    private JPanel studyPanel = new JPanel(new BorderLayout()); // Panel for study section
    private JLabel studyCardLabel = new JLabel("Click 'Refresh Card' to start", SwingConstants.CENTER); // Flashcard display
    private JButton refreshButton = new JButton("Refresh Card"); 
    private boolean showingQuestion = true;                      // Track if showing question or answer

    // Quiz tab 
    private JPanel quizPanel = new JPanel(new GridBagLayout());  
    private JLabel quizQuestionLabel = new JLabel("");        
    private JRadioButton[] choices = new JRadioButton[4];       // Radio buttons for answers
    private ButtonGroup group = new ButtonGroup();              // Group radio buttons
    private JButton submitButton = new JButton("Submit Answer");
    private JLabel scoreLabel = new JLabel("Score: 0 / 0"); 
    private Flashcard currentQuizCard;                           // for current flashcard to keep track

    // Add/Remove tab
    private JPanel addRemovePanel = new JPanel(new GridBagLayout()); 
    private JTextField addQ = new JTextField(20);                   
    private JTextField addA = new JTextField(20);                   
    private JButton addButton = new JButton("Add Flashcard");      
    private JComboBox<String> removeCombo = new JComboBox<>();      // Dropdown to remove flashcards
    private JButton removeButton = new JButton("Remove Selected"); 

    // CONSTRUCTOR 
    public newFlashcard() {
        setTitle("Flashcard Study Program"); 
        setSize(700, 550);             
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);      
        // loading saved fa;shcards and intializing tabs
        manager.loadFlashcards();            
        setupStudyTab();                     
        setupQuizTab();                     
        setupAddRemoveTab();                 

        tabs.addTab("Study", studyPanel);    // Add tabs to JTabbedPane
        tabs.addTab("Quiz", quizPanel);
        tabs.addTab("Add/Remove", addRemovePanel);

        add(tabs);                           // Add tabs to JFrame
        setVisible(true);                    
    }

    //  STUDY TAB 
    private void setupStudyTab() {
        Font flashcardFont = new Font("Comic Sans MS", Font.PLAIN, 40); // Handwritten-style font
        studyCardLabel.setFont(flashcardFont); // applying the fonts to the flashcard

        studyCardLabel.setHorizontalAlignment(SwingConstants.CENTER);  // Center text horizontally
        studyCardLabel.setVerticalAlignment(SwingConstants.CENTER);    // Center text vertically

        studyCardLabel.setOpaque(false);            
        studyCardLabel.setBackground(Color.WHITE);  
        studyCardLabel.setSize(new Dimension(500,250));                

        studyCardLabel.setBorder(BorderFactory.createCompoundBorder(   // Padding and border
            BorderFactory.createLineBorder(Color.WHITE, 0),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        // Custom panel to draw horizontal lines (like flashdcards)
        JPanel cardPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(173, 216, 230)); // Light blue
                int lineHeight = 30;
                for (int y = 30; y < getHeight(); y += lineHeight) {
                    g.drawLine(10, y, getWidth() - 10, y);
                }
            }
        };
        cardPanel.setOpaque(false);
        cardPanel.add(studyCardLabel, BorderLayout.CENTER);
        cardPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // Buttons below card
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(refreshButton);
        buttonPanel.add(new JLabel("  (Click to see answer)"));

        studyPanel.setLayout(new BorderLayout());
        studyPanel.add(cardPanel, BorderLayout.CENTER);   // Add card panel
        studyPanel.add(buttonPanel, BorderLayout.SOUTH);  // Add buttons below

        refreshButton.addActionListener(e -> showRandomStudyCard()); // waiting for click to change flashcard

        // show answer when card is clickrd 
        studyCardLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (manager.getCurrent() != null) {
                    if (showingQuestion) {
                        studyCardLabel.setText("<html><center>" + manager.getCurrent().getAnswer() + "</center></html>");
                        showingQuestion = false;
                    } else {
                        studyCardLabel.setText("<html><center>" + manager.getCurrent().getQuestion() + "</center></html>");
                        showingQuestion = true;
                    }
                }
            }
        });
    }

    private void showRandomStudyCard() {
        Flashcard fc = manager.getRandomFlashcard(); // Get random flashcard
        if (fc != null) {
            studyCardLabel.setText("<html><center>" + fc.getQuestion() + "</center></html>"); // Show question
            showingQuestion = true; // Track that question is showing
        } else {
            studyCardLabel.setText("<html><center>No flashcards available.<br>Go to Add/Remove tab to add some.</center></html>");
        }
    }

    //  QUIZ TAB 
    private void setupQuizTab() {
        GridBagConstraints c = new GridBagConstraints(); 
        c.insets = new Insets(10,10,10,10); // Spacing around components
        c.gridx=0; c.gridy=0; c.gridwidth=2;
        quizQuestionLabel.setFont(new Font("Arial", Font.BOLD, 18));
        quizPanel.add(quizQuestionLabel, c);

        c.gridwidth=1; c.anchor = GridBagConstraints.WEST;
        for (int i=0; i<4; i++) { // Create 4 radio buttons
            choices[i] = new JRadioButton();
            choices[i].setFont(new Font("Arial", Font.PLAIN, 14));
            group.add(choices[i]);   // Add to button group
            c.gridx = 0;
            c.gridy = 1 + i;
            quizPanel.add(choices[i], c);
        }

        c.gridx=0; c.gridy=5; c.anchor = GridBagConstraints.CENTER;
        quizPanel.add(submitButton, c);   // Add submit button
        
        c.gridx=0; c.gridy=6; 
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 16));
        quizPanel.add(scoreLabel, c);     // Add score label

        submitButton.addActionListener(e -> checkQuizAnswer()); // Check answer on click
        showNextQuizQuestion();          
    }

    private void checkQuizAnswer() {
        if (currentQuizCard == null) {
            JOptionPane.showMessageDialog(this, "No quiz question available. Add flashcards first!");
            return;
        }

        String selected = null;
        for (JRadioButton rb : choices) {
            if (rb.isSelected()) selected = rb.getText(); // Get selected answer
        }
        if (selected == null) {
            JOptionPane.showMessageDialog(this,"Please select an answer.");
            return;
        }

        total++; // Increment total questions
        if (selected.equals(currentQuizCard.getAnswer())) {
            score++; // Increment score if correct
            JOptionPane.showMessageDialog(this,"✓ Correct!");
        } else {
            JOptionPane.showMessageDialog(this,"Wrong! Correct answer: " + currentQuizCard.getAnswer());
        }
        scoreLabel.setText("Score: " + score + " / " + total); // Update score
        showNextQuizQuestion(); // Show next quiz question
    }

    private void showNextQuizQuestion() {
        if (manager.size() < 4) { // Require at least 4 flashcards
            quizQuestionLabel.setText("Add at least 4 flashcards to start quiz.");
            for (JRadioButton rb: choices) {
                rb.setText("");
                rb.setVisible(false);
            }
            submitButton.setEnabled(false);
            return;
        }

        submitButton.setEnabled(true);
        currentQuizCard = manager.getRandomFlashcard(); // Get random quiz question
        quizQuestionLabel.setText("Question: " + currentQuizCard.getQuestion());
        java.util.List<String> answers = manager.getRandomAnswers(currentQuizCard.getAnswer());
        for (int i=0; i<4; i++) {
            choices[i].setText(answers.get(i));
            choices[i].setSelected(false);
            choices[i].setVisible(true);
        }
    }

    //  ADD/REMOVE TAB 
    private void setupAddRemoveTab() {
        GridBagConstraints c = new GridBagConstraints(); // For layout
        c.insets = new Insets(10,10,10,10); 
        c.anchor = GridBagConstraints.WEST;

        // Add question label and text field
        c.gridx=0; c.gridy=0; 
        JLabel qLabel = new JLabel("Question:");
        qLabel.setFont(new Font("Arial", Font.BOLD, 14));
        addRemovePanel.add(qLabel, c);

        c.gridx=1; addRemovePanel.add(addQ,c);

        // Add answer label and text field
        c.gridx=0; c.gridy=1; 
        JLabel aLabel = new JLabel("Answer:");
        aLabel.setFont(new Font("Arial", Font.BOLD, 14));
        addRemovePanel.add(aLabel,c);

        c.gridx=1; addRemovePanel.add(addA,c);

        // Add flashcard button
        c.gridx=0; c.gridy=2; c.gridwidth=2; 
        c.anchor = GridBagConstraints.CENTER;
        addRemovePanel.add(addButton,c);

        addButton.addActionListener(e -> { // Add new flashcard
            String q = addQ.getText().trim();
            String a = addA.getText().trim();
            if (!q.isEmpty() && !a.isEmpty()) {
                manager.addFlashcard(q,a);
                updateRemoveCombo(); // Update remove dropdown
                addQ.setText(""); 
                addA.setText("");
                JOptionPane.showMessageDialog(this, "Flashcard added successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Please enter both question and answer.");
            }
        });

        // Remove flashcard dropdown and button
        c.gridy=3; c.gridwidth=1; 
        c.anchor = GridBagConstraints.WEST;
        JLabel removeLabel = new JLabel("Remove:");
        removeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        addRemovePanel.add(removeLabel,c);

        c.gridx=1; 
        removeCombo.setPreferredSize(new Dimension(250, 25));
        addRemovePanel.add(removeCombo,c);

        c.gridx=0; c.gridy=4; c.gridwidth=2;
        c.anchor = GridBagConstraints.CENTER;
        addRemovePanel.add(removeButton,c);

        removeButton.addActionListener(e -> { // Remove selected flashcard
            int index = removeCombo.getSelectedIndex();
            if (index >= 0) {
                manager.removeFlashcard(index);
                updateRemoveCombo();
                JOptionPane.showMessageDialog(this, "Flashcard removed!");
            } else {
                JOptionPane.showMessageDialog(this, "No flashcard selected.");
            }
        });

        updateRemoveCombo(); // Populate remove dropdown
    }

    private void updateRemoveCombo() {
        removeCombo.removeAllItems(); // Clear dropdown
        for (Flashcard fc : manager.getAll())
            removeCombo.addItem(fc.getQuestion()); // Add all questions
    }

    //  MAIN 
    public static void main(String[] args) {
         new newFlashcard();
    }
}
