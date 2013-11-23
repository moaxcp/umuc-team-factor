package job.server.factor;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the factor tree for a number.
 */
public class FactorTree implements Serializable {
    
    private Node root;
    
    private class Node implements Serializable {
        private BigInteger number;
        private boolean prime;
        private Node leftFactor;
        private Node rightFactor;
        
        public Node(BigInteger number) {
            this.number = number;
        }
    }
    
    /**
     * returns a list of nodes that match a number.
     * @param number
     * @return 
     */
    private List<Node> find(BigInteger number) {
        List<Node> nodes = new ArrayList<Node>();
        find(root, number, nodes);
        return nodes;
    }
    
    /**
     * Helper method that recursively finds the nodes that match a number.
     * @param root
     * @param number
     * @param nodes 
     */
    private void find(Node root, BigInteger number, List<Node> nodes) {
        if(root == null)
            return;
        
        if(root.number.equals(number)) {
            nodes.add(root);
        }
        
        if(root.leftFactor != null) {
            find(root.leftFactor, number, nodes);
        }
        
        if(root.rightFactor != null) {
            find(root.rightFactor, number, nodes);
        }
    }
    
    /**
     * Creates a FactorTree with a root node of number.
     * @param number 
     */
    public FactorTree(BigInteger number) {
        root = new Node(number);
    }
    
    /**
     * Returns the root number for the factor tree.
     * @return 
     */
    public BigInteger getNumber() {
        return root.number;
    }
    
    /**
     * sets the factors for a number. If there are no numbers in the tree
     * that match number no factors will be set.
     * @param number
     * @param left
     * @param right 
     */
    public void setFactors(BigInteger number, BigInteger left, BigInteger right) {
        List<Node> nodes = new ArrayList<Node>();
        find(root, number, nodes);
        for(Node node : nodes) {
            node.leftFactor = new Node(left);
            node.rightFactor = new Node(right);
        }
    }
    
    /**
     * returns true if the number is marked as prime in this tree.
     * @param number
     * @return 
     */
    public boolean isPrime(BigInteger number) {
        List<Node> nodes = new ArrayList<Node>();
        find(root, number, nodes);
        boolean prime = false;
        for(Node node : nodes) {
            if(node.prime) {
                prime = true;
                break;
            }
        }
        return prime;
    }
    
    /**
     * Sets all numbers that match to prime in the tree.
     * @param number
     * @param prime 
     */
    public void setPrime(BigInteger number, boolean prime) {
        List<Node> nodes = new ArrayList<Node>();
        find(root, number, nodes);
        for(Node node : nodes) {
            node.prime = prime;
        }
    }
    
    /**
     * returns the leaves of the factor tree.
     * @return 
     */
    public Map<BigInteger, Integer> getLeaves() {
        Map<BigInteger, Integer> factors = new HashMap<BigInteger, Integer>();
        List<Node> leaves = new ArrayList<Node>();
        getLeaves(root, leaves);
        Integer count = 0;
        for(Node i : leaves) {
            count = factors.get(i.number);
            count = count == null ? 0 : count;
            factors.put(i.number, count + 1);
        }
        return factors;
    }
    
    /**
     * adds leaf nodes to leaves. Helper method for getLeaves.
     * @param root
     * @param leaves 
     */
    private void getLeaves(Node root, List<Node> leaves) {
        if(root == null) {
            return;
        }
        
        if(root.leftFactor == null && root.rightFactor == null) {
            leaves.add(root);
        }
        
        getLeaves(root.leftFactor, leaves);
        getLeaves(root.rightFactor, leaves);
    }
    
    /**
     * performs a depth-first search for a leaf that has not been solved.
     * A leave is solved if it has factors or is marked as prime.
     * @return the first leave that has not been solved.
     */
    public BigInteger getNextUnsolvedNumber() {
        Node node = getNextUsolvedNode(root);
        if(node != null) {
            return node.number;
        }
        
        return null;
    }
    
    /**
     * helper method for getNextUnfactoredNumber(). Does the search and returns
     * the node object needed.
     * @param root
     * @return 
     */
    private Node getNextUsolvedNode(Node root) {
        if(root == null) {
            return null;
        }
        
        if(root.leftFactor == null && root.rightFactor == null) {
            if(!root.prime) {
                return root;
            } else {
                return null;
            }
        }
        
        Node left = getNextUsolvedNode(root.leftFactor);
        if(left != null) {
            return left;
        }
        
        Node right = getNextUsolvedNode(root.rightFactor);
        return right;
    }
    
    /**
     * true if the FactorTree is completely solved.
     * @return 
     */
    public boolean isComplete() {
        return getNextUnsolvedNumber() == null;
    }
}
