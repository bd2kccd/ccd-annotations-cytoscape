package edu.pitt.cs.admt.cytoscape.annotations.db;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Nikos R. Katsipoulakis
 */
public enum StorageDelegateFactory {

  INSTANCE;

  private static final ConcurrentHashMap<String, StorageDelegate> index = new ConcurrentHashMap<>();

  public static StorageDelegate newDelegate() {
    StorageDelegate delegate = new StorageDelegate();
    return index.putIfAbsent(delegate.getId(), delegate);
  }

  public static boolean destroyDelegate(StorageDelegate delegate) {
    return index.remove(delegate.getId(), delegate);
  }

}
