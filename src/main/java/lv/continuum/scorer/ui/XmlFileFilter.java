package lv.continuum.scorer.ui;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.Set;

class XmlFileFilter extends FileFilter {

    private static final Set<String> EXTENSIONS = Set.of("xml");

    public String getDescription() {
        return "XML Files";
    }

    public boolean accept(File file) {
        if (!file.isDirectory()) {
            var path = file.getAbsolutePath().toLowerCase();
            return EXTENSIONS.stream().anyMatch(e -> path.endsWith("." + e));
        } else {
            return true;
        }
    }
}
