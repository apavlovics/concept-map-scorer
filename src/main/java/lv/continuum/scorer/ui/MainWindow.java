package lv.continuum.scorer.ui;

import lv.continuum.scorer.common.Translations;
import lv.continuum.scorer.domain.ConceptMap;
import lv.continuum.scorer.logic.ConceptMapParser;
import lv.continuum.scorer.logic.ConceptMapScorer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

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

    private final ConceptMapParser conceptMapParser;

    public MainWindow() {
        studentTextField = new JTextField();
        teacherTextField = new JTextField();

        fileChooser = new JFileChooser(System.getProperty("user.dir"));
        fileChooser.setFileFilter(new XmlFileFilter());

        scoreButton = new JButton();
        scoreTextArea = new JTextArea();

        elementsCheckBox = new JCheckBox();
        closenessIndexesCheckBox = new JCheckBox();
        importanceIndexesCheckBox = new JCheckBox();
        propositionChainsCheckBox = new JCheckBox();
        errorAnalysisCheckBox = new JCheckBox();

        conceptMapParser = new ConceptMapParser();
        initComponents();
    }

    private String getSelectedFileName() {
        int state = fileChooser.showOpenDialog(this);
        return state == JFileChooser.APPROVE_OPTION ? fileChooser.getSelectedFile().getAbsolutePath() : null;
    }

    private void initComponents() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle(Translations.getInstance().get("title"));
        setBounds(new Rectangle(100, 100, 0, 0));
        setResizable(false);

        var studentButton = new JButton();
        var teacherButton = new JButton();
        var studentLabel = new JLabel();
        var teacherLabel = new JLabel();

        scoreTextArea.setColumns(20);
        scoreTextArea.setEditable(false);
        scoreTextArea.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        scoreTextArea.setLineWrap(true);
        scoreTextArea.setRows(5);
        scoreTextArea.setText(Translations.getInstance().get("default-text"));
        scoreTextArea.setWrapStyleWord(true);
        scoreTextArea.setDisabledTextColor(new java.awt.Color(140, 137, 126));
        scoreTextArea.setEnabled(false);

        var scoreScrollPane = new JScrollPane();
        scoreScrollPane.setViewportView(scoreTextArea);

        studentTextField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent evt) {
                textFieldsChanged(evt);
            }

            public void keyReleased(KeyEvent evt) {
                textFieldsChanged(evt);
            }
        });

        teacherTextField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent evt) {
                textFieldsChanged(evt);
            }

            public void keyReleased(KeyEvent evt) {
                textFieldsChanged(evt);
            }
        });

        studentButton.setText(Translations.getInstance().get("browse"));
        studentButton.addActionListener(this::studentButtonActionPerformed);

        teacherButton.setText(Translations.getInstance().get("browse"));
        teacherButton.addActionListener(this::teacherButtonActionPerformed);

        studentLabel.setText(Translations.getInstance().get("select-student-map"));

        teacherLabel.setText(Translations.getInstance().get("select-teacher-map"));

        scoreButton.setText(Translations.getInstance().get("score"));
        scoreButton.setEnabled(false);
        scoreButton.setMaximumSize(new java.awt.Dimension(83, 23));
        scoreButton.setMinimumSize(new java.awt.Dimension(83, 23));
        scoreButton.addActionListener(this::scoreButtonActionPerformed);

        elementsCheckBox.setSelected(true);
        elementsCheckBox.setText(Translations.getInstance().get("method-element-count"));
        elementsCheckBox.setEnabled(false);
        elementsCheckBox.addChangeListener(this::checkBoxStateChanged);

        closenessIndexesCheckBox.setText(Translations.getInstance().get("method-closeness-indexes"));
        closenessIndexesCheckBox.setEnabled(false);
        closenessIndexesCheckBox.addChangeListener(this::checkBoxStateChanged);

        importanceIndexesCheckBox.setText(Translations.getInstance().get("method-importance-indexes"));
        importanceIndexesCheckBox.setEnabled(false);
        importanceIndexesCheckBox.addChangeListener(this::checkBoxStateChanged);

        propositionChainsCheckBox.setText(Translations.getInstance().get("method-proposition-chains"));
        propositionChainsCheckBox.setEnabled(false);
        propositionChainsCheckBox.addChangeListener(this::checkBoxStateChanged);

        errorAnalysisCheckBox.setText(Translations.getInstance().get("method-error-analysis"));
        errorAnalysisCheckBox.setEnabled(false);
        errorAnalysisCheckBox.addChangeListener(this::checkBoxStateChanged);

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
            this.textFieldsChanged(null);
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
                        Translations.getInstance().get("invalid-file")
                );
            }

            String resultString = new String();
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
                    Translations.getInstance().get("error"),
                    JOptionPane.ERROR_MESSAGE
            );
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    Translations.getInstance().get("invalid-file"),
                    Translations.getInstance().get("error"),
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void teacherButtonActionPerformed(
            java.awt.event.ActionEvent evt) {
        String fileName = this.getSelectedFileName();
        if (fileName != null) {
            this.teacherTextField.setText(fileName);
            this.textFieldsChanged(null);
        }
    }

    private void textFieldsChanged(java.awt.event.KeyEvent evt) {
        boolean state = this.teacherTextField.getText().length() > 0 &&
                this.studentTextField.getText().length() > 0;
        if ((state && !this.elementsCheckBox.isEnabled()) ||
                (!state && this.elementsCheckBox.isEnabled())) {
            this.elementsCheckBox.setEnabled(state);
            this.closenessIndexesCheckBox.setEnabled(state);
            this.importanceIndexesCheckBox.setEnabled(state);
            this.propositionChainsCheckBox.setEnabled(state);
            this.errorAnalysisCheckBox.setEnabled(state);

            this.elementsCheckBox.setSelected(true);
            this.closenessIndexesCheckBox.setSelected(state);
            this.importanceIndexesCheckBox.setSelected(state);
            this.propositionChainsCheckBox.setSelected(state);
            this.errorAnalysisCheckBox.setSelected(state);
        }

        this.scoreButton.setEnabled(this.studentTextField.getText().length() > 0
                && (this.elementsCheckBox.isSelected() ||
                this.closenessIndexesCheckBox.isSelected() ||
                this.importanceIndexesCheckBox.isSelected() ||
                this.propositionChainsCheckBox.isSelected() ||
                this.errorAnalysisCheckBox.isSelected()));
    }//GEN-LAST:event_textFieldsChanged

    private void checkBoxStateChanged(
            javax.swing.event.ChangeEvent evt) {
        this.textFieldsChanged(null);
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
