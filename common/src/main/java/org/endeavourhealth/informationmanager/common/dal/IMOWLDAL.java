package org.endeavourhealth.informationmanager.common.dal;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.informationmanager.common.transform.model.*;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IMOWLDAL extends BaseJDBCDAL {
    public Map<String, Namespace> prefixNamespace = new HashMap<>();
    public Map<Namespace, Integer> namespaceId = new HashMap<>();

    // ------------------------------ SAVE ------------------------------

    public void save(Ontology ontology) throws SQLException, JsonProcessingException {
        processPrefixes(ontology.getNamespace());

        processClasses(ontology);
    }

    private void processPrefixes(List<Namespace> namespaces) {
        for(Namespace ns: namespaces) {
            prefixNamespace.put(ns.getPrefix(), ns);
        }
    }

    private void processClasses(Ontology ontology) throws SQLException, JsonProcessingException {
        String sql = "REPLACE INTO concept\n" +
            "(ontology, iri, type, definition)\n" +
            "VALUES\n" +
            "(?, ?, ?, ?)\n";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (Clazz c : ontology.getClazz()) {
                stmt.setInt(1, getNamespaceIdByPrefix(c.getIri()));
                stmt.setString(2, c.getIri());
                stmt.setInt(3, ConceptDefinitionType.CLASS.value());
                stmt.setString(4, ObjectMapperPool.getInstance().writeValueAsString(c));
                stmt.executeUpdate();
            }
            for (ObjectProperty op : ontology.getObjectProperty()) {
                stmt.setInt(1, getNamespaceIdByPrefix(op.getIri()));
                stmt.setString(2, op.getIri());
                stmt.setInt(3, ConceptDefinitionType.OBJECT_PROPERTY.value());
                stmt.setString(4, ObjectMapperPool.getInstance().writeValueAsString(op));
                stmt.executeUpdate();
            }
            for (DataProperty dp : ontology.getDataProperty()) {
                stmt.setInt(1, getNamespaceIdByPrefix(dp.getIri()));
                stmt.setString(2, dp.getIri());
                stmt.setInt(3, ConceptDefinitionType.DATA_PROPERTY.value());
                stmt.setString(4, ObjectMapperPool.getInstance().writeValueAsString(dp));
                stmt.executeUpdate();
            }
            for (DataType dt : ontology.getDataType()) {
                stmt.setInt(1, getNamespaceIdByPrefix(dt.getIri()));
                stmt.setString(2, dt.getIri());
                stmt.setInt(3, ConceptDefinitionType.DATA_TYPE.value());
                stmt.setString(4, ObjectMapperPool.getInstance().writeValueAsString(dt));
                stmt.executeUpdate();
            }
            for (AnnotationProperty ap : ontology.getAnnotationProperty()) {
                stmt.setInt(1, getNamespaceIdByPrefix(ap.getIri()));
                stmt.setString(2, ap.getIri());
                stmt.setInt(3, ConceptDefinitionType.ANNOTATION_PROPERTY.value());
                stmt.setString(4, ObjectMapperPool.getInstance().writeValueAsString(ap));
                stmt.executeUpdate();
            }
        }
    }

    private int getNamespaceIdByPrefix(String prefixIri) throws SQLException {
        String prefix = prefixIri.substring(0, prefixIri.indexOf(":") + 1);
        Namespace ns = prefixNamespace.get(prefix);

        if (ns == null)
            throw new IllegalStateException("Unknown namespace prefix: [" + prefix + "]");

        Integer id = namespaceId.get(ns);
        if (id == null) {
            id = getNamespaceIdFromDB(ns.getIri());

            if (id == null)
                id = addNamespaceToDB(ns);

            namespaceId.put(ns, id);
        }

        return id;
    }

    private Integer getNamespaceIdFromDB(String iri) throws SQLException {
        String sql = "SELECT id FROM ontology WHERE iri=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, iri);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return rs.getInt("id");
                else
                    return null;
            }
        }
    }

    private int addNamespaceToDB(Namespace ns) throws SQLException {
        String sql = "INSERT INTO ontology (iri, prefix, version) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, ns.getIri());
            stmt.setString(2, ns.getPrefix());
            stmt.setString(3, ns.getVersion());
            stmt.executeUpdate();
            return DALHelper.getGeneratedKey(stmt);
        }
    }

    // ------------------------------ LOAD ------------------------------

    public Ontology load(String iri) throws SQLException, IOException {
        Ontology ontology = new Ontology();
        add(iri, ontology);
        return ontology;
    }

    public void add(String iri, Ontology ontology) throws SQLException, IOException {
        if (ontology == null)
            throw new IllegalStateException("no ontology to add to");

        String sql = "SELECT c.type, c.definition\n" +
            "FROM ontology o\n" +
            "JOIN concept c ON c.ontology = o.id\n" +
            "WHERE o.iri = ?\n";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, iri);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    addConcept(
                        ontology,
                        rs.getInt("type"),
                        rs.getString("definition")
                        );
                }
            }
        }
    }

    private void addConcept(Ontology ontology, int type, String definition) throws IOException {
        if (type == ConceptDefinitionType.CLASS.value()) {
            Clazz c = ObjectMapperPool.getInstance().readValue(definition, Clazz.class);
            ontology.addClazz(c);
        } else if (type == ConceptDefinitionType.OBJECT_PROPERTY.value()) {
            ObjectProperty op = ObjectMapperPool.getInstance().readValue(definition, ObjectProperty.class);
            ontology.addObjectProperty(op);
        }
    }
}
