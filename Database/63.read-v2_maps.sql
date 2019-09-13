-- Build working tables
DROP TABLE IF EXISTS read_v2_map_tmp;
CREATE TABLE read_v2_map_tmp (
    readCode VARCHAR(6) COLLATE utf8_bin NOT NULL,
    conceptId BIGINT NOT NULL,
    INDEX read_v2_map_tmp_idx (readCode)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO read_v2_map_tmp
(readCode, conceptId)
SELECT m.readCode, m.conceptId
FROM read_v2_map m
         JOIN concept c ON c.id = CONCAT('SN_', m.conceptId)
WHERE m.assured = 1
  AND m.status = 1
  AND m.termCode = '00';

-- Populate summary
DROP TABLE IF EXISTS read_v2_map_summary;
CREATE TABLE read_v2_map_summary (
                                     readCode VARCHAR(6) COLLATE utf8_bin NOT NULL,
                                     multi BOOLEAN DEFAULT FALSE,
                                     altConceptId BIGINT DEFAULT NULL,
                                     PRIMARY KEY read_v2_map_summary_pk (readCode),
                                     INDEX read_v2_map_summary_multi_idx (multi)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO read_v2_map_summary
(readCode, multi, altConceptId)
SELECT t.readCode, COUNT(DISTINCT c.dbid) > 1 as multi, IF(c.dbid is null, null, a.conceptId) as conceptId
FROM read_v2_map_tmp t
LEFT JOIN read_v2_alt_map a ON a.readCode = t.readCode AND a.termCode = '00' AND a.conceptId IS NOT NULL AND a.useAlt = 'Y'
LEFT JOIN concept c ON c.id = CONCAT('SN_', a.conceptId)
GROUP BY t.readCode;

SELECT @equiv := dbid FROM concept WHERE id = 'isEquivalentTo';
SELECT @isA := dbid FROM concept WHERE id = 'isA';
SELECT @codeable := dbid FROM concept WHERE id = 'CodeableConcept';
SELECT @related := dbid FROM concept WHERE id = 'isRelatedTo';

-- Add 1:1 maps
INSERT INTO concept_property (dbid, property, concept)
SELECT c.dbid, @equiv, v.dbid
FROM read_v2_map_summary s
JOIN read_v2_map_tmp t ON t.readCode = s.readCode
JOIN concept c ON c.id = CONCAT('R2_', s.readCode)
JOIN concept v ON v.id = CONCAT('SN_', t.conceptId)
WHERE s.multi = FALSE;


-- Add 1:n maps with 1:1 (alternative) overrides
INSERT INTO concept_property (dbid, property, concept)
SELECT c.dbid, @equiv, v.dbid
FROM read_v2_map_summary s
JOIN concept c ON c.id = CONCAT('R2_', s.readCode)
JOIN concept v ON v.id = CONCAT('SN_', s.altConceptId)
WHERE s.multi = TRUE
AND s.altConceptId IS NOT NULL;


-- Create PROXY document
INSERT INTO document (path, version)
VALUES ('InformationModel/dm/R2-proxy', '1.0.0');

SET @doc = LAST_INSERT_ID();

-- Create proxy concepts
INSERT INTO concept (document, id, name, description)
SELECT @doc, CONCAT('DS_R2_', s.readCode), c.name, c.description
FROM read_v2_map_summary s
JOIN read_v2_map_tmp t ON t.readCode = s.readCode
JOIN concept c ON c.id = CONCAT('R2_', s.readCode)
WHERE s.multi = TRUE
  AND s.altConceptId IS NULL;

INSERT INTO concept_property (dbid, property, concept)
SELECT dbid, @isA, @codeable
FROM read_v2_map_summary s
JOIN read_v2_map_tmp t ON t.readCode = s.readCode
JOIN concept c ON c.id = CONCAT('DS_R2_', s.readCode)
WHERE s.multi = TRUE
AND s.altConceptId IS NULL;

INSERT INTO concept_property (dbid, property, concept)
SELECT c.dbid, @related, v.dbid
FROM read_v2_map_summary s
JOIN read_v2_map_tmp t ON t.readCode = s.readCode
JOIN concept c ON c.id = CONCAT('DS_R2_', s.readCode)
JOIN concept v ON v.id = CONCAT('SN_', t.conceptId)
WHERE s.multi = TRUE
AND s.altConceptId IS NULL;

-- Add 1:n maps with no alternative overrides (proxy concepts)
INSERT INTO concept_property (dbid, property, concept)
SELECT c.dbid, @equiv, v.dbid
FROM read_v2_map_summary s
JOIN concept c ON c.id = CONCAT('R2_', s.readCode)
JOIN concept v ON v.id = CONCAT('DS_R2_', s.readCode)
WHERE s.multi = TRUE
AND s.altConceptId IS NULL;





