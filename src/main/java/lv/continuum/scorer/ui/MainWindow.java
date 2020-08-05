package lv.continuum.scorer.ui;

import lv.continuum.scorer.common.Translations;
import lv.continuum.scorer.domain.ConceptMap;
import lv.continuum.scorer.logic.ConceptMapScorer;

import javax.swing.*;
import java.awt.*;

/**
 * @author Andrey Pavlovich
 */
public class MainWindow extends javax.swing.JFrame {
    private JFileChooser chooser;
    private XmlFileFilter filter;

    public MainWindow() {
        initComponents();
        this.chooser = new JFileChooser(System.getProperty("user.dir"));
        this.filter = new XmlFileFilter();
        chooser.setFileFilter(filter);
    }

    private String getSelectedFileName() {
        int operation = this.chooser.showOpenDialog(this);
        if (operation == JFileChooser.APPROVE_OPTION) return this.chooser.getSelectedFile().getAbsolutePath();
        return null;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scoreScrollPane = new javax.swing.JScrollPane();
        scoreTextArea = new javax.swing.JTextArea();
        studentTextField = new javax.swing.JTextField();
        teacherTextField = new javax.swing.JTextField();
        studentButton = new javax.swing.JButton();
        teacherButton = new javax.swing.JButton();
        studentLabel = new javax.swing.JLabel();
        teacherLabel = new javax.swing.JLabel();
        scoreButton = new javax.swing.JButton();
        elementsCheckBox = new javax.swing.JCheckBox();
        closenessIndexesCheckBox = new javax.swing.JCheckBox();
        importanceIndexesCheckBox = new javax.swing.JCheckBox();
        propositionChainsCheckBox = new javax.swing.JCheckBox();
        errorAnalysisCheckBox = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle(Translations.getInstance().get("title"));
        setBounds(new java.awt.Rectangle(100, 100, 0, 0));
        setResizable(false);

        scoreTextArea.setColumns(20);
        scoreTextArea.setEditable(false);
        scoreTextArea.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        scoreTextArea.setLineWrap(true);
        scoreTextArea.setRows(5);
        scoreTextArea.setText(Translations.getInstance()
                .get("default-text"));
        scoreTextArea.setWrapStyleWord(true);
        scoreTextArea.setDisabledTextColor(new java.awt.Color(140, 137, 126));
        scoreTextArea.setEnabled(false);
        scoreScrollPane.setViewportView(scoreTextArea);

        studentTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                textFieldsChanged(evt);
            }

            public void keyReleased(java.awt.event.KeyEvent evt) {
                textFieldsChanged(evt);
            }
        });

        teacherTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                textFieldsChanged(evt);
            }

            public void keyReleased(java.awt.event.KeyEvent evt) {
                textFieldsChanged(evt);
            }
        });

        studentButton.setText(
                Translations.getInstance().get("browse"));
        studentButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                studentButtonActionPerformed(evt);
            }
        });

        teacherButton.setText(
                Translations.getInstance().get("browse"));
        teacherButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                teacherButtonActionPerformed(evt);
            }
        });

        studentLabel.setText(Translations.getInstance()
                .get("select-student-map"));

        teacherLabel.setText(Translations.getInstance()
                .get("select-teacher-map"));

        scoreButton.setText(
                Translations.getInstance().get("score"));
        scoreButton.setEnabled(false);
        scoreButton.setMaximumSize(new java.awt.Dimension(83, 23));
        scoreButton.setMinimumSize(new java.awt.Dimension(83, 23));
        scoreButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scoreButtonActionPerformed(evt);
            }
        });

        elementsCheckBox.setSelected(true);
        elementsCheckBox.setText(Translations.getInstance()
                .get("method-element-count"));
        elementsCheckBox.setEnabled(false);
        elementsCheckBox.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                checkBoxStateChanged(evt);
            }
        });

        closenessIndexesCheckBox.setText(Translations.getInstance()
                .get("method-closeness-indexes"));
        closenessIndexesCheckBox.setEnabled(false);
        closenessIndexesCheckBox.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                checkBoxStateChanged(evt);
            }
        });

        importanceIndexesCheckBox.setText(Translations.getInstance()
                .get("method-importance-indexes"));
        importanceIndexesCheckBox.setEnabled(false);
        importanceIndexesCheckBox.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                checkBoxStateChanged(evt);
            }
        });

        propositionChainsCheckBox.setText(Translations.getInstance()
                .get("method-proposition-chains"));
        propositionChainsCheckBox.setEnabled(false);
        propositionChainsCheckBox.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                checkBoxStateChanged(evt);
            }
        });

        errorAnalysisCheckBox.setText(Translations.getInstance()
                .get("method-error-analysis"));
        errorAnalysisCheckBox.setEnabled(false);
        errorAnalysisCheckBox.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                checkBoxStateChanged(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(
                                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(layout.createSequentialGroup()
                                                        .addComponent(scoreScrollPane,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE, 680,
                                                                Short.MAX_VALUE)
                                                        .addContainerGap())
                                                .addGroup(layout.createSequentialGroup()
                                                        .addComponent(studentLabel)
                                                        .addContainerGap())
                                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
                                                        layout.createSequentialGroup()
                                                                .addGroup(layout.createParallelGroup(
                                                                        javax.swing.GroupLayout.Alignment.TRAILING)
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
                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
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
    }// </editor-fold>//GEN-END:initComponents

    private void studentButtonActionPerformed(
            java.awt.event.ActionEvent evt) {//GEN-FIRST:event_studentButtonActionPerformed
        String fileName = this.getSelectedFileName();
        if (fileName != null) {
            this.studentTextField.setText(fileName);
            this.textFieldsChanged(null);
        }
    }//GEN-LAST:event_studentButtonActionPerformed

    private void scoreButtonActionPerformed(
            java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scoreButtonActionPerformed
        try {
            ConceptMap studentMap, teacherMap;
            ConceptMapScorer scorer;
            if (this.teacherTextField.getText().length() > 0 &&
                    this.studentTextField.getText().length() > 0) {
                studentMap = new ConceptMap(this.studentTextField.getText());
                teacherMap = new ConceptMap(this.teacherTextField.getText());
                scorer = new ConceptMapScorer(studentMap, teacherMap);
            } else if (this.studentTextField.getText().length() > 0) {
                studentMap = new ConceptMap(this.studentTextField.getText());
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
            System.out.println("Scored concept map.");
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
    }//GEN-LAST:event_scoreButtonActionPerformed

    private void teacherButtonActionPerformed(
            java.awt.event.ActionEvent evt) {//GEN-FIRST:event_teacherButtonActionPerformed
        String fileName = this.getSelectedFileName();
        if (fileName != null) {
            this.teacherTextField.setText(fileName);
            this.textFieldsChanged(null);
        }
    }//GEN-LAST:event_teacherButtonActionPerformed

    private void textFieldsChanged(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textFieldsChanged
        boolean state = (this.teacherTextField.getText().length() > 0 &&
                this.studentTextField.getText().length() > 0) ? true : false;
        if ((state == true && !this.elementsCheckBox.isEnabled()) ||
                (state == false && this.elementsCheckBox.isEnabled())) {
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

        if (this.studentTextField.getText().length() > 0
                && (this.elementsCheckBox.isSelected() ||
                this.closenessIndexesCheckBox.isSelected() ||
                this.importanceIndexesCheckBox.isSelected() ||
                this.propositionChainsCheckBox.isSelected() ||
                this.errorAnalysisCheckBox.isSelected())) { this.scoreButton.setEnabled(true); } else {
            this.scoreButton.setEnabled(false);
        }
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

    private JCheckBox closenessIndexesCheckBox;
    private JCheckBox elementsCheckBox;
    private JCheckBox errorAnalysisCheckBox;
    private JCheckBox importanceIndexesCheckBox;
    private JCheckBox propositionChainsCheckBox;
    private JButton scoreButton;
    private JScrollPane scoreScrollPane;
    private JTextArea scoreTextArea;
    private JButton studentButton;
    private JLabel studentLabel;
    private JTextField studentTextField;
    private JButton teacherButton;
    private JLabel teacherLabel;
    private JTextField teacherTextField;
}
