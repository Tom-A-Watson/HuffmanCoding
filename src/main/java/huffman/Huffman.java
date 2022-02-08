package huffman;

import huffman.tree.Branch;
import huffman.tree.Leaf;
import huffman.tree.Node;

import java.util.*;
/**
 * The class implementing the Huffman coding algorithm.
 */
public class Huffman {

    /**
     * Build the frequency table containing the unique characters from the String `input' and the number of times
     * that character occurs.
     *
     * @param   input   The string.
     * @return          The frequency table.
     */
    public static Map<Character, Integer> freqTable (String input) {
        if (input == null || input.isEmpty()) {
            return null;
        } else {
            Map<Character, Integer> ft = new HashMap<>();
            for (char c : input.toCharArray()) {                    //for each character in the character array
                if (ft.containsKey(c)) {                            //if the map already contains it, get its frequency
                    ft.put(c, ft.get(c) + 1);                       //value, add 1 to it and update it to the new value
                } else {
                    ft.put(c, 1);                                   //otherwise, create a new value of 1 for the new
                }                                                   //character
            }
            return ft;
        }
    }

    /**
     * Given a frequency table, construct a Huffman tree.
     *
     * First, create an empty priority queue.
     *
     * Then make every entry in the frequency table into a leaf node and add it to the queue.
     *
     * Then, take the first two nodes from the queue and combine them in a branch node that is
     * labelled by the combined frequency of the nodes and put it back in the queue. The right-hand
     * child of the new branch node should be the node with the larger frequency of the two.
     *
     * Do this repeatedly until there is a single node in the queue, which is the Huffman tree.
     *
     * @param freqTable The frequency table.
     * @return          A Huffman tree.
     */
    public static Node treeFromFreqTable(Map<Character, Integer> freqTable) {
        if (freqTable == null) {return null;}

        PQueue queue = new PQueue();
        freqTable.forEach(((character, integer) -> {                //adds a new leaf node to the queue for each
            queue.enqueue(new Leaf(character, integer));            //character in the frequency table
        }));

        for (int i = 0; i < freqTable.size(); i++) {                //iterates through the frequency table
            Node leftChild = queue.dequeue();                       //removes/ takes the first node in the queue
            Node rightChild = queue.dequeue();                      //removes/ takes the new first (second) node
            Node node;
            if (rightChild != null) {                               //if the right child exists
                node = new Branch(leftChild.getFreq() + rightChild.getFreq(), leftChild, rightChild);
                /* instantiates node as a branch, labelled by the frequencies of its left and right child nodes */
            } else {                                                //otherwise, if the right child does not exist, the
                node = leftChild;                                   //node becomes a branch or leaf
            }
            queue.enqueue(node);                                    //adds new branch/ leaf node back into the queue
        }
        return queue.dequeue();
    }

    /**
     * Construct the map of characters and codes from a tree. Just call the traverse
     * method of the tree passing in an empty list, then return the populated code map.
     *
     * @param tree  The Huffman tree.
     * @return      The populated map, where each key is a character, c, that maps to a list of booleans
     *              representing the path through the tree from the root to the leaf node labelled c.
     */
    public static Map<Character, List<Boolean>> buildCode(Node tree) {
        ArrayList<Boolean> codeMap = new ArrayList<>();             //list of booleans that will represent the paths
        return tree.traverse(codeMap);                              //populates codeMap by traversing the tree
    }

    /**
     * Create the huffman coding for an input string by calling the various methods written above. I.e.
     *
     * + create the frequency table,
     * + use that to create the Huffman tree,
     * + extract the code map of characters and their codes from the tree.
     *
     * Then to encode the input data, loop through the input looking each character in the map and add
     * the code for that character to a list representing the data.
     *
     * @param input The data to encode.
     * @return      The Huffman coding.
     */
    public static HuffmanCoding encode(String input) {
        Map<Character, List<Boolean>> tree = buildCode(treeFromFreqTable(freqTable(input)));
        List<Boolean> dataList = new ArrayList<>();

        for (int i = 0; i < input.length(); i++)
            dataList.addAll(tree.get(input.charAt(i)));

        return new HuffmanCoding(tree, dataList);
    }

    /**
     * Reconstruct a Huffman tree from the map of characters and their codes. Only the structure of this tree
     * is required and frequency labels of all nodes can be set to zero.
     *
     * Your tree will start as a single Branch node with null children.
     *
     * Then for each character key in the code, c, take the list of booleans, bs, corresponding to c. Make
     * a local variable referring to the root of the tree. For every boolean, b, in bs, if b is false you want to "go
     * left" in the tree, otherwise "go right".
     *
     * Presume b is false, so you want to go left. So long as you are not at the end of the code so you should set the
     * current node to be the left-hand child of the node you are currently on. If that child does not
     * yet exist (i.e. it is null) you need to add a new branch node there first. Then carry on with the next entry in
     * bs. Reverse the logic of this if b is true.
     *
     * When you have reached the end of this code (i.e. b is the final element in bs), add a leaf node
     * labelled by c as the left-hand child of the current node (right-hand if b is true). Then take the next char from
     * the code and repeat the process, starting again at the root of the tree.
     *
     * @param code  The code.
     * @return      The reconstructed tree.
     */
    public static Node treeFromCode(Map<Character, List<Boolean>> code) {
        Branch root = new Branch(0, null, null);

        for (Character key : code.keySet()) {
            Node current = root;
            Iterator<Boolean> it = code.get(key).iterator();

            while (it.hasNext()) {
                Boolean currentBool = it.next();

                if (currentBool) {
                    if (!it.hasNext())
                        ((Branch) current).setRight(new Leaf(key, 0));
                    else if (((Branch) current).getRight() == null)
                        ((Branch) current).setRight(new Branch(0, null, null));
                    current = ((Branch) current).getRight();
                } else {
                    if (!it.hasNext())
                        ((Branch) current).setLeft(new Leaf(key, 0));
                    else if (((Branch) current).getLeft() == null)
                        ((Branch) current).setLeft(new Branch(0, null, null));
                    current = ((Branch) current).getLeft();
                }
            }
        }
        return root;
    }


    /**
     * Decode some data using a map of characters and their codes. To do this you need to reconstruct the tree from the
     * code using the method you wrote to do this. Then take one boolean at a time from the data and use it to traverse
     * the tree by going left for false, right for true. Every time you reach a leaf you have decoded a single
     * character (the label of the leaf). Add it to the result and return to the root of the tree. Keep going in this
     * way until you reach the end of the data.
     *
     * @param code  The code.
     * @param data  The encoded data.
     * @return      The decoded string.
     */
    public static String decode(Map<Character, List<Boolean>> code, List<Boolean> data) {
        StringBuilder result = new StringBuilder();
        Node root = treeFromCode(code);
        Node current = root;

        for (Boolean currentBool : data) {
            if (currentBool) {
                current = ((Branch) current).getRight();
            }
            else {
                current = ((Branch) current).getLeft();
            }

            if (current instanceof Leaf) {
                result.append(((Leaf) current).getLabel());
                current = root;
            }
        }
        return result.toString();
    }
}
