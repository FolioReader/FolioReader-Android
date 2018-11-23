package com.folioreader.util;

import androidx.recyclerview.widget.RecyclerView;
import com.folioreader.model.TOCLinkWrapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Multi-level expandable indentable list adapter.
 * Initially all elements in the list are single items. When you want to collapse an item and all its
 * descendants call {@link #collapseGroup(int)}. When you want to exapand a group call {@link #expandGroup(int)}.
 * Note that groups inside other groups are kept collapsed.
 * <p>
 * To collapse an item and all its descendants or expand a group at a certain position
 * you can call {@link #toggleGroup(int)}.
 * <p>
 * To preserve state (i.e. which items are collapsed) when a configuration change happens (e.g. screen rotation)
 * you should call {@link #saveGroups()} inside onSaveInstanceState and save the returned value into
 * the Bundle. When the activity/fragment is recreated you can call {@link #restoreGroups(List)}
 * to restore the previous state. The actual data (e.g. the comments in the sample app) is not preserved,
 * so you should save it yourself with a static field or implementing Parcelable or using setRetainInstance(true)
 * or saving data to a file or something like that.
 * <p>
 * To see an example of how to extend this abstract class see MyAdapter.java in sampleapp.
 */
public abstract class MultiLevelExpIndListAdapter extends RecyclerView.Adapter {
    /**
     * Indicates whether or not the observers must be notified whenever
     * {@link #mData} is modified.
     */
    private boolean mNotifyOnChange;

    /**
     * List of items to display.
     */
    private List<ExpIndData> mData;

    /**
     * Map an item to the relative group.
     * e.g.: if the user click on item 6 then mGroups(item(6)) = {all items/groups below item 6}
     */
    private HashMap<ExpIndData, List<? extends ExpIndData>> mGroups;

    /**
     * Interface that every item to be displayed has to implement. If an object implements
     * this interface it means that it can be expanded/collapsed and has a level of indentation.
     * Note: some methods are commented out because they're not used here, but they should be
     * implemented if you want your data to be expandable/collapsible and indentable.
     * See MyComment in the sample app to see an example of how to implement this.
     */
    public interface ExpIndData {
        /**
         * @return The children of this item.
         */
        List<? extends ExpIndData> getChildren();

        /**
         * @return True if this item is a group.
         */
        boolean isGroup();

        /**
         * @param value True if this item is a group
         */
        void setIsGroup(boolean value);

        /**
         * @param groupSize Set the number of items in the group.
         *                  Note: groups contained in other groups are counted just as one, not
         *                  as the number of items that they contain.
         */
        void setGroupSize(int groupSize);

        /** Note: actually this method is never called in MultiLevelExpIndListAdapter,
         * that's why it's not strictly required that you implement this function and so
         * it's commented out.
         * @return The number of items in the group.
         *         Note: groups contained in other groups are counted just as one, not
         *               as the number of items that they contain.
         */
        //int getGroupSize();

        /** Note: actually this method is never called in MultiLevelExpIndListAdapter,
         * that's why it's not strictly required that you implement this function and so
         * it's commented out.
         * @return The level of indentation in the range [0, n-1]
         */
        //int getIndentation();

        /** Note: actually this method is never called in MultiLevelExpIndListAdapter,
         * that's why it's not strictly required that you implement this function and so
         * it's commented out.
         * @param indentation The level of indentation in the range [0, n-1]
         */
        //int setIndentation(int indentation);
    }

    public MultiLevelExpIndListAdapter() {
        mData = new ArrayList<ExpIndData>();
        mGroups = new HashMap<ExpIndData, List<? extends ExpIndData>>();
        mNotifyOnChange = true;
    }

    public MultiLevelExpIndListAdapter(ArrayList<TOCLinkWrapper> tocLinkWrappers) {
        mData = new ArrayList<ExpIndData>();
        mGroups = new HashMap<ExpIndData, List<? extends ExpIndData>>();
        mNotifyOnChange = true;
        mData.addAll(tocLinkWrappers);
        collapseAllTOCLinks(tocLinkWrappers);
    }

    public void add(ExpIndData item) {
        if (item != null) {
            mData.add(item);
            if (mNotifyOnChange)
                notifyItemChanged(mData.size() - 1);
        }
    }

    public void addAll(int position, Collection<? extends ExpIndData> data) {
        if (data != null && data.size() > 0) {
            mData.addAll(position, data);
            if (mNotifyOnChange)
                notifyItemRangeInserted(position, data.size());
        }
    }

    public void addAll(Collection<? extends ExpIndData> data) {
        addAll(mData.size(), data);
    }

    public void insert(int position, ExpIndData item) {
        mData.add(position, item);
        if (mNotifyOnChange)
            notifyItemInserted(position);
    }

    /**
     * Clear all items and groups.
     */
    public void clear() {
        if (mData.size() > 0) {
            int size = mData.size();
            mData.clear();
            mGroups.clear();
            if (mNotifyOnChange)
                notifyItemRangeRemoved(0, size);
        }
    }

    /**
     * Remove an item or group.If it's a group it removes also all the
     * items and groups that it contains.
     *
     * @param item The item or group to be removed.
     * @return true if this adapter was modified by this operation, false otherwise.
     */
    public boolean remove(ExpIndData item) {
        return remove(item, false);
    }

    /**
     * Remove an item or group. If it's a group it removes also all the
     * items and groups that it contains if expandGroupBeforeRemoval is false.
     * If it's true the group is expanded and then only the item is removed.
     *
     * @param item                     The item or group to be removed.
     * @param expandGroupBeforeRemoval True to expand the group before removing the item.
     *                                 False to remove also all the items and groups contained if
     *                                 the item to be removed is a group.
     * @return true if this adapter was modified by this operation, false otherwise.
     */
    public boolean remove(ExpIndData item, boolean expandGroupBeforeRemoval) {
        int index;
        boolean removed = false;
        if (item != null && (index = mData.indexOf(item)) != -1 && (removed = mData.remove(item))) {
            if (mGroups.containsKey(item)) {
                if (expandGroupBeforeRemoval)
                    expandGroup(index);
                mGroups.remove(item);
            }
            if (mNotifyOnChange)
                notifyItemRemoved(index);
        }
        return removed;
    }

    public ExpIndData getItemAt(int position) {
        return mData.get(position);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    /**
     * Expand the group at position "posititon".
     *
     * @param position The position (range [0,n-1]) of the group that has to be expanded
     */
    public void expandGroup(int position) {
        ExpIndData firstItem = getItemAt(position);

        if (!firstItem.isGroup()) {
            return;
        }

        // get the group of the descendants of firstItem
        List<? extends ExpIndData> group = mGroups.remove(firstItem);

        firstItem.setIsGroup(false);
        firstItem.setGroupSize(0);

        notifyItemChanged(position);
        addAll(position + 1, group);
    }

    /**
     * Collapse the descendants of the item at position "position".
     *
     * @param position The position (range [0,n-1]) of the element that has to be collapsed
     */
    public void collapseGroup(int position) {
        ExpIndData firstItem = getItemAt(position);

        if (firstItem.getChildren() == null || firstItem.getChildren().isEmpty())
            return;

        // group containing all the descendants of firstItem
        List<ExpIndData> group = new ArrayList<ExpIndData>();
        // stack for depth first search
        List<ExpIndData> stack = new ArrayList<ExpIndData>();
        int groupSize = 0;

        for (int i = firstItem.getChildren().size() - 1; i >= 0; i--) {
            stack.add(firstItem.getChildren().get(i));
        }

        while (!stack.isEmpty()) {
            ExpIndData item = stack.remove(stack.size() - 1);
            group.add(item);
            groupSize++;
            // stop when the item is a leaf or a group
            if (item.getChildren() != null && !item.getChildren().isEmpty() && !item.isGroup()) {
                for (int i = item.getChildren().size() - 1; i >= 0; i--) {
                    stack.add(item.getChildren().get(i));
                }
            }

            if (mData.contains(item)) mData.remove(item);
        }

        mGroups.put(firstItem, group);
        firstItem.setIsGroup(true);
        firstItem.setGroupSize(groupSize);

        notifyItemChanged(position);
        notifyItemRangeRemoved(position + 1, groupSize);
    }

    private void collapseAllTOCLinks(ArrayList<TOCLinkWrapper> tocLinkWrappers) {
        if (tocLinkWrappers == null || tocLinkWrappers.isEmpty()) return;

        for (TOCLinkWrapper tocLinkWrapper : tocLinkWrappers) {
            groupTOCLink(tocLinkWrapper);
            collapseAllTOCLinks(tocLinkWrapper.getTocLinkWrappers());
        }
    }

    private void groupTOCLink(TOCLinkWrapper tocLinkWrapper) {
        // group containing all the descendants of firstItem
        List<ExpIndData> group = new ArrayList<ExpIndData>();
        int groupSize = 0;
        if (tocLinkWrapper.getChildren() != null && !tocLinkWrapper.getChildren().isEmpty()) {
            group.addAll(tocLinkWrapper.getChildren());
            groupSize = tocLinkWrapper.getChildren().size();
        }
        // stack for depth first search
        //List<ExpIndData> stack = new ArrayList<ExpIndData>();
        //int groupSize = 0;

        /*for (int i = tocLinkWrapper.getChildren().size() - 1; i >= 0; i--)
            stack.add(tocLinkWrapper.getChildren().get(i));

        while (!stack.isEmpty()) {
            ExpIndData item = stack.remove(stack.size() - 1);
            group.add(item);
            groupSize++;
            // stop when the item is a leaf or a group
            if (item.getChildren() != null && !item.getChildren().isEmpty() && !item.isGroup()) {
                for (int i = item.getChildren().size() - 1; i >= 0; i--)
                    stack.add(item.getChildren().get(i));
            }
        }*/

        mGroups.put(tocLinkWrapper, group);
        tocLinkWrapper.setIsGroup(true);
        tocLinkWrapper.setGroupSize(groupSize);
    }

    /**
     * Collpase/expand the item at position "position"
     *
     * @param position The position (range [0,n-1]) of the element that has to be collapsed/expanded
     */
    public void toggleGroup(int position) {
        if (getItemAt(position).isGroup()) {
            expandGroup(position);
        } else {
            collapseGroup(position);
        }
    }

    /**
     * In onSaveInstanceState, you should save the groups' indices returned by this function
     * in the Bundle so that later they can be restored using {@link #restoreGroups(List)}.
     * saveGroups() expand all the groups so you should call this function only inside onSaveInstanceState.
     *
     * @return A list of indices of items that are groups.
     */
    public ArrayList<Integer> saveGroups() {
        boolean notify = mNotifyOnChange;
        mNotifyOnChange = false;
        ArrayList<Integer> groupsIndices = new ArrayList<Integer>();
        for (int i = 0; i < mData.size(); i++) {
            if (mData.get(i).isGroup()) {
                expandGroup(i);
                groupsIndices.add(i);
            }
        }
        mNotifyOnChange = notify;
        return groupsIndices;
    }

    /**
     * Call this function to restore the groups that were collapsed before the configuration change
     * happened (e.g. screen rotation). See {@link #saveGroups()}.
     *
     * @param groupsNum The list of indices of items that are groups and should be collapsed.
     */
    public void restoreGroups(List<Integer> groupsNum) {
        if (groupsNum == null)
            return;
        boolean notify = mNotifyOnChange;
        mNotifyOnChange = false;
        for (int i = groupsNum.size() - 1; i >= 0; i--) {
            collapseGroup(groupsNum.get(i));
        }
        mNotifyOnChange = notify;
    }
}