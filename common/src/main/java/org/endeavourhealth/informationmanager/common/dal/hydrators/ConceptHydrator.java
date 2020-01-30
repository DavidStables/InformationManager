package org.endeavourhealth.informationmanager.common.dal.hydrators;

import org.endeavourhealth.informationmanager.common.dal.DALHelper;
import org.endeavourhealth.informationmanager.common.models.*;
import org.endeavourhealth.informationmanager.common.models.definitionTypes.ConceptDefinition;
import org.endeavourhealth.informationmanager.common.models.definitionTypes.PropertyDefinition;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ConceptHydrator {
    public static List<Concept> createConceptList(ResultSet rs) throws SQLException {
        List<Concept> result = new ArrayList<>();
        while (rs.next())
            result.add(createConcept(rs));

        return result;
    }

    public static Concept createConcept(ResultSet rs) throws SQLException {
        return populate(new Concept(), rs);
    }

    public static Concept populate(Concept c, ResultSet rs) throws SQLException {
        return c
            .setIri(DALHelper.getString(rs, "iri"))
            .setStatus(DALHelper.getString(rs, "status"))
            .setName(DALHelper.getString(rs, "name"))
            .setDescription(DALHelper.getString(rs, "description"))
            .setCode(DALHelper.getString(rs, "code"));
    }

    public static List<ConceptDefinition> createConceptDefinitionList(ResultSet rs) throws SQLException {
        List<ConceptDefinition> result = new ArrayList<>();
        while (rs.next())
            result.add(createConceptDefinition(rs));

        return result;
    }

    public static ConceptDefinition createConceptDefinition(ResultSet rs) throws SQLException {
        return populate(new ConceptDefinition(), rs);
    }

    public static ConceptDefinition populate(ConceptDefinition conceptDefinition, ResultSet rs) throws SQLException {
        return conceptDefinition
            .setConcept(rs.getString("iri"))
            .setInferred(rs.getBoolean("inferred"));
    }

    public static List<PropertyDefinition> createPropertyDefinitionList(ResultSet rs) throws  SQLException {
        List<PropertyDefinition> result = new ArrayList<>();
        while (rs.next())
            result.add(createPropertyDefinition(rs));

        return result;
    }

    public static PropertyDefinition createPropertyDefinition(ResultSet rs) throws SQLException {
        return populate(new PropertyDefinition(), rs);
    }

    public static PropertyDefinition populate(PropertyDefinition propertyDefinition, ResultSet rs) throws SQLException {
        propertyDefinition
            .setGroup(rs.getInt("group"))
            .setProperty(rs.getString("property"))
            .setMinCardinality(DALHelper.getInt(rs, "minCardinality"))
            .setMaxCardinality(DALHelper.getInt(rs, "maxCardinality"))
            .setInferred(rs.getBoolean("inferred"));

        if (rs.getString("object") != null)
            propertyDefinition.setObject(rs.getString("object"));
        else
            propertyDefinition.setData(rs.getString("data"));

        return propertyDefinition;
    }

/*

    public static List<ConceptRelation> createConceptRelations(ResultSet rs) throws SQLException {
        List<ConceptRelation> relations = new ArrayList<>();

        return populate(relations, rs);
    }

    public static List<ConceptRelation> populate(List<ConceptRelation> relations, ResultSet rs) throws SQLException {
        while (rs.next()) {
            ConceptRelation rel = new ConceptRelation()
                .setSubject(DALHelper.getString(rs, "subject"))
                .setGroup(DALHelper.getInt(rs, "group"))
                .setRelation(DALHelper.getString(rs, "relation"))
                .setObject(DALHelper.getString(rs, "object"));

*/
/*            if (DALHelper.getInt(rs, "cardDbid") != null) {
                rel.setCardinality(createConceptRelationCardinality(rs));
            }

            if (DALHelper.getInt(rs, "dataDbid") != null) {
                rel.setValue(createConceptPropertyData(rs)
                );
            }*//*


            relations.add(rel);
        }

        return relations;
    }

    public static ConceptRelationCardinality createConceptRelationCardinality(ResultSet rs) throws SQLException {
        return populate(new ConceptRelationCardinality(), rs);
    }

    public static ConceptRelationCardinality populate(ConceptRelationCardinality relationCardinality, ResultSet rs) throws SQLException {
        return relationCardinality
            .setMinCardinality(DALHelper.getInt(rs, "minCardinality"))
            .setMaxCardinality(DALHelper.getInt(rs, "maxCardinality"))
            .setMaxInGroup(DALHelper.getInt(rs, "maxInGroup"));
    }

    public static ConceptPropertyData createConceptPropertyData(ResultSet rs) throws SQLException {
        return populate(new ConceptPropertyData(), rs);
    }

    public static ConceptPropertyData populate(ConceptPropertyData propertyData, ResultSet rs) throws SQLException {
        return propertyData
            .setConcept(DALHelper.getString(rs, "concept"))
            .setData(DALHelper.getString(rs, "data"));
    }
*/

    public static Namespace createNamespace(ResultSet rs) throws SQLException {
        return populate(new Namespace(), rs);
    }

    public static Namespace populate(Namespace namespace, ResultSet resultSet) throws SQLException {
        namespace
            .setIri(resultSet.getString("iri"))
            .setName(resultSet.getString("name"))
            .setPrefix(resultSet.getString("prefix"));

        return namespace;
    }

/*











    public static FullConcept create(ResultSet resultSet) throws SQLException, IOException {
        return populate(new FullConcept(), resultSet);
    }
    public static FullConcept populate(FullConcept concept, ResultSet resultSet) throws SQLException, IOException {
        return concept
            .setModel(resultSet.getString("model"))
            .setConcept(createConcept(resultSet))
            ;
    }

*/
/*    public static JsonNode createDefinition(ResultSet resultSet) throws SQLException, IOException {
        if (resultSet.next())
            return ObjectMapperPool.getInstance().readTree(resultSet.getString("data"));
        else
            return null;
    }*//*


    public static PropertyDomain createPropertyDomain(ResultSet resultSet) throws SQLException, IOException {
        return populate(new PropertyDomain(), resultSet);
    }
    public static PropertyDomain populate(PropertyDomain propertyDomain, ResultSet resultSet) throws SQLException, IOException {
        propertyDomain.setProperty(resultSet.getString("property"));
        propertyDomain.setStatus(resultSet.getString("status"));
        propertyDomain.getDomain().add(createDomain(resultSet));

        while (resultSet.next())
            propertyDomain.getDomain().add(createDomain(resultSet));

        return propertyDomain;
    }

    public static Domain createDomain(ResultSet resultSet) throws SQLException, IOException {
        return populate(new Domain(), resultSet);
    }
    public static Domain populate(Domain domain, ResultSet resultSet) throws SQLException, IOException {
        domain.setOperator(Operator.byValue(resultSet.getShort("operator")));
        domain.setClazz(resultSet.getString("property"));
        domain.setMinCardinality(resultSet.getInt("minimum"));
        domain.setMaxCardinality(resultSet.getInt("maximum"));
        domain.setMaxInGroup(resultSet.getInt("max_in_group"));
        return domain;
    }

    public static List<PropertyRange> createPropertyRange(ResultSet resultSet) throws SQLException, IOException {
        List<PropertyRange> ranges = new ArrayList<>();

        String property = "";
        PropertyRange pr = null;
        while (resultSet.next()) {
            if (!property.equals(resultSet.getString("property"))) {
                property = resultSet.getString("property");
                pr = new PropertyRange()
                    .setProperty(property)
                    .setStatus(resultSet.getString("status"));
                ranges.add(pr);
            }

            SimpleExpressionConstraint exp = new SimpleExpressionConstraint()
                .setName(resultSet.getString("name"));

            pr.getRangeClass().add(exp);
        }

        return ranges;
    }

    public static DraftConcept createDraft(ResultSet resultSet) throws SQLException {
        return populate(new DraftConcept(), resultSet);
    }
    public static DraftConcept populate(DraftConcept draftConcept, ResultSet resultSet) throws SQLException {
        return draftConcept
            .setId(resultSet.getString("id"))
            .setName(resultSet.getString("name"))
            .setPublished(resultSet.getString("published"))
            .setUpdated(resultSet.getTimestamp("updated"));
    }


*/

}
