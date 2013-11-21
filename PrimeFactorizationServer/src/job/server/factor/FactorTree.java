package job.server.factor;



/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author john
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
    
    private List<Node> find(BigInteger number) {
        List<Node> nodes = new ArrayList<Node>();
        find(root, number, nodes);
        return nodes;
    }
    
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
    
    public FactorTree(BigInteger number) {
        root = new Node(number);
    }
    
    public BigInteger getNumber() {
        return root.number;
    }
    
    public void setFactors(BigInteger number, BigInteger left, BigInteger right) {
        List<Node> nodes = new ArrayList<Node>();
        find(root, number, nodes);
        for(Node node : nodes) {
            node.leftFactor = new Node(left);
            node.rightFactor = new Node(right);
        }
    }
    
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
    
    public void setPrime(BigInteger number, boolean prime) {
        List<Node> nodes = new ArrayList<Node>();
        find(root, number, nodes);
        for(Node node : nodes) {
            node.prime = prime;
        }
    }
    
    public List<BigInteger> getLeaves() {
        List<BigInteger> leaves = new ArrayList<BigInteger>();
        getLeaves(root, leaves);
        return leaves;
    }
    
    private void getLeaves(Node root, List<BigInteger> leaves) {
        if(root == null) {
            return;
        }
        
        if(root.leftFactor == null && root.rightFactor == null) {
            leaves.add(root.number);
        }
        
        getLeaves(root.leftFactor, leaves);
        getLeaves(root.rightFactor, leaves);
    }
    
    public BigInteger getNextUnfactoredNumber() {
        Node node = getNextUnfactoredNode(root);
        if(node != null) {
            return node.number;
        }
        
        return null;
    }
    
    private Node getNextUnfactoredNode(Node root) {
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
        
        Node left = getNextUnfactoredNode(root.leftFactor);
        if(left != null) {
            return left;
        }
        
        Node right = getNextUnfactoredNode(root.rightFactor);
        return right;
    }
    
    public boolean isComplete() {
        return getNextUnfactoredNumber() == null;
    }
}
