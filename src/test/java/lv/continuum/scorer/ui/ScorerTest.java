package lv.continuum.scorer.ui;

import lv.continuum.scorer.common.Translations;
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static lv.continuum.scorer.common.Translations.PROPERTIES_TEST;

class ScorerTest {

    private FrameFixture frameFixture;

    @BeforeAll
    static void beforeAll() {
        FailOnThreadViolationRepaintManager.install();
    }

    @BeforeEach
    void beforeEach() {
        Scorer scorer = GuiActionRunner.execute(() -> new Scorer(new Translations(PROPERTIES_TEST)));
        frameFixture = new FrameFixture(scorer);
        frameFixture.show();
    }

    @Test
    void sanityCheck() {
        frameFixture.requireTitle("Concept Map Scorer");
    }

    @AfterEach
    void afterEach() {
        frameFixture.cleanUp();
    }
}
