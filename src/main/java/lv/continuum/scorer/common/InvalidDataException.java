package lv.continuum.scorer.common;

import lombok.RequiredArgsConstructor;

public class InvalidDataException extends Exception {

    public final ErrorCode errorCode;
    public final String fileName;

    public InvalidDataException(ErrorCode errorCode, String fileName) {
        super(errorCode.name());
        this.errorCode = errorCode;
        this.fileName = fileName;
    }

    public InvalidDataException(ErrorCode errorCode) {
        this(errorCode, null);
    }

    @RequiredArgsConstructor
    public enum ErrorCode {
        INVALID_FILE("invalid-file"),
        INVALID_XML("invalid-xml"),
        CONCEPT_NO_NAME("concept-no-name"),
        CONCEPT_MAP_NO_CONCEPTS("concept-map-no-concepts"),
        CONCEPT_MAP_DUPLICATE_CONCEPTS("concept-map-duplicate-concepts"),
        CONCEPT_MAP_NO_RELATIONSHIPS("concept-map-no-relationships"),
        CONCEPT_MAP_INVALID_RELATIONSHIP("concept-map-invalid-relationship"),
        DIFFERENT_CONCEPTS_IMPORTANCE_INDEXES("different-concepts-importance-indexes"),
        DIFFERENT_CONCEPTS_PROPOSITION_CHAINS("different-concepts-proposition-chains"),
        DIFFERENT_CONCEPTS_ERROR_ANALYSIS("different-concepts-error-analysis"),
        CYCLES_PROPOSITION_CHAINS("cycles-proposition-chains");

        /**
         * Translation key intended to be passed to {@link Translations} instance.
         */
        public final String translationKey;
    }
}
