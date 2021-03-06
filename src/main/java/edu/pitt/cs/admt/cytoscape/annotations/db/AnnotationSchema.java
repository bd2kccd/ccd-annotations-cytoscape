package edu.pitt.cs.admt.cytoscape.annotations.db;

/**
 * @author Nikos R. Katsipoulakis
 */
class AnnotationSchema {

  private static final String NODE_TABLE = "NODE";

  private static final String EDGE_TABLE = "EDGE";

  private static final String ANNOTATION_TABLE = "ANNOTATION";

  private static final String ANNOT_TO_NODE_TABLE = "ANNOT_TO_NODE";

  private static final String ANNOT_TO_EDGE_TABLE = "ANNOT_TO_EDGE";

  static final String DROP_NODE_TABLE = "DROP TABLE " + NODE_TABLE + " IF EXISTS CASCADE";

  static final String DROP_EDGE_TABLE = "DROP TABLE " + EDGE_TABLE + " IF EXISTS CASCADE";

  static final String DROP_ANNOT_TABLE = "DROP TABLE " + ANNOTATION_TABLE + " IF EXISTS CASCADE";

  static final String DROP_ANNOT_TO_NODE_TABLE = "DROP TABLE " + ANNOT_TO_NODE_TABLE +
      " IF EXISTS CASCADE";

  static final String DROP_ANNOT_TO_EDGE_TABLE = "DROP TABLE " + ANNOT_TO_EDGE_TABLE +
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
      "name VARCHAR(32) NOT NULL, " +
      "type VARCHAR(16) NOT NULL CHECK(type in ('BOOLEAN', 'INT', 'FLOAT', 'CHAR', 'STRING')), " +
      "description VARCHAR(64) DEFAULT 'N/A', " +
      "CONSTRAINT annotation_name_unique UNIQUE (name)" +
      ")";

  static final String CREATE_ANNOT_TO_NODE_TABLE = "CREATE TABLE " + ANNOT_TO_NODE_TABLE +
      "(" +
      "a_id UUID NOT NULL, " +
      "cy_a_id UUID, " +
      "suid INTEGER NOT NULL, " +
      "value LONGVARBINARY, " +
      "FOREIGN KEY (a_id) REFERENCES " + ANNOTATION_TABLE +
      "(id) ON DELETE CASCADE ON UPDATE CASCADE, " +
      "FOREIGN KEY (suid) REFERENCES " + NODE_TABLE +
      "(suid) ON DELETE CASCADE ON UPDATE CASCADE," +
      "CONSTRAINT ann_node_unique UNIQUE (a_id, suid) " +
      ")";

  static final String CREATE_ANNOT_TO_EDGE_TABLE = "CREATE TABLE " + ANNOT_TO_EDGE_TABLE
      + " (" +
      "a_id UUID NOT NULL, " +
      "cy_a_id UUID, " +
      "suid INTEGER NOT NULL, " +
      "value LONGVARBINARY, " +
      "FOREIGN KEY (a_id) REFERENCES " + ANNOTATION_TABLE +
      "(id) ON DELETE CASCADE ON UPDATE CASCADE, " +
      "FOREIGN KEY (suid) REFERENCES " + EDGE_TABLE +
      "(suid) ON DELETE CASCADE ON UPDATE CASCADE, " +
      "CONSTRAINT ann_edge_unique UNIQUE (a_id, suid) " +
      " )";

  static final String INSERT_NODE = "INSERT INTO " + NODE_TABLE + "(suid) VALUES (?)";

  static final String INSERT_EDGE = "INSERT INTO " + EDGE_TABLE + "(suid, source, destination) " +
      "VALUES (?,?,?)";

  static final String INSERT_ANNOT = "INSERT INTO " + ANNOTATION_TABLE +
      "(id, name, type, description) VALUES (?,?,?,?)";

  static final String INSERT_ANNOT_TO_NODE = "INSERT INTO " + ANNOT_TO_NODE_TABLE +
      "(a_id, cy_a_id, suid, value) VALUES (?,?,?,?)";

  static final String SELECT_ANNOT_TO_NODE = "SELECT a_id, cy_a_id, suid, value FROM " +
      ANNOT_TO_NODE_TABLE;

  static final String SELECT_ANNOT_TO_EDGE = "SELECT a_id, cy_a_id, suid, value FROM " +
      ANNOT_TO_EDGE_TABLE;

  static final String INSERT_ANNOT_TO_EDGE = "INSERT INTO " + ANNOT_TO_EDGE_TABLE +
      "(a_id, cy_a_id, suid, value) VALUES (?,?,?,?)";

  static final String SELECT_ALL_NODES = "SELECT suid FROM " + NODE_TABLE;

  static final String SELECT_ALL_EDGES = "SELECT suid, source, destination FROM " + EDGE_TABLE;

  static final String SELECT_ALL_ANNOTATIONS = "SELECT id, name, type, description FROM " +
      ANNOTATION_TABLE;

  static final String SELECT_ANNOTATION = "SELECT id, name, type, description FROM " +
      ANNOTATION_TABLE + " WHERE id = ?";

  static final String SELECT_ANNOTATION_BY_NAME = "SELECT id, name, type, description FROM " +
      ANNOTATION_TABLE + " WHERE name LIKE ?";

  static final String SELECT_ALL_ANNOT_VALUES = "SELECT a_id, cy_a_id, suid, value FROM " +
      ANNOT_TO_NODE_TABLE +
      " UNION SELECT a_id, cy_a_id, suid, value FROM " + ANNOT_TO_EDGE_TABLE;

  static final String SELECT_ANNOT_VALUES_WITH_ANNOT_ID = "SELECT a_id, cy_a_id, suid, " +
      "value FROM " +
      ANNOT_TO_NODE_TABLE + " WHERE a_id = ? UNION SELECT a_id, cy_a_id, suid, value FROM " +
      ANNOT_TO_EDGE_TABLE + " " + "WHERE a_id = ?";

  static final String SELECT_ANNOT_TO_NODES_ON_NAME = "SELECT a_id, cy_a_id, suid, value FROM " +
      ANNOT_TO_NODE_TABLE + " JOIN " + ANNOTATION_TABLE + " ON " + ANNOT_TO_NODE_TABLE + ".a_id = "
      +
      ANNOTATION_TABLE + ".id WHERE " + ANNOTATION_TABLE + ".name = ?";

  static final String SEARCH_ANNOT_TO_NODES = "SELECT a_id, cy_a_id, suid, value FROM " +
      ANNOT_TO_NODE_TABLE + " JOIN " + ANNOTATION_TABLE + " ON " + ANNOT_TO_NODE_TABLE + ".a_id = "
      +
      ANNOTATION_TABLE + ".id WHERE " + ANNOTATION_TABLE + ".name LIKE ?"
      +
      " OR " + ANNOTATION_TABLE + ".description LIKE ?";

  static final String SELECT_ANNOT_TO_EDGES_ON_NAME = "SELECT a_id, cy_a_id, suid, value FROM " +
      ANNOT_TO_EDGE_TABLE + " JOIN " + ANNOTATION_TABLE + " ON " + ANNOT_TO_EDGE_TABLE + ".a_id = "
      +
      ANNOTATION_TABLE + ".id WHERE " + ANNOTATION_TABLE + ".name = ?";

  static final String SEARCH_ANNOT_TO_EDGES = "SELECT a_id, cy_a_id, suid, value FROM " +
      ANNOT_TO_EDGE_TABLE + " JOIN " + ANNOTATION_TABLE + " ON " + ANNOT_TO_EDGE_TABLE + ".a_id = "
      +
      ANNOTATION_TABLE + ".id WHERE " + ANNOTATION_TABLE + ".name LIKE ?"
      +
      " OR " + ANNOTATION_TABLE + ".description LIKE ?";

  static final String SELECT_ANNOT_TO_EDGES = "SELECT a_id, cy_a_id, suid, value FROM " +
      ANNOT_TO_EDGE_TABLE + " JOIN " + ANNOTATION_TABLE + " ON " + ANNOT_TO_EDGE_TABLE + ".a_id = "
      +
      ANNOTATION_TABLE + ".id";
  
  static final String NETWORK_COMPONENTS_BASED_ON_CY_ID = "SELECT suid FROM " +
      ANNOT_TO_NODE_TABLE + " WHERE cy_a_id = ? UNION SELECT suid FROM " + ANNOT_TO_EDGE_TABLE +
      " WHERE cy_a_id = ?";
  
  static final String SELECT_ANNOT_TO_NODES_ON_ANNOT_ID = "SELECT a_id, cy_a_id, suid, value FROM" +
      " " + ANNOT_TO_NODE_TABLE + " WHERE suid = ?";
  
  static final String SELECT_ANNOT_TO_EDGES_ON_ANNOT_ID = "SELECT a_id, cy_a_id, suid, value FROM" +
      " " + ANNOT_TO_EDGE_TABLE + " WHERE suid = ?";
}
