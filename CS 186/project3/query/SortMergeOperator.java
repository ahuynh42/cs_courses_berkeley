package edu.berkeley.cs186.database.query;

import edu.berkeley.cs186.database.Database;
import edu.berkeley.cs186.database.DatabaseException;
import edu.berkeley.cs186.database.databox.DataBox;
import edu.berkeley.cs186.database.io.Page;
import edu.berkeley.cs186.database.table.Record;
import edu.berkeley.cs186.database.table.RecordID;

import java.util.*;
import java.lang.*;

public class SortMergeOperator extends JoinOperator {

  public SortMergeOperator(QueryOperator leftSource,
                           QueryOperator rightSource,
                           String leftColumnName,
                           String rightColumnName,
                           Database.Transaction transaction) throws QueryPlanException, DatabaseException {
    super(leftSource, rightSource, leftColumnName, rightColumnName, transaction, JoinType.SORTMERGE);

  }

  public Iterator<Record> iterator() throws QueryPlanException, DatabaseException {
    return new SortMergeOperator.SortMergeIterator();
  }

  public int estimateIOCost() throws QueryPlanException {
    // You don't need to implement this.
    throw new QueryPlanException("Not yet implemented!");
  }

  /**
   * An implementation of Iterator that provides an iterator interface for this operator.
   */
  private class SortMergeIterator implements Iterator<Record> {
    /* TODO: Implement the SortMergeIterator */
    private String leftTableName;
    private String rightTableName;
    private Iterator<Page> leftIterator;
    private Iterator<Page> rightIterator;
    private Page leftPage;
    private Page rightPage;
    private Record leftRecord;
    private Record rightRecord;
    private Record nextRecord;
    private int leftMaxEntries;
    private int rightMaxEntries;
    private int leftEntryNum;
    private int rightEntryNum;
    private int rightMark;

    public SortMergeIterator() throws QueryPlanException, DatabaseException {
      /* TODO */
      leftTableName = "leftTableSorted";
      rightTableName = "rightTableSorted";
      leftPage = null;
      rightPage = null;
      leftRecord = null;
      rightRecord = null;
      nextRecord = null;
      leftEntryNum = -1;
      rightEntryNum = -1;
      rightMark = -1;

      Iterator<Record> leftTempIter = getLeftSource().iterator();
      Iterator<Record> rightTempIter = getRightSource().iterator();

      List<Record> leftTempList = new ArrayList<Record>();
      List<Record> rightTempList = new ArrayList<Record>();

      while (leftTempIter.hasNext()) {
        leftTempList.add(leftTempIter.next());
      }
      while (rightTempIter.hasNext()) {
        rightTempList.add(rightTempIter.next());
      }

      Collections.sort(leftTempList, new LeftRecordComparator());
      Collections.sort(rightTempList, new RightRecordComparator());

      createTempTable(getLeftSource().getOutputSchema(), leftTableName);
      createTempTable(getRightSource().getOutputSchema(), rightTableName);

      leftMaxEntries = getNumEntriesPerPage(leftTableName);
      rightMaxEntries = getNumEntriesPerPage(rightTableName);

      for (Record r : leftTempList) {
        addRecord(leftTableName, r.getValues());
      }
      for (Record r : rightTempList) {
        addRecord(rightTableName, r.getValues());
      }

      leftIterator = getPageIterator(leftTableName);
      rightIterator = getPageIterator(rightTableName);
      leftIterator.next();
      rightIterator.next();
    }

    /**
     * Checks if there are more record(s) to yield
     *
     * @return true if this iterator has another record to yield, otherwise false
     */
    public boolean hasNext() {
      /* TODO */
      if (nextRecord != null) {
        return true;
      }

      while (true) {
        if (leftPage == null) {
          if (leftIterator.hasNext()) {
            leftPage = leftIterator.next();
            leftEntryNum = 0;
          } else {
            return nextRecord != null;
          }
        }

        if (rightPage == null) {
          if (rightIterator.hasNext()) {
            rightPage = rightIterator.next();
            rightEntryNum = 0;
          } else {
            return false;
          }
        }

        if (leftRecord == null) {
          try {
            RecordID leftRecordID = new RecordID(leftPage.getPageNum(), leftEntryNum);
            leftRecord = getTransaction().getRecord(leftTableName, leftRecordID);
            leftEntryNum++;
          } catch (DatabaseException e) {
            leftPage = null;

            continue;
          }
        }

        if (rightRecord == null) {
          try {
            RecordID rightRecordID = new RecordID(rightPage.getPageNum(), rightEntryNum);
            rightRecord = getTransaction().getRecord(rightTableName, rightRecordID);
            rightEntryNum++;
          } catch (DatabaseException e) {
            rightPage = null;

            continue;
          }
        }

        while (leftRecord.getValues().get(getLeftColumnIndex()).compareTo(
                rightRecord.getValues().get(getRightColumnIndex())) < 0) {
          if (leftEntryNum == leftMaxEntries) {
            leftPage = null;

            break;
          }

          try {
            RecordID leftRecordID = new RecordID(leftPage.getPageNum(), leftEntryNum);
            leftRecord = getTransaction().getRecord(leftTableName, leftRecordID);
            leftEntryNum++;
          } catch (DatabaseException e) {
            return false;
          }
        }

        if (leftPage == null) {
          leftRecord = null;

          continue;
        }

        while (leftRecord.getValues().get(getLeftColumnIndex()).compareTo(
                rightRecord.getValues().get(getRightColumnIndex())) > 0) {
          if (rightEntryNum == rightMaxEntries) {
            rightPage = null;

            break;
          }

          try {
            RecordID rightRecordID = new RecordID(rightPage.getPageNum(), rightEntryNum);
            rightRecord = getTransaction().getRecord(rightTableName, rightRecordID);
            rightEntryNum++;
          } catch (DatabaseException e) {
            return false;
          }
        }

        if (rightPage == null) {
          rightRecord = null;

          continue;
        }

        if (leftRecord.getValues().get(getLeftColumnIndex()).compareTo(
                rightRecord.getValues().get(getRightColumnIndex())) != 0) {
          continue;
        }

        if (rightMark == -1) {
          rightMark = rightEntryNum - 1;
        }

        List<DataBox> leftValues = new ArrayList<DataBox>(leftRecord.getValues());
        List<DataBox> rightValues = new ArrayList<DataBox>(rightRecord.getValues());
        leftValues.addAll(rightValues);
        nextRecord = new Record(leftValues);

        try {
          RecordID tempRightRecordID = new RecordID(rightPage.getPageNum(), rightEntryNum);
          Record tempRightRecord = getTransaction().getRecord(rightTableName, tempRightRecordID);

          if (tempRightRecord.getValues().get(getRightColumnIndex()).compareTo(
                  rightRecord.getValues().get(getRightColumnIndex())) == 0) {
            rightRecord = tempRightRecord;
            rightEntryNum++;

          } else {
            RecordID tempLeftRecordID = new RecordID(leftPage.getPageNum(), leftEntryNum);
            Record tempLeftRecord = getTransaction().getRecord(leftTableName, tempLeftRecordID);

            if (tempLeftRecord.getValues().get(getLeftColumnIndex()).compareTo(
                    leftRecord.getValues().get(getLeftColumnIndex())) == 0) {
              rightEntryNum = rightMark;
              rightRecord = null;
            } else {
              rightMark = -1;
              rightRecord = tempRightRecord;
              rightEntryNum++;
            }

            leftRecord = tempLeftRecord;
            leftEntryNum++;
          }
        } catch (DatabaseException e) {
          try {
            RecordID tempLeftRecordID = new RecordID(leftPage.getPageNum(), leftEntryNum);
            Record tempLeftRecord = getTable(leftTableName).getRecord(tempLeftRecordID);

            if (tempLeftRecord.getValues().get(getLeftColumnIndex()).compareTo(
                    leftRecord.getValues().get(getLeftColumnIndex())) == 0) {
              rightEntryNum = rightMark;
              rightRecord = null;
            } else {
              rightMark = -1;
              rightPage = null;

              continue;
            }

            leftRecord = tempLeftRecord;
            leftEntryNum++;
          } catch (DatabaseException d) {
            leftPage = null;

            continue;
          }
        }

        return true;
      }
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
        Record r = nextRecord;
        nextRecord = null;
        return r;
      }

      throw new NoSuchElementException();
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }

    private class LeftRecordComparator implements Comparator<Record> {
      public int compare(Record o1, Record o2) {
        return o1.getValues().get(SortMergeOperator.this.getLeftColumnIndex()).compareTo(
                o2.getValues().get(SortMergeOperator.this.getLeftColumnIndex()));
      }
    }

    private class RightRecordComparator implements Comparator<Record> {
      public int compare(Record o1, Record o2) {
        return o1.getValues().get(SortMergeOperator.this.getRightColumnIndex()).compareTo(
                o2.getValues().get(SortMergeOperator.this.getRightColumnIndex()));
      }
    }

  }

}