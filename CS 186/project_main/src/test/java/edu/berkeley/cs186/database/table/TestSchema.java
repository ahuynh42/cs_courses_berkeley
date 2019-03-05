package edu.berkeley.cs186.database.table;

import edu.berkeley.cs186.database.StudentTest;
import edu.berkeley.cs186.database.TestUtils;
import edu.berkeley.cs186.database.databox.DataBox;
import edu.berkeley.cs186.database.databox.IntDataBox;
import edu.berkeley.cs186.database.databox.StringDataBox;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class TestSchema {
  @Test
  @Category(StudentTest.class)
  public void testFieldTypesBeforeAfter() {
    Schema beoforeSchema = TestUtils.createSchemaWithAllTypes();
    List<DataBox> beforeFieldTypes = beoforeSchema.getFieldTypes();

    Record record = TestUtils.createRecordWithAllTypes();
    byte[] encoded = beoforeSchema.encode(record);
    Record decoded = beoforeSchema.decode(encoded);

    List<String> fieldNames = new ArrayList<String>();
    fieldNames.add("bool");
    fieldNames.add("int");
    fieldNames.add("string");
    fieldNames.add("float");

    Schema afterSchema = new Schema(fieldNames, decoded.getValues());
    List<DataBox> afterFieldTypes = afterSchema.getFieldTypes();

    for (int i = 0; i < 4; i++) {
      assertEquals(beforeFieldTypes.get(i).type(), afterFieldTypes.get(i).type());
    }
  }

  @Test
  public void testSchemaRetrieve() {
    Schema schema = TestUtils.createSchemaWithAllTypes();

    Record input = TestUtils.createRecordWithAllTypes();
    byte[] encoded = schema.encode(input);
    Record decoded = schema.decode(encoded);

    assertEquals(input, decoded);
  }

  @Test
  public void testValidRecord() {
    Schema schema = TestUtils.createSchemaWithAllTypes();
    Record input = TestUtils.createRecordWithAllTypes();

    try {
      Record output = schema.verify(input.getValues());
      assertEquals(input, output);
    } catch (SchemaException se) {
      fail();
    }
  }

  @Test(expected = SchemaException.class)
  public void testInvalidRecordLength() throws SchemaException {
    Schema schema = TestUtils.createSchemaWithAllTypes();
    schema.verify(new ArrayList<DataBox>());
  }

  @Test(expected = SchemaException.class)
  public void testInvalidFields() throws SchemaException {
    Schema schema = TestUtils.createSchemaWithAllTypes();
    List<DataBox> values = new ArrayList<DataBox>();

    values.add(new StringDataBox("abcde", 5));
    values.add(new IntDataBox(10));

    schema.verify(values);
  }

}
