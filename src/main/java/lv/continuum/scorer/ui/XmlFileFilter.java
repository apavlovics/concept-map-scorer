package lv.continuum.scorer.ui;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;
import javax.swing.filechooser.FileFilter;

class XmlFileFilter extends FileFilter {

    private static final List<String> EXTENSIONS = List.of("xml");

    public String getDescription() {
        return "XML Files";
    }

    public boolean accept(File file) {
        var result = true;
        if (!file.isDirectory()) {
            var path = file.getAbsolutePath().toLowerCase();
            result = EXTENSIONS.stream().anyMatch(e -> path.endsWith("." + e));
        }
        return result;
    }
}
