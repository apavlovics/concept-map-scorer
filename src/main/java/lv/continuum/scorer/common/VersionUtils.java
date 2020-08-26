package lv.continuum.scorer.common;

import org.apache.commons.lang3.StringUtils;

public class VersionUtils {

    private static final String VERSION_NUMBER_DEFAULT = "N/A";

    /**
     * Gets the version number of the application, as defined in <i>pom.xml</i>.
     * <p>
     * This method works only if Maven maven is used and {@code <manifest>} section of the package
     * to be generated contains the following line:
     * <p>
     * {@code <addDefaultImplementationEntries>true</addDefaultImplementationEntries>}
     *
     * @return The version number or {@value #VERSION_NUMBER_DEFAULT}, if it is not known.
     */
    public static String getVersionNumber() {
        var versionNumber = VersionUtils.class.getPackage().getImplementationVersion();
        return StringUtils.isNotBlank(versionNumber) ? versionNumber : VERSION_NUMBER_DEFAULT;
    }
}
