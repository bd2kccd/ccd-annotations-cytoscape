package edu.pitt.cs.admt.cytoscape.annotations.db;

/**
 * @author Nikos R. Katsipoulakis
 */
class AnnotationSchema {

  static final String NODE_TABLE = "NODE";

  static final String EDGE_TABLE = "EDGE";

  static final String ANNOTATION_TABLE = "ANNOTATION";

  static final String ANNOT_TO_NODE_TABLE = "ANNOT_TO_NODE";

  static final String ANNOT_TO_EDGE_TABLE = "ANNOT_TO_EDGE";

  static final String ANNOT_EXT_ATTR_TABLE = "ANNOT_EXT_ATTR";

  static final String DROP_NODE_TABLE = "DROP TABLE " + NODE_TABLE + " IF EXISTS CASCADE";

  static final String DROP_EDGE_TABLE = "DROP TABLE " + EDGE_TABLE + " IF EXISTS CASCADE";

  static final String DROP_ANNOT_TABLE = "DROP TABLE " + ANNOTATION_TABLE + " IF EXISTS CASCADE";

  static final String DROP_ANNOT_TO_NODE_TABLE = "DROP TABLE " + ANNOT_TO_NODE_TABLE +
      " IF EXISTS CASCADE";

  static final String DROP_ANNOT_TO_EDGE_TABLE = "DROP TABLE " + ANNOT_TO_EDGE_TABLE +
      " IF EXISTS CASCADE";

  static final String DROP_EXT_ATTR_TABLE = "DROP TABLE " + ANNOT_EXT_ATTR_TABLE +
      " IF EXISTS CASCADE";

  static final String CREATE_NODE_TABLE = "CREATE TABLE " + NODE_TABLE +
      "(" +
      "suid INTEGER PRIMARY KEY, " +
      "CONSTRAINT node_positive CHECK(suid >= 0)" +
      ")";

  static final String CREATE_EDGE_TABLE = "CREATE TABLE " + EDGE_TABLE +
      "(" +
      "suid INTEGER PRIMARY KEY, " +
      "source INTEGER NOT NULL, " +
      "destination INTEGER NOT NULL, " +
      "CONSTRAINT id_edge CHECK(suid >= 0), " +
      "FOREIGN KEY (source) REFERENCES " + NODE_TABLE +
      "(suid) ON DELETE CASCADE ON UPDATE CASCADE, " +
      "FOREIGN KEY (destination) REFERENCES " + NODE_TABLE +
      "(suid) ON DELETE CASCADE ON UPDATE CASCADE" +
      ")";

  static final String CREATE_ANNOT_TABLE = "CREATE TABLE " + ANNOTATION_TABLE +
      "(" +
      "id UUID PRIMARY KEY, " +
      "description VARCHAR(64) DEFAULT 'N/A')";

  static final String CREATE_ANNOT_TO_NODE_TABLE = "CREATE TABLE " + ANNOT_TO_NODE_TABLE +
      "(" +
      "a_id UUID NOT NULL, " +
      "suid INTEGER NOT NULL, " +
      "ext_attr_id INTEGER, " +
      "ext_attr_value LONGVARBINARY, " +
      "FOREIGN KEY (a_id) REFERENCES " + ANNOTATION_TABLE +
      "(id) ON DELETE CASCADE ON UPDATE CASCADE, " +
      "FOREIGN KEY (suid) REFERENCES " + NODE_TABLE +
      "(suid) ON DELETE CASCADE ON UPDATE CASCADE," +
      "CONSTRAINT ann_node_unique UNIQUE (a_id, suid) " +
      ")";

  static final String ALTER_ANNOT_TO_NODE_TABLE = "ALTER TABLE " + ANNOT_TO_NODE_TABLE +
      " ADD FOREIGN KEY (ext_attr_id) REFERENCES " + ANNOT_EXT_ATTR_TABLE +
      " (id) ON DELETE SET NULL ON UPDATE CASCADE";

  static final String CREATE_ANNOT_TO_EDGE_TABLE = "CREATE TABLE " + ANNOT_TO_EDGE_TABLE
      + " (" +
      "a_id UUID NOT NULL, " +
      "suid INTEGER NOT NULL, " +
      "ext_attr_id INTEGER, " +
      "ext_attr_value LONGVARBINARY, " +
      "FOREIGN KEY (a_id) REFERENCES " + ANNOTATION_TABLE +
      "(id) ON DELETE CASCADE ON UPDATE CASCADE, " +
      "FOREIGN KEY (suid) REFERENCES " + EDGE_TABLE +
      "(suid) ON DELETE CASCADE ON UPDATE CASCADE, " +
      "CONSTRAINT ann_edge_unique UNIQUE (a_id, suid) " +
      " )";

  static final String ALTER_ANNOT_TO_EDGE_TABLE = "ALTER TABLE " + ANNOT_TO_EDGE_TABLE +
      " ADD FOREIGN KEY (ext_attr_id) REFERENCES " + ANNOT_EXT_ATTR_TABLE +
      " (id) ON DELETE SET NULL ON UPDATE CASCADE";

  static final String CREATE_ANNOT_EXT_ATTR_TABLE = "CREATE TABLE " + ANNOT_EXT_ATTR_TABLE +
      " (" +
      "id INTEGER PRIMARY KEY CHECK(id >= 0), " +
      "name VARCHAR(32) NOT NULL, " +
      "type VARCHAR(16) NOT NULL CHECK(type in ('BOOLEAN', 'INT', 'FLOAT', 'CHAR', 'STRING')), " +
      "description VARCHAR(64) DEFAULT 'N/A'" +
      ")";

  static final String INSERT_NODE = "INSERT INTO " + NODE_TABLE + "(suid) VALUES (?)";

  static final String INSERT_EDGE = "INSERT INTO " + EDGE_TABLE + "(suid, source, destination) " +
      "VALUES (?,?,?)";

  static final String INSERT_ANNOT = "INSERT INTO " + ANNOTATION_TABLE + "(id,description) " +
      "VALUES (?,?)";

  static final String INSERT_ANNOT_TO_NODE = "INSERT INTO " + ANNOT_TO_NODE_TABLE +
      "(a_id, suid, ext_attr_id, ext_attr_value) VALUES (?,?,?,?)";

  static final String INSERT_ANNOT_TO_EDGE = "INSERT INTO " + ANNOT_TO_EDGE_TABLE +
      "(a_id, suid, ext_attr_id, ext_attr_value) VALUES (?,?,?,?)";

  static final String INSERT_ANNOT_EXT_ATTR = "INSERT INTO " + ANNOT_EXT_ATTR_TABLE +
      "(id,name,type) VALUES(?,?,?)";

  static final String SELECT_ALL_EXT_ATTRS = "SELECT id, name, type, description FROM " +
      ANNOT_EXT_ATTR_TABLE + "";

  static final String SELECT_ALL_EXT_ATTRS_VALUES = "SELECT * FROM " + ANNOT_TO_NODE_TABLE +
      " UNION SELECT * FROM " + ANNOT_TO_EDGE_TABLE;

  static final String SELECT_EXT_ATTR_VALUES_WITH_ANNOT_ID = "SELECT * FROM " +
      ANNOT_TO_NODE_TABLE + " WHERE a_id = ? UNION SELECT * FROM " + ANNOT_TO_EDGE_TABLE + " " +
      "WHERE a_id = ?";

  static final String SEARCH_ANNOTATIONS = "SELECT * FROM " +
      ANNOT_TO_NODE_TABLE + " WHERE ext_attr_value LIKE %?% UNION SELECT * FROM "
      + ANNOT_TO_EDGE_TABLE + " " +
      "WHERE ext_attr_value LIKE %?%";
}
