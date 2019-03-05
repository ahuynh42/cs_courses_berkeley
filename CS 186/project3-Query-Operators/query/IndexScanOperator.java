package edu.berkeley.cs186.database.query;

import edu.berkeley.cs186.database.Database;
import edu.berkeley.cs186.database.DatabaseException;
import edu.berkeley.cs186.database.databox.DataBox;
import edu.berkeley.cs186.database.table.Record;
import edu.berkeley.cs186.database.table.Schema;
import edu.berkeley.cs186.database.table.stats.TableStats;

import java.util.*;

public class IndexScanOperator extends QueryOperator {
  private Database.Transaction transaction;
  private String tableName;
  private String columnName;
  private QueryPlan.PredicateOperator predicate;
  private DataBox value;

  private int columnIndex;

  /**
   * An index scan operator.
   *
   * @param transaction the transaction containing this operator
   * @param tableName the table to iterate over
   * @param columnName the name of the column the index is on
   * @throws QueryPlanException
   * @throws DatabaseException
   */
  public IndexScanOperator(Database.Transaction transaction,
                           String tableName,
                           String columnName,
                           QueryPlan.PredicateOperator predicate,
                           DataBox value) throws QueryPlanException, DatabaseException {
    super(OperatorType.INDEXSCAN);
    this.tableName = tableName;
    this.transaction = transaction;
    this.columnName = columnName;
    this.predicate = predicate;
    this.value = value;
    this.setOutputSchema(this.computeSchema());
    columnName = this.checkSchemaForColumn(this.getOutputSchema(), columnName);
    this.columnIndex = this.getOutputSchema().getFieldNames().indexOf(columnName);

    this.stats = this.estimateStats();
    this.cost = this.estimateIOCost();
  }

  public String str() {
    return "type: " + this.getType() +
            "\ntable: " + this.tableName +
            "\ncolumn: " + this.columnName +
            "\noperator: " + this.predicate +
            "\nvalue: " + this.value;
  }

  /**
   * Estimates the table statistics for the result of executing this query operator.
   *
   * @return estimated TableStats
   */
  public TableStats estimateStats() throws QueryPlanException {
    TableStats stats;

    try {
      stats = this.transaction.getStats(this.tableName);
    } catch (DatabaseException de) {
      throw new QueryPlanException(de);
    }

    return stats.copyWithPredicate(this.columnIndex,
            this.predicate,
            this.value);
  }

  /**
   * Estimates the IO cost of executing this query operator.
   * You should calculate this estimate cost with the formula
   * taught to you in class. Note that the index you've implemented
   * in this project is an unclustered index.
   *
   * You will find the following instance variables helpful:
   * this.transaction, this.tableName, this.columnName,
   * this.columnIndex, this.predicate, and this.value.
   *
   * You will find the following methods helpful: this.transaction.getStats,
   * this.transaction.getNumRecords, this.transaction.getNumIndexPages,
   * and tableStats.getReductionFactor.
   *
   * @return estimate IO cost
   * @throws QueryPlanException
   */
  public int estimateIOCost() throws QueryPlanException {
    /* TODO: Implement me! */
    try {
      int numIndexPages = transaction.getNumIndexPages(tableName, columnName);
      int recordsPerPage = transaction.getStats(tableName).getNumRecords();
      float reduct = transaction.getStats(tableName).getReductionFactor(columnIndex, predicate, value);
      return (int) Math.ceil((numIndexPages + recordsPerPage) * reduct);
    } catch (DatabaseException e) {
      throw new QueryPlanException(e);
    }
  }

  public Iterator<Record> iterator() throws QueryPlanException, DatabaseException {
    return new IndexScanIterator();
  }

  public Schema computeSchema() throws QueryPlanException {
    try {
      return this.transaction.getFullyQualifiedSchema(this.tableName);
    } catch (DatabaseException de) {
      throw new QueryPlanException(de);
    }
  }

  /**
   * An implementation of Iterator that provides an iterator interface for this operator.
   */
  private class IndexScanIterator implements Iterator<Record> {
    /* TODO: Implement the IndexScanIterator */
    Iterator<Record> iter;

    public IndexScanIterator() throws QueryPlanException, DatabaseException {
      /* TODO */
      if (!transaction.indexExists(tableName, columnName)) {
        throw new QueryPlanException("Index does not exist in table.");
      }

      iter = transaction.getRecordIterator(tableName);
      List<Record> temp = new ArrayList<Record>();

      while (iter.hasNext()) {
        Record r = iter.next();
        int compare = r.getValues().get(columnIndex).compareTo(value);

        switch (predicate) {
          case EQUALS: {
            if (compare == 0) {
              temp.add(r);
            }
            break;
          }

          case NOT_EQUALS: {
            if (compare != 0) {
              temp.add(r);
            }
            break;
          }

          case LESS_THAN: {
            if (compare < 0) {
              temp.add(r);
            }
            break;
          }

          case LESS_THAN_EQUALS: {
            if (compare < 0 || compare == 0) {
              temp.add(r);
            }
            break;
          }

          case GREATER_THAN: {
            if (compare > 0) {
              temp.add(r);
            }
            break;
          }

          case GREATER_THAN_EQUALS: {
            if (compare > 0 || compare == 0) {
              temp.add(r);
            }
            break;
          }

        }

      }

      Collections.sort(temp, new recordComparator());
      iter = temp.iterator();
    }

    private class recordComparator implements Comparator<Record> {
      public int compare(Record o1, Record o2) {
        return o1.getValues().get(columnIndex).compareTo(o2.getValues().get(columnIndex));
      }
    }

    /**
     * Checks if there are more record(s) to yield
     *
     * @return true if this iterator has another record to yield, otherwise false
     */
    public boolean hasNext() {
      /* TODO */
      return iter.hasNext();
    }

    /**
     * Yields the next record of this iterator.
     *
     * @return the next Record
     * @throws NoSuchElementException if there are no more Records to yield
     */
    public Record next() {
      /* TODO */
      if (hasNext()) {
        return iter.next();
      }

      throw new NoSuchElementException();
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

}