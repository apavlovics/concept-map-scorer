package lv.continuum.scorer.ui;

import lv.continuum.scorer.common.Translations;
import lv.continuum.scorer.logic.ConceptMapParser;
import lv.continuum.scorer.logic.ConceptMapScorer;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Set;

public class MainWindow extends JFrame {

    private final JTextField studentTextField;
    private final JTextField teacherTextField;
    private final JFileChooser fileChooser;
    private final JButton scoreButton;
    private final JTextArea scoreTextArea;

    private final JCheckBox elementsCheckBox;
    private final JCheckBox closenessIndexesCheckBox;
    private final JCheckBox importanceIndexesCheckBox;
    private final JCheckBox propositionChainsCheckBox;
    private final JCheckBox errorAnalysisCheckBox;
    private final Set<JCheckBox> checkBoxes;

    private final Translations translations = Translations.getInstance();
    private final ConceptMapParser conceptMapParser = new ConceptMapParser();

    public MainWindow() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle(translations.get("title"));
        setBounds(new Rectangle(100, 100, 0, 0));
        setResizable(false);

        var keyAdapter = new KeyAdapter() {

            public void keyPressed(KeyEvent evt) {
                textFieldChanged();
            }

            public void keyReleased(KeyEvent evt) {
                textFieldChanged();
            }
        };

        studentTextField = new JTextField();
        studentTextField.addKeyListener(keyAdapter);

        teacherTextField = new JTextField();
        teacherTextField.addKeyListener(keyAdapter);

        fileChooser = new JFileChooser(System.getProperty("user.dir"));
        fileChooser.setFileFilter(new XmlFileFilter());

        scoreButton = new JButton();
        scoreButton.setText(translations.get("score"));
        scoreButton.setEnabled(false);
        scoreButton.setSize(new Dimension(83, 23));
        scoreButton.addActionListener(e -> scoreButtonActionPerformed());

        elementsCheckBox = new JCheckBox();
        elementsCheckBox.setText(translations.get("method-element-count"));
        elementsCheckBox.setSelected(true);

        closenessIndexesCheckBox = new JCheckBox();
        closenessIndexesCheckBox.setText(translations.get("method-closeness-indexes"));

        importanceIndexesCheckBox = new JCheckBox();
        importanceIndexesCheckBox.setText(translations.get("method-importance-indexes"));

        propositionChainsCheckBox = new JCheckBox();
        propositionChainsCheckBox.setText(translations.get("method-proposition-chains"));

        errorAnalysisCheckBox = new JCheckBox();
        errorAnalysisCheckBox.setText(translations.get("method-error-analysis"));

        checkBoxes = Set.of(
                elementsCheckBox,
                closenessIndexesCheckBox,
                importanceIndexesCheckBox,
                propositionChainsCheckBox,
                errorAnalysisCheckBox
        );
        checkBoxes.forEach(cb -> {
            cb.setEnabled(false);
            cb.addChangeListener(e -> checkBoxChanged());
        });

        scoreTextArea = new JTextArea();
        scoreTextArea.setColumns(20);
        scoreTextArea.setEditable(false);
        scoreTextArea.setLineWrap(true);
        scoreTextArea.setRows(5);
        scoreTextArea.setText(translations.get("default-text"));
        scoreTextArea.setWrapStyleWord(true);
        scoreTextArea.setEnabled(false);

        layoutComponents();
    }

    private void layoutComponents() {
        var scoreScrollPane = new JScrollPane();
        scoreScrollPane.setViewportView(scoreTextArea);

        var studentButton = new JButton();
        studentButton.setText(translations.get("browse"));
        studentButton.addActionListener(e -> studentButtonActionPerformed());

        var teacherButton = new JButton();
        teacherButton.setText(translations.get("browse"));
        teacherButton.addActionListener(e -> teacherButtonActionPerformed());

        var studentLabel = new JLabel();
        studentLabel.setText(translations.get("select-student-map"));

        var teacherLabel = new JLabel();
        teacherLabel.setText(translations.get("select-teacher-map"));

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);

        var studentLabelGroup = layout.createSequentialGroup()
                .addComponent(studentLabel);
        var studentTextFieldGroup = layout.createSequentialGroup()
                .addComponent(studentTextField, GroupLayout.DEFAULT_SIZE, 489, Short.MAX_VALUE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(studentButton);
        var teacherLabelGroup = layout.createSequentialGroup()
                .addComponent(teacherLabel);
        var teacherTextFieldGroup = layout.createSequentialGroup()
                .addComponent(teacherTextField, GroupLayout.DEFAULT_SIZE, 489, Short.MAX_VALUE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(teacherButton);
        var checkBoxesGroup = layout.createParallelGroup()
                .addComponent(importanceIndexesCheckBox)
                .addComponent(closenessIndexesCheckBox)
                .addComponent(propositionChainsCheckBox)
                .addComponent(errorAnalysisCheckBox)
                .addComponent(elementsCheckBox);
        var scoreButtonAndCheckBoxesGroup = layout.createSequentialGroup()
                .addComponent(scoreButton)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(checkBoxesGroup);
        var scoreScrollPaneGroup = layout.createSequentialGroup()
                .addComponent(scoreScrollPane, GroupLayout.DEFAULT_SIZE, 680, Short.MAX_VALUE);

        layout.setAutoCreateContainerGaps(true);
        layout.setHorizontalGroup(
                layout.createParallelGroup()
                        .addGroup(scoreScrollPaneGroup)
                        .addGroup(studentLabelGroup)
                        .addGroup(teacherLabelGroup)
                        .addGroup(studentTextFieldGroup)
                        .addGroup(teacherTextFieldGroup)
                        .addGroup(scoreButtonAndCheckBoxesGroup)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup()
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(studentLabel)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(
                                        layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                .addComponent(studentTextField)
                                                .addComponent(studentButton))
                                .addGap(18)
                                .addComponent(teacherLabel)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(
                                        layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                .addComponent(teacherTextField)
                                                .addComponent(teacherButton))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(
                                        layout.createParallelGroup()
                                                .addComponent(scoreButton)
                                                .addGroup(layout.createSequentialGroup()
                                                        .addComponent(elementsCheckBox)
                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                        .addComponent(closenessIndexesCheckBox)
                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(importanceIndexesCheckBox)
                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(propositionChainsCheckBox)
                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(errorAnalysisCheckBox)))
                                .addGap(18)
                                .addComponent(scoreScrollPane, GroupLayout.DEFAULT_SIZE, 257, Short.MAX_VALUE)
                                .addContainerGap())
        );
        pack();
    }

    private void scoreButtonActionPerformed() {
        try {
            var studentText = studentTextField.getText();
            var teacherText = teacherTextField.getText();

            ConceptMapScorer scorer;
            if (!studentText.isEmpty() && !teacherText.isEmpty()) {
                var studentMap = conceptMapParser.parse(studentText);
                var teacherMap = conceptMapParser.parse(teacherText);
                scorer = new ConceptMapScorer(studentMap, teacherMap);
            } else if (!studentText.isEmpty()) {
                var studentMap = conceptMapParser.parse(studentText);
                scorer = new ConceptMapScorer(studentMap);
            } else {
                throw new UnsupportedOperationException(translations.get("invalid-file"));
            }

            var sb = new StringBuilder();
            if (elementsCheckBox.isSelected()) {
                sb.append(scorer.countConceptMapsElements()).append("\n\n");
            }
            if (closenessIndexesCheckBox.isSelected()) {
                sb.append(scorer.compareConceptMapsUsingClosenessIndexes()).append("\n\n");
            }
            if (importanceIndexesCheckBox.isSelected()) {
                sb.append(scorer.compareConceptMapsUsingImportanceIndexes()).append("\n\n");
            }
            if (propositionChainsCheckBox.isSelected()) {
                sb.append(scorer.compareConceptMapsUsingPropositionChains()).append("\n\n");
            }
            if (errorAnalysisCheckBox.isSelected()) {
                sb.append(scorer.compareConceptMapsUsingErrorAnalysis()).append("\n\n");
            }
            var score = sb.substring(0, sb.length() - 2);

            scoreTextArea.setText(score);
            scoreTextArea.setEnabled(true);
            System.out.println("Scored concept map");
        } catch (UnsupportedOperationException uoe) {
            JOptionPane.showMessageDialog(
                    this,
                    uoe.getMessage(),
                    translations.get("error"),
                    JOptionPane.ERROR_MESSAGE
            );
        } catch (IOException | ParserConfigurationException | SAXException e) {
            JOptionPane.showMessageDialog(
                    this,
                    translations.get("invalid-file"),
                    translations.get("error"),
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private String getSelectedFileName() {
        var state = fileChooser.showOpenDialog(this);
        return state == JFileChooser.APPROVE_OPTION ? fileChooser.getSelectedFile().getAbsolutePath() : null;
    }

    private void studentButtonActionPerformed() {
        String fileName = getSelectedFileName();
        if (fileName != null) {
            studentTextField.setText(fileName);
            textFieldChanged();
        }
    }

    private void teacherButtonActionPerformed() {
        String fileName = getSelectedFileName();
        if (fileName != null) {
            teacherTextField.setText(fileName);
            textFieldChanged();
        }
    }

    private void textFieldChanged() {
        updateScoreButtonAndCheckBoxes(true);
    }

    private void checkBoxChanged() {
        updateScoreButtonAndCheckBoxes(false);
    }

    private void updateScoreButtonAndCheckBoxes(boolean updateCheckBoxes) {
        var studentText = studentTextField.getText();
        var teacherText = teacherTextField.getText();
        if (updateCheckBoxes) {
            boolean selectedEnabled = !studentText.isEmpty() && !teacherText.isEmpty();
            checkBoxes.forEach(cb -> {
                if (cb == elementsCheckBox) {
                    cb.setSelected(true);
                } else {
                    cb.setSelected(selectedEnabled);
                }
                cb.setEnabled(selectedEnabled);
            });
        }
        scoreButton.setEnabled(!studentText.isEmpty() && checkBoxes.stream().anyMatch(JCheckBox::isSelected));
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                Translations.getInstance();
                new MainWindow().setVisible(true);
                System.out.println("Created main application window");
            } catch (IllegalStateException ise) {
                JOptionPane.showMessageDialog(
                        null,
                        ise.getMessage() + ".",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }
}
