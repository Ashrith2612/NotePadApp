import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class NotepadApp extends JFrame implements ActionListener {

    private JTextArea textArea;
    private JFileChooser fileChooser;
    private File currentFile = null;
    private boolean changed = false;

    public NotepadApp() {

        setTitle("Notepad - Untitled");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        textArea = new JTextArea();
        textArea.setFont(new Font("Serif", Font.PLAIN, 18));

        textArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { changed = true; }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { changed = true; }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { changed = true; }
        });

        add(new JScrollPane(textArea));

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");

        JMenuItem newFile = new JMenuItem("New File");
        JMenuItem saveFile = new JMenuItem("Save");
        JMenuItem saveAsFile = new JMenuItem("Save As");
        JMenuItem deleteFile = new JMenuItem("Delete File");
        JMenuItem exitApp = new JMenuItem("Exit");

        newFile.addActionListener(this);
        saveFile.addActionListener(this);
        saveAsFile.addActionListener(this);
        deleteFile.addActionListener(this);
        exitApp.addActionListener(this);

        fileMenu.add(newFile);
        fileMenu.add(saveFile);
        fileMenu.add(saveAsFile);
        fileMenu.add(deleteFile);
        fileMenu.addSeparator();
        fileMenu.add(exitApp);

        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt"));

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                exitApplication();
            }
        });

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        switch (e.getActionCommand()) {

            case "New File":
                newFile();
                break;

            case "Save":
                saveFile();
                break;

            case "Save As":
                saveFileAs();
                break;

            case "Delete File":
                deleteFile();
                break;

            case "Exit":
                exitApplication();
                break;
        }
    }

    // Ask user whether to save changes
    private boolean confirmSave() {
        if (!changed)
            return true;

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Do you want to save this file?",
                "Save Confirmation",
                JOptionPane.YES_NO_CANCEL_OPTION
        );

        if (choice == JOptionPane.CANCEL_OPTION)
            return false;

        if (choice == JOptionPane.YES_OPTION)
            saveFile();

        return true;
    }

    // New file logic
    private void newFile() {
        if (!confirmSave())
            return;

        textArea.setText("");
        currentFile = null;
        changed = false;
        setTitle("Notepad - Untitled");
    }

    // Save file
    private void saveFile() {
        if (currentFile == null) {
            saveFileAs();
            return;
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(currentFile))) {
            textArea.write(bw);
            changed = false;
            setTitle("Notepad - " + currentFile.getName());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving file!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Save As (always asks for name)
    private void saveFileAs() {
        int choice = fileChooser.showSaveDialog(this);

        if (choice == JFileChooser.APPROVE_OPTION) {

            currentFile = fileChooser.getSelectedFile();

            if (!currentFile.getName().endsWith(".txt")) {
                currentFile = new File(currentFile.getAbsolutePath() + ".txt");
            }

            saveFile();
        }
    }

    // Delete file → Move to Windows Recycle Bin
    private void deleteFile() {
        if (currentFile == null) {
            JOptionPane.showMessageDialog(this,
                    "There is no saved file to delete.",
                    "Delete Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this file?",
                "Delete File",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {

            try {
                String cmd = "cmd /c move \"" + currentFile.getAbsolutePath() +
                        "\" %USERPROFILE%\\Recycle.Bin";

                Process p = Runtime.getRuntime().exec(cmd);
                p.waitFor();

                JOptionPane.showMessageDialog(this, "File moved to Recycle Bin.");

                textArea.setText("");
                currentFile = null;
                changed = false;
                setTitle("Notepad - Untitled");

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Failed to delete file.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exitApplication() {
        if (confirmSave())
            System.exit(0);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(NotepadApp::new);
    }
}
