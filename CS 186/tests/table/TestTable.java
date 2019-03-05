package edu.berkeley.cs186.database.table;

import edu.berkeley.cs186.database.Database;
import edu.berkeley.cs186.database.DatabaseException;
import edu.berkeley.cs186.database.TestUtils;
import edu.berkeley.cs186.database.StudentTest;
import edu.berkeley.cs186.database.databox.*;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.io.IOException;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.DEFAULT)
public class TestTable {
  public static final String TABLENAME = "testtable";
  private Schema schema;
  private Table table;

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  @Before
  public void beforeEach() throws Exception {
    this.schema = TestUtils.createSchemaWithAllTypes();
    this.table = createTestTable(this.schema, TABLENAME);
  }

  @After
  public void afterEach() {
    table.close();
  }

  private Table createTestTable(Schema s, String tableName) throws DatabaseException {
    try {
      tempFolder.newFile(tableName);
      String tempFolderPath = tempFolder.getRoot().getAbsolutePath();
      return new Table(s, tableName, tempFolderPath);
    } catch (IOException e) {
      throw new DatabaseException(e.getMessage());
    }
  }

  @Test
  @Category(StudentTest.class)
  public void testRecordID() throws DatabaseException {
    Schema schema = TestUtils.createSchemaOfString(15);
    Table table = createTestTable(schema, "stringTable");

    List<DataBox> values1 = new ArrayList<DataBox>();
    values1.add(new StringDataBox("page 1 entry 0", 15));
    RecordID id1 = table.addRecord(values1);
    assertEquals(new RecordID(1, 0), id1);

    List<DataBox> values2 = new ArrayList<DataBox>();
    values2.add(new StringDataBox("page 1 entry 1", 15));
    RecordID id2 = table.addRecord(values2);
    assertEquals(new RecordID(1, 1), id2);

    RecordID id3 = null;
    for (int i = 0; i < 263; i++) {
      id3 = table.addRecord(values1);
    }
    assertEquals(new RecordID(2, 0), id3);
  }

  @Test
  @Category(StudentTest.class)
  public void testCheckRecordIDValidity() throws DatabaseException {
    Schema schema = TestUtils.createSchemaWithAllTypes();
    Table table = createTestTable(schema, "allTypesTable");

    Record record = TestUtils.createRecordWithAllTypes();
    RecordID id = table.addRecord(record.getValues());
    assertTrue(table.checkRecordIDValid(id));

    // Should be false, SHOULD NOT THROW AN ERROR!!
    assertFalse(table.checkRecordIDValid(new RecordID(1, 1)));

    try {
      table.checkRecordIDValid(new RecordID(2, 0));
    } catch (DatabaseException e) {
      return;
    }

    fail();
  }

  @Test
  @Category(StudentTest.class)
  public void testSameRecord() throws DatabaseException {
    Schema schema = TestUtils.createSchemaWithAllTypes();
    Table table = createTestTable(schema, "allTypesTable");

    Record record = TestUtils.createRecordWithAllTypes();
    RecordID id = table.addRecord(record.getValues());
    Record added = table.getRecord(id);
    assertEquals(record, added);

    Record removed = table.deleteRecord(id);
    assertEquals(added, removed);
    assertEquals(record, removed);
  }

  @Test
  @Category(StudentTest.class)
  public void testNestedDeleteAdd() throws DatabaseException {
    Schema schema = TestUtils.createSchemaWithTwoInts();
    Table table = createTestTable(schema, "intTable");

    for (int i = 0; i < 1000; i++) {
      List<DataBox> values = new ArrayList<DataBox>();
      values.add(new IntDataBox(i));
      values.add(new IntDataBox(2 * i));

      table.deleteRecord(table.addRecord(table.deleteRecord(table.addRecord(values)).getValues()));
    }

    assertEquals(0, table.getNumRecords());
  }

  @Test
  @Category(StudentTest.class)
  public void testMultipleAddDelete() throws DatabaseException {
    Schema schema = TestUtils.createSchemaOfBool();
    Table table = createTestTable(schema, "boolTable");

    List<DataBox> values = new ArrayList<DataBox>();
    values.add(new BoolDataBox(true));

    for (int i = 0; i < 250; i++) {
      table.addRecord(values);
    }

    assertEquals(250, table.getNumRecords());

    // Should be pageNum = 1 with variable entryNumber = i
    for (int i = 0; i < 250; i++) {
      RecordID id = new RecordID(1, i);
      table.deleteRecord(id);
    }

    assertEquals(0, table.getNumRecords());

    // Should always be pageNum = 1 and entryNumber = 0
    RecordID id = new RecordID(1, 0);

    for (int i = 0; i < 1000; i++) {
      table.addRecord(values);
      table.deleteRecord(id);
    }

    assertEquals(0, table.getNumRecords());
  }

  @Test(expected = DatabaseException.class)
  @Category(StudentTest.class)
  public void testDeleteThenUpdate() throws DatabaseException {
    Schema schema = TestUtils.createSchemaWithTwoInts();
    Table table = createTestTable(schema, "intTable");

    List<DataBox> values1 = new ArrayList<DataBox>();
    values1.add(new IntDataBox(1));
    values1.add(new IntDataBox(2));
    RecordID id = table.addRecord(values1);

    List<DataBox> values2 = new ArrayList<DataBox>();
    values2.add(new IntDataBox(3));
    values2.add(new IntDataBox(4));

    table.updateRecord(values2, id);
    table.deleteRecord(id);
    table.updateRecord(values1, id);
  }

  @Test
  @Category(StudentTest.class)
  public void testSimpleUpdate() throws DatabaseException {
    Schema schema = TestUtils.createSchemaOfString(5);
    Table table = createTestTable(schema, "stringTable");

    List<DataBox> values = new ArrayList<DataBox>();
    values.add(new StringDataBox("hello", 5));
    RecordID id = table.addRecord(values);
    assertEquals("hello", table.getRecord(id).getValues().get(0).getString());

    List<DataBox> updateValues = new ArrayList<DataBox>();
    updateValues.add(new StringDataBox("bye", 5));
    table.updateRecord(updateValues, id);
    assertEquals("bye  ", table.getRecord(id).getValues().get(0).getString());
  }

  @Test(expected = DatabaseException.class)
  @Category(StudentTest.class)
  public void testUpdateOutOfBounds() throws DatabaseException {
    Schema schema = TestUtils.createSchemaWithTwoInts();
    Table table = createTestTable(schema, "intTable");

    List<DataBox> values1 = new ArrayList<DataBox>();
    values1.add(new IntDataBox(1));
    values1.add(new IntDataBox(2));
    table.addRecord(values1);

    List<DataBox> values2 = new ArrayList<DataBox>();
    values2.add(new IntDataBox(3));
    values2.add(new IntDataBox(4));

    RecordID notInTable = new RecordID(1, 1);
    table.updateRecord(values2, notInTable);
  }

  @Test(expected = DatabaseException.class)
  @Category(StudentTest.class)
  public void testInvalidUpdateType() throws DatabaseException {
    Schema schema = TestUtils.createSchemaOfBool();
    Table table = createTestTable(schema, "boolTable");

    List<DataBox> values = new ArrayList<DataBox>();
    values.add(new BoolDataBox(true));
    RecordID id = table.addRecord(values);

    List<DataBox> updateValues = new ArrayList<DataBox>();
    updateValues.add(new StringDataBox("fail", 5));
    table.updateRecord(updateValues, id);
  }

//  /**
//   * Test sample, do not modify.
//   */
//  @Test
//  @Category(StudentTest.class)
//  public void testSample() {
//    assertEquals(true, true); // Do not actually write a test like this!
//  }

  @Test
  public void testTableNumEntries() throws DatabaseException {
    assertEquals("NumEntries per page is incorrect", 288, this.table.getNumEntriesPerPage());
  }

  @Test
  public void testTableNumEntriesBool() throws DatabaseException {

    Schema boolSchema = TestUtils.createSchemaOfBool();
    Table boolTable = createTestTable(boolSchema, "boolTable");
    int numEntries = boolTable.getNumEntriesPerPage();
    boolTable.close();
    assertEquals("NumEntries per page is incorrect", 3640, numEntries);
  }

  @Test
  public void testTableNumEntriesString() throws DatabaseException {

    Schema stringSchema = TestUtils.createSchemaOfString(100);
    Table stringTable = createTestTable(stringSchema, "stringTable");
    int numEntries = stringTable.getNumEntriesPerPage();
    stringTable.close();
    assertEquals("NumEntries per page is incorrect", 40, numEntries);
  }

  @Test
  public void testTableSimpleInsert() throws DatabaseException {
    Record input = TestUtils.createRecordWithAllTypes();

    RecordID rid = table.addRecord(input.getValues());

    // This is a new table, so it should be put into the first slot of the first page.
    assertEquals(1, rid.getPageNum());
    assertEquals(0, rid.getEntryNumber());

    Record output = table.getRecord(rid);
    assertEquals(input, output);
  }

  @Test
  public void testTableMultiplePages() throws DatabaseException {
    Record input = TestUtils.createRecordWithAllTypes();

    int numEntriesPerPage = table.getNumEntriesPerPage();

    // create one page's worth of entries
    for (int i = 0; i < numEntriesPerPage; i++) {
      RecordID rid = table.addRecord(input.getValues());

      // ensure that records are created in sequential slot order on the sam page
      assertEquals(1, rid.getPageNum());
      assertEquals(i, rid.getEntryNumber());
    }

    // add one more to make sure the next page is created
    RecordID rid = table.addRecord(input.getValues());
    assertEquals(2, rid.getPageNum());
    assertEquals(0, rid.getEntryNumber());
  }

  @Test(expected = DatabaseException.class)
  public void testInvalidRetrieve() throws DatabaseException {
    table.getRecord(new RecordID(1, 1));
  }

  @Test
  public void testSchemaSerialization() throws DatabaseException {
    // open another reference to the same file
    String tempFolderPath = tempFolder.getRoot().getAbsolutePath();
    Table table = new Table(TABLENAME, tempFolderPath);

    assertEquals(table.getSchema(), this.table.getSchema());
  }

  @Test
  public void testTableIterator() throws DatabaseException {
    Record input = TestUtils.createRecordWithAllTypes();

    for (int i = 0; i < 1000; i++) {
      RecordID rid = table.addRecord(input.getValues());
    }
    Iterator<Record> iRec = table.iterator();

    for (int i = 0; i < 1000; i++) {
      assertTrue(iRec.hasNext());
      assertEquals(input, iRec.next());
    }
    assertFalse(iRec.hasNext());
  }

  @Test
  public void testTableIteratorGap() throws DatabaseException {
    Record input = TestUtils.createRecordWithAllTypes();
    RecordID[] recordIds = new RecordID[1000];

    for (int i = 0; i < 1000; i++) {
      recordIds[i] = table.addRecord(input.getValues());
    }

    for (int i = 0; i < 1000; i += 2) {
      table.deleteRecord(recordIds[i]);
    }

    Iterator<Record> iRec = table.iterator();
    for (int i = 0; i < 1000; i += 2) {
      assertTrue(iRec.hasNext());
      assertEquals(input, iRec.next());
    }
    assertFalse(iRec.hasNext());
  }

  @Test
  public void testTableIteratorGapFront() throws DatabaseException {
    Record input = TestUtils.createRecordWithAllTypes();
    RecordID[] recordIds = new RecordID[1000];

    for (int i = 0; i < 1000; i++) {
      recordIds[i] = table.addRecord(input.getValues());
    }

    for (int i = 0; i < 500; i++) {
      table.deleteRecord(recordIds[i]);
    }

    Iterator<Record> iRec = table.iterator();
    for (int i = 500; i < 1000; i++) {
      assertTrue(iRec.hasNext());
      assertEquals(input, iRec.next());
    }
    assertFalse(iRec.hasNext());
  }

  @Test
  public void testTableIteratorGapBack() throws DatabaseException {
    Record input = TestUtils.createRecordWithAllTypes();
    RecordID[] recordIds = new RecordID[1000];
    for (int i = 0; i < 1000; i++) {
      recordIds[i] = table.addRecord(input.getValues());
    }

    for (int i = 500; i < 1000; i++) {
      table.deleteRecord(recordIds[i]);
    }

    Iterator<Record> iRec = table.iterator();
    for (int i = 0; i < 500; i++) {
      assertTrue(iRec.hasNext());
      assertEquals(input, iRec.next());
    }
    assertFalse(iRec.hasNext());
  }

  @Test
  public void testTableDurable() throws Exception {
    Record input = TestUtils.createRecordWithAllTypes();

    int numEntriesPerPage = table.getNumEntriesPerPage();

    // create one page's worth of entries
    for (int i = 0; i < numEntriesPerPage; i++) {
      RecordID rid = table.addRecord(input.getValues());

      // ensure that records are created in sequential slot order on the sam page
      assertEquals(1, rid.getPageNum());
      assertEquals(i, rid.getEntryNumber());
    }

    // add one more to make sure the next page is created
    RecordID rid = table.addRecord(input.getValues());
    assertEquals(2, rid.getPageNum());
    assertEquals(0, rid.getEntryNumber());
    // close table and reopen
    table.close();

    String tempFolderPath = tempFolder.getRoot().getAbsolutePath();
    this.table = new Table(TABLENAME, tempFolderPath);

    for (int i = 0; i < numEntriesPerPage; i++) {
      Record rec = table.getRecord(new RecordID(1, i));
      assertEquals(input, rec);
    }
    Record rec = table.getRecord(new RecordID(2, 0));
    assertEquals(input, rec);
  }

  @Test
  public void testTableDurableAppends() throws Exception {
    Record input = TestUtils.createRecordWithAllTypes();

    int numEntriesPerPage = table.getNumEntriesPerPage();

    for (int i = 0; i < numEntriesPerPage; i++) {
      RecordID rid = table.addRecord(input.getValues());
      assertEquals(1, rid.getPageNum());
      assertEquals(i, rid.getEntryNumber());
    }

    for (int i = 0; i < numEntriesPerPage; i++) {
      RecordID rid = table.addRecord(input.getValues());
      assertEquals(2, rid.getPageNum());
      assertEquals(i, rid.getEntryNumber());
    }

    for (int i = 0; i < numEntriesPerPage; i++) {
      RecordID rid = table.addRecord(input.getValues());
      assertEquals(3, rid.getPageNum());
      assertEquals(i, rid.getEntryNumber());
    }

    // close table and reopen
    table.close();
    String tempFolderPath = tempFolder.getRoot().getAbsolutePath();
    this.table = new Table(TABLENAME, tempFolderPath);

    for (int i = 0; i < numEntriesPerPage; i++) {
      RecordID rid = table.addRecord(input.getValues());
      assertEquals(4, rid.getPageNum());
      assertEquals(i, rid.getEntryNumber());
    }

  }
  @Test
  public void testTableDurablePartialAppend() throws Exception {
    Record input = TestUtils.createRecordWithAllTypes();

    int numEntriesPerPage = table.getNumEntriesPerPage();

    // create one page's worth of entries
    for (int i = 0; i < numEntriesPerPage; i++) {
      input.getValues().get(1).setInt(i);
      RecordID rid = table.addRecord(input.getValues());

      // ensure that records are created in sequential slot order on the sam page
      assertEquals(1, rid.getPageNum());
      assertEquals(i, rid.getEntryNumber());
    }

    // add one more to make sure the next page is created
    input.getValues().get(1).setInt(0);
    RecordID rid = table.addRecord(input.getValues());
    assertEquals(2, rid.getPageNum());
    assertEquals(0, rid.getEntryNumber());
    // close table and reopen
    table.close();

    String tempFolderPath = tempFolder.getRoot().getAbsolutePath();
    this.table = new Table(TABLENAME, tempFolderPath);

    for (int i = 0; i < numEntriesPerPage; i++) {
      Record rec = table.getRecord(new RecordID(1, i));
      input.getValues().get(1).setInt(i);
      assertEquals(input, rec);
    }
    Record rec = table.getRecord(new RecordID(2, 0));
    input.getValues().get(1).setInt(0);

    assertEquals(input, rec);

    rid = table.addRecord(input.getValues());
    assertEquals(2, rid.getPageNum());
    assertEquals(1, rid.getEntryNumber());
  }

  @Test
  public void testTableIteratorGapBackDurable() throws DatabaseException {
    Record input = TestUtils.createRecordWithAllTypes();
    RecordID[] recordIds = new RecordID[1000];
    for (int i = 0; i < 1000; i++) {
      input.getValues().get(1).setInt(i);
      recordIds[i] = table.addRecord(input.getValues());
    }

    for (int i = 500; i < 1000; i++) {
      table.deleteRecord(recordIds[i]);
    }

    Iterator<Record> iRec = table.iterator();
    for (int i = 0; i < 500; i++) {
      assertTrue(iRec.hasNext());
      input.getValues().get(1).setInt(i);
      assertEquals(input, iRec.next());
    }
    assertFalse(iRec.hasNext());

    table.close();
    String tempFolderPath = tempFolder.getRoot().getAbsolutePath();
    this.table = new Table(TABLENAME, tempFolderPath);

    iRec = table.iterator();
    for (int i = 0; i < 500; i++) {
      assertTrue(iRec.hasNext());
      input.getValues().get(1).setInt(i);
      assertEquals(input, iRec.next());
    }
    assertFalse(iRec.hasNext());

  }
  @Test
  public void testTableIteratorGapFrontDurable() throws DatabaseException {
    Record input = TestUtils.createRecordWithAllTypes();
    RecordID[] recordIds = new RecordID[1000];
    for (int i = 0; i < 1000; i++) {
      input.getValues().get(1).setInt(i);
      recordIds[i] = table.addRecord(input.getValues());
    }

    for (int i = 0; i < 500; i++) {
      table.deleteRecord(recordIds[i]);
    }

    Iterator<Record> iRec = table.iterator();
    for (int i = 500; i < 1000; i++) {
      assertTrue(iRec.hasNext());
      input.getValues().get(1).setInt(i);
      assertEquals(input, iRec.next());
    }
    assertFalse(iRec.hasNext());

    table.close();
    String tempFolderPath = tempFolder.getRoot().getAbsolutePath();
    this.table = new Table(TABLENAME, tempFolderPath);

    iRec = table.iterator();
    for (int i = 500; i < 1000; i++) {
      assertTrue(iRec.hasNext());
      Record r = iRec.next();
      input.getValues().get(1).setInt(i);
      assertEquals(input, r);
    }
    assertFalse(iRec.hasNext());
  }

}
