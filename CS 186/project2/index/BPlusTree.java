package edu.berkeley.cs186.database.index;

import edu.berkeley.cs186.database.io.Page;
import edu.berkeley.cs186.database.io.PageAllocator;
import edu.berkeley.cs186.database.table.RecordID;
import edu.berkeley.cs186.database.databox.*;

import java.util.*;
import java.nio.file.Paths;

import static edu.berkeley.cs186.database.databox.DataBox.Types.STRING;

/**
 * A B+ tree. Allows the user to add, delete, search, and scan for keys in an
 * index. A BPlusTree has an associated page allocator. The first page in the
 * page allocator is a header page that serializes the search key data type,
 * root node page, and first leaf node page. Each subsequent page is a
 * BPlusNode, specifically either an InnerNode or LeafNode. Note that a
 * BPlusTree can have duplicate keys that appear across multiple pages.
 *
 * Properties:
 * allocator: PageAllocator for this index
 * keySchema: DataBox for this index's search key
 * rootPageNum: page number of the root node
 * firstLeafPageNum: page number of the first leaf node
 * numNodes: number of BPlusNodes
 */
public class BPlusTree {
    public static final String FILENAME_PREFIX = "db";
    public static final String FILENAME_EXTENSION = ".index";

    protected PageAllocator allocator;
    protected DataBox keySchema;
    private int rootPageNum;
    private int firstLeafPageNum;
    private int numNodes;

    private BPlusTree self = this;

    /**
     * This constructor is used for creating an empty BPlusTree.
     *
     * @param keySchema the schema of the index key
     * @param fName the filename of select the index will be built
     */
    public BPlusTree(DataBox keySchema, String fName) {
        this(keySchema, fName, FILENAME_PREFIX);
    }

    public BPlusTree(DataBox keySchema, String fName, String filePrefix) {
        String pathname = Paths.get(filePrefix, fName + FILENAME_EXTENSION).toString();
        this.allocator = new PageAllocator(pathname, true);
        this.keySchema = keySchema;
        int headerPageNum = this.allocator.allocPage();
        assert(headerPageNum == 0);
        this.numNodes = 0;
        BPlusNode root = new LeafNode(this);
        this.rootPageNum = root.getPageNum();
        this.firstLeafPageNum = rootPageNum;
        writeHeader();
    }

    /**
     * This constructor is used for loading a BPlusTree from a file.
     *
     * @param fName the filename of a preexisting BPlusTree
     */
    public BPlusTree(String fName) {
        this(fName, FILENAME_PREFIX);
    }

    public BPlusTree(String fName, String filePrefix) {
        String pathname = Paths.get(filePrefix, fName + FILENAME_EXTENSION).toString();
        this.allocator = new PageAllocator(pathname, false);
        this.readHeader();
    }

    public void incrementNumNodes() {
        this.numNodes++;
    }

    public void decrementNumNodes() {
        this.numNodes--;
    }

    public int getRootPageNum() {
        return rootPageNum;
    }

    public int getNumNodes() {
        return this.numNodes;
    }

    /**
     * Perform a sorted scan.
     * The iterator should return all RecordIDs, starting from the beginning to
     * the end of the index.
     *
     * @return Iterator of all RecordIDs in sorted order
     */
    public Iterator<RecordID> sortedScan() {
        BPlusNode rootNode = BPlusNode.getBPlusNode(this, rootPageNum);
        return new BPlusIterator(rootNode);
    }

    /**
     * Perform a range search beginning from a specified key.
     * The iterator should return all RecordIDs, starting from the specified
     * key to the end of the index.
     *
     * @param keyStart the key to start iterating from
     * @return Iterator of RecordIDs that are equal to or greater than keyStart
     * in sorted order
     */
    public Iterator<RecordID> sortedScanFrom(DataBox keyStart) {
        BPlusNode root = BPlusNode.getBPlusNode(this, rootPageNum);
        return new BPlusIterator(root, keyStart, true);
    }

    /**
     * Perform an equality search on the specified key.
     * The iterator should return all RecordIDs that match the specified key.
     *
     * @param key the key to match
     * @return Iterator of RecordIDs that match the given key
     */
    public Iterator<RecordID> lookupKey(DataBox key) {
        BPlusNode root = BPlusNode.getBPlusNode(this, rootPageNum);
        return new BPlusIterator(root, key, false);
    }

    /**
     * Insert a (Key, RecordID) tuple.
     *
     * @param key the key to insert
     * @param rid the RecordID of the given key
     */
    public void insertKey(DataBox key, RecordID rid) {
        // Implement me!
        Page p = allocator.fetchPage(rootPageNum);
        InnerEntry ret = null;

        if (p.readByte(0) == (byte) 1) {
            LeafNode leaf = new LeafNode(this, rootPageNum);
            ret = leaf.insertBEntry(new LeafEntry(key, rid));
        } else {
            InnerNode inner = new InnerNode(this, rootPageNum);
            ret = inner.insertBEntry(new LeafEntry(key, rid));
        }

        if (ret != null) {
            InnerNode newRoot = new InnerNode(this);
            List<BEntry> rootEntry = new ArrayList<BEntry>();
            rootEntry.add(ret);
            newRoot.overwriteBNodeEntries(rootEntry);
            newRoot.setFirstChild(rootPageNum);
            updateRoot(newRoot.getPageNum());
        }
    }

    /**
     * Delete an entry with the matching key and RecordID.
     *
     * @param key the key to be deleted
     * @param rid the RecordID of the key to be deleted
     */
    public boolean deleteKey(DataBox key, RecordID rid) {
        /* You will not have to implement this in this project. */
        throw new BPlusTreeException("BPlusTree#DeleteKey Not Implemented!");
    }

    /**
     * Perform an equality search on the specified key.
     *
     * @param key the key to lookup
     * @return true if the key exists in this BPlusTree, false otherwise
     */
    public boolean containsKey(DataBox key) {
        return lookupKey(key).hasNext();
    }

    /**
     * Return the number of pages.
     *
     * @return the number of pages.
     */
    public int getNumPages() {
        return this.allocator.getNumPages();
    }

    /**
     * Update the root page.
     *
     * @param pNum the page number of the new root node
     */
    protected void updateRoot(int pNum) {
        this.rootPageNum = pNum;
        writeHeader();
    }

    private void writeHeader() {
        Page headerPage = allocator.fetchPage(0);
        int bytesWritten = 0;

        headerPage.writeInt(bytesWritten, this.rootPageNum);
        bytesWritten += 4;

        headerPage.writeInt(bytesWritten, this.firstLeafPageNum);
        bytesWritten += 4;

        headerPage.writeInt(bytesWritten, keySchema.type().ordinal());
        bytesWritten += 4;

        if (this.keySchema.type().equals(STRING)) {
            headerPage.writeInt(bytesWritten, this.keySchema.getSize());
            bytesWritten += 4;
        }
        headerPage.flush();
    }

    private void readHeader() {
        Page headerPage = allocator.fetchPage(0);

        int bytesRead = 0;

        this.rootPageNum = headerPage.readInt(bytesRead);
        bytesRead += 4;

        this.firstLeafPageNum = headerPage.readInt(bytesRead);
        bytesRead += 4;

        int keyOrd = headerPage.readInt(bytesRead);
        bytesRead += 4;
        DataBox.Types type = DataBox.Types.values()[keyOrd];

        switch(type) {
            case INT:
                this.keySchema = new IntDataBox();
                break;
            case STRING:
                int len = headerPage.readInt(bytesRead);
                this.keySchema = new StringDataBox(len);
                break;
            case BOOL:
                this.keySchema = new BoolDataBox();
                break;
            case FLOAT:
                this.keySchema = new FloatDataBox();
                break;
        }
    }

    private enum TypeOfScan {SORTED_SCAN, SORTED_SCAN_FROM, LOOKUP_KEY};

    /**
     * A BPlusIterator provides several ways of iterating over RecordIDs stored
     * in a BPlusTree.
     */
    private class BPlusIterator implements Iterator<RecordID> {
        // Implement me!
        Iterator<RecordID> iter;

        /**
         * Construct an iterator that performs a sorted scan on this BPlusTree
         * tree.
         * The iterator should return all RecordIDs, starting from the
         * beginning to the end of the index.
         *
         * @param root the root node of this BPlusTree
         */
        public BPlusIterator(BPlusNode root) {
            // Implement me!
            List<RecordID> rid = iteratorHelper1(root.getPage());
            iter = rid.iterator();
        }

        private List<RecordID> iteratorHelper1(Page p) {
            List<RecordID> ret = new ArrayList<RecordID>();
            if (p.readByte(0) == (byte) 1) {
                LeafNode leaf = new LeafNode(self, p.getPageNum());
                Iterator<RecordID> temp = leaf.scan();
                while (temp.hasNext()) {
                    ret.add(temp.next());
                }
                return ret;
            } else {
                InnerNode inner = new InnerNode(self, p.getPageNum());
                PageAllocator alloc = self.allocator;
                ret.addAll(iteratorHelper1(alloc.fetchPage(inner.getFirstChild())));
                List<BEntry> allEntries = inner.getAllValidEntries();
                int numEntries = allEntries.size();
                for (int i = 0; i < numEntries; i++) {
                    ret.addAll(iteratorHelper1(alloc.fetchPage(allEntries.get(i).getPageNum())));
                }
            }
            return ret;
        }

        /**
         * Construct an iterator that performs either an equality or range
         * search with a specified key.
         * If @param scan is true, the iterator should return all RecordIDs,
         * starting from the specified key to the end of the index.
         * If @param scan is false, the iterator should return all RecordIDs
         * that match the specified key.
         *
         * @param root the root node of this BPlusTree
         * @param key the specified key value
         * @param scan if true, do a range search; else, equality search
         */
        public BPlusIterator(BPlusNode root, DataBox key, boolean scan) {
            // Implement me!
            if (scan) {
                List<RecordID> rid = iteratorHelper2(root.getPage(), key);
                iter = rid.iterator();
            } else {
                List<RecordID> rid = iteratorHelper3(root.getPage(), key);
                iter = rid.iterator();
            }
        }

        private List<RecordID> iteratorHelper2(Page p, DataBox key) {
            List<RecordID> ret = new ArrayList<RecordID>();
            if (p.readByte(0) == (byte) 1) {
                LeafNode leaf = new LeafNode(self, p.getPageNum());
                Iterator<RecordID> temp = leaf.scanFrom(key);
                while (temp.hasNext()) {
                    ret.add(temp.next());
                }
                return ret;
            } else {
                InnerNode inner = new InnerNode(self, p.getPageNum());
                PageAllocator alloc = self.allocator;
                ret.addAll(iteratorHelper2(alloc.fetchPage(inner.getFirstChild()), key));
                List<BEntry> allEntries = inner.getAllValidEntries();
                int numEntries = allEntries.size();
                for (int i = 0; i < numEntries; i++) {
                    ret.addAll(iteratorHelper2(alloc.fetchPage(allEntries.get(i).getPageNum()), key));
                }
            }
            return ret;
        }

        private List<RecordID> iteratorHelper3(Page p, DataBox key) {
            List<RecordID> ret = new ArrayList<RecordID>();
            if (p.readByte(0) == (byte) 1) {
                LeafNode leaf = new LeafNode(self, p.getPageNum());
                Iterator<RecordID> temp = leaf.scanForKey(key);
                while (temp.hasNext()) {
                    ret.add(temp.next());
                }
                return ret;
            } else {
                InnerNode inner = new InnerNode(self, p.getPageNum());
                PageAllocator alloc = self.allocator;
                ret.addAll(iteratorHelper3(alloc.fetchPage(inner.getFirstChild()), key));
                List<BEntry> allEntries = inner.getAllValidEntries();
                int numEntries = allEntries.size();
                for (int i = 0; i < numEntries; i++) {
                    ret.addAll(iteratorHelper3(alloc.fetchPage(allEntries.get(i).getPageNum()), key));
                }
            }
            return ret;
        }

        /**
         * Confirm if iterator has more RecordIDs to return.
         *
         * @return true if there are still RecordIDs to be returned, false
         * otherwise
         */
        public boolean hasNext() {
            // Implement me!
            return iter.hasNext();
        }

        /**
         * Yield the next RecordID of this iterator.
         *
         * @return the next RecordID
         * @throws NoSuchElementException if there are no more RecordIDs to
         * yield
         */
        public RecordID next() {
            // Implement me!
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return iter.next();
        }

        public void remove() {
            /* You will not have to implement this in this project. */
            throw new UnsupportedOperationException();
        }
    }
}
