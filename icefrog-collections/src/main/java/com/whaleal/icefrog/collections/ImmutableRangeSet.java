

package com.whaleal.icefrog.collections;

import com.whaleal.icefrog.core.collection.AbstractIterator;
import com.whaleal.icefrog.core.collection.IterUtil;
import com.whaleal.icefrog.core.collection.ListUtil;
import com.whaleal.icefrog.core.util.NumberUtil;

import javax.annotation.CheckForNull;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collector;

import static com.whaleal.icefrog.collections.SortedLists.KeyAbsentBehavior;
import static com.whaleal.icefrog.collections.SortedLists.KeyAbsentBehavior.NEXT_HIGHER;
import static com.whaleal.icefrog.collections.SortedLists.KeyAbsentBehavior.NEXT_LOWER;
import static com.whaleal.icefrog.collections.SortedLists.KeyPresentBehavior;
import static com.whaleal.icefrog.collections.SortedLists.KeyPresentBehavior.ANY_PRESENT;
import static com.whaleal.icefrog.core.lang.Preconditions.*;
import static java.util.Objects.requireNonNull;

/**
 * A {@link RangeSet} whose contents will never change, with many other important properties
 * detailed at {@link ImmutableCollection}.
 *
 * 
 * 
 */



public final class ImmutableRangeSet<C extends Comparable> extends AbstractRangeSet<C>
    implements Serializable {

  private static final ImmutableRangeSet<Comparable<?>> EMPTY =
      new ImmutableRangeSet<>(ImmutableList.of());

  private static final ImmutableRangeSet<Comparable<?>> ALL =
      new ImmutableRangeSet<>(ImmutableList.of(Range.all()));

  /**
   * Returns a {@code Collector} that accumulates the input elements into a new {@code
   * ImmutableRangeSet}. As in {@link Builder}, overlapping ranges are not permitted and adjacent
   * ranges will be merged.
   *
   * 
   */
  public static <E extends Comparable<? super E>>
      Collector<Range<E>, ?, ImmutableRangeSet<E>> toImmutableRangeSet() {
    return CollectCollectors.toImmutableRangeSet();
  }

  /**
   * Returns an empty immutable range set.
   *
   * <p><b>Performance note:</b> the instance returned is a singleton.
   */
  @SuppressWarnings("unchecked")
  public static <C extends Comparable> ImmutableRangeSet<C> of() {
    return (ImmutableRangeSet<C>) EMPTY;
  }

  /**
   * Returns an immutable range set containing the specified single range. If {@link Range#isEmpty()
   * range.isEmpty()}, this is equivalent to {@link ImmutableRangeSet#of()}.
   */
  public static <C extends Comparable> ImmutableRangeSet<C> of(Range<C> range) {
    checkNotNull(range);
    if (range.isEmpty()) {
      return of();
    } else if (range.equals(Range.all())) {
      return all();
    } else {
      return new ImmutableRangeSet<C>(ImmutableList.of(range));
    }
  }

  /** Returns an immutable range set containing the single range {@link Range#all()}. */
  @SuppressWarnings("unchecked")
  static <C extends Comparable> ImmutableRangeSet<C> all() {
    return (ImmutableRangeSet<C>) ALL;
  }

  /** Returns an immutable copy of the specified {@code RangeSet}. */
  public static <C extends Comparable> ImmutableRangeSet<C> copyOf(RangeSet<C> rangeSet) {
    checkNotNull(rangeSet);
    if (rangeSet.isEmpty()) {
      return of();
    } else if (rangeSet.encloses(Range.all())) {
      return all();
    }

    if (rangeSet instanceof ImmutableRangeSet) {
      ImmutableRangeSet<C> immutableRangeSet = (ImmutableRangeSet<C>) rangeSet;
      if (!immutableRangeSet.isPartialView()) {
        return immutableRangeSet;
      }
    }
    return new ImmutableRangeSet<C>(ImmutableList.copyOf(rangeSet.asRanges()));
  }

  /**
   * Returns an {@code ImmutableRangeSet} containing each of the specified disjoint ranges.
   * Overlapping ranges and empty ranges are forbidden, though adjacent ranges are permitted and
   * will be merged.
   *
   * @throws IllegalArgumentException if any ranges overlap or are empty
   * 
   */
  public static <C extends Comparable<?>> ImmutableRangeSet<C> copyOf(Iterable<Range<C>> ranges) {
    return new Builder<C>().addAll(ranges).build();
  }

  /**
   * Returns an {@code ImmutableRangeSet} representing the union of the specified ranges.
   *
   * <p>This is the smallest {@code RangeSet} which encloses each of the specified ranges. Duplicate
   * or connected ranges are permitted, and will be coalesced in the result.
   *
   * 
   */
  public static <C extends Comparable<?>> ImmutableRangeSet<C> unionOf(Iterable<Range<C>> ranges) {
    return copyOf(TreeRangeSet.create(ranges));
  }

  ImmutableRangeSet(ImmutableList<Range<C>> ranges) {
    this.ranges = ranges;
  }

  private ImmutableRangeSet(ImmutableList<Range<C>> ranges, ImmutableRangeSet<C> complement) {
    this.ranges = ranges;
    this.complement = complement;
  }

  private final transient ImmutableList<Range<C>> ranges;

  @Override
  public boolean intersects(Range<C> otherRange) {
    int ceilingIndex =
        SortedLists.binarySearch(
            ranges,
            Range.lowerBoundFn(),
            otherRange.lowerBound,
            Ordering.natural(),
            ANY_PRESENT,
            NEXT_HIGHER);
    if (ceilingIndex < ranges.size()
        && ranges.get(ceilingIndex).isConnected(otherRange)
        && !ranges.get(ceilingIndex).intersection(otherRange).isEmpty()) {
      return true;
    }
    return ceilingIndex > 0
        && ranges.get(ceilingIndex - 1).isConnected(otherRange)
        && !ranges.get(ceilingIndex - 1).intersection(otherRange).isEmpty();
  }

  @Override
  public boolean encloses(Range<C> otherRange) {
    int index =
        SortedLists.binarySearch(
            ranges,
            Range.lowerBoundFn(),
            otherRange.lowerBound,
            Ordering.natural(),
            ANY_PRESENT,
            NEXT_LOWER);
    return index != -1 && ranges.get(index).encloses(otherRange);
  }

  @Override
  @CheckForNull
  public Range<C> rangeContaining(C value) {
    int index =
        SortedLists.binarySearch(
            ranges,
            Range.lowerBoundFn(),
            Cut.belowValue(value),
            Ordering.natural(),
            ANY_PRESENT,
            NEXT_LOWER);
    if (index != -1) {
      Range<C> range = ranges.get(index);
      return range.contains(value) ? range : null;
    }
    return null;
  }

  @Override
  public Range<C> span() {
    if (ranges.isEmpty()) {
      throw new NoSuchElementException();
    }
    return Range.create(ranges.get(0).lowerBound, ranges.get(ranges.size() - 1).upperBound);
  }

  @Override
  public boolean isEmpty() {
    return ranges.isEmpty();
  }

  /**
   * Guaranteed to throw an exception and leave the {@code RangeSet} unmodified.
   *
   * @throws UnsupportedOperationException always
   * @deprecated Unsupported operation.
   */
  @Deprecated
  @Override

  public void add(Range<C> range) {
    throw new UnsupportedOperationException();
  }

  /**
   * Guaranteed to throw an exception and leave the {@code RangeSet} unmodified.
   *
   * @throws UnsupportedOperationException always
   * @deprecated Unsupported operation.
   */
  @Deprecated
  @Override

  public void addAll(RangeSet<C> other) {
    throw new UnsupportedOperationException();
  }

  /**
   * Guaranteed to throw an exception and leave the {@code RangeSet} unmodified.
   *
   * @throws UnsupportedOperationException always
   * @deprecated Unsupported operation.
   */
  @Deprecated
  @Override

  public void addAll(Iterable<Range<C>> other) {
    throw new UnsupportedOperationException();
  }

  /**
   * Guaranteed to throw an exception and leave the {@code RangeSet} unmodified.
   *
   * @throws UnsupportedOperationException always
   * @deprecated Unsupported operation.
   */
  @Deprecated
  @Override

  public void remove(Range<C> range) {
    throw new UnsupportedOperationException();
  }

  /**
   * Guaranteed to throw an exception and leave the {@code RangeSet} unmodified.
   *
   * @throws UnsupportedOperationException always
   * @deprecated Unsupported operation.
   */
  @Deprecated
  @Override

  public void removeAll(RangeSet<C> other) {
    throw new UnsupportedOperationException();
  }

  /**
   * Guaranteed to throw an exception and leave the {@code RangeSet} unmodified.
   *
   * @throws UnsupportedOperationException always
   * @deprecated Unsupported operation.
   */
  @Deprecated
  @Override

  public void removeAll(Iterable<Range<C>> other) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ImmutableSet<Range<C>> asRanges() {
    if (ranges.isEmpty()) {
      return ImmutableSet.of();
    }
    return new RegularImmutableSortedSet<>(ranges, Range.rangeLexOrdering());
  }

  @Override
  public ImmutableSet<Range<C>> asDescendingSetOfRanges() {
    if (ranges.isEmpty()) {
      return ImmutableSet.of();
    }
    return new RegularImmutableSortedSet<>(ranges.reverse(), Range.<C>rangeLexOrdering().reverse());
  }

   @CheckForNull private transient ImmutableRangeSet<C> complement;

  private final class ComplementRanges extends ImmutableList<Range<C>> {
    // True if the "positive" range set is empty or bounded below.
    private final boolean positiveBoundedBelow;

    // True if the "positive" range set is empty or bounded above.
    private final boolean positiveBoundedAbove;

    private final int size;

    ComplementRanges() {
      this.positiveBoundedBelow = ranges.get(0).hasLowerBound();
      this.positiveBoundedAbove = Iterables.getLast(ranges).hasUpperBound();

      int size = ranges.size() - 1;
      if (positiveBoundedBelow) {
        size++;
      }
      if (positiveBoundedAbove) {
        size++;
      }
      this.size = size;
    }

    @Override
    public int size() {
      return size;
    }

    @Override
    public Range<C> get(int index) {
      checkElementIndex(index, size);

      Cut<C> lowerBound;
      if (positiveBoundedBelow) {
        lowerBound = (index == 0) ? Cut.belowAll() : ranges.get(index - 1).upperBound;
      } else {
        lowerBound = ranges.get(index).upperBound;
      }

      Cut<C> upperBound;
      if (positiveBoundedAbove && index == size - 1) {
        upperBound = Cut.aboveAll();
      } else {
        upperBound = ranges.get(index + (positiveBoundedBelow ? 0 : 1)).lowerBound;
      }

      return Range.create(lowerBound, upperBound);
    }

    @Override
    boolean isPartialView() {
      return true;
    }
  }

  @Override
  public ImmutableRangeSet<C> complement() {
    ImmutableRangeSet<C> result = complement;
    if (result != null) {
      return result;
    } else if (ranges.isEmpty()) {
      return complement = all();
    } else if (ranges.size() == 1 && ranges.get(0).equals(Range.all())) {
      return complement = of();
    } else {
      ImmutableList<Range<C>> complementRanges = new ComplementRanges();
      result = complement = new ImmutableRangeSet<C>(complementRanges, this);
    }
    return result;
  }

  /**
   * Returns a new range set consisting of the union of this range set and {@code other}.
   *
   * <p>This is essentially the same as {@code TreeRangeSet.create(this).addAll(other)} except it
   * returns an {@code ImmutableRangeSet}.
   *
   * 
   */
  public ImmutableRangeSet<C> union(RangeSet<C> other) {
    return unionOf(Iterables.concat(asRanges(), other.asRanges()));
  }

  /**
   * Returns a new range set consisting of the intersection of this range set and {@code other}.
   *
   * <p>This is essentially the same as {@code
   * TreeRangeSet.create(this).removeAll(other.complement())} except it returns an {@code
   * ImmutableRangeSet}.
   *
   * 
   */
  public ImmutableRangeSet<C> intersection(RangeSet<C> other) {
    RangeSet<C> copy = TreeRangeSet.create(this);
    copy.removeAll(other.complement());
    return copyOf(copy);
  }

  /**
   * Returns a new range set consisting of the difference of this range set and {@code other}.
   *
   * <p>This is essentially the same as {@code TreeRangeSet.create(this).removeAll(other)} except it
   * returns an {@code ImmutableRangeSet}.
   *
   * 
   */
  public ImmutableRangeSet<C> difference(RangeSet<C> other) {
    RangeSet<C> copy = TreeRangeSet.create(this);
    copy.removeAll(other);
    return copyOf(copy);
  }

  /**
   * Returns a list containing the nonempty intersections of {@code range} with the ranges in this
   * range set.
   */
  private ImmutableList<Range<C>> intersectRanges(final Range<C> range) {
    if (ranges.isEmpty() || range.isEmpty()) {
      return ImmutableList.of();
    } else if (range.encloses(span())) {
      return ranges;
    }

    final int fromIndex;
    if (range.hasLowerBound()) {
      fromIndex =
          SortedLists.binarySearch(
              ranges,
              Range.upperBoundFn(),
              range.lowerBound,
              KeyPresentBehavior.FIRST_AFTER,
              KeyAbsentBehavior.NEXT_HIGHER);
    } else {
      fromIndex = 0;
    }

    int toIndex;
    if (range.hasUpperBound()) {
      toIndex =
          SortedLists.binarySearch(
              ranges,
              Range.lowerBoundFn(),
              range.upperBound,
              KeyPresentBehavior.FIRST_PRESENT,
              KeyAbsentBehavior.NEXT_HIGHER);
    } else {
      toIndex = ranges.size();
    }
    final int length = toIndex - fromIndex;
    if (length == 0) {
      return ImmutableList.of();
    } else {
      return new ImmutableList<Range<C>>() {
        @Override
        public int size() {
          return length;
        }

        @Override
        public Range<C> get(int index) {
          checkElementIndex(index, length);
          if (index == 0 || index == length - 1) {
            return ranges.get(index + fromIndex).intersection(range);
          } else {
            return ranges.get(index + fromIndex);
          }
        }

        @Override
        boolean isPartialView() {
          return true;
        }
      };
    }
  }

  /** Returns a view of the intersection of this range set with the given range. */
  @Override
  public ImmutableRangeSet<C> subRangeSet(Range<C> range) {
    if (!isEmpty()) {
      Range<C> span = span();
      if (range.encloses(span)) {
        return this;
      } else if (range.isConnected(span)) {
        return new ImmutableRangeSet<C>(intersectRanges(range));
      }
    }
    return of();
  }

  /**
   * Returns an {@link ImmutableSortedSet} containing the same values in the given domain
   * {@linkplain RangeSet#contains contained} by this range set.
   *
   * <p><b>Note:</b> {@code a.asSet(d).equals(b.asSet(d))} does not imply {@code a.equals(b)}! For
   * example, {@code a} and {@code b} could be {@code [2..4]} and {@code (1..5)}, or the empty
   * ranges {@code [3..3)} and {@code [4..4)}.
   *
   * <p><b>Warning:</b> Be extremely careful what you do with the {@code asSet} view of a large
   * range set (such as {@code ImmutableRangeSet.of(Range.greaterThan(0))}). Certain operations on
   * such a set can be performed efficiently, but others (such as {@link Set#hashCode} or {@link
   * Collections#frequency}) can cause major performance problems.
   *
   * <p>The returned set's {@link Object#toString} method returns a short-hand form of the set's
   * contents, such as {@code "[1..100]}"}.
   *
   * @throws IllegalArgumentException if neither this range nor the domain has a lower bound, or if
   *     neither has an upper bound
   */
  public ImmutableSortedSet<C> asSet(DiscreteDomain<C> domain) {
    checkNotNull(domain);
    if (isEmpty()) {
      return ImmutableSortedSet.of();
    }
    Range<C> span = span().canonical(domain);
    if (!span.hasLowerBound()) {
      // according to the spec of canonical, neither this ImmutableRangeSet nor
      // the range have a lower bound
      throw new IllegalArgumentException(
          "Neither the DiscreteDomain nor this range set are bounded below");
    } else if (!span.hasUpperBound()) {
      try {
        domain.maxValue();
      } catch (NoSuchElementException e) {
        throw new IllegalArgumentException(
            "Neither the DiscreteDomain nor this range set are bounded above");
      }
    }

    return new AsSet(domain);
  }

  private final class AsSet extends ImmutableSortedSet<C> {
    private final DiscreteDomain<C> domain;

    AsSet(DiscreteDomain<C> domain) {
      super(Ordering.natural());
      this.domain = domain;
    }

    @CheckForNull private transient Integer size;

    @Override
    public int size() {
      // racy single-check idiom
      Integer result = size;
      if (result == null) {
        long total = 0;
        for (Range<C> range : ranges) {
          total += ContiguousSet.create(range, domain).size();
          if (total >= Integer.MAX_VALUE) {
            break;
          }
        }
        result = size =(int)NumberUtil.saturatedCast(total,Integer.class);
      }
      return result.intValue();
    }

    @Override
    public Iterator<C> iterator() {
      return new AbstractIterator<C>() {
        final Iterator<Range<C>> rangeItr = ranges.iterator();
        Iterator<C> elemItr = Iterators.emptyIterator();

        @Override
        @CheckForNull
        protected C computeNext() {
          while (!elemItr.hasNext()) {
            if (rangeItr.hasNext()) {
              elemItr = ContiguousSet.create(rangeItr.next(), domain).iterator();
            } else {
              return endOfData();
            }
          }
          return elemItr.next();
        }
      };
    }

    @Override

    public Iterator<C> descendingIterator() {
      return new AbstractIterator<C>() {
        final Iterator<Range<C>> rangeItr = ranges.reverse().iterator();
        Iterator<C> elemItr = Iterators.emptyIterator();

        @Override
        @CheckForNull
        protected C computeNext() {
          while (!elemItr.hasNext()) {
            if (rangeItr.hasNext()) {
              elemItr = ContiguousSet.create(rangeItr.next(), domain).descendingIterator();
            } else {
              return endOfData();
            }
          }
          return elemItr.next();
        }
      };
    }

    ImmutableSortedSet<C> subSet(Range<C> range) {
      return subRangeSet(range).asSet(domain);
    }

    @Override
    ImmutableSortedSet<C> headSetImpl(C toElement, boolean inclusive) {
      return subSet(Range.upTo(toElement, BoundType.forBoolean(inclusive)));
    }

    @Override
    ImmutableSortedSet<C> subSetImpl(
        C fromElement, boolean fromInclusive, C toElement, boolean toInclusive) {
      if (!fromInclusive && !toInclusive && Range.compareOrThrow(fromElement, toElement) == 0) {
        return ImmutableSortedSet.of();
      }
      return subSet(
          Range.range(
              fromElement, BoundType.forBoolean(fromInclusive),
              toElement, BoundType.forBoolean(toInclusive)));
    }

    @Override
    ImmutableSortedSet<C> tailSetImpl(C fromElement, boolean inclusive) {
      return subSet(Range.downTo(fromElement, BoundType.forBoolean(inclusive)));
    }

    @Override
    public boolean contains(@CheckForNull Object o) {
      if (o == null) {
        return false;
      }
      try {
        @SuppressWarnings("unchecked") // we catch CCE's
        C c = (C) o;
        return ImmutableRangeSet.this.contains(c);
      } catch (ClassCastException e) {
        return false;
      }
    }

    @Override
    int indexOf(@CheckForNull Object target) {
      if (contains(target)) {
        @SuppressWarnings("unchecked") // if it's contained, it's definitely a C
        C c = (C) requireNonNull(target);
        long total = 0;
        for (Range<C> range : ranges) {
          if (range.contains(c)) {
            return (int)NumberUtil.saturatedCast(total + ContiguousSet.create(range, domain).indexOf(c),Integer.class);
          } else {
            total += ContiguousSet.create(range, domain).size();
          }
        }
        throw new AssertionError("impossible");
      }
      return -1;
    }

    @Override
    ImmutableSortedSet<C> createDescendingSet() {
      return new DescendingImmutableSortedSet<C>(this);
    }

    @Override
    boolean isPartialView() {
      return ranges.isPartialView();
    }

    @Override
    public String toString() {
      return ranges.toString();
    }

    @Override
    Object writeReplace() {
      return new AsSetSerializedForm<C>(ranges, domain);
    }
  }

  private static class AsSetSerializedForm<C extends Comparable> implements Serializable {
    private final ImmutableList<Range<C>> ranges;
    private final DiscreteDomain<C> domain;

    AsSetSerializedForm(ImmutableList<Range<C>> ranges, DiscreteDomain<C> domain) {
      this.ranges = ranges;
      this.domain = domain;
    }

    Object readResolve() {
      return new ImmutableRangeSet<C>(ranges).asSet(domain);
    }
  }

  /**
   * Returns {@code true} if this immutable range set's implementation contains references to
   * user-created objects that aren't accessible via this range set's methods. This is generally
   * used to determine whether {@code copyOf} implementations should make an explicit copy to avoid
   * memory leaks.
   */
  boolean isPartialView() {
    return ranges.isPartialView();
  }

  /** Returns a new builder for an immutable range set. */
  public static <C extends Comparable<?>> Builder<C> builder() {
    return new Builder<C>();
  }

  /**
   * A builder for immutable range sets.
   *
   * 
   */
  public static class Builder<C extends Comparable<?>> {
    private final List<Range<C>> ranges;

    public Builder() {
      this.ranges = ListUtil.list(false);
    }

    // TODO(lowasser): consider adding union, in addition to add, that does allow overlap

    /**
     * Add the specified range to this builder. Adjacent ranges are permitted and will be merged,
     * but overlapping ranges will cause an exception when {@link #build()} is called.
     *
     * @throws IllegalArgumentException if {@code range} is empty
     */

    public Builder<C> add(Range<C> range) {
      checkArgument(!range.isEmpty(), "range must not be empty, but was %s", range);
      ranges.add(range);
      return this;
    }

    /**
     * Add all ranges from the specified range set to this builder. Adjacent ranges are permitted
     * and will be merged, but overlapping ranges will cause an exception when {@link #build()} is
     * called.
     */

    public Builder<C> addAll(RangeSet<C> ranges) {
      return addAll(ranges.asRanges());
    }

    /**
     * Add all of the specified ranges to this builder. Adjacent ranges are permitted and will be
     * merged, but overlapping ranges will cause an exception when {@link #build()} is called.
     *
     * @throws IllegalArgumentException if any inserted ranges are empty
     * 
     */

    public Builder<C> addAll(Iterable<Range<C>> ranges) {
      for (Range<C> range : ranges) {
        add(range);
      }
      return this;
    }


    Builder<C> combine(Builder<C> builder) {
      addAll(builder.ranges);
      return this;
    }

    /**
     * Returns an {@code ImmutableRangeSet} containing the ranges added to this builder.
     *
     * @throws IllegalArgumentException if any input ranges have nonempty overlap
     */
    public ImmutableRangeSet<C> build() {
      ImmutableList.Builder<Range<C>> mergedRangesBuilder =
          new ImmutableList.Builder<>(ranges.size());
      Collections.sort(ranges, Range.rangeLexOrdering());
      PeekingIterator<Range<C>> peekingItr = Iterators.peekingIterator(ranges.iterator());
      while (peekingItr.hasNext()) {
        Range<C> range = peekingItr.next();
        while (peekingItr.hasNext()) {
          Range<C> nextRange = peekingItr.peek();
          if (range.isConnected(nextRange)) {
            checkArgument(
                range.intersection(nextRange).isEmpty(),
                "Overlapping ranges not permitted but found %s overlapping %s",
                range,
                nextRange);
            range = range.span(peekingItr.next());
          } else {
            break;
          }
        }
        mergedRangesBuilder.add(range);
      }
      ImmutableList<Range<C>> mergedRanges = mergedRangesBuilder.build();
      if (mergedRanges.isEmpty()) {
        return of();
      } else if (mergedRanges.size() == 1
          && IterUtil.getOnlyElement(mergedRanges).equals(Range.all())) {
        return all();
      } else {
        return new ImmutableRangeSet<C>(mergedRanges);
      }
    }
  }

  private static final class SerializedForm<C extends Comparable> implements Serializable {
    private final ImmutableList<Range<C>> ranges;

    SerializedForm(ImmutableList<Range<C>> ranges) {
      this.ranges = ranges;
    }

    Object readResolve() {
      if (ranges.isEmpty()) {
        return of();
      } else if (ranges.equals(ImmutableList.of(Range.all()))) {
        return all();
      } else {
        return new ImmutableRangeSet<C>(ranges);
      }
    }
  }

  Object writeReplace() {
    return new SerializedForm<C>(ranges);
  }
}
