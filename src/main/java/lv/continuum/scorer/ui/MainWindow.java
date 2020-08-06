package lv.continuum.scorer.ui;

import lv.continuum.scorer.common.Translations;
import lv.continuum.scorer.domain.ConceptMap;
import lv.continuum.scorer.logic.ConceptMapParser;
import lv.continuum.scorer.logic.ConceptMapScorer;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Set;

public class MainWindow extends JFrame {

    private static final Translations translations = Translations.getInstance();

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

    private final ConceptMapParser conceptMapParser;

    public MainWindow() {
        studentTextField = new JTextField();
        teacherTextField = new JTextField();

        fileChooser = new JFileChooser(System.getProperty("user.dir"));
        fileChooser.setFileFilter(new XmlFileFilter());

        scoreButton = new JButton();
        scoreButton.setText(translations.get("score"));
        scoreButton.setEnabled(false);
        scoreButton.setSize(new Dimension(83, 23));
        scoreButton.addActionListener(this::scoreButtonActionPerformed);

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

        conceptMapParser = new ConceptMapParser();
        initComponents();
    }

    private String getSelectedFileName() {
        int state = fileChooser.showOpenDialog(this);
        return state == JFileChooser.APPROVE_OPTION ? fileChooser.getSelectedFile().getAbsolutePath() : null;
    }

    private void initComponents() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle(translations.get("title"));
        setBounds(new Rectangle(100, 100, 0, 0));
        setResizable(false);

        var studentButton = new JButton();
        var teacherButton = new JButton();
        var studentLabel = new JLabel();
        var teacherLabel = new JLabel();

        var scoreScrollPane = new JScrollPane();
        scoreScrollPane.setViewportView(scoreTextArea);

        var keyAdapter = new KeyAdapter() {

            public void keyPressed(KeyEvent evt) {
                textFieldChanged();
            }

            public void keyReleased(KeyEvent evt) {
                textFieldChanged();
            }
        };
        studentTextField.addKeyListener(keyAdapter);
        teacherTextField.addKeyListener(keyAdapter);

        studentButton.setText(translations.get("browse"));
        studentButton.addActionListener(this::studentButtonActionPerformed);

        teacherButton.setText(translations.get("browse"));
        teacherButton.addActionListener(this::teacherButtonActionPerformed);

        studentLabel.setText(translations.get("select-student-map"));

        teacherLabel.setText(translations.get("select-teacher-map"));

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(
                                        layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                .addGroup(layout.createSequentialGroup()
                                                        .addComponent(scoreScrollPane,
                                                                GroupLayout.DEFAULT_SIZE, 680,
                                                                Short.MAX_VALUE)
                                                        .addContainerGap())
                                                .addGroup(layout.createSequentialGroup()
                                                        .addComponent(studentLabel)
                                                        .addContainerGap())
                                                .addGroup(GroupLayout.Alignment.TRAILING,
                                                        layout.createSequentialGroup()
                                                                .addGroup(layout
                                                                        .createParallelGroup(GroupLayout.Alignment.TRAILING)
                                                                        .addGroup(
                                                                                layout.createSequentialGroup()
                                                                                        .addComponent(
                                                                                                studentTextField,
                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                489,
                                                                                                Short.MAX_VALUE)
                                                                                        .addPreferredGap(
                                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                        .addComponent(
                                                                                                studentButton))
                                                                        .addGroup(
                                                                                layout.createSequentialGroup()
                                                                                        .addGroup(
                                                                                                layout.createParallelGroup(
                                                                                                        javax.swing.GroupLayout.Alignment.LEADING)
                                                                                                        .addGroup(
                                                                                                                layout.createSequentialGroup()
                                                                                                                        .addComponent(
                                                                                                                                teacherLabel)
                                                                                                                        .addGap(183,
                                                                                                                                183,
                                                                                                                                183))
                                                                                                        .addGroup(
                                                                                                                layout.createSequentialGroup()
                                                                                                                        .addComponent(
                                                                                                                                teacherTextField,
                                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                489,
                                                                                                                                Short.MAX_VALUE)
                                                                                                                        .addPreferredGap(
                                                                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                                                                                        .addComponent(
                                                                                                teacherButton)))
                                                                .addGap(96, 96, 96))
                                                .addGroup(layout.createSequentialGroup()
                                                        .addComponent(scoreButton,
                                                                GroupLayout.PREFERRED_SIZE,
                                                                GroupLayout.DEFAULT_SIZE,
                                                                GroupLayout.PREFERRED_SIZE)
                                                        .addGap(18, 18, 18)
                                                        .addGroup(layout.createParallelGroup(
                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                .addComponent(importanceIndexesCheckBox)
                                                                .addComponent(closenessIndexesCheckBox)
                                                                .addComponent(propositionChainsCheckBox)
                                                                .addComponent(errorAnalysisCheckBox)
                                                                .addComponent(elementsCheckBox))
                                                        .addContainerGap())))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(studentLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 14,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(
                                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(studentTextField,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 20,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(studentButton))
                                .addGap(18, 18, 18)
                                .addComponent(teacherLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(
                                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(teacherTextField,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 20,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(teacherButton))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(
                                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(scoreButton,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGroup(layout.createSequentialGroup()
                                                        .addComponent(elementsCheckBox)
                                                        .addPreferredGap(
                                                                javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                        .addComponent(closenessIndexesCheckBox)
                                                        .addPreferredGap(
                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(importanceIndexesCheckBox)
                                                        .addPreferredGap(
                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(propositionChainsCheckBox)
                                                        .addPreferredGap(
                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(errorAnalysisCheckBox)))
                                .addGap(18, 18, 18)
                                .addComponent(scoreScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 257,
                                        Short.MAX_VALUE)
                                .addContainerGap())
        );

        pack();
    }

    private void studentButtonActionPerformed(
            java.awt.event.ActionEvent evt) {
        String fileName = this.getSelectedFileName();
        if (fileName != null) {
            this.studentTextField.setText(fileName);
            this.textFieldChanged();
        }
    }

    private void scoreButtonActionPerformed(
            java.awt.event.ActionEvent evt) {
        try {
            ConceptMap studentMap, teacherMap;
            ConceptMapScorer scorer;
            if (!this.teacherTextField.getText().isEmpty() &&
                    !this.studentTextField.getText().isEmpty()) {
                studentMap = conceptMapParser.parse(this.studentTextField.getText());
                teacherMap = conceptMapParser.parse(this.teacherTextField.getText());
                scorer = new ConceptMapScorer(studentMap, teacherMap);
            } else if (!this.studentTextField.getText().isEmpty()) {
                studentMap = conceptMapParser.parse(this.studentTextField.getText());
                scorer = new ConceptMapScorer(studentMap);
            } else {
                throw new UnsupportedOperationException(
                        translations.get("invalid-file")
                );
            }

            String resultString = "";
            if (this.elementsCheckBox.isSelected()) {
                resultString += scorer.countConceptMapsElements() + "\n\n";
            }
            if (this.closenessIndexesCheckBox.isSelected()) {
                resultString += scorer.compareConceptMapsUsingClosenessIndexes() + "\n\n";
            }
            if (this.importanceIndexesCheckBox.isSelected()) {
                resultString += scorer.compareConceptMapsUsingImportanceIndexes() + "\n\n";
            }
            if (this.propositionChainsCheckBox.isSelected()) {
                resultString += scorer.compareConceptMapsUsingPropositionChains() + "\n\n";
            }
            if (this.errorAnalysisCheckBox.isSelected()) {
                resultString += scorer.compareConceptMapsUsingErrorAnalysis() + "\n\n";
            }
            resultString = resultString.substring(0, resultString.length() - 2);

            this.scoreTextArea.setText(resultString);
            this.scoreTextArea.setEnabled(true);
            System.out.println("Scored concept map");
        } catch (UnsupportedOperationException e) {
            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    translations.get("error"),
                    JOptionPane.ERROR_MESSAGE
            );
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    translations.get("invalid-file"),
                    translations.get("error"),
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void teacherButtonActionPerformed(
            java.awt.event.ActionEvent evt) {
        String fileName = this.getSelectedFileName();
        if (fileName != null) {
            this.teacherTextField.setText(fileName);
            this.textFieldChanged();
        }
    }

    private void textFieldChanged() {
        updateScoreButtonAndCheckBoxes(true);
    }

    private void checkBoxChanged() {
        updateScoreButtonAndCheckBoxes(false);
    }

    private void updateScoreButtonAndCheckBoxes(boolean updateCheckBoxes) {
        if (updateCheckBoxes) {
            boolean checkBoxesSelectedEnabled =
                    !teacherTextField.getText().isEmpty() && !studentTextField.getText().isEmpty();
            checkBoxes.forEach(cb -> {
                if (cb == elementsCheckBox) {
                    cb.setSelected(true);
                } else {
                    cb.setSelected(checkBoxesSelectedEnabled);
                }
                cb.setEnabled(checkBoxesSelectedEnabled);
            });
        }
        boolean scoreButtonEnabled =
                !studentTextField.getText().isEmpty() && checkBoxes.stream().anyMatch(JCheckBox::isSelected);
        scoreButton.setEnabled(scoreButtonEnabled);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                Translations.getInstance();
            } catch (IllegalStateException ise) {
                JOptionPane.showMessageDialog(null, ise.getMessage() + ".", "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }
            new MainWindow().setVisible(true);
        });
        System.out.println("Created main application window");
    }
}
