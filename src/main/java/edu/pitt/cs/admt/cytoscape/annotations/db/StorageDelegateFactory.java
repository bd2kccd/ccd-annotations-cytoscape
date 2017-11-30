package edu.pitt.cs.admt.cytoscape.annotations.db;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Nikos R. Katsipoulakis
 */
public enum StorageDelegateFactory {

  INSTANCE;

  private ConcurrentHashMap<String, StorageDelegate> index;

  StorageDelegateFactory() {
    index = new ConcurrentHashMap<>();
  }

  public StorageDelegate newDelegate() {
    StorageDelegate delegate = new StorageDelegate();
    index.putIfAbsent(delegate.getId(), delegate);
    return index.get(delegate.getId());
  }

  public void destroyDelegate(String id) {
    index.remove(id);
  }

}
