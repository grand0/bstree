package ru.kpfu.itis.ponomarev.btree;

import java.util.Arrays;
import java.util.Objects;

public class BTree {
    private final int MAX_KEYS;
    private final int MAX_CHILDREN;
    private final int MIN_KEYS;
    private final int MIN_CHILDREN;

    private Node root;

    private long operations = 0;

    public BTree(int order) {
        this.MAX_KEYS = order - 1;
        this.MAX_CHILDREN = MAX_KEYS + 1;
        this.MIN_KEYS = order / 2;
        this.MIN_CHILDREN = MIN_KEYS + 1;
        this.root = null;
    }

    public void add(int k) {
        if (root == null) {
            root = new Node();
            root.addKey(k);
        } else {
            Node node = root;
            while (node != null) {
                if (node.getChildrenSize() == 0) {
                    node.addKey(k);
                    if (node.getKeysSize() > MAX_KEYS) split(node);
                    break;
                }

                boolean found = false;
                for (int i = 0; i < node.keysSize; i++) {
                    if (k <= node.getKey(i)) {
                        node = node.getChild(i);
                        found = true;
                        break;
                    }
                }
                if (!found) node = node.getChild(node.keysSize);
            }
        }
    }

    private void split(Node node) {
        int mid = node.getKeysSize() / 2;
        int midKey = node.getKey(mid);

        Node left = new Node();
        for (int i = 0; i < mid; i++) {
            left.addKey(node.getKey(i));
        }
        if (node.getChildrenSize() > 0) {
            for (int i = 0; i <= mid; i++) {
                left.addChild(node.getChild(i));
            }
        }

        Node right = new Node();
        for (int i = mid + 1; i < node.getKeysSize(); i++) {
            right.addKey(node.getKey(i));
        }
        if (node.getChildrenSize() > 0) {
            for (int i = mid + 1; i < node.getChildrenSize(); i++) {
                right.addChild(node.getChild(i));
            }
        }

        if (node.parent == null) {
            root = new Node();
            root.addKey(midKey);
            root.addChild(left);
            root.addChild(right);
        } else {
            Node parent = node.parent;
            parent.addKey(midKey);
            parent.removeChild(node);
            parent.addChild(left);
            parent.addChild(right);

            if (parent.getKeysSize() > MAX_KEYS) split(parent);
        }
    }

    public boolean remove(int k) {
        Node node = findNode(k, root);
        return remove(k, node);
    }

    private boolean remove(int k, Node node) {
        if (node == null) return false;
        int index = node.indexOf(k);
        boolean removed = node.removeKey(k);
        if (node.getChildrenSize() == 0) {
            if (node.parent != null && node.getKeysSize() < MIN_KEYS) {
                combine(node);
            } else if (node.parent == null && node.getKeysSize() == 0) {
                root = null;
            }
        } else {
            Node greatest = getGreatestChild(node.getChild(index));
            int val = removeGreatestKey(greatest);
            node.addKey(val);
            if (greatest.parent != null && greatest.getKeysSize() < MIN_KEYS) {
                combine(greatest);
            }
            if (greatest.getChildrenSize() > MAX_CHILDREN) {
                split(greatest);
            }
        }

        return removed;
    }

    private Node getGreatestChild(Node node) {
        while (node.getChildrenSize() > 0) {
            node = node.getChild(node.getChildrenSize() - 1);
        }
        return node;
    }

    private int removeGreatestKey(Node node) {
        int k = Integer.MIN_VALUE;
        if (node.getKeysSize() > 0) {
            k = node.removeKeyByIndex(node.getKeysSize() - 1);
        }
        return k;
    }

    private Node findNode(int k, Node node) {
        if (node == null || node.getKeysSize() == 0) return null;

        for (int i = 0; i < node.getKeysSize(); i++) {
            if (k == node.getKey(i)) return node;
            else if (node.getChildrenSize() != 0 && k <= node.getKey(i)) return findNode(k, node.getChild(i));
        }

        return node.getChildrenSize() == 0 ? null : findNode(k, node.getChild(node.getKeysSize()));
    }

    private void combine(Node node) {
        Node parent = node.parent;
        int index = parent.indexOf(node);

        Node right = parent.getChild(index + 1);
        Node left = parent.getChild(index - 1);

        if (right != null && right.getKeysSize() > MIN_KEYS) {
            // take from right neighbor
            int prev = getIndexOfPrevKey(parent, right.getKey(0));
            int parentKey = parent.removeKeyByIndex(prev);
            int rightKey = right.removeKeyByIndex(0);
            node.addKey(parentKey);
            parent.addKey(rightKey);
            if (right.getChildrenSize() > 0) {
                node.addChild(right.removeChild(0));
            }
        } else if (left != null && left.getKeysSize() > MIN_KEYS) {
            // take from left neighbor
            int next = getIndexOfNextKey(parent, left.getKey(left.getKeysSize() - 1));
            int parentKey = parent.removeKeyByIndex(next);
            int leftKey = left.removeKeyByIndex(left.getKeysSize() - 1);
            node.addKey(parentKey);
            parent.addKey(leftKey);
            if (left.getChildrenSize() > 0) {
                node.addChild(left.removeChild(left.getKeysSize() - 1));
            }
        } else if (right != null && parent.getKeysSize() > 0) {
            // combine with right neighbor
            int prev = getIndexOfPrevKey(parent, right.getKey(0));
            int parentKey = parent.removeKeyByIndex(prev);
            parent.removeChild(right);
            node.addKey(parentKey);
            for (int i = 0; i < right.getKeysSize(); i++) {
                node.addKey(right.getKey(i));
            }
            for (int i = 0; i < right.getChildrenSize(); i++) {
                node.addChild(right.getChild(i));
            }
            if (parent.parent != null && parent.getKeysSize() < MIN_KEYS) {
                combine(parent);
            } else if (parent.getKeysSize() == 0) {
                node.parent = null;
                root = node;
            }
        } else if (left != null && parent.getKeysSize() > 0) {
            // combine with left neighbor
            int next = getIndexOfNextKey(parent, left.getKey(left.getKeysSize() - 1));
            int parentKey = parent.removeKeyByIndex(next);
            parent.removeChild(left);
            node.addKey(parentKey);
            for (int i = 0; i < left.getKeysSize(); i++) {
                node.addKey(left.getKey(i));
            }
            for (int i = 0; i < left.getChildrenSize(); i++) {
                node.addChild(left.getChild(i));
            }
            if (parent.parent != null && parent.getKeysSize() < MIN_KEYS) {
                combine(parent);
            } else if (parent.getKeysSize() == 0) {
                node.parent = null;
                root = node;
            }
        }
    }

    private int getIndexOfPrevKey(Node node, int k) {
        int index = node.indexOf(k);
        if (index < 0) index = -index - 1;
        if (index > 0) index--;
        return index;
    }

    private int getIndexOfNextKey(Node node, int k) {
        int index = node.indexOf(k);
        if (index < 0) index = -index - 1;
        return index == node.getKeysSize() ? -1 : index;
    }

    public boolean contains(int k) {
        return findNode(k, root) != null;
    }

    @Override
    public String toString() {
        return root.toString();
    }

    public long getAndClearOperations() {
        long op = operations;
        operations = 0;
        return op;
    }

    private class Node implements Comparable<Node> {
        private int[] keys;
        private Node[] children;
        private int keysSize;
        private int childrenSize;
        private Node parent;

        public Node() {
            this(null);
        }

        private Node(Node parent) {
            this.parent = parent;
            this.keys = new int[MAX_KEYS + 1]; // greater by 1 for convenience
            this.children = new Node[MAX_CHILDREN + 1]; // greater by 1 for convenience
            this.keysSize = 0;
            this.childrenSize = 0;

            operations += 2;
        }

        private int getKey(int index) {
            operations++;
            return keys[index];
        }

        private int indexOf(int k) {
            return binarySearch(k, keys, keysSize);
        }

        private int addKey(int k) {
            int ind = indexOf(k);
            if (ind < 0) ind = -ind - 1;
            if (keysSize - ind > 0) {
                System.arraycopy(keys, ind, keys, ind + 1, keysSize - ind);
                operations += keysSize - ind;
            }
            keysSize++;
            keys[ind] = k;
            operations++;
            return ind;
        }

        private boolean removeKey(int k) {
            if (keysSize == 0) return false;

            int ind = binarySearch(k, keys, keysSize);
            if (ind < 0) return false;
            if (keysSize - ind - 1 > 0) {
                System.arraycopy(keys, ind + 1, keys, ind, keysSize - ind - 1);
                operations += keysSize - ind - 1;
            }
            keysSize--;
            return true;
        }

        private int removeKeyByIndex(int ind) {
            int removed = keys[ind];
            operations++;
            if (keysSize - ind - 1 > 0) {
                System.arraycopy(keys, ind + 1, keys, ind, keysSize - ind - 1);
                operations += keysSize - ind - 1;
            }
            keysSize--;
            return removed;
        }

        private int getKeysSize() {
            return keysSize;
        }

        private Node getChild(int index) {
            if (index > keysSize || index < 0) {
                return null;
            }
            operations++;
            return children[index];
        }

        private int indexOf(Node child) {
            for (int i = 0; i < keysSize + 1; i++) {
                operations++;
                if (children[i].equals(child)) return i;
            }
            return -1;
        }

        private int addChild(Node child) {
            child.parent = this;
            int ind = binarySearch(child, children, childrenSize);
            if (ind < 0) ind = -ind - 1;
            if (childrenSize - ind > 0) {
                System.arraycopy(children, ind, children, ind + 1, childrenSize - ind);
                operations += childrenSize - ind;
            }
            children[ind] = child;
            operations++;
            childrenSize++;
            return ind;
        }

        private boolean removeChild(Node child) {
            boolean removed = false;
            for (int i = 0; i < childrenSize; i++) {
                operations++;
                if (children[i].equals(child)) {
                    removed = true;
                } else if (removed) {
                    children[i - 1] = children[i];
                }
            }
            childrenSize--;
            children[childrenSize] = null;
            operations++;
            return removed;
        }

        private Node removeChild(int index) {
            if (index >= childrenSize) return null;

            Node removed = children[index];
            operations++;
            if (childrenSize - index - 1 > 0) {
                System.arraycopy(children, index + 1, children, index, childrenSize - index - 1);
                operations += childrenSize - index - 1;
            }
            childrenSize--;
            return removed;
        }

        private int getChildrenSize() {
            return childrenSize;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node node = (Node) o;
            operations += keysSize + childrenSize;
            return keysSize == node.keysSize && Arrays.equals(keys, node.keys) && Arrays.equals(children, node.children) && Objects.equals(parent, node.parent);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(keysSize, parent);
            result = 31 * result + Arrays.hashCode(keys);
            result = 31 * result + Arrays.hashCode(children);
            return result;
        }

        @Override
        public int compareTo(Node o) {
            operations += 2;
            return Integer.compare(keys[0], o.keys[0]);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < keysSize; i++) {
                if (children[i] != null) sb.append(children[i]).append(" ");
                sb.append(keys[i]).append(" ");
            }
            if (children[keysSize] != null) sb.append(children[keysSize]);
            return sb.toString();
        }
    }

    private int binarySearch(int k, int[] arr, int len) {
        if (len == 0) return -1;

        int l = 0, r = len - 1;
        while (l <= r) {
            operations++;
            int mid = (l + r) / 2;
            if (arr[mid] == k) return mid;
            else if (arr[mid] > k) r = mid - 1;
            else l = mid + 1;
        }
        return -l - 1;
    }

    private int binarySearch(Node k, Node[] arr, int len) {
        if (len == 0) return -1;

        int l = 0, r = len - 1;
        while (l <= r) {
            operations++;
            int mid = (l + r) / 2;
            int comp = arr[mid].compareTo(k);
            if (comp == 0) return mid;
            else if (comp > 0) r = mid - 1;
            else l = mid + 1;
        }
        return -l - 1;
    }
}
