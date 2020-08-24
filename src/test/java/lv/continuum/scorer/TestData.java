package lv.continuum.scorer;

import lombok.Value;
import lv.continuum.scorer.common.InvalidDataException;
import lv.continuum.scorer.domain.Concept;
import lv.continuum.scorer.domain.ConceptMap;
import lv.continuum.scorer.domain.Relationship;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Value
public class TestData {

    private TestData() throws InvalidDataException {
        // Do nothing
    }

    private static TestData instance;

    public static TestData getInstance() {
        if (instance == null) {
            synchronized (TestData.class) {
                try {
                    if (instance == null) instance = new TestData();
                } catch (InvalidDataException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return instance;
    }

    public Concept a = new Concept("A");
    public Concept b = new Concept("B");
    public Concept c = new Concept("C");
    public Concept d = new Concept("D");
    public Concept e = new Concept("E");
    public Concept f = new Concept("F");
    public Concept g = new Concept("G");

    public Set<Concept> concepts = Set.of(a, b, c, d, e, f, g);
    public Set<Concept> conceptsOther = Set.of(a, b, c);

    public Set<Relationship> relationshipsWithLevels = Set.of(
            new Relationship(a, b, "contains"),
            new Relationship(a, c, "is example of"),
            new Relationship(b, d, "includes"),
            new Relationship(c, e, "ir piemērs"),
            new Relationship(c, f),
            new Relationship(f, g)
    );
    public Set<Relationship> relationshipsWithCycles = Set.of(
            new Relationship(a, a, "is instance of"),
            new Relationship(a, b, "ir eksemplārs"),
            new Relationship(b, b, "corresponds to"),
            new Relationship(b, c),
            new Relationship(c, a),
            new Relationship(d, e),
            new Relationship(e, f),
            new Relationship(f, e),
            new Relationship(f, g),
            new Relationship(g, e)
    );
    public Set<Relationship> relationshipsOther = Set.of(
            new Relationship(a, b)
    );

    public String fileName = "concept-map.xml";

    public Map<Concept, Set<Concept>> outgoingRelationshipsWithLevels = Map.of(
            a, Set.of(b, c),
            b, Set.of(d),
            c, Set.of(e, f),
            d, Set.of(),
            e, Set.of(),
            f, Set.of(g),
            g, Set.of()
    );
    public Map<Concept, Set<Concept>> outgoingRelationshipsWithCycles = Map.of(
            a, Set.of(a, b),
            b, Set.of(b, c),
            c, Set.of(a),
            d, Set.of(e),
            e, Set.of(f),
            f, Set.of(e, g),
            g, Set.of(e)
    );
    public Map<Concept, Set<Concept>> outgoingRelationshipsOther = Map.of(
            a, Set.of(b),
            b, Set.of(),
            c, Set.of()
    );

    public Map<Concept, Set<Concept>> incomingRelationshipsWithLevels = Map.of(
            a, Set.of(),
            b, Set.of(a),
            c, Set.of(a),
            d, Set.of(b),
            e, Set.of(c),
            f, Set.of(c),
            g, Set.of(f)
    );
    public Map<Concept, Set<Concept>> incomingRelationshipsWithCycles = Map.of(
            a, Set.of(a, c),
            b, Set.of(a, b),
            c, Set.of(b),
            d, Set.of(),
            e, Set.of(d, f, g),
            f, Set.of(e),
            g, Set.of(f)
    );
    public Map<Concept, Set<Concept>> incomingRelationshipsOther = Map.of(
            a, Set.of(),
            b, Set.of(a),
            c, Set.of()
    );

    public Map<Concept, Set<Concept>> allRelationshipsWithLevels = Map.of(
            a, Set.of(b, c),
            b, Set.of(a, d),
            c, Set.of(a, e, f),
            d, Set.of(b),
            e, Set.of(c),
            f, Set.of(c, g),
            g, Set.of(f)
    );
    public Map<Concept, Set<Concept>> allRelationshipsWithCycles = Map.of(
            a, Set.of(a, b, c),
            b, Set.of(a, b, c),
            c, Set.of(a, b),
            d, Set.of(e),
            e, Set.of(d, f, g),
            f, Set.of(e, g),
            g, Set.of(e, f)
    );
    public Map<Concept, Set<Concept>> allRelationshipsOther = Map.of(
            a, Set.of(b),
            b, Set.of(a),
            c, Set.of()
    );

    public ConceptMap conceptMapWithLevels = new ConceptMap(concepts, relationshipsWithLevels, fileName);
    public ConceptMap conceptMapWithCycles = new ConceptMap(concepts, relationshipsWithCycles, fileName);
    public ConceptMap conceptMapOther = new ConceptMap(conceptsOther, relationshipsOther, fileName);

    public Set<List<Concept>> longestPathsWithLevels = Set.of(
            List.of(a, b, d),
            List.of(a, c, e),
            List.of(a, c, f, g)
    );
    public Set<List<Concept>> longestPathsWithCycles = Set.of();
    public Set<List<Concept>> longestPathsOther = Set.of(
            List.of(a, b)
    );
}
