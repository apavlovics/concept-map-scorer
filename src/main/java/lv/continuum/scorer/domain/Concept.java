package lv.continuum.scorer.domain;

import lombok.EqualsAndHashCode;
import lv.continuum.scorer.common.InvalidDataException;
import lv.continuum.scorer.common.InvalidDataException.ErrorCode;
import org.apache.commons.lang3.StringUtils;

@EqualsAndHashCode
public class Concept {

    public final String id;

    @EqualsAndHashCode.Exclude
    public final String name;

    public Concept(String name) throws InvalidDataException {
        if (StringUtils.isBlank(name)) {
            throw new InvalidDataException(ErrorCode.CONCEPT_NO_NAME);
        }
        this.id = deriveId(name);
        this.name = name.trim();
    }

    public static String deriveId(String name) {
        return name.trim().toLowerCase().replace(' ', '-');
    }

    @Override
    public String toString() {
        return name;
    }
}
