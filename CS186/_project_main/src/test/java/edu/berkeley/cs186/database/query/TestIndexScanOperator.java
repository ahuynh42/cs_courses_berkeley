package edu.berkeley.cs186.database.query;

import edu.berkeley.cs186.database.*;
import edu.berkeley.cs186.database.io.Page;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.berkeley.cs186.database.databox.BoolDataBox;
import edu.berkeley.cs186.database.databox.DataBox;
import edu.berkeley.cs186.database.databox.FloatDataBox;
import edu.berkeley.cs186.database.databox.IntDataBox;
import edu.berkeley.cs186.database.databox.StringDataBox;
import edu.berkeley.cs186.database.table.Record;
import edu.berkeley.cs186.database.table.Schema;
import org.junit.rules.TemporaryFolder;

import javax.management.Query;

import static org.junit.Assert.*;

public class TestIndexScanOperator {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    @Category(StudentTestP3.class)
    public void testEmptyIndexScan() throws QueryPlanException, DatabaseException, IOException {
        File tempDir = tempFolder.newFolder("joinTest");
        Database d = new Database(tempDir.getAbsolutePath(), 4);
        Database.Transaction transaction = d.beginTransaction();

        List<String> indexList = new ArrayList<String>();
        indexList.add("int");
        d.createTableWithIndices(TestUtils.createSchemaWithAllTypes(), "myTable", indexList);

        QueryOperator equals = new IndexScanOperator(transaction,"myTable", "int", QueryPlan.PredicateOperator.EQUALS, new IntDataBox(0));
        QueryOperator not_equals = new IndexScanOperator(transaction,"myTable", "int", QueryPlan.PredicateOperator.NOT_EQUALS, new IntDataBox(0));
        QueryOperator less_than = new IndexScanOperator(transaction,"myTable", "int", QueryPlan.PredicateOperator.LESS_THAN, new IntDataBox(0));
        QueryOperator less_than_equals = new IndexScanOperator(transaction,"myTable", "int", QueryPlan.PredicateOperator.LESS_THAN_EQUALS, new IntDataBox(0));
        QueryOperator greater_than = new IndexScanOperator(transaction,"myTable", "int", QueryPlan.PredicateOperator.GREATER_THAN, new IntDataBox(0));
        QueryOperator greater_than_equals = new IndexScanOperator(transaction,"myTable", "int", QueryPlan.PredicateOperator.GREATER_THAN_EQUALS, new IntDataBox(0));

        Iterator<Record> iter1 = equals.iterator();
        Iterator<Record> iter2 = not_equals.iterator();
        Iterator<Record> iter3 = less_than.iterator();
        Iterator<Record> iter4 = less_than_equals.iterator();
        Iterator<Record> iter5 = greater_than.iterator();
        Iterator<Record> iter6 = greater_than_equals.iterator();

        assertFalse(iter1.hasNext());
        assertFalse(iter2.hasNext());
        assertFalse(iter3.hasNext());
        assertFalse(iter4.hasNext());
        assertFalse(iter5.hasNext());
        assertFalse(iter6.hasNext());
    }

    @Test
    @Category(StudentTestP3.class)
    public void testIndexScanNotEquals() throws QueryPlanException, DatabaseException, IOException {
        File tempDir = tempFolder.newFolder("joinTest");
        Database d = new Database(tempDir.getAbsolutePath(), 4);
        Database.Transaction transaction = d.beginTransaction();

        List<String> indexList = new ArrayList<String>();
        indexList.add("int");
        d.createTableWithIndices(TestUtils.createSchemaWithAllTypes(), "myTable", indexList);

        for (int i = 0; i < 1000; i++) {
            List<DataBox> temp = TestUtils.createRecordWithAllTypesWithValue(i).getValues();
            transaction.addRecord("myTable", temp);
            transaction.addRecord("myTable", temp);
            transaction.addRecord("myTable", temp);
            transaction.addRecord("myTable", temp);
            transaction.addRecord("myTable", temp);
        }

        QueryOperator qo = new IndexScanOperator(transaction,"myTable", "int", QueryPlan.PredicateOperator.NOT_EQUALS, new IntDataBox(777));
        Iterator<Record> iter = qo.iterator();
        int count = 0;

        while (iter.hasNext()) {
            if (count < 777) {
                Record expected = TestUtils.createRecordWithAllTypesWithValue(count);
                assertEquals(expected, iter.next());
                assertEquals(expected, iter.next());
                assertEquals(expected, iter.next());
                assertEquals(expected, iter.next());
                assertEquals(expected, iter.next());
            } else {
                Record expected = TestUtils.createRecordWithAllTypesWithValue(count + 1);
                assertEquals(expected, iter.next());
                assertEquals(expected, iter.next());
                assertEquals(expected, iter.next());
                assertEquals(expected, iter.next());
                assertEquals(expected, iter.next());
            }
            count++;
        }

        assertFalse(iter.hasNext());
        assertEquals(999*5, count*5);
    }

    @Test
    @Category(StudentTestP3.class)
    public void testIndexScanLessThan() throws QueryPlanException, DatabaseException, IOException {
        File tempDir = tempFolder.newFolder("joinTest");
        Database d = new Database(tempDir.getAbsolutePath(), 4);
        Database.Transaction transaction = d.beginTransaction();

        List<String> indexList = new ArrayList<String>();
        indexList.add("int");
        d.createTableWithIndices(TestUtils.createSchemaWithAllTypes(), "myTable", indexList);

        for (int i = 0; i < 1000; i++) {
            List<DataBox> temp = TestUtils.createRecordWithAllTypesWithValue(i).getValues();
            transaction.addRecord("myTable", temp);
            transaction.addRecord("myTable", temp);
            transaction.addRecord("myTable", temp);
            transaction.addRecord("myTable", temp);
            transaction.addRecord("myTable", temp);
        }

        QueryOperator qo = new IndexScanOperator(transaction,"myTable", "int", QueryPlan.PredicateOperator.LESS_THAN, new IntDataBox(314));
        Iterator<Record> iter = qo.iterator();
        int count = 0;

        while (iter.hasNext()) {
            Record expected = TestUtils.createRecordWithAllTypesWithValue(count);
            assertEquals(expected, iter.next());
            assertEquals(expected, iter.next());
            assertEquals(expected, iter.next());
            assertEquals(expected, iter.next());
            assertEquals(expected, iter.next());
            count++;
        }

        assertFalse(iter.hasNext());
        assertEquals(314*5, count*5);
    }

    @Test
    @Category(StudentTestP3.class)
    public void testIndexScanLessThanEquals() throws QueryPlanException, DatabaseException, IOException {
        File tempDir = tempFolder.newFolder("joinTest");
        Database d = new Database(tempDir.getAbsolutePath(), 4);
        Database.Transaction transaction = d.beginTransaction();

        List<String> indexList = new ArrayList<String>();
        indexList.add("int");
        d.createTableWithIndices(TestUtils.createSchemaWithAllTypes(), "myTable", indexList);

        for (int i = 0; i < 1000; i++) {
            List<DataBox> temp = TestUtils.createRecordWithAllTypesWithValue(i).getValues();
            transaction.addRecord("myTable", temp);
            transaction.addRecord("myTable", temp);
            transaction.addRecord("myTable", temp);
            transaction.addRecord("myTable", temp);
            transaction.addRecord("myTable", temp);
        }

        QueryOperator qo = new IndexScanOperator(transaction,"myTable", "int", QueryPlan.PredicateOperator.LESS_THAN_EQUALS, new IntDataBox(314));
        Iterator<Record> iter = qo.iterator();
        int count = 0;

        while (iter.hasNext()) {
            Record expected = TestUtils.createRecordWithAllTypesWithValue(count);
            assertEquals(expected, iter.next());
            assertEquals(expected, iter.next());
            assertEquals(expected, iter.next());
            assertEquals(expected, iter.next());
            assertEquals(expected, iter.next());
            count++;
        }

        assertFalse(iter.hasNext());
        assertEquals(315*5, count*5);
    }

    @Test
    @Category(StudentTestP3.class)
    public void testIndexScanGreaterThan() throws QueryPlanException, DatabaseException, IOException {
        File tempDir = tempFolder.newFolder("joinTest");
        Database d = new Database(tempDir.getAbsolutePath(), 4);
        Database.Transaction transaction = d.beginTransaction();

        List<String> indexList = new ArrayList<String>();
        indexList.add("int");
        d.createTableWithIndices(TestUtils.createSchemaWithAllTypes(), "myTable", indexList);

        for (int i = 0; i < 1000; i++) {
            List<DataBox> temp = TestUtils.createRecordWithAllTypesWithValue(i).getValues();
            transaction.addRecord("myTable", temp);
            transaction.addRecord("myTable", temp);
            transaction.addRecord("myTable", temp);
            transaction.addRecord("myTable", temp);
            transaction.addRecord("myTable", temp);
        }

        QueryOperator qo = new IndexScanOperator(transaction,"myTable", "int", QueryPlan.PredicateOperator.GREATER_THAN, new IntDataBox(272));
        Iterator<Record> iter = qo.iterator();
        int count = 0;

        while (iter.hasNext()) {
            Record expected = TestUtils.createRecordWithAllTypesWithValue(273 + count);
            assertEquals(expected, iter.next());
            assertEquals(expected, iter.next());
            assertEquals(expected, iter.next());
            assertEquals(expected, iter.next());
            assertEquals(expected, iter.next());
            count++;
        }

        assertFalse(iter.hasNext());
        assertEquals(727*5, count*5);
    }

    @Test
    @Category(StudentTestP3.class)
    public void testIndexScanGreaterThanEquals() throws QueryPlanException, DatabaseException, IOException {
        File tempDir = tempFolder.newFolder("joinTest");
        Database d = new Database(tempDir.getAbsolutePath(), 4);
        Database.Transaction transaction = d.beginTransaction();

        List<String> indexList = new ArrayList<String>();
        indexList.add("int");
        d.createTableWithIndices(TestUtils.createSchemaWithAllTypes(), "myTable", indexList);

        for (int i = 0; i < 1000; i++) {
            List<DataBox> temp = TestUtils.createRecordWithAllTypesWithValue(i).getValues();
            transaction.addRecord("myTable", temp);
            transaction.addRecord("myTable", temp);
            transaction.addRecord("myTable", temp);
            transaction.addRecord("myTable", temp);
            transaction.addRecord("myTable", temp);
        }

        QueryOperator qo = new IndexScanOperator(transaction,"myTable", "int", QueryPlan.PredicateOperator.GREATER_THAN_EQUALS, new IntDataBox(272));
        Iterator<Record> iter = qo.iterator();
        int count = 0;

        while (iter.hasNext()) {
            Record expected = TestUtils.createRecordWithAllTypesWithValue(272 + count);
            assertEquals(expected, iter.next());
            assertEquals(expected, iter.next());
            assertEquals(expected, iter.next());
            assertEquals(expected, iter.next());
            assertEquals(expected, iter.next());
            count++;
        }

        assertFalse(iter.hasNext());
        assertEquals(728*5, count*5);
    }

  @Test(timeout=5000)
  public void testIndexScanEqualsRecords() throws QueryPlanException, DatabaseException, IOException {
    File tempDir = tempFolder.newFolder("joinTest");
    Database d = new Database(tempDir.getAbsolutePath(), 4);
    Database.Transaction transaction = d.beginTransaction();

    Record r1 = TestUtils.createRecordWithAllTypesWithValue(1);
    List<DataBox> r1Vals = r1.getValues();
    Record r2 = TestUtils.createRecordWithAllTypesWithValue(2);
    List<DataBox> r2Vals = r2.getValues();
    Record r3 = TestUtils.createRecordWithAllTypesWithValue(3);
    List<DataBox> r3Vals = r3.getValues();
    Record r4 = TestUtils.createRecordWithAllTypesWithValue(4);
    List<DataBox> r4Vals = r4.getValues();
    Record r5 = TestUtils.createRecordWithAllTypesWithValue(5);
    List<DataBox> r5Vals = r5.getValues();
    Record r6 = TestUtils.createRecordWithAllTypesWithValue(6);
    List<DataBox> r6Vals = r6.getValues();
    List<DataBox> expectedRecordValues1 = new ArrayList<DataBox>();
    List<DataBox> expectedRecordValues2 = new ArrayList<DataBox>();
    List<DataBox> expectedRecordValues3 = new ArrayList<DataBox>();
    List<DataBox> expectedRecordValues4 = new ArrayList<DataBox>();
    List<DataBox> expectedRecordValues5 = new ArrayList<DataBox>();
    List<DataBox> expectedRecordValues6 = new ArrayList<DataBox>();


    for (int i = 0; i < 1; i++) {
      for (DataBox val: r1Vals) {
        expectedRecordValues1.add(val);
      }
      for (DataBox val: r2Vals) {
        expectedRecordValues2.add(val);
      }
      for (DataBox val: r3Vals) {
        expectedRecordValues3.add(val);
      }
      for (DataBox val: r4Vals) {
        expectedRecordValues4.add(val);
      }
      for (DataBox val: r5Vals) {
        expectedRecordValues5.add(val);
      }
      for (DataBox val: r6Vals) {
        expectedRecordValues6.add(val);
      }
    }

    Record expectedRecord3 = new Record(expectedRecordValues3);
    List<String> indexList = new ArrayList<String>();
    indexList.add("int");
    d.createTableWithIndices(TestUtils.createSchemaWithAllTypes(), "myTable", indexList);

    for (int i = 0; i < 99; i++) {
      transaction.addRecord("myTable", r3Vals);
      transaction.addRecord("myTable", r5Vals);
      transaction.addRecord("myTable", r2Vals);
      transaction.addRecord("myTable", r1Vals);
      transaction.addRecord("myTable", r6Vals);
    }

    QueryOperator s1 = new IndexScanOperator(transaction,"myTable", "int", QueryPlan.PredicateOperator.EQUALS, new IntDataBox(3));
    Iterator<Record> outputIterator = s1.iterator();
    int count = 0;

    while (outputIterator.hasNext()) {
      if (count < 99) {
        assertEquals(expectedRecord3, outputIterator.next());
      }
      count++;
    }
    assertTrue(count == 99);
  }

  @Test(timeout=5000)
  public void testIndexScanLessThanEqualsRecords() throws QueryPlanException, DatabaseException, IOException {
    File tempDir = tempFolder.newFolder("joinTest");
    Database d = new Database(tempDir.getAbsolutePath(), 4);
    Database.Transaction transaction = d.beginTransaction();

    Record r1 = TestUtils.createRecordWithAllTypesWithValue(1);
    List<DataBox> r1Vals = r1.getValues();
    Record r2 = TestUtils.createRecordWithAllTypesWithValue(2);
    List<DataBox> r2Vals = r2.getValues();
    Record r3 = TestUtils.createRecordWithAllTypesWithValue(3);
    List<DataBox> r3Vals = r3.getValues();
    Record r4 = TestUtils.createRecordWithAllTypesWithValue(4);
    List<DataBox> r4Vals = r4.getValues();
    Record r5 = TestUtils.createRecordWithAllTypesWithValue(5);
    List<DataBox> r5Vals = r5.getValues();
    Record r6 = TestUtils.createRecordWithAllTypesWithValue(6);
    List<DataBox> r6Vals = r6.getValues();
    List<DataBox> expectedRecordValues1 = new ArrayList<DataBox>();
    List<DataBox> expectedRecordValues2 = new ArrayList<DataBox>();
    List<DataBox> expectedRecordValues3 = new ArrayList<DataBox>();
    List<DataBox> expectedRecordValues4 = new ArrayList<DataBox>();
    List<DataBox> expectedRecordValues5 = new ArrayList<DataBox>();
    List<DataBox> expectedRecordValues6 = new ArrayList<DataBox>();


    for (int i = 0; i < 1; i++) {
      for (DataBox val: r1Vals) {
        expectedRecordValues1.add(val);
      }
      for (DataBox val: r2Vals) {
        expectedRecordValues2.add(val);
      }
      for (DataBox val: r3Vals) {
        expectedRecordValues3.add(val);
      }
      for (DataBox val: r4Vals) {
        expectedRecordValues4.add(val);
      }
      for (DataBox val: r5Vals) {
        expectedRecordValues5.add(val);
      }
      for (DataBox val: r6Vals) {
        expectedRecordValues6.add(val);
      }
    }

    Record expectedRecord1 = new Record(expectedRecordValues1);
    Record expectedRecord2 = new Record(expectedRecordValues2);
    Record expectedRecord3 = new Record(expectedRecordValues3);
    List<String> indexList = new ArrayList<String>();
    indexList.add("int");
    d.createTableWithIndices(TestUtils.createSchemaWithAllTypes(), "myTable", indexList);

    for (int i = 0; i < 99; i++) {
      transaction.addRecord("myTable", r3Vals);
      transaction.addRecord("myTable", r5Vals);
      transaction.addRecord("myTable", r2Vals);
      transaction.addRecord("myTable", r1Vals);
      transaction.addRecord("myTable", r6Vals);
    }

    QueryOperator s1 = new IndexScanOperator(transaction,"myTable", "int", QueryPlan.PredicateOperator.LESS_THAN_EQUALS, new IntDataBox(3));
    Iterator<Record> outputIterator = s1.iterator();
    int count = 0;

    while (outputIterator.hasNext()) {
      if (count < 99) {
        assertEquals(expectedRecord1, outputIterator.next());
      } else if (count < 99*2) {
        assertEquals(expectedRecord2, outputIterator.next());
      } else {
        assertEquals(expectedRecord3, outputIterator.next());
      }
      count++;
    }
    assertTrue(count == 99*3);
  }

  @Test(timeout=5000)
  public void testIndexScanLessThanRecords() throws QueryPlanException, DatabaseException, IOException {
    File tempDir = tempFolder.newFolder("joinTest");
    Database d = new Database(tempDir.getAbsolutePath(), 4);
    Database.Transaction transaction = d.beginTransaction();

    Record r1 = TestUtils.createRecordWithAllTypesWithValue(1);
    List<DataBox> r1Vals = r1.getValues();
    Record r2 = TestUtils.createRecordWithAllTypesWithValue(2);
    List<DataBox> r2Vals = r2.getValues();
    Record r3 = TestUtils.createRecordWithAllTypesWithValue(3);
    List<DataBox> r3Vals = r3.getValues();
    Record r4 = TestUtils.createRecordWithAllTypesWithValue(4);
    List<DataBox> r4Vals = r4.getValues();
    Record r5 = TestUtils.createRecordWithAllTypesWithValue(5);
    List<DataBox> r5Vals = r5.getValues();
    Record r6 = TestUtils.createRecordWithAllTypesWithValue(6);
    List<DataBox> r6Vals = r6.getValues();
    List<DataBox> expectedRecordValues1 = new ArrayList<DataBox>();
    List<DataBox> expectedRecordValues2 = new ArrayList<DataBox>();
    List<DataBox> expectedRecordValues3 = new ArrayList<DataBox>();
    List<DataBox> expectedRecordValues4 = new ArrayList<DataBox>();
    List<DataBox> expectedRecordValues5 = new ArrayList<DataBox>();
    List<DataBox> expectedRecordValues6 = new ArrayList<DataBox>();


    for (int i = 0; i < 1; i++) {
      for (DataBox val: r1Vals) {
        expectedRecordValues1.add(val);
      }
      for (DataBox val: r2Vals) {
        expectedRecordValues2.add(val);
      }
      for (DataBox val: r3Vals) {
        expectedRecordValues3.add(val);
      }
      for (DataBox val: r4Vals) {
        expectedRecordValues4.add(val);
      }
      for (DataBox val: r5Vals) {
        expectedRecordValues5.add(val);
      }
      for (DataBox val: r6Vals) {
        expectedRecordValues6.add(val);
      }
    }

    Record expectedRecord1 = new Record(expectedRecordValues1);
    Record expectedRecord2 = new Record(expectedRecordValues2);
    List<String> indexList = new ArrayList<String>();
    indexList.add("int");
    d.createTableWithIndices(TestUtils.createSchemaWithAllTypes(), "myTable", indexList);

    for (int i = 0; i < 99; i++) {
      transaction.addRecord("myTable", r3Vals);
      transaction.addRecord("myTable", r5Vals);
      transaction.addRecord("myTable", r2Vals);
      transaction.addRecord("myTable", r1Vals);
      transaction.addRecord("myTable", r6Vals);
    }

    QueryOperator s1 = new IndexScanOperator(transaction,"myTable", "int", QueryPlan.PredicateOperator.LESS_THAN, new IntDataBox(3));
    Iterator<Record> outputIterator = s1.iterator();
    int count = 0;

    while (outputIterator.hasNext()) {
      if (count < 99) {
        assertEquals(expectedRecord1, outputIterator.next());
      } else {
        assertEquals(expectedRecord2, outputIterator.next());
      }
      count++;
    }
    assertTrue(count == 99*2);
  }

  @Test(timeout=5000)
  public void testIndexScanGreaterThanEqualsRecords() throws QueryPlanException, DatabaseException, IOException {
    File tempDir = tempFolder.newFolder("joinTest");
    Database d = new Database(tempDir.getAbsolutePath(), 4);
    Database.Transaction transaction = d.beginTransaction();

    Record r1 = TestUtils.createRecordWithAllTypesWithValue(1);
    List<DataBox> r1Vals = r1.getValues();
    Record r2 = TestUtils.createRecordWithAllTypesWithValue(2);
    List<DataBox> r2Vals = r2.getValues();
    Record r3 = TestUtils.createRecordWithAllTypesWithValue(3);
    List<DataBox> r3Vals = r3.getValues();
    Record r4 = TestUtils.createRecordWithAllTypesWithValue(4);
    List<DataBox> r4Vals = r4.getValues();
    Record r5 = TestUtils.createRecordWithAllTypesWithValue(5);
    List<DataBox> r5Vals = r5.getValues();
    Record r6 = TestUtils.createRecordWithAllTypesWithValue(6);
    List<DataBox> r6Vals = r6.getValues();
    List<DataBox> expectedRecordValues1 = new ArrayList<DataBox>();
    List<DataBox> expectedRecordValues2 = new ArrayList<DataBox>();
    List<DataBox> expectedRecordValues3 = new ArrayList<DataBox>();
    List<DataBox> expectedRecordValues4 = new ArrayList<DataBox>();
    List<DataBox> expectedRecordValues5 = new ArrayList<DataBox>();
    List<DataBox> expectedRecordValues6 = new ArrayList<DataBox>();


    for (int i = 0; i < 1; i++) {
      for (DataBox val: r1Vals) {
        expectedRecordValues1.add(val);
      }
      for (DataBox val: r2Vals) {
        expectedRecordValues2.add(val);
      }
      for (DataBox val: r3Vals) {
        expectedRecordValues3.add(val);
      }
      for (DataBox val: r4Vals) {
        expectedRecordValues4.add(val);
      }
      for (DataBox val: r5Vals) {
        expectedRecordValues5.add(val);
      }
      for (DataBox val: r6Vals) {
        expectedRecordValues6.add(val);
      }
    }

    Record expectedRecord3 = new Record(expectedRecordValues3);
    Record expectedRecord5 = new Record(expectedRecordValues5);
    Record expectedRecord6 = new Record(expectedRecordValues6);
    List<String> indexList = new ArrayList<String>();
    indexList.add("int");
    d.createTableWithIndices(TestUtils.createSchemaWithAllTypes(), "myTable", indexList);

    for (int i = 0; i < 99; i++) {
      transaction.addRecord("myTable", r3Vals);
      transaction.addRecord("myTable", r5Vals);
      transaction.addRecord("myTable", r2Vals);
      transaction.addRecord("myTable", r1Vals);
      transaction.addRecord("myTable", r6Vals);
    }

    QueryOperator s1 = new IndexScanOperator(transaction,"myTable", "int", QueryPlan.PredicateOperator.GREATER_THAN_EQUALS, new IntDataBox(3));
    Iterator<Record> outputIterator = s1.iterator();
    int count = 0;

    while (outputIterator.hasNext()) {
      if (count < 99) {
        assertEquals(expectedRecord3, outputIterator.next());
      } else if (count < 99*2) {
        assertEquals(expectedRecord5, outputIterator.next());
      } else {
        assertEquals(expectedRecord6, outputIterator.next());
      }
      count++;
    }
    assertTrue(count == 99*3);
  }

  @Test(timeout=5000)
  public void testIndexScanGreaterThanRecords() throws QueryPlanException, DatabaseException, IOException {
    File tempDir = tempFolder.newFolder("joinTest");
    Database d = new Database(tempDir.getAbsolutePath(), 4);
    Database.Transaction transaction = d.beginTransaction();

    Record r1 = TestUtils.createRecordWithAllTypesWithValue(1);
    List<DataBox> r1Vals = r1.getValues();
    Record r2 = TestUtils.createRecordWithAllTypesWithValue(2);
    List<DataBox> r2Vals = r2.getValues();
    Record r3 = TestUtils.createRecordWithAllTypesWithValue(3);
    List<DataBox> r3Vals = r3.getValues();
    Record r4 = TestUtils.createRecordWithAllTypesWithValue(4);
    List<DataBox> r4Vals = r4.getValues();
    Record r5 = TestUtils.createRecordWithAllTypesWithValue(5);
    List<DataBox> r5Vals = r5.getValues();
    Record r6 = TestUtils.createRecordWithAllTypesWithValue(6);
    List<DataBox> r6Vals = r6.getValues();
    List<DataBox> expectedRecordValues1 = new ArrayList<DataBox>();
    List<DataBox> expectedRecordValues2 = new ArrayList<DataBox>();
    List<DataBox> expectedRecordValues3 = new ArrayList<DataBox>();
    List<DataBox> expectedRecordValues4 = new ArrayList<DataBox>();
    List<DataBox> expectedRecordValues5 = new ArrayList<DataBox>();
    List<DataBox> expectedRecordValues6 = new ArrayList<DataBox>();


    for (int i = 0; i < 1; i++) {
      for (DataBox val: r1Vals) {
        expectedRecordValues1.add(val);
      }
      for (DataBox val: r2Vals) {
        expectedRecordValues2.add(val);
      }
      for (DataBox val: r3Vals) {
        expectedRecordValues3.add(val);
      }
      for (DataBox val: r4Vals) {
        expectedRecordValues4.add(val);
      }
      for (DataBox val: r5Vals) {
        expectedRecordValues5.add(val);
      }
      for (DataBox val: r6Vals) {
        expectedRecordValues6.add(val);
      }
    }

    Record expectedRecord5 = new Record(expectedRecordValues5);
    Record expectedRecord6 = new Record(expectedRecordValues6);
    List<String> indexList = new ArrayList<String>();
    indexList.add("int");
    d.createTableWithIndices(TestUtils.createSchemaWithAllTypes(), "myTable", indexList);

    for (int i = 0; i < 99; i++) {
      transaction.addRecord("myTable", r3Vals);
      transaction.addRecord("myTable", r5Vals);
      transaction.addRecord("myTable", r2Vals);
      transaction.addRecord("myTable", r1Vals);
      transaction.addRecord("myTable", r6Vals);
    }

    QueryOperator s1 = new IndexScanOperator(transaction,"myTable", "int", QueryPlan.PredicateOperator.GREATER_THAN, new IntDataBox(3));
    Iterator<Record> outputIterator = s1.iterator();
    int count = 0;

    while (outputIterator.hasNext()) {
      if (count < 99) {
        assertEquals(expectedRecord5, outputIterator.next());
      } else {
        assertEquals(expectedRecord6, outputIterator.next());
      }
      count++;
    }
    assertTrue(count == 99*2);
  }

  @Test(timeout=5000)
  public void testIndexScanInvalidRecords() throws QueryPlanException, DatabaseException, IOException {
    File tempDir = tempFolder.newFolder("joinTest");
    Database d = new Database(tempDir.getAbsolutePath(), 4);
    Database.Transaction transaction = d.beginTransaction();

    Record r1 = TestUtils.createRecordWithAllTypesWithValue(1);
    List<DataBox> r1Vals = r1.getValues();
    Record r2 = TestUtils.createRecordWithAllTypesWithValue(2);
    List<DataBox> r2Vals = r2.getValues();
    Record r3 = TestUtils.createRecordWithAllTypesWithValue(3);
    List<DataBox> r3Vals = r3.getValues();
    Record r4 = TestUtils.createRecordWithAllTypesWithValue(4);
    List<DataBox> r4Vals = r4.getValues();
    Record r5 = TestUtils.createRecordWithAllTypesWithValue(5);
    List<DataBox> r5Vals = r5.getValues();
    Record r6 = TestUtils.createRecordWithAllTypesWithValue(6);
    List<DataBox> r6Vals = r6.getValues();
    List<DataBox> expectedRecordValues1 = new ArrayList<DataBox>();
    List<DataBox> expectedRecordValues2 = new ArrayList<DataBox>();
    List<DataBox> expectedRecordValues3 = new ArrayList<DataBox>();
    List<DataBox> expectedRecordValues4 = new ArrayList<DataBox>();
    List<DataBox> expectedRecordValues5 = new ArrayList<DataBox>();
    List<DataBox> expectedRecordValues6 = new ArrayList<DataBox>();


    for (int i = 0; i < 1; i++) {
      for (DataBox val: r1Vals) {
        expectedRecordValues1.add(val);
      }
      for (DataBox val: r2Vals) {
        expectedRecordValues2.add(val);
      }
      for (DataBox val: r3Vals) {
        expectedRecordValues3.add(val);
      }
      for (DataBox val: r4Vals) {
        expectedRecordValues4.add(val);
      }
      for (DataBox val: r5Vals) {
        expectedRecordValues5.add(val);
      }
      for (DataBox val: r6Vals) {
        expectedRecordValues6.add(val);
      }
    }

    List<String> indexList = new ArrayList<String>();
    indexList.add("int");
    d.createTableWithIndices(TestUtils.createSchemaWithAllTypes(), "myTable", indexList);

    for (int i = 0; i < 99; i++) {
      transaction.addRecord("myTable", r3Vals);
      transaction.addRecord("myTable", r5Vals);
      transaction.addRecord("myTable", r2Vals);
      transaction.addRecord("myTable", r1Vals);
      transaction.addRecord("myTable", r6Vals);
    }

    QueryOperator s1 = new IndexScanOperator(transaction,"myTable", "int", QueryPlan.PredicateOperator.EQUALS, new IntDataBox(10));
    Iterator<Record> outputIterator = s1.iterator();
    int count = 0;

    while (outputIterator.hasNext()) {
      count++;
    }
    assertTrue(count == 0);
  }

  @Test(expected = QueryPlanException.class)
  public void testIndexScanInvalidField() throws QueryPlanException, DatabaseException, IOException {
    File tempDir = tempFolder.newFolder("joinTest");
    Database d = new Database(tempDir.getAbsolutePath(), 4);
    Database.Transaction transaction = d.beginTransaction();

    Record r1 = TestUtils.createRecordWithAllTypesWithValue(1);
    List<DataBox> r1Vals = r1.getValues();
    Record r2 = TestUtils.createRecordWithAllTypesWithValue(2);
    List<DataBox> r2Vals = r2.getValues();
    Record r3 = TestUtils.createRecordWithAllTypesWithValue(3);
    List<DataBox> r3Vals = r3.getValues();
    Record r4 = TestUtils.createRecordWithAllTypesWithValue(4);
    List<DataBox> r4Vals = r4.getValues();
    Record r5 = TestUtils.createRecordWithAllTypesWithValue(5);
    List<DataBox> r5Vals = r5.getValues();
    Record r6 = TestUtils.createRecordWithAllTypesWithValue(6);
    List<DataBox> r6Vals = r6.getValues();

    List<String> indexList = new ArrayList<String>();
    indexList.add("int");
    d.createTableWithIndices(TestUtils.createSchemaWithAllTypes(), "myTable", indexList);

    for (int i = 0; i < 99; i++) {
      transaction.addRecord("myTable", r3Vals);
      transaction.addRecord("myTable", r5Vals);
      transaction.addRecord("myTable", r2Vals);
      transaction.addRecord("myTable", r1Vals);
      transaction.addRecord("myTable", r6Vals);
    }

    QueryOperator s1 = new IndexScanOperator(transaction,"myTable", "nonexistentField", QueryPlan.PredicateOperator.EQUALS, new IntDataBox(10));
  }
}