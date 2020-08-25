package lv.continuum.scorer.ui;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XmlFileFilterTest {

    private final XmlFileFilter xmlFileFilter = new XmlFileFilter();

    @Test
    void getDescription() {
        assertTrue(StringUtils.isNotBlank(xmlFileFilter.getDescription()));
    }

    @Test
    void accept() {
        String[] fileNames = {
                "test.xml",
                "Some File.XML",
                "files/other-test-file.xml"
        };
        for (var fileName : fileNames) {
            assertTrue(xmlFileFilter.accept(new File(fileName)));
        }
    }

    @Test
    void notAccept() {
        String[] fileNames = {
                "test.doc",
                "Some File XML",
                "files/other-test-file.xmls"
        };
        for (var fileName : fileNames) {
            assertFalse(xmlFileFilter.accept(new File(fileName)));
        }
    }
}
