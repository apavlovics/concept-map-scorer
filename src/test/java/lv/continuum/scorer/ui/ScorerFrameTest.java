package lv.continuum.scorer.ui;

import lv.continuum.scorer.common.Translations;
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.junit.jupiter.api.*;

@Tag("integration")
class ScorerFrameTest {

    private FrameFixture frameFixture;

    @BeforeAll
    static void beforeAll() {
        FailOnThreadViolationRepaintManager.install();
    }

    @BeforeEach
    void beforeEach() {
        ScorerFrame scorerFrame = GuiActionRunner.execute(() -> new ScorerFrame(new Translations()));
        frameFixture = new FrameFixture(scorerFrame);
        frameFixture.show();
    }

    @Test
    void sanityCheck() {
        frameFixture.requireTitle("Concept Map Scorer N/A");
    }

    @AfterEach
    void afterEach() {
        frameFixture.cleanUp();
    }
}
