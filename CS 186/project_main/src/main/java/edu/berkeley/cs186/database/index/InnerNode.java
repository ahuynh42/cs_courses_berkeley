package edu.berkeley.cs186.database.index;

import edu.berkeley.cs186.database.databox.*;
import edu.berkeley.cs186.database.io.Page;

import java.util.List;

/**
 * An inner node of a B+ tree. An InnerNode header contains an `isLeaf` flag
 * set to 0 and the page number of the first child node (or -1 if no child
 * exists). An InnerNode contains InnerEntries.
 *
 * Inherits all the properties of a BPlusNode.
 */
public class InnerNode extends BPlusNode {
    public static int headerSize = 5;       // isLeaf + pageNum of first child

    public InnerNode(BPlusTree tree) {
        super(tree, false);
        tree.incrementNumNodes();
        getPage().writeByte(0, (byte) 0);   // isLeaf = 0
        setFirstChild(-1);
    }

    public InnerNode(BPlusTree tree, int pageNum) {
        super(tree, pageNum, false);
        if (getPage().readByte(0) != (byte) 0) {
            throw new BPlusTreeException("Page is not Inner Node!");
        }
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    public int getFirstChild() {
        return getPage().readInt(1);
    }

    public void setFirstChild(int val) {
        getPage().writeInt(1, val);
    }

    /**
     * Finds the correct child of this InnerNode whose subtree contains the
     * given key.
     *
     * @param key the given key
     * @return page number of the child of this InnerNode whose subtree
     * contains the given key
     */
    public int findChildFromKey(DataBox key) {
        int childToTraverse = getFirstChild();
        List<BEntry> validEntries = getAllValidEntries();
        for (BEntry entry : validEntries) {
            if (key.compareTo(entry.getKey()) < 0) {
                break;
            } else {
                childToTraverse = entry.getPageNum();
            }
        }
        return childToTraverse;
    }

    /**
     * Inserts a LeafEntry into the corresponding LeafNode in this subtree.
     *
     * @param ent the LeafEntry to be inserted
     * @return the InnerEntry to be pushed/copied up to this InnerNode's parent
     * as a result of this InnerNode being split, null otherwise
     */
    public InnerEntry insertBEntry(LeafEntry ent) {
        // Implement me!
        int pageNum = findChildFromKey(ent.getKey());
        BPlusTree tree = getTree();
        Page p = tree.allocator.fetchPage(pageNum);
        InnerEntry ret = null;

        if (p.readByte(0) == (byte) 0) {
            ret = new InnerNode(tree, pageNum).insertBEntry(ent);
        } else {
            ret = new LeafNode(tree, pageNum).insertBEntry(ent);
        }

        if (ret != null) {
            if (!hasSpace()) {
                return splitNode(ret);
            }

            List<BEntry> allInners = getAllValidEntries();
            int index = 0;
            int numEntries = allInners.size();
            DataBox retKey = ret.getKey();

            while (index < numEntries) {
                if (retKey.compareTo(allInners.get(index).getKey()) < 0) {
                    break;
                }
                index++;
            }

            allInners.add(index, ret);
            overwriteBNodeEntries(allInners);
        }

        return null;
    }

    /**
     * Splits this InnerNode and returns the resulting InnerEntry to be
     * pushed/copied up to this InnerNode's parent as a result of the split.
     * The left node should contain d entries and the right node should contain
     * d entries.
     *
     * @param newEntry the BEntry that is being added to this InnerNode
     * @return the resulting InnerEntry to be pushed/copied up to this
     * InnerNode's parent as a result of this InnerNode being split
     */
    @Override
    public InnerEntry splitNode(BEntry newEntry) {
        // Implement me!
        List<BEntry> allInners = getAllValidEntries();
        int index = 0;
        DataBox newEntryKey = newEntry.getKey();

        while (index < allInners.size()) {
            if (newEntryKey.compareTo(allInners.get(index).getKey()) < 0) {
                break;
            }
            index++;
        }

        allInners.add(index, newEntry);
        List<BEntry> leftEntries = allInners.subList(0, numEntries / 2);
        List<BEntry> rightEntries = allInners.subList(numEntries / 2 + 1, numEntries + 1);

        overwriteBNodeEntries(leftEntries);
        InnerNode rightNode = new InnerNode(getTree());
        rightNode.overwriteBNodeEntries(rightEntries);
        rightNode.setFirstChild(allInners.get(numEntries / 2).getPageNum());

        DataBox ret = allInners.get(numEntries / 2).getKey();
        DataBox.Types type = ret.type();
        byte[] toWrite = ret.getBytes();
        int pageNum = rightNode.getPageNum();

        if (type.equals(DataBox.Types.INT)) {
            return new InnerEntry(new IntDataBox(toWrite), pageNum);
        } else if (type.equals(DataBox.Types.STRING)) {
            return new InnerEntry(new StringDataBox(toWrite), pageNum);
        } else if (type.equals(DataBox.Types.BOOL)) {
            return new InnerEntry(new BoolDataBox(toWrite), pageNum);
        } else {
            return new InnerEntry(new FloatDataBox(toWrite), pageNum);
        }
    }
}
