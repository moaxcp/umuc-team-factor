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
    
    private Node find(Node root, BigInteger number) {
        if(root == null)
            return null;
        
        if(root.number.equals(number)) {
            return root;
        }
        
        Node left = find(root.leftFactor, number);
        if(left != null) {
            return left;
        }
        
        Node right = find(root.rightFactor, number);
        return right;
    }
    
    public FactorTree(BigInteger number) {
        root = new Node(number);
    }
    
    public BigInteger getNumber() {
        return root.number;
    }
    
    public void setFactors(BigInteger number, BigInteger left, BigInteger right) {
        Node node = find(root, number);
        
        if(node != null) {
            node.leftFactor = new Node(left);
            node.rightFactor = new Node(right);
        }
    }
    
    public boolean isPrime(BigInteger number) {
        Node node = find(root, number);
        return node == null ? false : node.prime;
    }
    
    public boolean setPrime(BigInteger number, boolean prime) {
        Node node = find(root, number);
        if(node != null) {
            node.prime = prime;
            return true;
        }
        return false;
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
