package lv.continuum.scorer.ui;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 * @author Andrey Pavlovich
 */
class FileFilterXml extends FileFilter {
    final private String description = "XML Files";
    final private String extensions[] = {"xml"};

    public String getDescription() {
        return this.description;
    }

    public boolean accept(File file) {
        if (file.isDirectory()) return true;
        else {
            String path = file.getAbsolutePath().toLowerCase();
            for (int i = 0, n = extensions.length; i < n; i++) {
                String extension = extensions[i];
                if ((path.endsWith(extension) && (path.charAt(path.length() - extension.length() - 1)) == '.')) return true;
            }
        }
        return false;
    }
}
