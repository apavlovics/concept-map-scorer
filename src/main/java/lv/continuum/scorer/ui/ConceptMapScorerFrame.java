package lv.continuum.scorer.ui;

import lombok.extern.slf4j.Slf4j;
import lv.continuum.scorer.common.InvalidDataException;
import lv.continuum.scorer.common.InvalidDataException.ErrorCode;
import lv.continuum.scorer.common.Translations;
import lv.continuum.scorer.common.VersionUtils;
import lv.continuum.scorer.domain.ConceptMap;
import lv.continuum.scorer.logic.ConceptMapComparator;
import lv.continuum.scorer.logic.ConceptMapFormatter;
import lv.continuum.scorer.logic.ConceptMapParser;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;

@Slf4j
public class ConceptMapScorerFrame extends JFrame {

    // Width and height of score scroll pane ultimately determine window size
    private static final int SCORE_SCROLL_PANE_WIDTH = 780;
    private static final int SCORE_SCROLL_PANE_HEIGHT = 260;

    private final Translations translations;
    private final ConceptMapParser conceptMapParser;
    private final ConceptMapFormatter conceptMapFormatter;

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

    public ConceptMapScorerFrame(Translations translations) {
        this.translations = translations;
        this.conceptMapParser = new ConceptMapParser();
        this.conceptMapFormatter = new ConceptMapFormatter(translations);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle(translations.format("title", VersionUtils.getVersionNumber()));
        setResizable(false);

        var keyAdapter = new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent evt) {
                textFieldChanged();
            }

            @Override
            public void keyReleased(KeyEvent evt) {
                textFieldChanged();
            }
        };

        studentTextField = new JTextField();
        studentTextField.setName("studentTextField");
        studentTextField.addKeyListener(keyAdapter);

        teacherTextField = new JTextField();
        teacherTextField.setName("teacherTextField");
        teacherTextField.addKeyListener(keyAdapter);

        fileChooser = new JFileChooser(System.getProperty("user.dir"));
        fileChooser.setFileFilter(new XmlFileFilter());

        scoreButton = new JButton(translations.get("score"));
        scoreButton.setName("scoreButton");
        scoreButton.setEnabled(false);
        scoreButton.addActionListener(e -> scoreButtonActionPerformed());

        elementsCheckBox = new JCheckBox(translations.get("method-element-count"));
        elementsCheckBox.setSelected(true);

        closenessIndexesCheckBox = new JCheckBox(translations.get("method-closeness-indexes"));
        importanceIndexesCheckBox = new JCheckBox(translations.get("method-importance-indexes"));
        propositionChainsCheckBox = new JCheckBox(translations.get("method-proposition-chains"));
        errorAnalysisCheckBox = new JCheckBox(translations.get("method-error-analysis"));

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

        scoreTextArea = new JTextArea(translations.get("score-text-default"));
        scoreTextArea.setName("scoreTextArea");
        scoreTextArea.setEditable(false);
        scoreTextArea.setLineWrap(true);
        scoreTextArea.setWrapStyleWord(true);
        scoreTextArea.setEnabled(false);

        var scoreScrollPane = new JScrollPane();
        scoreScrollPane.setViewportView(scoreTextArea);

        var studentBrowseButton = new JButton(translations.get("browse"));
        studentBrowseButton.addActionListener(e -> browseButtonActionPerformed(studentTextField));

        var teacherBrowseButton = new JButton(translations.get("browse"));
        teacherBrowseButton.addActionListener(e -> browseButtonActionPerformed(teacherTextField));

        var studentLabel = new JLabel(translations.get("select-student-concept-map"));
        var teacherLabel = new JLabel(translations.get("select-teacher-concept-map"));

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);

        var studentTextFieldHorizontalGroup = layout.createSequentialGroup()
                .addComponent(studentTextField)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(studentBrowseButton);
        var teacherTextFieldHorizontalGroup = layout.createSequentialGroup()
                .addComponent(teacherTextField)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(teacherBrowseButton);
        var checkBoxesHorizontalGroup = layout.createParallelGroup()
                .addComponent(importanceIndexesCheckBox)
                .addComponent(closenessIndexesCheckBox)
                .addComponent(propositionChainsCheckBox)
                .addComponent(errorAnalysisCheckBox)
                .addComponent(elementsCheckBox);
        var scoreButtonAndCheckBoxesHorizontalGroup = layout.createSequentialGroup()
                .addComponent(scoreButton)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(checkBoxesHorizontalGroup);

        var studentTextFieldVerticalGroup = layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(studentTextField)
                .addComponent(studentBrowseButton);
        var teacherTextFieldVerticalGroup = layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(teacherTextField)
                .addComponent(teacherBrowseButton);
        var checkBoxesVerticalGroup = layout.createSequentialGroup()
                .addComponent(elementsCheckBox)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(closenessIndexesCheckBox)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(importanceIndexesCheckBox)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(propositionChainsCheckBox)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(errorAnalysisCheckBox);
        var scoreButtonAndCheckBoxesVerticalGroup = layout.createParallelGroup()
                .addComponent(scoreButton)
                .addGroup(checkBoxesVerticalGroup);

        layout.setAutoCreateContainerGaps(true);
        layout.setHorizontalGroup(layout.createParallelGroup()
                .addComponent(studentLabel)
                .addGroup(studentTextFieldHorizontalGroup)
                .addComponent(teacherLabel)
                .addGroup(teacherTextFieldHorizontalGroup)
                .addGroup(scoreButtonAndCheckBoxesHorizontalGroup)
                .addComponent(scoreScrollPane, GroupLayout.DEFAULT_SIZE, SCORE_SCROLL_PANE_WIDTH, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(studentLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(studentTextFieldVerticalGroup)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(teacherLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(teacherTextFieldVerticalGroup)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(scoreButtonAndCheckBoxesVerticalGroup)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(scoreScrollPane, GroupLayout.DEFAULT_SIZE, SCORE_SCROLL_PANE_HEIGHT, Short.MAX_VALUE)
        );
        pack();
        setLocationRelativeTo(null); // Center window on screen
        setVisible(true);
    }

    private void scoreButtonActionPerformed() {
        try {
            var studentText = studentTextField.getText();
            var teacherText = teacherTextField.getText();

            ConceptMap studentConceptMap;
            Optional<ConceptMap> teacherConceptMap;
            if (!studentText.isBlank() && !teacherText.isBlank()) {
                studentConceptMap = conceptMapParser.parse(studentText);
                teacherConceptMap = Optional.of(conceptMapParser.parse(teacherText));
            } else if (!studentText.isBlank()) {
                studentConceptMap = conceptMapParser.parse(studentText);
                teacherConceptMap = Optional.empty();
            } else {
                throw new InvalidDataException(ErrorCode.INVALID_FILE);
            }

            var strings = new ArrayList<String>();
            if (elementsCheckBox.isSelected()) {
                strings.add(conceptMapFormatter.formatCounts("student", studentConceptMap));
            }
            teacherConceptMap.ifPresent(tcm -> {
                var comparator = new ConceptMapComparator(studentConceptMap, tcm);
                if (elementsCheckBox.isSelected()) {
                    strings.add(conceptMapFormatter.formatCounts("teacher", tcm));
                }
                if (closenessIndexesCheckBox.isSelected()) {
                    strings.add(conceptMapFormatter.formatSimilarityDegree(
                            "closeness-indexes", comparator::compareUsingClosenessIndexes));
                }
                if (importanceIndexesCheckBox.isSelected()) {
                    strings.add(conceptMapFormatter.formatSimilarityDegree(
                            "importance-indexes", comparator::compareUsingImportanceIndexes));
                }
                if (propositionChainsCheckBox.isSelected()) {
                    strings.add(conceptMapFormatter.formatSimilarityDegree(
                            "proposition-chains", comparator::compareUsingPropositionChains));
                }
                if (errorAnalysisCheckBox.isSelected()) {
                    strings.add(conceptMapFormatter.formatSimilarityDegrees(
                            "error-analysis", comparator::compareUsingErrorAnalysis));
                }
            });
            scoreTextArea.setText(String.join("\n\n", strings));
            scoreTextArea.setEnabled(true);
            log.debug("Scored concept map");
        } catch (InvalidDataException e) {
            log.debug("Issue while scoring concept map", e);
            var message = e.fileName != null ?
                    translations.format(e.errorCode.translationKey, e.fileName) :
                    translations.get(e.errorCode.translationKey);
            JOptionPane.showMessageDialog(
                    this,
                    message,
                    translations.get("error"),
                    JOptionPane.ERROR_MESSAGE
            );
        } catch (Exception e) {
            log.debug("Issue while scoring concept map", e);
            JOptionPane.showMessageDialog(
                    this,
                    translations.get("invalid-file"),
                    translations.get("error"),
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private Optional<String> getSelectedFileName() {
        var state = fileChooser.showOpenDialog(this);
        return state == JFileChooser.APPROVE_OPTION ?
                Optional.of(fileChooser.getSelectedFile().getAbsolutePath()) :
                Optional.empty();
    }

    private void browseButtonActionPerformed(JTextField textField) {
        getSelectedFileName().ifPresent(fileName -> {
            textField.setText(fileName);
            textFieldChanged();
        });
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
            boolean selectedEnabled = !studentText.isBlank() && !teacherText.isBlank();
            checkBoxes.forEach(cb -> {
                if (cb == elementsCheckBox) {
                    cb.setSelected(true);
                } else {
                    cb.setSelected(selectedEnabled);
                }
                cb.setEnabled(selectedEnabled);
            });
        }
        scoreButton.setEnabled(!studentText.isBlank() && checkBoxes.stream().anyMatch(JCheckBox::isSelected));
    }
}
