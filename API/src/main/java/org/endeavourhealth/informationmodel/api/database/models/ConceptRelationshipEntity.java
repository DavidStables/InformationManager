package org.endeavourhealth.informationmodel.api.database.models;

import org.endeavourhealth.informationmodel.api.database.PersistenceManager;
import org.endeavourhealth.informationmodel.api.json.JsonConceptRelationship;

import javax.persistence.*;
import javax.persistence.criteria.*;
import java.util.List;

@Entity
@Table(name = "concept_relationship", schema = "information_model")
public class ConceptRelationshipEntity {
    private Long id;
    private long sourceConcept;
    private long targetConcept;
    private String targetLabel;
    private Long relationshipType;
    private Integer order;

    @Id
    @Column(name = "id", nullable = false)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Basic
    @Column(name = "source_concept", nullable = false)
    public long getSourceConcept() {
        return sourceConcept;
    }

    public void setSourceConcept(long sourceConcept) {
        this.sourceConcept = sourceConcept;
    }

    @Basic
    @Column(name = "target_concept", nullable = false)
    public long getTargetConcept() {
        return targetConcept;
    }

    public void setTargetConcept(long targetConcept) {
        this.targetConcept = targetConcept;
    }

    @Basic
    @Column(name = "target_label", nullable = true, length = 125)
    public String getTargetLabel() {
        return targetLabel;
    }

    public void setTargetLabel(String targetLabel) {
        this.targetLabel = targetLabel;
    }

    @Basic
    @Column(name = "relationship_type", nullable = true)
    public Long getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(Long relationshipType) {
        this.relationshipType = relationshipType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConceptRelationshipEntity that = (ConceptRelationshipEntity) o;

        if (id != that.id) return false;
        if (sourceConcept != that.sourceConcept) return false;
        if (targetConcept != that.targetConcept) return false;
        if (targetLabel != null ? !targetLabel.equals(that.targetLabel) : that.targetLabel != null) return false;
        if (relationshipType != null ? !relationshipType.equals(that.relationshipType) : that.relationshipType != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int)(sourceConcept ^ (sourceConcept >>> 32));
        result = 31 * result + (int) (sourceConcept ^ (sourceConcept >>> 32));
        result = 31 * result + (int) (targetConcept ^ (targetConcept >>> 32));
        result = 31 * result + (targetLabel != null ? targetLabel.hashCode() : 0);
        result = 31 * result + (relationshipType != null ? relationshipType.hashCode() : 0);
        return result;
    }

    @Basic
    @Column(name = "order", nullable = true)
    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }


    public static List<Object[]> getConceptsRelationships(Integer conceptId) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();
        Query query = entityManager.createQuery(
                "Select cr.id, sc.id, sc.name, sc.description, sc.shortName, " +
                        "c.id, c.name, c.description, c.shortName, " +
                        "tc.id, tc.name, tc.description, tc.shortName " +
                        "from ConceptRelationshipEntity cr " +
                        "join ConceptEntity c on c.id = cr.relationshipType " +
                        "join ConceptEntity sc on sc.id = cr.sourceConcept " +
                        "join ConceptEntity tc on tc.id = cr.targetConcept " +
                        "where cr.sourceConcept = :concept " +
                        "or cr.targetConcept = :concept " +
                        "order by c.id asc");
        query.setParameter("concept", (long)conceptId);

        List<Object[]> resultList = query.getResultList();

        entityManager.close();

        return resultList;// ret;
    }

    public static void deleteConceptRelationship(Long conceptRelationshipId) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        ConceptRelationshipEntity conceptRelationshipEntity = entityManager.find(ConceptRelationshipEntity.class, conceptRelationshipId);
        entityManager.getTransaction().begin();
        entityManager.remove(conceptRelationshipEntity);
        entityManager.getTransaction().commit();

        entityManager.close();
    }

    public static JsonConceptRelationship saveConceptRelationship(JsonConceptRelationship conceptRelationship) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        ConceptRelationshipEntity conceptRelationshipEntity = (conceptRelationship.getId() == null)
                ? new ConceptRelationshipEntity()
                : entityManager.find(ConceptRelationshipEntity.class, conceptRelationship.getId());

        entityManager.getTransaction().begin();
        conceptRelationshipEntity.setSourceConcept(conceptRelationship.getSourceConcept());
        conceptRelationshipEntity.setTargetConcept(conceptRelationship.getTargetConcept());
        conceptRelationshipEntity.setTargetLabel(conceptRelationship.getTargetLabel());
        conceptRelationshipEntity.setRelationshipType(conceptRelationship.getRelationship_type());
        entityManager.persist(conceptRelationshipEntity);
        entityManager.getTransaction().commit();

        entityManager.close();

        conceptRelationship.setId(conceptRelationshipEntity.getId());

        return conceptRelationship;
    }

    public static void deleteSnomedRelationships() throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        entityManager.getTransaction().begin();
        Query query = entityManager.createQuery(
                "DELETE from ConceptRelationshipEntity cr " +
                        "where cr.sourceConcept in (SELECT c.id FROM ConceptEntity c" +
                        "                           WHERE c.id > :sno)" +
                        "or cr.targetConcept in (SELECT c.id FROM ConceptEntity c" +
                        "                           WHERE c.id > :sno)");
        query.setParameter("sno", 100000);

        int deletedCount = query.executeUpdate();

        entityManager.getTransaction().commit();

        System.out.println(deletedCount + " deleted");
        entityManager.close();
    }

    public static void bulkSaveConceptRelationships(List<ConceptRelationshipEntity> relationshipEntities) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();
        int batchSize = 50;
        int infoSize = 1000;
        entityManager.getTransaction().begin();

        for(int i = 0; i < relationshipEntities.size(); ++i) {
            ConceptRelationshipEntity relationshipEntity = (ConceptRelationshipEntity)relationshipEntities.get(i);
            entityManager.persist(relationshipEntity);
            if(i % batchSize == 0) {
                entityManager.flush();
                entityManager.clear();
            }
            if(i % infoSize == 0) {
                System.out.println(i + " Completed");
            }
        }

        entityManager.getTransaction().commit();
        entityManager.close();
        System.out.println(relationshipEntities.size() + " Added");
    }

}
