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

    public Relationship aa = new Relationship(a, a, "is instance of");
    public Relationship ab = new Relationship(a, b, "contains");
    public Relationship ac = new Relationship(a, c, "is example of");
    public Relationship ad = new Relationship(a, d);
    public Relationship ae = new Relationship(a, e);
    public Relationship af = new Relationship(a, f);
    public Relationship ag = new Relationship(a, g);
    public Relationship ba = new Relationship(b, a);
    public Relationship bb = new Relationship(b, b, "corresponds to");
    public Relationship bc = new Relationship(b, c, "ir eksemplārs");
    public Relationship bd = new Relationship(b, d, "includes");
    public Relationship ca = new Relationship(c, a);
    public Relationship cb = new Relationship(c, b);
    public Relationship cc = new Relationship(c, c);
    public Relationship ce = new Relationship(c, e, "ir piemērs");
    public Relationship cf = new Relationship(c, f);
    public Relationship cg = new Relationship(c, g);
    public Relationship de = new Relationship(d, e);
    public Relationship df = new Relationship(d, f);
    public Relationship dg = new Relationship(d, g);
    public Relationship ee = new Relationship(e, e);
    public Relationship ef = new Relationship(e, f);
    public Relationship eg = new Relationship(e, g);
    public Relationship fe = new Relationship(f, e);
    public Relationship ff = new Relationship(f, f);
    public Relationship fg = new Relationship(f, g);
    public Relationship ge = new Relationship(g, e);
    public Relationship gf = new Relationship(g, f);
    public Relationship gg = new Relationship(g, g);

    public Set<Relationship> relationshipsWithLevels = Set.of(ab, ac, bd, ce, cf, fg);
    public Set<Relationship> relationshipsWithCycles = Set.of(aa, ab, bb, bc, ca, de, ef, fe, fg, ge);
    public Set<Relationship> relationshipsOther = Set.of(ab);

    public String fileNameWithLevels = "concept-map-with-levels.xml";
    public String fileNameWithCycles = "concept-map-with-cycles.xml";
    public String fileNameOther = "concept-map-other.xml";
    public String fileNameInvalid = "concept-map-invalid.xml";
    public String fileNameNotFound = "whatever.xml";

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

    public ConceptMap conceptMapWithLevels = new ConceptMap(concepts, relationshipsWithLevels, fileNameWithLevels);
    public ConceptMap conceptMapWithCycles = new ConceptMap(concepts, relationshipsWithCycles, fileNameWithCycles);
    public ConceptMap conceptMapOther = new ConceptMap(conceptsOther, relationshipsOther, fileNameOther);

    public Map<Relationship, Set<Relationship>> allPathsWithLevels = Map.of(
            ab, Set.of(ab, ad),
            ac, Set.of(ac, ae, af, ag),
            bd, Set.of(ad, bd),
            ce, Set.of(ae, ce),
            cf, Set.of(af, ag, cf, cg),
            fg, Set.of(ag, cg, fg)
    );
    public Map<Relationship, Set<Relationship>> allPathsWithCycles = Map.of(
            aa, Set.of(aa, ab, ac, ba, bb, bc, ca, cb, cc),
            ab, Set.of(aa, ab, ac, ba, bb, bc, ca, cb, cc),
            bb, Set.of(aa, ab, ac, ba, bb, bc, ca, cb, cc),
            bc, Set.of(aa, ab, ac, ba, bb, bc, ca, cb, cc),
            ca, Set.of(aa, ab, ac, ba, bb, bc, ca, cb, cc),
            de, Set.of(de, df, dg),
            ef, Set.of(de, df, dg, ee, ef, eg, fe, ff, fg, ge, gf, gg),
            fe, Set.of(de, df, dg, ee, ef, eg, fe, ff, fg, ge, gf, gg),
            fg, Set.of(de, df, dg, ee, ef, eg, fe, ff, fg, ge, gf, gg),
            ge, Set.of(de, df, dg, ee, ef, eg, fe, ff, fg, ge, gf, gg)
    );
    public Map<Relationship, Set<Relationship>> allPathsOther = Map.of(
            ab, Set.of(ab)
    );

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
