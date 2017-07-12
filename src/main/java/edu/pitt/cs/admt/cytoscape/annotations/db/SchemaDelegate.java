package edu.pitt.cs.admt.cytoscape.annotations.db;

/**
 * Created by Nikos R. Katsipoulakis on 7/12/17.
 */
public class SchemaDelegate {
  
  private static final String CREATE_NODE_TABLE = "CREATE TABLE node (" +
      "suid INTEGER PRIMARY KEY, " +
      "CONSTRAINT node_positive CHECK(suid >= 0) )";
  
  private static final String CREATE_EDGE_TABLE = "CREATE TABLE edge (" +
      "suid INTEGER PRIMARY KEY, " +
      "source INTEGER NOT NULL, " +
      "destination INTEGER NOT NULL, " +
      "CONSTRAINT id_edge CHECK(suid >= 0), " +
      "FOREIGN KEY (source) REFERENCES node(suid) ON DELETE CASCADE ON UPDATE CASCADE, " +
      "FOREIGN KEY (destination) REFERENCES node(suid) ON DELETE CASCADE ON UPDATE CASCADE )";
  
  private static final String CREATE_ANNOT_TABLE = "CREATE TABLE annotation (" +
      "suid INTEGER PRIMARY KEY, " +
      "description VARCHAR(64), " +
      "CONSTRAINT id_annot CHECK(suid >= 0) )";
  
  private static final String CREATE_ANNOT_TO_NODE_TABLE = "CREATE TABLE annot_to_node (" +
      "a_id INTEGER, " +
      "suid INTEGER, " +
      "FOREIGN KEY (a_id) REFERENCES annotation(suid) ON DELETE CASCADE ON UPDATE CASCADE, " +
      "FOREIGN KEY (suid) REFERENCES node(suid) ON DELETE CASCADE ON UPDATE CASCADE )";
  
  private static final String CREATE_ANNOT_TO_EDGE_TABLE = "CREATE TABLE annot_to_edge (" +
      "a_id INTEGER, " +
      "suid INTEGER, " +
      "FOREIGN KEY (a_id) REFERENCES annotation(suid) ON DELETE CASCADE ON UPDATE CASCADE, " +
      "FOREIGN KEY (suid) REFERENCES edge(suid) ON DELETE CASCADE ON UPDATE CASCADE )";
  
  private static final String CREATE_ANNOT_EXT_ATTR_TABLE = "CREATE TABLE annot_ext_attr (" +
      "id INTEGER PRIMARY KEY, " +
      "name VARCHAR(32) NOT NULL, " +
      "type VARCHAR(16) NOT NULL CHECK(type in ('BOOLEAN', 'INT', 'FLOAT', 'CHAR', 'STRING')), " +
      "description VARCHAR(64))";
  
  private static final String CREATE_ANNOT_EXT_ATTR_VAL = "CREATE TABLE ANNOT_EXT_ATTR_VAL (" +
      "id INTEGER, " +
      "attr_id INTEGER, " +
      "value LONGVARBINARY, " +
      "FOREIGN KEY (id) REFERENCES annotation(suid) ON DELETE CASCADE ON UPDATE DELETE, " +
      "FOREIGN KEY (attr_id) REFERENCES annot_ext_attr(id) ON DELETE CASCADE ON UPDATE DELETE )";
  
}
