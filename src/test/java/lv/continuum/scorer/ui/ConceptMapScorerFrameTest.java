package lv.continuum.scorer.ui;

import lv.continuum.scorer.common.Translations;
import org.assertj.swing.core.KeyPressInfo;
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.junit.jupiter.api.*;

import java.awt.event.KeyEvent;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

@Tag("integration")
class ConceptMapScorerFrameTest {

    private static final KeyPressInfo ENTER = KeyPressInfo.keyCode(KeyEvent.VK_ENTER);

    private FrameFixture frameFixture;

    @BeforeAll
    static void beforeAll() {
        FailOnThreadViolationRepaintManager.install();
    }

    @BeforeEach
    void beforeEach() {
        var conceptMapScorerFrame = GuiActionRunner.execute(() -> new ConceptMapScorerFrame(new Translations()));
        frameFixture = new FrameFixture(conceptMapScorerFrame);
        frameFixture.show();
    }

    @Test
    void happyPath() {
        frameFixture.requireTitle("Concept Map Scorer N/A");

        var scoreButton = frameFixture.button("scoreButton");
        var scoreTextArea = frameFixture.textBox("scoreTextArea");
        var studentTextField = frameFixture.textBox("studentTextField");
        var teacherTextField = frameFixture.textBox("teacherTextField");

        scoreButton
                .requireDisabled();
        scoreTextArea
                .requireDisabled()
                .requireNotEditable();

        var scoreTextBefore = scoreTextArea.text();

        studentTextField
                .setText("src/test/resources/samples/similar-map-1.xml")
                .pressAndReleaseKey(ENTER); // Trigger key listener event
        teacherTextField
                .setText("src/test/resources/samples/similar-map-2.xml")
                .pressAndReleaseKey(ENTER); // Trigger key listener event
        scoreButton
                .requireEnabled()
                .click();
        scoreTextArea
                .requireEnabled()
                .requireNotEditable();

        var scoreTextAfter = scoreTextArea.text();
        assertNotEquals(scoreTextBefore, scoreTextAfter);
    }

    @AfterEach
    void afterEach() {
        frameFixture.cleanUp();
    }
}
