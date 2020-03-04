package org.endeavourhealth.informationmanager.common.transform;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.endeavourhealth.informationmanager.common.transform.model.*;
import org.endeavourhealth.informationmanager.common.transform.model.ClassExpression;
import org.endeavourhealth.informationmanager.common.transform.model.PropertyDomain;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.PrefixDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class OwlToJson {
    private DefaultPrefixManager defaultPrefixManager;
    Map<String, ConceptData> conceptDataMap = new HashMap<>();

    public void transform() throws OWLOntologyCreationException, JsonProcessingException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager.loadOntology(IRI.create(new File("IMCore.owl")));

        getPrefixes(ontology);
        ontology.axioms().forEach(this::processAxiom);

        Document jsonDocument = new Document();
        jsonDocument.setInformationModel(getModelDocument());

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonDocument);
        System.out.println(json);
    }

    private void getPrefixes(OWLOntology ontology) {
        defaultPrefixManager = new DefaultPrefixManager();

        OWLDocumentFormat ontologyFormat = ontology.getNonnullFormat();
        if (ontologyFormat instanceof PrefixDocumentFormat) {
            defaultPrefixManager.copyPrefixesFrom((PrefixDocumentFormat) ontologyFormat);
            defaultPrefixManager.setPrefixComparator(((PrefixDocumentFormat) ontologyFormat).getPrefixComparator());
        }
    }

    private InformationModel getModelDocument() {
        InformationModel informationModel = new InformationModel();

        informationModel.setNamespace(getNamespace());

        for (ConceptData cd : conceptDataMap.values()) {
            informationModel.getConcept().add(cd.getConcept());
            informationModel.getConceptAxiom().add(cd.getConceptAxiom());
        }

        return informationModel;
    }

    private List<Namespace> getNamespace() {
        List<Namespace> namespaces = new ArrayList<>();


        for (Map.Entry<String, String> prefix : defaultPrefixManager.getPrefixName2PrefixMap().entrySet()) {
            namespaces.add(
                new Namespace()
                    .setPrefix(prefix.getKey())
                    .setIri(prefix.getValue())
            );
        }

        return namespaces;
    }

    private void processAxiom(OWLAxiom a) {
        if (a.isOfType(AxiomType.DECLARATION))
            processDeclarationAxiom((OWLDeclarationAxiom) a);
        else if (a.isOfType(AxiomType.OBJECT_PROPERTY_DOMAIN))
            processObjectPropertyDomainAxiom((OWLObjectPropertyDomainAxiom) a);
        else if (a.isOfType(AxiomType.DISJOINT_CLASSES))
            processDisjointAxiom((OWLDisjointClassesAxiom) a);
        else if (a.isOfType(AxiomType.SUBCLASS_OF))
            processSubClassAxiom((OWLSubClassOfAxiom) a);
        else if (a.isOfType(AxiomType.INVERSE_OBJECT_PROPERTIES))
            processInverseAxiom((OWLInverseObjectPropertiesAxiom) a);
        else if (a.isOfType(AxiomType.OBJECT_PROPERTY_RANGE))
            processObjectPropertyRangeAxiom((OWLObjectPropertyRangeAxiom) a);
        else if (a.isOfType(AxiomType.DIFFERENT_INDIVIDUALS))
            processDifferentIndividualsAxiom((OWLDifferentIndividualsAxiom) a);
        else if (a.isOfType(AxiomType.FUNCTIONAL_DATA_PROPERTY))
            processFunctionalDataPropertyAxiom((OWLFunctionalDataPropertyAxiom) a);
        else if (a.isOfType(AxiomType.FUNCTIONAL_OBJECT_PROPERTY))
            processFunctionalObjectPropertyAxiom((OWLFunctionalObjectPropertyAxiom) a);
        else if (a.isOfType(AxiomType.ANNOTATION_ASSERTION))
            processAnnotationAssertionAxiom((OWLAnnotationAssertionAxiom) a);
        else if (a.isOfType(AxiomType.ANNOTATION_PROPERTY_RANGE))
            processAnnotationPropertyRangeAxiom((OWLAnnotationPropertyRangeAxiom) a);
        else if (a.isOfType(AxiomType.EQUIVALENT_CLASSES))
            processEquivalentClassesAxiom((OWLEquivalentClassesAxiom) a);
        else if (a.isOfType(AxiomType.SUB_OBJECT_PROPERTY))
            processSubObjectPropertyAxiom((OWLSubObjectPropertyOfAxiom) a);
        else if (a.isOfType(AxiomType.SUB_DATA_PROPERTY))
            processSubDataPropertyAxiom((OWLSubDataPropertyOfAxiom) a);
        else if (a.isOfType(AxiomType.DATA_PROPERTY_RANGE))
            processDataPropertyRangeAxiom((OWLDataPropertyRangeAxiom) a);
        else if (a.isOfType(AxiomType.SUB_ANNOTATION_PROPERTY_OF))
            processSubAnnotationPropertyAxiom((OWLSubAnnotationPropertyOfAxiom) a);
        else if (a.isOfType(AxiomType.TRANSITIVE_OBJECT_PROPERTY))
            processTransitiveObjectProperty((OWLTransitiveObjectPropertyAxiom) a);
        else if (a.isOfType(AxiomType.CLASS_ASSERTION))
            processClassAssertionAxiom((OWLClassAssertionAxiom) a);
        else
            System.out.println(a);
    }

    private void processSubClassAxiom(OWLSubClassOfAxiom a) {
        String iri = defaultPrefixManager.getPrefixIRI(((OWLClass) a.getSubClass()).getIRI());

        OWLClassExpression superClass = a.getSuperClass();
        ClassExpression cex = getConceptData(iri).getConceptAxiom().getSubClassOf();

        if (cex == null)
            getConceptData(iri).getConceptAxiom().setSubClassOf(cex = new ClassExpression());

        addOwlClassExpressionToClassExpression(cex, superClass);
    }

    private void processEquivalentClassesAxiom(OWLEquivalentClassesAxiom a) {
        Iterator<OWLClassExpression> i = a.getClassExpressions().iterator();
        String iri = getIri(i.next().asOWLClass().getIRI());
        ClassExpression cex = getConceptData(iri).getConceptAxiom().getEquivalentTo();

        if (cex == null)
            getConceptData(iri).getConceptAxiom().setEquivalentTo(cex = new ClassExpression());

        while (i.hasNext()) {
            addOwlClassExpressionToClassExpression(cex, i.next());
        }
    }

    private void addOwlClassExpressionToClassExpression(ClassExpression cex, OWLClassExpression oce) {
        if (oce.getClassExpressionType() == ClassExpressionType.OWL_CLASS) {
            cex.addConcept(
                defaultPrefixManager.getPrefixIRI(oce.asOWLClass().getIRI())
            );
        } else if (oce.getClassExpressionType() == ClassExpressionType.OBJECT_INTERSECTION_OF) {
            for (OWLClassExpression ce : ((OWLObjectIntersectionOf) oce).getOperandsAsList()) {
                if (ce.isOWLClass())
                    cex.addConcept(
                        defaultPrefixManager.getPrefixIRI(ce.asOWLClass().getIRI())

                    );
                else if (ce.getClassExpressionType() == ClassExpressionType.DATA_HAS_VALUE) {
                    OWLDataHasValue dhv = (OWLDataHasValue) ce;
                    cex.addRoleGroup(
                        new RoleGroup()
                            .addRole(
                                new Role()
                                    .setProperty(defaultPrefixManager.getPrefixIRI(dhv.getProperty().asOWLDataProperty().getIRI()))
                                    .setValueData(dhv.getValue().getLiteral())
                            )
                    );
                } else if (ce.getClassExpressionType() == ClassExpressionType.OBJECT_EXACT_CARDINALITY) {
                    OWLObjectExactCardinality oec = (OWLObjectExactCardinality) ce;
                    cex.addRoleGroup(
                        new RoleGroup()
                            .addRole(
                                new Role()
                                    .setProperty(defaultPrefixManager.getPrefixIRI(oec.getProperty().asOWLObjectProperty().getIRI()))
                                    .setValueClass(
                                        new ClassExpression()
                                            .addConcept(
                                                defaultPrefixManager.getPrefixIRI(oec.getFiller().asOWLClass().getIRI())
                                            )
                                    )
                                    .setMinCardinality(oec.getCardinality())
                                    .setMaxCardinality(oec.getCardinality())
                            )
                    );
                } else if (ce.getClassExpressionType() == ClassExpressionType.OBJECT_MIN_CARDINALITY) {
                    OWLObjectMinCardinality oec = (OWLObjectMinCardinality) ce;
                    cex.addRoleGroup(
                        new RoleGroup()
                            .addRole(
                                new Role()
                                    .setProperty(defaultPrefixManager.getPrefixIRI(oec.getProperty().asOWLObjectProperty().getIRI()))
                                    .setValueClass(
                                        new ClassExpression()
                                            .addConcept(
                                                defaultPrefixManager.getPrefixIRI(oec.getFiller().asOWLClass().getIRI())
                                            )
                                    )
                                    .setMinCardinality(oec.getCardinality())
                            )
                    );
                } else if (ce.getClassExpressionType() == ClassExpressionType.DATA_EXACT_CARDINALITY) {
                    OWLDataExactCardinality oec = (OWLDataExactCardinality) ce;
                    cex.addRoleGroup(
                        new RoleGroup()
                            .addRole(
                                new Role()
                                    .setProperty(defaultPrefixManager.getPrefixIRI(oec.getProperty().asOWLDataProperty().getIRI()))
                                    .setValueClass(
                                        new ClassExpression()
                                            .addConcept(defaultPrefixManager.getPrefixIRI(oec.getFiller().asOWLDatatype().getIRI()))
                                    )
                                    .setMinCardinality(oec.getCardinality())
                                    .setMaxCardinality(oec.getCardinality())

                            )
                    );
                } else if (ce.getClassExpressionType() == ClassExpressionType.DATA_MAX_CARDINALITY) {
                    OWLDataMaxCardinality oec = (OWLDataMaxCardinality) ce;
                    cex.addRoleGroup(
                        new RoleGroup()
                            .addRole(
                                new Role()
                                    .setProperty(defaultPrefixManager.getPrefixIRI(oec.getProperty().asOWLDataProperty().getIRI()))
                                    .setValueClass(
                                        new ClassExpression()
                                            .addConcept(
                                                defaultPrefixManager.getPrefixIRI(oec.getFiller().asOWLDatatype().getIRI())
                                            )
                                    )
                                    .setMaxCardinality(oec.getCardinality())

                            )
                    );
                } else if (ce.getClassExpressionType() == ClassExpressionType.OBJECT_SOME_VALUES_FROM) {
                    OWLObjectSomeValuesFrom oec = (OWLObjectSomeValuesFrom) ce;
                    cex.addRoleGroup(
                        new RoleGroup()
                            .addRole(
                                new Role()
                                    .setProperty(defaultPrefixManager.getPrefixIRI(oec.getProperty().asOWLObjectProperty().getIRI()))
                                    .setValueClass(
                                        new ClassExpression()
                                            .addConcept(
                                                defaultPrefixManager.getPrefixIRI(oec.getFiller().asOWLClass().getIRI())
                                            )
                                    )
                            )
                    );
                } else {
                    System.err.println(oce);
                    // throw new IllegalStateException("Unhandled class expression type");
                }
            }
        } else {
            System.err.println(oce);
            throw new IllegalStateException("Unhandled class expression type");
        }
    }

    private void processDisjointAxiom(OWLDisjointClassesAxiom a) {
        List<String> iris = a.getOperandsAsList()
            .stream()
            .map(e -> defaultPrefixManager.getPrefixIRI(((OWLClass) e).getIRI()))
            .collect(Collectors.toList());

        for (String iri : iris) {
            getConceptData(iri)
                .getConceptAxiom()
                .addAllDisjointWith(iris
                    .stream()
                    .filter(i -> !i.equals(iri))
                    .collect(Collectors.toList()));
        }
    }

    private void processObjectPropertyDomainAxiom(OWLObjectPropertyDomainAxiom a) {
        String iri = getIri(a.getDomain().asOWLClass().getIRI());

        PropertyDomain pd = getConceptData(iri)
            .getConceptAxiom()
            .getPropertyDomain();

        if (pd == null)
            getConceptData(iri).getConceptAxiom().setPropertyDomain(pd = new PropertyDomain());

        pd.addDomain(
            new Domain()
                .setConcept(getIri(a.getProperty().asOWLObjectProperty().getIRI()))
        );
    }

    private void processObjectPropertyRangeAxiom(OWLObjectPropertyRangeAxiom a) {
        String iri = getIri(a.getProperty().asOWLObjectProperty().getIRI());

        ClassExpressionConstraint pr = getConceptData(iri).getConceptAxiom().getPropertyRange();

        if (pr == null)
            getConceptData(iri).getConceptAxiom().setPropertyRange(pr = new ClassExpressionConstraint());

        pr.addConcept(
            new SubsumptionConcept()
                .setConcept(getIri(a.getRange().asOWLClass().getIRI()))
        );
    }

    private void processDeclarationAxiom(OWLDeclarationAxiom a) {
        String iri = getIri(a.getEntity().getIRI());
        getConceptData(iri)
            .getConcept()
            .setIri(iri);
    }

    private void processInverseAxiom(OWLInverseObjectPropertiesAxiom a) {
        String firstIri = getIri(a.getFirstProperty().getNamedProperty().getIRI());
        String secondIri = getIri(a.getSecondProperty().getNamedProperty().getIRI());

        getConceptData(firstIri).getConceptAxiom().addInversePropertyOf(secondIri);
        getConceptData(secondIri).getConceptAxiom().addInversePropertyOf(firstIri);
    }

    private void processDifferentIndividualsAxiom(OWLDifferentIndividualsAxiom a) {
        // TODO
        System.err.println(a);
    }

    private void processFunctionalDataPropertyAxiom(OWLFunctionalDataPropertyAxiom a) {
        // TODO
        System.err.println(a);
    }

    private void processFunctionalObjectPropertyAxiom(OWLFunctionalObjectPropertyAxiom a) {
        // TODO
        System.err.println(a);
    }

    private void processAnnotationAssertionAxiom(OWLAnnotationAssertionAxiom a) {
        String iri = getIri(a.getSubject().asIRI().get());

        String property = getIri(a.getProperty().asOWLAnnotationProperty().getIRI());

        String value = a.getValue().asLiteral().get().toString();

        Concept c = getConceptData(iri).getConcept();
        if (property.equals("rdfs:comment"))
            c.setDescription(value);
        else if (property.equals("rdfs:label"))
            c.setName(value);
        else {
            System.err.println(a);
        }

    }

    private void processAnnotationPropertyRangeAxiom(OWLAnnotationPropertyRangeAxiom a) {
        String iri = getIri(a.getProperty().asOWLAnnotationProperty().getIRI());

        ClassExpressionConstraint pr = getConceptData(iri).getConceptAxiom().getPropertyRange();

        if (pr == null)
            getConceptData(iri).getConceptAxiom().setPropertyRange(pr = new ClassExpressionConstraint());

        pr.addConcept(
            new SubsumptionConcept()
                .setConcept(getIri(a.getRange()))
        );
    }

    private void processSubObjectPropertyAxiom(OWLSubObjectPropertyOfAxiom a) {
        String iri = getIri(a.getSubProperty().asOWLObjectProperty().getIRI());
        getConceptData(iri)
            .getConceptAxiom()
            .setSubPropertyOf(
                new SubProperty()
                    .setConcept(
                        getIri(a.getSuperProperty().asOWLObjectProperty().getIRI())
                    )
            );
    }

    private void processSubDataPropertyAxiom(OWLSubDataPropertyOfAxiom a) {
        String iri = getIri(a.getSubProperty().asOWLDataProperty().getIRI());
        getConceptData(iri)
            .getConceptAxiom()
            .setSubPropertyOf(
                new SubProperty()
                    .setConcept(
                        getIri(a.getSuperProperty().asOWLDataProperty().getIRI())
                    )
            );
    }

    private void processDataPropertyRangeAxiom(OWLDataPropertyRangeAxiom a) {
        String iri = getIri(a.getProperty().asOWLDataProperty().getIRI());

        ClassExpressionConstraint pr = getConceptData(iri).getConceptAxiom().getPropertyRange();

        if (pr == null)
            getConceptData(iri).getConceptAxiom().setPropertyRange(pr = new ClassExpressionConstraint());

        pr.addConcept(
            new SubsumptionConcept()
                .setConcept(getIri(a.getRange().asOWLDatatype().getIRI()))
        );
    }

    private void processSubAnnotationPropertyAxiom(OWLSubAnnotationPropertyOfAxiom a) {
        String iri = getIri(a.getSubProperty().asOWLAnnotationProperty().getIRI());
        getConceptData(iri)
            .getConceptAxiom()
            .setSubPropertyOf(
                new SubProperty()
                    .setConcept(
                        getIri(a.getSuperProperty().asOWLAnnotationProperty().getIRI())
                    )
            );
    }

    private void processTransitiveObjectProperty(OWLTransitiveObjectPropertyAxiom a) {
        String iri = getIri(a.getProperty().asOWLObjectProperty().getIRI());
        getConceptData(iri)
            .getConceptAxiom()
            .setTransitive(true);
    }

    private void processClassAssertionAxiom(OWLClassAssertionAxiom a) {
        // Ignore/Do nothing!
    }

    private String getIri(IRI iri) {
        return defaultPrefixManager.getPrefixIRI(iri);
    }

    private ConceptData getConceptData(String iri) {
        ConceptData cd = conceptDataMap.get(iri);
        if (cd == null) {
            cd = new ConceptData();
            cd.getConcept().setIri(iri);
            conceptDataMap.put(iri, cd);
        }
        return cd;
    }
}
