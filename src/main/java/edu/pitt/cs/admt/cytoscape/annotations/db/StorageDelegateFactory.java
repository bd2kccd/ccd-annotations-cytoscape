package edu.pitt.cs.admt.cytoscape.annotations.db;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Nikos R. Katsipoulakis
 */
public enum StorageDelegateFactory {

  INSTANCE;

  //  private static final ConcurrentHashMap<String, StorageDelegate> index = new ConcurrentHashMap<>();
  private static final ConcurrentHashMap<Long, StorageDelegate> index = new ConcurrentHashMap<>(0);

//  public static StorageDelegate newDelegate() {
//    StorageDelegate delegate = new StorageDelegate();
//    index.putIfAbsent(delegate.getId(), delegate);
//    return delegate;
//  }

  public static StorageDelegate newDelegate(final Long networkSUID) {
    StorageDelegate delegate = new StorageDelegate(networkSUID);
    index.putIfAbsent(networkSUID, delegate);
    return delegate;
  }

  public static Optional<StorageDelegate> getDelegate(final Long networkSUID) {
    return Optional.ofNullable(index.get(networkSUID));
  }

  public static boolean destroyDelegate(StorageDelegate delegate) {
    return index.remove(delegate.getNetwork(), delegate);
  }

  public static boolean destroyDelegateByNetwork(StorageDelegate delegate) {
    return index.remove(delegate.getNetwork(), delegate);
  }

}
