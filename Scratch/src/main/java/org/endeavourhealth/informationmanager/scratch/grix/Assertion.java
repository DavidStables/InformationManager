package org.endeavourhealth.informationmanager.scratch.grix;

public class Assertion {
    private Grix grix;
    private String firstConcept;
    private String relationship;
    private boolean reverse;

    public Assertion(Grix grix, String firstConcept) {
        this.grix = grix;
        this.reverse = false;
        this.firstConcept = firstConcept;
    }

    public Assertion has(String relationshipId) {
        this.relationship = relationshipId;
        return this;
    }

    public Assertion is(String relationshipId) {
        this.relationship = relationshipId;
        this.reverse = true;
        return this;
    }

    public Relation of(String secondConcept) {
        Node source = grix.getNodeWithCreate(reverse ? secondConcept : firstConcept);
        Node target = grix.getNodeWithCreate(reverse ? firstConcept : secondConcept);

        grix.log("Assert: " + source.getId() + " -- " + relationship + " --> " + target.getId());

        return new Relation(grix, source, relationship, target);
    }
}
