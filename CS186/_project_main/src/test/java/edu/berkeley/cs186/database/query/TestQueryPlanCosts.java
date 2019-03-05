package edu.berkeley.cs186.database.query;

import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import edu.berkeley.cs186.database.TestUtils;

import edu.berkeley.cs186.database.Database;
import edu.berkeley.cs186.database.DatabaseException;
import edu.berkeley.cs186.database.databox.DataBox;
import edu.berkeley.cs186.database.databox.IntDataBox;
import edu.berkeley.cs186.database.table.Schema;
import edu.berkeley.cs186.database.query.QueryPlan.PredicateOperator;
import edu.berkeley.cs186.database.table.stats.StringHistogram;
import edu.berkeley.cs186.database.StudentTestP4;

import static org.junit.Assert.*;

public class TestQueryPlanCosts {
  private Database database;
  private Random random = new Random();
  private String alphabet = StringHistogram.alphaNumeric;
  private String defaulTableName = "testAllTypes";
  private int defaultNumRecords = 1000;

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  @Before
  public void setUp() throws DatabaseException, IOException {
    File tempDir = tempFolder.newFolder("db");
    this.database = new Database(tempDir.getAbsolutePath());
    this.database.deleteAllTables();
  }

  @Test
  @Category(StudentTestP4.class)
  public void testEmptyTablesCost() throws DatabaseException, QueryPlanException {
    this.database.createTable(TestUtils.createSchemaWithAllTypes(), "tempIntTable1");
    this.database.createTable(TestUtils.createSchemaWithAllTypes(), "tempIntTable2");

    Database.Transaction transaction = database.beginTransaction();

    QueryOperator left = new SequentialScanOperator(transaction, "tempIntTable1");
    QueryOperator right = new SequentialScanOperator(transaction, "tempIntTable2");

    JoinOperator leftJoinRight = new SNLJOperator(left, right, "int", "int", transaction);
    assertEquals(0, leftJoinRight.estimateIOCost());
    JoinOperator rightJoinLeft = new SNLJOperator(right, left, "int", "int", transaction);
    assertEquals(0, rightJoinLeft.estimateIOCost());

    leftJoinRight = new PNLJOperator(left, right, "int", "int", transaction);
    assertEquals(0, leftJoinRight.estimateIOCost());
    rightJoinLeft = new PNLJOperator(right, left, "int", "int", transaction);
    assertEquals(0, rightJoinLeft.estimateIOCost());

    leftJoinRight = new BNLJOperator(left, right, "int", "int", transaction);
    assertEquals(0, leftJoinRight.estimateIOCost());
    rightJoinLeft = new BNLJOperator(right, left, "int", "int", transaction);
    assertEquals(0, rightJoinLeft.estimateIOCost());

    leftJoinRight = new GraceHashOperator(left, right, "int", "int", transaction);
    assertEquals(0, leftJoinRight.estimateIOCost());
    rightJoinLeft = new GraceHashOperator(right, left, "int", "int", transaction);
    assertEquals(0, rightJoinLeft.estimateIOCost());

    transaction.end();
  }


  @Test
  @Category(StudentTestP4.class)
  public void testBigSNLJOperatorCost() throws DatabaseException, QueryPlanException {
    List<DataBox> values1 = TestUtils.createRecordWithAllTypes().getValues();
    List<DataBox> values2 = new ArrayList<DataBox>();
    values2.add(new IntDataBox(1));
    values2.add(new IntDataBox(2));

    database.createTable(TestUtils.createSchemaWithAllTypes(), "tempIntTable1");
    database.createTable(TestUtils.createSchemaWithTwoInts(), "tempIntTable2");

    Database.Transaction transaction = database.beginTransaction();
    int numEntries1 = transaction.getNumEntriesPerPage("tempIntTable1");
    int numEntries2 = transaction.getNumEntriesPerPage("tempIntTable2");

    for (int i = 0; i < 25 * numEntries1; i++) {
      transaction.addRecord("tempIntTable1", values1);
      transaction.addRecord("tempIntTable1", values1);
      transaction.addRecord("tempIntTable1", values1);
      transaction.addRecord("tempIntTable1", values1);
      transaction.addRecord("tempIntTable1", values1);
      transaction.addRecord("tempIntTable1", values1);
      transaction.addRecord("tempIntTable1", values1);
      transaction.addRecord("tempIntTable1", values1);
    }
    for (int i = 0; i < 50 * numEntries2; i++) {
      transaction.addRecord("tempIntTable2", values2);
      transaction.addRecord("tempIntTable2", values2);
      transaction.addRecord("tempIntTable2", values2);
      transaction.addRecord("tempIntTable2", values2);
      transaction.addRecord("tempIntTable2", values2);
      transaction.addRecord("tempIntTable2", values2);
      transaction.addRecord("tempIntTable2", values2);
      transaction.addRecord("tempIntTable2", values2);
    }

    QueryOperator left = new SequentialScanOperator(transaction, "tempIntTable1");
    QueryOperator right = new SequentialScanOperator(transaction, "tempIntTable2");

    JoinOperator leftJoinRight = new SNLJOperator(left, right, "int", "int1", transaction);
    assertEquals(23040200, leftJoinRight.estimateIOCost());

    JoinOperator rightJoinLeft = new SNLJOperator(right, left, "int1", "int", transaction);
    assertEquals(40320400, rightJoinLeft.estimateIOCost());

    transaction.end();
  }


  @Test
  @Category(StudentTestP4.class)
  public void testBigPNLJOperatorCost() throws DatabaseException, QueryPlanException {
    List<DataBox> values1 = TestUtils.createRecordWithAllTypes().getValues();
    List<DataBox> values2 = new ArrayList<DataBox>();
    values2.add(new IntDataBox(1));
    values2.add(new IntDataBox(2));

    database.createTable(TestUtils.createSchemaWithAllTypes(), "tempIntTable1");
    database.createTable(TestUtils.createSchemaWithTwoInts(), "tempIntTable2");

    Database.Transaction transaction = database.beginTransaction();
    int numEntries1 = transaction.getNumEntriesPerPage("tempIntTable1");
    int numEntries2 = transaction.getNumEntriesPerPage("tempIntTable2");

    for (int i = 0; i < 25 * numEntries1; i++) {
      transaction.addRecord("tempIntTable1", values1);
      transaction.addRecord("tempIntTable1", values1);
      transaction.addRecord("tempIntTable1", values1);
      transaction.addRecord("tempIntTable1", values1);
      transaction.addRecord("tempIntTable1", values1);
      transaction.addRecord("tempIntTable1", values1);
      transaction.addRecord("tempIntTable1", values1);
      transaction.addRecord("tempIntTable1", values1);
    }
    for (int i = 0; i < 50 * numEntries2; i++) {
      transaction.addRecord("tempIntTable2", values2);
      transaction.addRecord("tempIntTable2", values2);
      transaction.addRecord("tempIntTable2", values2);
      transaction.addRecord("tempIntTable2", values2);
      transaction.addRecord("tempIntTable2", values2);
      transaction.addRecord("tempIntTable2", values2);
      transaction.addRecord("tempIntTable2", values2);
      transaction.addRecord("tempIntTable2", values2);
    }

    QueryOperator left = new SequentialScanOperator(transaction, "tempIntTable1");
    QueryOperator right = new SequentialScanOperator(transaction, "tempIntTable2");

    JoinOperator leftJoinRight = new PNLJOperator(left, right, "int", "int1", transaction);
    assertEquals(80200, leftJoinRight.estimateIOCost());

    JoinOperator rightJoinLeft = new PNLJOperator(right, left, "int1", "int", transaction);
    assertEquals(80400, rightJoinLeft.estimateIOCost());

    transaction.end();
  }


  @Test
  @Category(StudentTestP4.class)
  public void testBigBNLJOperatorCost() throws DatabaseException, QueryPlanException {
    List<DataBox> values1 = TestUtils.createRecordWithAllTypes().getValues();
    List<DataBox> values2 = new ArrayList<DataBox>();
    values2.add(new IntDataBox(1));
    values2.add(new IntDataBox(2));

    database.createTable(TestUtils.createSchemaWithAllTypes(), "tempIntTable1");
    database.createTable(TestUtils.createSchemaWithTwoInts(), "tempIntTable2");

    Database.Transaction transaction = database.beginTransaction();
    int numEntries1 = transaction.getNumEntriesPerPage("tempIntTable1");
    int numEntries2 = transaction.getNumEntriesPerPage("tempIntTable2");

    for (int i = 0; i < 25 * numEntries1; i++) {
      transaction.addRecord("tempIntTable1", values1);
      transaction.addRecord("tempIntTable1", values1);
      transaction.addRecord("tempIntTable1", values1);
      transaction.addRecord("tempIntTable1", values1);
      transaction.addRecord("tempIntTable1", values1);
      transaction.addRecord("tempIntTable1", values1);
      transaction.addRecord("tempIntTable1", values1);
      transaction.addRecord("tempIntTable1", values1);
    }
    for (int i = 0; i < 50 * numEntries2; i++) {
      transaction.addRecord("tempIntTable2", values2);
      transaction.addRecord("tempIntTable2", values2);
      transaction.addRecord("tempIntTable2", values2);
      transaction.addRecord("tempIntTable2", values2);
      transaction.addRecord("tempIntTable2", values2);
      transaction.addRecord("tempIntTable2", values2);
      transaction.addRecord("tempIntTable2", values2);
      transaction.addRecord("tempIntTable2", values2);
    }

    QueryOperator left = new SequentialScanOperator(transaction, "tempIntTable1");
    QueryOperator right = new SequentialScanOperator(transaction, "tempIntTable2");

    JoinOperator leftJoinRight = new BNLJOperator(left, right, "int", "int1", transaction);
    assertEquals(27000, leftJoinRight.estimateIOCost());

    JoinOperator rightJoinLeft = new BNLJOperator(right, left, "int1", "int", transaction);
    assertEquals(27200, rightJoinLeft.estimateIOCost());

    transaction.end();
  }


  @Test
  @Category(StudentTestP4.class)
  public void testBigGraceHashOperatorCost() throws DatabaseException, QueryPlanException {
    List<DataBox> values1 = TestUtils.createRecordWithAllTypes().getValues();
    List<DataBox> values2 = new ArrayList<DataBox>();
    values2.add(new IntDataBox(1));
    values2.add(new IntDataBox(2));

    database.createTable(TestUtils.createSchemaWithAllTypes(), "tempIntTable1");
    database.createTable(TestUtils.createSchemaWithTwoInts(), "tempIntTable2");

    Database.Transaction transaction = database.beginTransaction();
    int numEntries1 = transaction.getNumEntriesPerPage("tempIntTable1");
    int numEntries2 = transaction.getNumEntriesPerPage("tempIntTable2");

    for (int i = 0; i < 25 * numEntries1; i++) {
      transaction.addRecord("tempIntTable1", values1);
      transaction.addRecord("tempIntTable1", values1);
      transaction.addRecord("tempIntTable1", values1);
      transaction.addRecord("tempIntTable1", values1);
      transaction.addRecord("tempIntTable1", values1);
      transaction.addRecord("tempIntTable1", values1);
      transaction.addRecord("tempIntTable1", values1);
      transaction.addRecord("tempIntTable1", values1);
    }
    for (int i = 0; i < 50 * numEntries2; i++) {
      transaction.addRecord("tempIntTable2", values2);
      transaction.addRecord("tempIntTable2", values2);
      transaction.addRecord("tempIntTable2", values2);
      transaction.addRecord("tempIntTable2", values2);
      transaction.addRecord("tempIntTable2", values2);
      transaction.addRecord("tempIntTable2", values2);
      transaction.addRecord("tempIntTable2", values2);
      transaction.addRecord("tempIntTable2", values2);
    }

    QueryOperator left = new SequentialScanOperator(transaction, "tempIntTable1");
    QueryOperator right = new SequentialScanOperator(transaction, "tempIntTable2");

    JoinOperator leftJoinRight = new GraceHashOperator(left, right, "int", "int1", transaction);
    assertEquals(1800, leftJoinRight.estimateIOCost());

    JoinOperator rightJoinLeft = new GraceHashOperator(right, left, "int1", "int", transaction);
    assertEquals(1800, rightJoinLeft.estimateIOCost());

    transaction.end();
  }


  @Test(timeout=10000)
  public void testIndexScanOperatorCost() throws DatabaseException, QueryPlanException {
    List<String> intTableNames = new ArrayList<String>();
    intTableNames.add("int");

    List<DataBox> intTableTypes = new ArrayList<DataBox>();
    intTableTypes.add(new IntDataBox());

    String tableName = "tempIntTable";

    this.database.createTableWithIndices(
      new Schema(intTableNames, intTableTypes), tableName, intTableNames);

    Database.Transaction transaction = this.database.beginTransaction();

    for (int i = 0; i < 300; i++) {
      List<DataBox> values = new ArrayList<DataBox>();
      values.add(new IntDataBox(i));

      transaction.addRecord(tableName, values);
    }

    QueryOperator indexScanOperator;

    indexScanOperator = new IndexScanOperator(
      transaction, tableName, "int", PredicateOperator.GREATER_THAN_EQUALS, new IntDataBox(200));

    assertEquals(115, indexScanOperator.estimateIOCost());

    for (int i = 0; i < 700; i++) {
      List<DataBox> values = new ArrayList<DataBox>();
      values.add(new IntDataBox(i));

      transaction.addRecord(tableName, values);
    }

    indexScanOperator = new IndexScanOperator(
      transaction, tableName, "int", PredicateOperator.GREATER_THAN_EQUALS, new IntDataBox(500));

    assertEquals(379, indexScanOperator.estimateIOCost());

    transaction.end();
  }


  @Test(timeout=2000)
  public void testSNLJOperatorCost() throws DatabaseException, QueryPlanException {
    List<DataBox> values = TestUtils.createRecordWithAllTypes().getValues();
    this.database.createTable(TestUtils.createSchemaWithAllTypes(), "tempIntTable1");
    this.database.createTable(TestUtils.createSchemaWithAllTypes(), "tempIntTable2");

    Database.Transaction transaction = this.database.beginTransaction();
    int numEntries = transaction.getNumEntriesPerPage("tempIntTable1");

    for (int i = 0; i < 2 * numEntries; i++) {
      transaction.addRecord("tempIntTable1", values);
    }
    for (int i = 0; i < 4 * numEntries; i++) {
      transaction.addRecord("tempIntTable2", values);
    }

    QueryOperator left = new SequentialScanOperator(transaction, "tempIntTable1");
    QueryOperator right = new SequentialScanOperator(transaction, "tempIntTable2");

    JoinOperator leftJoinRight = new SNLJOperator(left, right, "int", "int", transaction);
    assertEquals(2306, leftJoinRight.estimateIOCost());

    JoinOperator rightJoinLeft = new SNLJOperator(right, left, "int", "int", transaction);
    assertEquals(2308, rightJoinLeft.estimateIOCost());
  }


  @Test(timeout=2000)
  public void testPNLJOperatorCost() throws DatabaseException, QueryPlanException {
    List<DataBox> values = TestUtils.createRecordWithAllTypes().getValues();
    this.database.createTable(TestUtils.createSchemaWithAllTypes(), "tempIntTable1");
    this.database.createTable(TestUtils.createSchemaWithAllTypes(), "tempIntTable2");

    Database.Transaction transaction = this.database.beginTransaction();
    int numEntries = transaction.getNumEntriesPerPage("tempIntTable1");

    for (int i = 0; i < 2 * numEntries; i++) {
      transaction.addRecord("tempIntTable1", values);
    }
    for (int i = 0; i < 3 * numEntries + 10; i++) {
      transaction.addRecord("tempIntTable2", values);
    }

    QueryOperator left = new SequentialScanOperator(transaction, "tempIntTable1");
    QueryOperator right = new SequentialScanOperator(transaction, "tempIntTable2");

    JoinOperator leftJoinRight = new PNLJOperator(left, right, "int", "int", transaction);
    assertEquals(10, leftJoinRight.estimateIOCost());

    JoinOperator rightJoinLeft = new PNLJOperator(right, left, "int", "int", transaction);
    assertEquals(12, rightJoinLeft.estimateIOCost());
  }


  @Test(timeout=2000)
  public void testBNLJOperatorCost() throws DatabaseException, QueryPlanException {
    List<DataBox> values = TestUtils.createRecordWithAllTypes().getValues();
    this.database.createTable(TestUtils.createSchemaWithAllTypes(), "tempIntTable1");
    this.database.createTable(TestUtils.createSchemaWithAllTypes(), "tempIntTable2");

    Database.Transaction transaction = this.database.beginTransaction();
    int numEntries = transaction.getNumEntriesPerPage("tempIntTable1");

    for (int i = 0; i < 17 * numEntries + 100; i++) {
      transaction.addRecord("tempIntTable1", values);
    }
    for (int i = 0; i < 4 * numEntries; i++) {
      transaction.addRecord("tempIntTable2", values);
    }

    QueryOperator left = new SequentialScanOperator(transaction, "tempIntTable1");
    QueryOperator right = new SequentialScanOperator(transaction, "tempIntTable2");

    JoinOperator leftJoinRight = new BNLJOperator(left, right, "int", "int", transaction);
    assertEquals(42, leftJoinRight.estimateIOCost());

    JoinOperator rightJoinLeft = new BNLJOperator(right, left, "int", "int", transaction);
    assertEquals(40, rightJoinLeft.estimateIOCost());
  }


  @Test(timeout=2000)
  public void testGraceHashOperatorCost() throws DatabaseException, QueryPlanException {
    List<DataBox> values = TestUtils.createRecordWithAllTypes().getValues();
    this.database.createTable(TestUtils.createSchemaWithAllTypes(), "tempIntTable1");
    this.database.createTable(TestUtils.createSchemaWithAllTypes(), "tempIntTable2");

    Database.Transaction transaction = this.database.beginTransaction();
    int numEntries = transaction.getNumEntriesPerPage("tempIntTable1");

    for (int i = 0; i < 18 * numEntries; i++) {
      transaction.addRecord("tempIntTable1", values);
    }
    for (int i = 0; i < 3 * numEntries + 287; i++) {
      transaction.addRecord("tempIntTable2", values);
    }

    QueryOperator left = new SequentialScanOperator(transaction, "tempIntTable1");
    QueryOperator right = new SequentialScanOperator(transaction, "tempIntTable2");

    JoinOperator leftJoinRight = new GraceHashOperator(left, right, "int", "int", transaction);
    assertEquals(66, leftJoinRight.estimateIOCost());

    JoinOperator rightJoinLeft = new GraceHashOperator(right, left, "int", "int", transaction);
    assertEquals(66, rightJoinLeft.estimateIOCost());
  }

}
