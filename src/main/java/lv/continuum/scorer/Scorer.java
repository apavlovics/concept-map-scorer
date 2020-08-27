package lv.continuum.scorer;

import lombok.extern.slf4j.Slf4j;
import lv.continuum.scorer.common.TranslationException;
import lv.continuum.scorer.common.Translations;
import lv.continuum.scorer.ui.ScorerFrame;

import javax.swing.*;
import java.awt.*;

/**
 * The main class is named {@code Scorer} because Swing uses it as the application name.
 */
@Slf4j
public class Scorer {

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                new ScorerFrame(new Translations());
                log.debug("Created main application window");
            } catch (TranslationException e) {
                log.error("Issue while creating main application window", e);
                JOptionPane.showMessageDialog(
                        null,
                        e.getMessage() + ".",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }
}
