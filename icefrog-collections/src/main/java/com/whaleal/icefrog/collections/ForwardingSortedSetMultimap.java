

package com.whaleal.icefrog.collections;

import javax.annotation.CheckForNull;
import java.util.Comparator;
import java.util.SortedSet;






/**
 * A sorted set multimap which forwards all its method calls to another sorted set multimap.
 * Subclasses should override one or more methods to modify the behavior of the backing multimap as
 * desired per the <a href="http://en.wikipedia.org/wiki/Decorator_pattern">decorator pattern</a>.
 *
 * <p><b>{@code default} method warning:</b> This class does <i>not</i> forward calls to {@code
 * default} methods. Instead, it inherits their default implementations. When those implementations
 * invoke methods, they invoke methods on the {@code ForwardingSortedSetMultimap}.
 *
 * @author Kurt Alfred Kluever
 * 
 */


public abstract class ForwardingSortedSetMultimap<
        K extends Object, V extends Object>
    extends ForwardingSetMultimap<K, V> implements SortedSetMultimap<K, V> {

  /** Constructor for use by subclasses. */
  protected ForwardingSortedSetMultimap() {}

  @Override
  protected abstract SortedSetMultimap<K, V> delegate();

  @Override
  public SortedSet<V> get(@ParametricNullness K key) {
    return delegate().get(key);
  }

  @Override
  public SortedSet<V> removeAll(@CheckForNull Object key) {
    return delegate().removeAll(key);
  }

  @Override
  public SortedSet<V> replaceValues(@ParametricNullness K key, Iterable<? extends V> values) {
    return delegate().replaceValues(key, values);
  }

  @Override
  @CheckForNull
  public Comparator<? super V> valueComparator() {
    return delegate().valueComparator();
  }
}
