package edu.berkeley.cs186.database.index;

import edu.berkeley.cs186.database.table.RecordID;
import edu.berkeley.cs186.database.databox.*;
import edu.berkeley.cs186.database.StudentTestP2;

import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.Timeout;
import org.junit.experimental.categories.Category;

import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;

public class TestLargeBPlusTree {
    public static final String testFile = "BPlusTreeTest";
    private BPlusTree bp;
    public static final int intLeafPageSize = 400;
    public static final int intInnPageSize = 496;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Rule
    // 160 seconds max per method tested.
    public Timeout globalTimeout = Timeout.seconds(160);

    @Before
    public void beforeEach() throws Exception {
        tempFolder.newFile(testFile);
        String tempFolderPath = tempFolder.getRoot().getAbsolutePath();
        this.bp = new BPlusTree(new IntDataBox(), testFile, tempFolderPath);
    }

    @Test
    @Category(StudentTestP2.class)
    public void testSimpleInsert() {
        bp.insertKey(new IntDataBox(0), new RecordID(0, 0));

        assertEquals(1, bp.getNumNodes());
        assertEquals((byte) 1, bp.allocator.fetchPage(bp.getRootPageNum()).readByte(0));

        LeafNode root = new LeafNode(bp, bp.getRootPageNum());
        assertEquals(1, root.getAllValidEntries().size());
        assertEquals(0, root.getAllValidEntries().get(0).getKey().getInt());
    }

    @Test
    @Category(StudentTestP2.class)
    public void testIsSorted() {
        for (int i = 0; i < intLeafPageSize; i += 2) {
            bp.insertKey(new IntDataBox(i), new RecordID(i, i));
        }
        for (int i = 1; i < intLeafPageSize; i += 2) {
            bp.insertKey(new IntDataBox(i), new RecordID(i, i));
        }

        LeafNode leaf = new LeafNode(bp, bp.getRootPageNum());
        List<BEntry> allEntries = leaf.getAllValidEntries();

        for (int i = 0; i < intLeafPageSize; i++) {
            assertEquals(i, allEntries.get(i).getKey().getInt());
        }
    }

    @Test
    @Category(StudentTestP2.class)
    public void testNewRootNode() {
        int prevRootPageNum = bp.getRootPageNum();

        for (int i = 0; i < intLeafPageSize + 1; i++) {
            bp.insertKey(new IntDataBox(i), new RecordID(i, i));
        }

        assertNotEquals(prevRootPageNum, bp.getRootPageNum());
        assertEquals(prevRootPageNum, new InnerNode(bp, bp.getRootPageNum()).getFirstChild());
        assertEquals((byte) 0, bp.allocator.fetchPage(bp.getRootPageNum()).readByte(0));
    }

    @Test
    @Category(StudentTestP2.class)
    public void testNewRootEntry() {
        for (int i = 0; i < intLeafPageSize + 1; i++) {
            bp.insertKey(new IntDataBox(i), new RecordID(i, i));
        }

        InnerNode inner = new InnerNode(bp, bp.getRootPageNum());
        assertEquals(1, inner.getAllValidEntries().size());
        assertEquals(200, inner.getAllValidEntries().get(0).getKey().getInt());
    }

    @Test
    @Category(StudentTestP2.class)
    public void testNewLeaves() {
        for (int i = 0; i < intLeafPageSize + 1; i++) {
            bp.insertKey(new IntDataBox(i), new RecordID(i, i));
        }

        InnerNode inner = new InnerNode(bp, bp.getRootPageNum());
        LeafNode leftLeaf = new LeafNode(bp, inner.getFirstChild());
        LeafNode rightLeaf = new LeafNode(bp, inner.getAllValidEntries().get(0).getPageNum());

        List<BEntry> leftEntries = leftLeaf.getAllValidEntries();
        assertEquals(200, leftEntries.size());
        for (int i = 0; i < 200; i++) {
            assertEquals(i, leftEntries.get(i).getKey().getInt());
        }

        List<BEntry> rightEntries = rightLeaf.getAllValidEntries();
        assertEquals(201, rightEntries.size());
        for (int i = 0; i < 201; i++) {
            assertEquals(i + 200, rightEntries.get(i).getKey().getInt());
        }
    }

    @Test
    @Category(StudentTestP2.class)
    public void testSplitOne() {
        for (int i = 0; i < intLeafPageSize + 1; i++) {
            bp.insertKey(new IntDataBox(i % 2), new RecordID(i % 2, i));
        }

        assertEquals(3, bp.getNumNodes());

        Iterator<RecordID> iter0 = bp.lookupKey(new IntDataBox(0));
        Iterator<RecordID> iter1 = bp.lookupKey(new IntDataBox(1));

        for (int i = 0; i < intLeafPageSize / 2; i++) {
            assertEquals(0, iter0.next().getPageNum());
            assertEquals(1, iter1.next().getPageNum());
        }

        assertEquals(0, iter0.next().getPageNum());
        assertFalse(iter0.hasNext());
        assertFalse(iter1.hasNext());
    }

    @Test
    @Category(StudentTestP2.class)
    public void testSplitMany() {
        for (int i = 0; i < 8 * intLeafPageSize + 1; i++) {
            bp.insertKey(new IntDataBox(i % 8), new RecordID(i % 8, i));
        }

        assertEquals(13, bp.getNumNodes());

        Iterator<RecordID> iter0 = bp.lookupKey(new IntDataBox(0));
        Iterator<RecordID> iter1 = bp.lookupKey(new IntDataBox(1));
        Iterator<RecordID> iter2 = bp.lookupKey(new IntDataBox(2));
        Iterator<RecordID> iter3 = bp.lookupKey(new IntDataBox(3));
        Iterator<RecordID> iter4 = bp.lookupKey(new IntDataBox(4));
        Iterator<RecordID> iter5 = bp.lookupKey(new IntDataBox(5));
        Iterator<RecordID> iter6 = bp.lookupKey(new IntDataBox(6));
        Iterator<RecordID> iter7 = bp.lookupKey(new IntDataBox(7));

        for (int i = 0; i < intLeafPageSize; i++) {
            assertEquals(0, iter0.next().getPageNum());
            assertEquals(1, iter1.next().getPageNum());
            assertEquals(2, iter2.next().getPageNum());
            assertEquals(3, iter3.next().getPageNum());
            assertEquals(4, iter4.next().getPageNum());
            assertEquals(5, iter5.next().getPageNum());
            assertEquals(6, iter6.next().getPageNum());
            assertEquals(7, iter7.next().getPageNum());
        }

        assertEquals(0, iter0.next().getPageNum());
        assertFalse(iter0.hasNext());
        assertFalse(iter1.hasNext());
        assertFalse(iter2.hasNext());
        assertFalse(iter3.hasNext());
        assertFalse(iter4.hasNext());
        assertFalse(iter5.hasNext());
        assertFalse(iter6.hasNext());
        assertFalse(iter7.hasNext());
    }

    @Test
    @Category(StudentTestP2.class)
    public void testIterLeaf() {
        for (int i = 0; i < intLeafPageSize; i++) {
            bp.insertKey(new IntDataBox(i), new RecordID(i, i));
        }

        Iterator<RecordID> all = bp.sortedScan();
        Iterator<RecordID> range = bp.sortedScanFrom(new IntDataBox(intLeafPageSize / 2));
        Iterator<RecordID> equality = bp.lookupKey(new IntDataBox(0));

        for (int i = 0; i < intLeafPageSize; i++) {
            assertEquals(i, all.next().getPageNum());
        }
        assertFalse(all.hasNext());

        for (int i = intLeafPageSize / 2; i < intLeafPageSize; i++) {
            assertEquals(i, range.next().getPageNum());
        }
        assertFalse(range.hasNext());

        assertEquals(0, equality.next().getPageNum());
        assertFalse(equality.hasNext());
    }

    @Test
    @Category(StudentTestP2.class)
    public void testIterSplitSmall() {
        for (int i = 0; i < intLeafPageSize; i++) {
            for (int j = 0; j < 4; j++) {
                bp.insertKey(new IntDataBox(j), new RecordID(j, i));
            }
        }

        Iterator<RecordID> all = bp.sortedScan();
        Iterator<RecordID> range = bp.sortedScanFrom(new IntDataBox(2));
        Iterator<RecordID> equality = bp.lookupKey(new IntDataBox(0));

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < intLeafPageSize; j++) {
                assertEquals(i, all.next().getPageNum());
            }
        }
        assertFalse(all.hasNext());

        for (int i = 2; i < 4; i++) {
            for (int j = 0; j < intLeafPageSize; j++) {
                assertEquals(i, range.next().getPageNum());
            }
        }
        assertFalse(range.hasNext());

        for (int i = 0; i < intLeafPageSize; i++) {
            assertEquals(0, equality.next().getPageNum());
        }
        assertFalse(equality.hasNext());
    }

    @Test
    @Category(StudentTestP2.class)
    public void testIterSplitMany() {
        for (int i = 0; i < 2 * intLeafPageSize; i++) {
            for (int j = 0; j < 16; j++) {
                bp.insertKey(new IntDataBox(j), new RecordID(j, i));
            }
        }

        Iterator<RecordID> all = bp.sortedScan();
        Iterator<RecordID> range = bp.sortedScanFrom(new IntDataBox(8));
        Iterator<RecordID> equality = bp.lookupKey(new IntDataBox(0));

        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 2 * intLeafPageSize; j++) {
                assertEquals(i, all.next().getPageNum());
            }
        }
        assertFalse(all.hasNext());

        for (int i = 8; i < 16; i++) {
            for (int j = 0; j < 2 * intLeafPageSize; j++) {
                assertEquals(i, range.next().getPageNum());
            }
        }
        assertFalse(range.hasNext());

        for (int i = 0; i < 2 * intLeafPageSize; i++) {
            assertEquals(0, equality.next().getPageNum());
        }
        assertFalse(equality.hasNext());
    }

    @Test
    public void testBPlusTreeInsert() {
        /** Insert records to separate pages in increasing pageNum order. */
        for (int i = 0; i < 10; i++) {
            bp.insertKey(new IntDataBox(i), new RecordID(i,0));
        }

        Iterator<RecordID> rids = bp.sortedScan();
        int expectedPageNum = 0;
        while (rids.hasNext()) {
            assertEquals(expectedPageNum, rids.next().getPageNum());
            expectedPageNum++;
        }
        assertEquals(1, this.bp.getNumNodes());
    }

    @Test
    public void testBPlusTreeInsertIterateFrom() {
        /**
         * Scan records starting from the middle page after inserting records
         * to separate pages in decreasing pageNum order.
         */
        for (int i = 16; i >= 0; i--) {
            bp.insertKey(new IntDataBox(i), new RecordID(i,0));
        }
        Iterator<RecordID> rids = bp.sortedScanFrom(new IntDataBox(10));
        int expectedPageNum = 10;
        while (rids.hasNext()) {
            assertEquals(expectedPageNum, rids.next().getPageNum());
            expectedPageNum++;
        }
        assertEquals(17, expectedPageNum);
        assertEquals(1, this.bp.getNumNodes());
    }

    @Test
    public void testBPlusTreeInsertIterateFullLeafNode() {
        /** Insert enough records to fill a leaf. */
        for (int i = 0; i < intLeafPageSize; i++) {
            bp.insertKey(new IntDataBox(i), new RecordID(i,0));
        }
        Iterator<RecordID> rids = bp.sortedScan();
        int expectedPageNum = 0;
        while (rids.hasNext()) {
            RecordID rid = rids.next();
            assertEquals(expectedPageNum, rid.getPageNum());
            expectedPageNum++;
        }
        assertEquals(intLeafPageSize, expectedPageNum);
        assertEquals(1, this.bp.getNumNodes());
    }

    @Test
    public void testBPlusTreeInsertIterateFullLeafSplit() {
        /** Split a full leaf by inserting a leaf's page size + 1 records. */
        for (int i = 0; i < intLeafPageSize + 1; i++) {
            bp.insertKey(new IntDataBox(i), new RecordID(i,0));
        }

        Iterator<RecordID> rids = bp.sortedScan();
        assertTrue(rids.hasNext());
        int expectedPageNum = 0;
        while (rids.hasNext()) {
            RecordID rid = rids.next();
            assertEquals(expectedPageNum, rid.getPageNum());
            expectedPageNum++;
        }
        assertEquals(intLeafPageSize + 1, expectedPageNum);
        assertEquals(3, this.bp.getNumNodes());
    }

    @Test
    public void testBPlusTreeInsertAppendIterateMultipleFullLeafSplit() {
        /**
         * Split leaves three times by inserting enough records for four
         * leaves: three full and one with a record.
         */
        for (int i = 0; i < 3*intLeafPageSize + 1; i++) {
//            System.out.println(i);
            bp.insertKey(new IntDataBox(i), new RecordID(i,0));
        }

        Iterator<RecordID> rids = bp.sortedScan();
        int expectedPageNum = 0;
        while (rids.hasNext()) {
            RecordID rid = rids.next();
            assertEquals(expectedPageNum, rid.getPageNum());
            expectedPageNum++;
        }
        assertEquals(3*intLeafPageSize + 1, expectedPageNum);
        assertEquals(7, this.bp.getNumNodes());
    }


    @Test
    public void testFullPage() {
        /**
         * Insert four leaves in a sweeping fashion: three full and one with a
         * record.
         */
        for (int i = 0; i < 3*intLeafPageSize + 1; i++) {
            bp.insertKey(new IntDataBox(i % 3), new RecordID(i % 3, i));
        }

        assertEquals(5, this.bp.getNumNodes());

        Iterator<RecordID> rids = bp.sortedScan();
        for (int i = 0; i < intLeafPageSize + 1; i++) {
            assertTrue(rids.hasNext());
            RecordID rid = rids.next();
            assertEquals(0, rid.getPageNum());
        }
    }

    @Test
    public void testBPlusTreeSweepInsertSortedScanMultipleFullLeafSplit() {
        /**
         * Insert 3 full leafs of records plus one additional record.
         * Inserts are done in a sweeping fashion:
         *   1st insert of value 0 on (page 0, entry 0)
         *   2nd insert of value 1 on (page 1, entry 1)
         *   3rd insert of value 2 on (page 2, entry 2)
         *   4th insert of value 0 on (page 0, entry 4) ...
         * Expect page number and key number to be the same.
         */
        for (int i = 0; i < 3*intLeafPageSize + 1; i++) {
            bp.insertKey(new IntDataBox(i % 3), new RecordID(i % 3, i));
        }

        assertEquals(5, this.bp.getNumNodes());

        Iterator<RecordID> rids = bp.sortedScan();
        assertTrue(rids.hasNext());

        for (int i = 0; i < intLeafPageSize + 1; i++) {
            assertTrue(rids.hasNext());
            RecordID rid = rids.next();
            assertEquals(0, rid.getPageNum());
            assertEquals(i * 3, rid.getEntryNumber());
        }

        for (int i = 0; i < intLeafPageSize; i++) {
            assertTrue(rids.hasNext());
            RecordID rid = rids.next();
            assertEquals(1, rid.getPageNum());
        }

        for (int i = 0; i < intLeafPageSize; i++) {
            assertTrue(rids.hasNext());
            RecordID rid = rids.next();
            assertEquals(2, rid.getPageNum());
        }
        assertFalse(rids.hasNext());
    }

    @Test
    public void testBPlusTreeSweepInsertLookupKeyMultipleFullLeafSplit() {
        /**
         * Insert four full leaves of records in a sweeping fashion.
         * Ensure that you correctly handle multiple leaf splits.
         */
        for (int i = 0; i < 8*intLeafPageSize; i++) {
            bp.insertKey(new IntDataBox(i % 4), new RecordID(i % 4, i));
        }

        System.out.println("pass 0");

        assertEquals(15, this.bp.getNumNodes());

        System.out.println("pass 1");

        Iterator<RecordID> rids;

        rids = bp.lookupKey(new IntDataBox(0));
        assertTrue(rids.hasNext());

        for (int i = 0; i < 2*intLeafPageSize; i++) {
            assertTrue("iteration " + i, rids.hasNext());
            RecordID rid = rids.next();
            assertEquals(0, rid.getPageNum());
        }
        assertFalse(rids.hasNext());

        rids = bp.lookupKey(new IntDataBox(1));
        assertTrue(rids.hasNext());
        for (int i = 0; i < 2*intLeafPageSize; i++) {
            assertTrue(rids.hasNext());
            RecordID rid = rids.next();
            assertEquals(1, rid.getPageNum());
        }
        assertFalse(rids.hasNext());

        rids = bp.lookupKey(new IntDataBox(2));
        assertTrue(rids.hasNext());

        for (int i = 0; i < 2*intLeafPageSize; i++) {
            assertTrue(rids.hasNext());
            RecordID rid = rids.next();
            assertEquals(2, rid.getPageNum());
        }
        assertFalse(rids.hasNext());

        rids = bp.lookupKey(new IntDataBox(3));
        assertTrue(rids.hasNext());

        for (int i = 0; i < 2*intLeafPageSize; i++) {
            assertTrue(rids.hasNext());
            RecordID rid = rids.next();
            assertEquals(3, rid.getPageNum());
        }
        assertFalse(rids.hasNext());

    }

    @Test
    public void testBPlusTreeSweepInsertSortedScanLeafSplit() {
        /**
         * Insert ten full leaves of records in a sweeping fashion.
         * Ensure that iterator works when a value spans more than one page.
         * Example: Two leaf pages will contain keys with a value of 0.
         */
        for (int i = 0; i < 10*intLeafPageSize; i++) {
            bp.insertKey(new IntDataBox(i % 5), new RecordID(i % 5, i));
        }

        assertEquals(19, this.bp.getNumNodes());

        Iterator<RecordID> rids = bp.sortedScan();
        assertTrue(rids.hasNext());
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 2*intLeafPageSize; j++) {
                assertTrue(rids.hasNext());
                RecordID rid = rids.next();
                assertEquals(i, rid.getPageNum());
            }
        }
        assertFalse(rids.hasNext());

    }

    @Test
    public void testBPlusTreeSweepInsertSortedScanFromLeafSplit() {
        /**
         * Insert ten full leaves of records in a sweeping fashion.
         * Ensure that sortedScanFrom works when a value spans more than one
         * page.
         */
        for (int i = 0; i < 10*intLeafPageSize; i++) {
            bp.insertKey(new IntDataBox(i % 5), new RecordID(i % 5, i));
        }

        assertEquals(19, this.bp.getNumNodes());

        for (int k = 0; k < 5; k++) {
            Iterator<RecordID> rids = bp.sortedScanFrom(new IntDataBox(k));
            assertTrue(rids.hasNext());
            for (int i = k; i < 5; i++) {
                for (int j = 0; j < 2*intLeafPageSize; j++) {
                    assertTrue(rids.hasNext());
                    RecordID rid = rids.next();
                    assertEquals(i, rid.getPageNum());
                }
            }
            assertFalse(rids.hasNext());
        }
    }

    @Test
    public void testBPlusTreeAppendInsertSortedScanInnerSplit() {
        /**
         * Insert enough keys to cause an InnerNode split.
         */
        for (int i = 0; i < (intInnPageSize/2 + 1)*(intLeafPageSize); i++) {
            bp.insertKey(new IntDataBox(i), new RecordID(i, 0));
        }

        assertEquals(498, this.bp.getNumNodes());

        Iterator<RecordID> rids = bp.sortedScan();

        for (int i = 0; i < (intInnPageSize/2 + 1)*(intLeafPageSize); i++) {
            assertTrue(rids.hasNext());
            RecordID rid = rids.next();
            assertEquals(i, rid.getPageNum());
        }
        assertFalse(rids.hasNext());

    }

    @Test
    public void testBPlusTreeSweepInsertLookupInnerSplit() {
        /**
         * Insert enough keys to cause an InnerNode split: numEntries +
         * firstChild.
         * Each key should span 2 pages.
         */
        for (int i = 0; i < 2*intLeafPageSize; i++) {
            for (int k = 0; k < 250; k++) {
                bp.insertKey(new IntDataBox(k), new RecordID(k, 0));
            }
        }

        assertEquals(865, this.bp.getNumNodes());

        for (int k = 0; k < 250; k++) {
            Iterator<RecordID> rids = bp.lookupKey(new IntDataBox(k));
            for (int i = 0; i < 2*intLeafPageSize; i++) {
                assertTrue("Loop: " + k + " iteration " + i, rids.hasNext());
                RecordID rid = rids.next();
                assertEquals(k, rid.getPageNum());
            }
            assertFalse(rids.hasNext());
        }
    }
}
