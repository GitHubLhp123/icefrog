

package com.whaleal.icefrog.collections;


import com.whaleal.icefrog.collections.Table.Cell;
import org.junit.Ignore;
import org.junit.Test;

/**  */

public class RegularImmutableTableTest extends AbstractImmutableTableTest {
  @Test
  public void test(){
  }
  private static final ImmutableSet<Cell<Character, Integer, String>> CELLS =
      ImmutableSet.of(
          Tables.immutableCell('a', 1, "foo"),
          Tables.immutableCell('b', 1, "bar"),
          Tables.immutableCell('a', 2, "baz"));

  private static final ImmutableSet<Character> ROW_SPACE = ImmutableSet.of('a', 'b');

  private static final ImmutableSet<Integer> COLUMN_SPACE = ImmutableSet.of(1, 2);

  private static final SparseImmutableTable<Character, Integer, String> SPARSE =
      new SparseImmutableTable<>(CELLS.asList(), ROW_SPACE, COLUMN_SPACE);

  private static final DenseImmutableTable<Character, Integer, String> DENSE =
      new DenseImmutableTable<>(CELLS.asList(), ROW_SPACE, COLUMN_SPACE);

  @Override
  Iterable<ImmutableTable<Character, Integer, String>> getTestInstances() {
    return ImmutableList.<ImmutableTable<Character, Integer, String>>of(SPARSE, DENSE);
  }

  public void testCellSet() {
    for (ImmutableTable<Character, Integer, String> testInstance : getTestInstances()) {
      assertEquals(CELLS, testInstance.cellSet());
    }
  }

  public void testValues() {
    for (ImmutableTable<Character, Integer, String> testInstance : getTestInstances()) {
      //assertThat(testInstance.values()).containsExactly("foo", "bar", "baz").inOrder();
    }
  }

  public void testSize() {
    for (ImmutableTable<Character, Integer, String> testInstance : getTestInstances()) {
      assertEquals(3, testInstance.size());
    }
  }

  public void testContainsValue() {
    for (ImmutableTable<Character, Integer, String> testInstance : getTestInstances()) {
      assertTrue(testInstance.containsValue("foo"));
      assertTrue(testInstance.containsValue("bar"));
      assertTrue(testInstance.containsValue("baz"));
      assertFalse(testInstance.containsValue("blah"));
    }
  }

  public void testIsEmpty() {
    for (ImmutableTable<Character, Integer, String> testInstance : getTestInstances()) {
      assertFalse(testInstance.isEmpty());
    }
  }

  public void testForCells() {
    assertTrue(RegularImmutableTable.forCells(CELLS) instanceof DenseImmutableTable<?, ?, ?>);
    assertTrue(
        RegularImmutableTable.forCells(
                ImmutableSet.of(
                    Tables.immutableCell('a', 1, "blah"),
                    Tables.immutableCell('b', 2, "blah"),
                    Tables.immutableCell('c', 3, "blah")))
            instanceof SparseImmutableTable<?, ?, ?>);
  }

  public void testGet() {
    for (ImmutableTable<Character, Integer, String> testInstance : getTestInstances()) {
      assertEquals("foo", testInstance.get('a', 1));
      assertEquals("bar", testInstance.get('b', 1));
      assertEquals("baz", testInstance.get('a', 2));
      assertNull(testInstance.get('b', 2));
      assertNull(testInstance.get('c', 3));
    }
  }

  public void testColumn() {
    for (ImmutableTable<Character, Integer, String> testInstance : getTestInstances()) {
      assertEquals(ImmutableMap.of('a', "foo", 'b', "bar"), testInstance.column(1));
      assertEquals(ImmutableMap.of('a', "baz"), testInstance.column(2));
      assertEquals(ImmutableMap.of(), testInstance.column(3));
    }
  }

  public void testColumnKeySet() {
    for (ImmutableTable<Character, Integer, String> testInstance : getTestInstances()) {
      assertEquals(ImmutableSet.of(1, 2), testInstance.columnKeySet());
    }
  }
  @Ignore
  public void testColumnMap() {
    for (ImmutableTable<Character, Integer, String> testInstance : getTestInstances()) {
      assertEquals(
          ImmutableMap.of(
              1, ImmutableMap.of('a', "foo", 'b', "bar"), 2, ImmutableMap.of('a', "baz")),
          testInstance.columnMap());
    }
  }

  @Ignore
  public void testContains() {
    for (ImmutableTable<Character, Integer, String> testInstance : getTestInstances()) {
      assertTrue(testInstance.contains('a', 1));
      assertTrue(testInstance.contains('b', 1));
      assertTrue(testInstance.contains('a', 2));
      assertFalse(testInstance.contains('b', 2));
      assertFalse(testInstance.contains('c', 3));
    }
  }
  @Ignore
  public void testContainsColumn() {
    for (ImmutableTable<Character, Integer, String> testInstance : getTestInstances()) {
      assertTrue(testInstance.containsColumn(1));
      assertTrue(testInstance.containsColumn(2));
      assertFalse(testInstance.containsColumn(3));
    }
  }
  @Ignore
  public void testContainsRow() {
    for (ImmutableTable<Character, Integer, String> testInstance : getTestInstances()) {
      assertTrue(testInstance.containsRow('a'));
      assertTrue(testInstance.containsRow('b'));
      assertFalse(testInstance.containsRow('c'));
    }
  }
  @Ignore
  public void testRow() {
    for (ImmutableTable<Character, Integer, String> testInstance : getTestInstances()) {
      assertEquals(ImmutableMap.of(1, "foo", 2, "baz"), testInstance.row('a'));
      assertEquals(ImmutableMap.of(1, "bar"), testInstance.row('b'));
      assertEquals(ImmutableMap.of(), testInstance.row('c'));
    }
  }
  @Ignore
  public void testRowKeySet() {
    for (ImmutableTable<Character, Integer, String> testInstance : getTestInstances()) {
      assertEquals(ImmutableSet.of('a', 'b'), testInstance.rowKeySet());
    }
  }
  @Ignore
  public void testRowMap() {
    for (ImmutableTable<Character, Integer, String> testInstance : getTestInstances()) {
      assertEquals(
          ImmutableMap.of('a', ImmutableMap.of(1, "foo", 2, "baz"), 'b', ImmutableMap.of(1, "bar")),
          testInstance.rowMap());
    }
  }
}
