// Shahar Lerner, shaharlerner, 203057294 | Rinat Sikunda, rinatsikunda, 312495369

import java.util.ArrayList;
import java.util.List;

/**
 * FibonacciHeap
 *
 * An implementation of fibonacci heap over non-negative integers.
 */
public class FibonacciHeap {
	
	HeapNode min;
	int size;
	int numberOfMarkedNodes;
	int numberOfTrees;
	static int numberOfCuts;
	static int numberOfLinks;
	
   /**
    * public boolean empty()
    *
    * precondition: none
    * 
    * The method returns true if and only if the heap
    * is empty.
    *   
    */
    public boolean empty() {
    	return (this.min == null);
    }
	
   /**
    * public HeapNode insert(int key)
    *
    * Creates a node (of type HeapNode) which contains the given key, and inserts it into the heap. 
    */
    public HeapNode insert(int key) {
    	HeapNode heapNode = new HeapNode(key);
    	
    	if (this.empty()) 
    		this.min = heapNode;
    	else { // inserts a single-node tree to the heap, which becomes the prev of the current minimum
    		heapNode.prev = this.min.prev;
    		heapNode.next = this.min;
            this.min.prev = heapNode;
            heapNode.prev.next = heapNode;
            
			if (this.min.key > heapNode.key) // updating the minimum if needed
				this.min = heapNode;
    	}
    	
    	this.numberOfTrees++;
    	this.size++;
    	
    	return heapNode;    		
    }
    
   /**
    * public void deleteMin()
    *
    * Delete the node containing the minimum key.
    *
    */
    public void deleteMin() {
    	if (this.empty())
    		return;
    	
    	if (this.size() == 1) {
    		this.min = null;
    		this.numberOfTrees = 0;
    		this.size = 0;
    		return;
    	}
    	
    	// when the minimum is deleted, all its children become trees in the heap
    	this.numberOfTrees += (this.min.rank - 1);
    	
    	if (this.min.child == null) {
    		HeapNode newMin = this.min.next;
    		newMin.prev = this.min.prev;
    		this.min.prev.next = newMin;
    		this.min.next = null;
    		this.min.prev = null;
    		this.min = newMin;
    	}
    	else { // if the minimum heap-node has children, we concatenate them to the roots chain
    		HeapNode childOfCurrMin = this.min.child;
    		HeapNode currNode = childOfCurrMin;
    		
    		do { // marked heap-node that becomes root gets unmarked 
    			if (currNode.mark) {
    				currNode.mark = false;
    				numberOfMarkedNodes--;
    			}
    			
    			currNode = currNode.next;
    		} while (currNode != childOfCurrMin);
    			
    		if (this.min.next == this.min) 
    			this.min = childOfCurrMin;
    		else {
    			childOfCurrMin.prev.next = this.min.next;
    			this.min.next.prev = childOfCurrMin.prev; 
    			childOfCurrMin.prev = this.min.prev;
    			this.min.prev.next = childOfCurrMin;
    			
    			// disconnecting the the minimum heap-node from the roots chain (and therefore from the heap)
    			HeapNode currMinNode = this.min.next;
    			this.min.next = null;
    			this.min.prev = null;
    			this.min = currMinNode;
    		}
    		
    		childOfCurrMin.parent.child = null;
    	}
    	
    	this.size--;
    	consolidate();
    }
    
    // connecting trees of the same rank, the loop continues until there is at most one binomial tree of each degree at the heap
    private void consolidate() {
    	HeapNode[] fibTrees = new HeapNode[(int)(Math.log(this.size()) / Math.log((1 + Math.sqrt(5)) / 2))];
    	List<HeapNode> roots = new ArrayList<>();
    	
    	HeapNode currMin = this.min;
    	HeapNode currRoot = currMin;
    	
    	do {
    		currRoot.parent = null;
    		roots.add(currRoot);
    		currRoot = currRoot.next;
    	} while (currRoot != currMin);
    	
    	int rank;
    	
    	// the loop that controls the procces until is done
    	while (!roots.isEmpty()) {
    		currRoot = roots.get(0);
			rank = currRoot.rank;
			
    		while (fibTrees[rank] != null) { // the loop continues until there aren't other trees of the same degree
    			HeapNode treeInArray = fibTrees[rank];
    			
    			if (treeInArray.key < currRoot.key) {
    				link(currRoot,treeInArray);
    				roots.remove(currRoot);
    				currRoot = treeInArray;
    			}
    			else 
    				link(treeInArray,currRoot);
    			
    			fibTrees[rank] = null;
    			rank++;
    		}
    		
    		fibTrees[rank] = currRoot; // updating the array of the new heap trees
    		roots.remove(currRoot);
    	}
    	
    	currMin = this.min;
    	currRoot = currMin.next;
		HeapNode newMinRoot = currMin;
		
		// finding the new minimum from the new roots list
		while (currRoot != currMin) {
			if (currRoot.key < newMinRoot.key) 
				newMinRoot = currRoot;
			
			currRoot = currRoot.next;
		}
    	
		this.min = newMinRoot;
    }

    // connecting two heap trees - y becomes the child of x
    private void link(HeapNode y, HeapNode x) {
    	y.prev.next = y.next;
		y.next.prev = y.prev;
		
		if (x.child == null) {
			y.next = y;
			y.prev = y;
		}
		else {
			if (x.child.next == x.child) {
				x.child.next = y;
				x.child.prev = y;
				y.next = x.child;
				y.prev = x.child;
			}
			else {
				y.next = x.child;
				y.prev = x.child.prev;
				x.child.prev.next = y;
				x.child.prev = y;
				
			}
		}
		
		y.parent = x;
		x.child = y;
		x.rank++;
		
		// updating the relevant static fields of the class when two trees became linked
		numberOfLinks++;
		this.numberOfTrees--;
		
		if (this.min == y)
			this.min = x;
    }
    
   /**
    * public HeapNode findMin()
    *
    * Return the node of the heap whose key is minimal. 
    *
    */
    public HeapNode findMin() {
    	return this.min;
    } 
    
   /**
    * public void meld (FibonacciHeap heap2)
    *
    * Meld the heap with heap2
    *
    */
    public void meld(FibonacciHeap heap2) {
    	if (heap2.empty())
    		return;
    	
    	if (this.empty()) 
    		this.min = heap2.min;
    	else { // updating the relevant pointers to concatenate the roots chain of heap2 to the roots chain of the current heap 
    		heap2.min.prev.next = this.min.next;
    		this.min.next.prev = heap2.min.prev;
    		this.min.next = heap2.min;
        	heap2.min.prev = this.min;
        	
        	// updating the minimum if necessary
    		if (heap2.min.key < this.min.key)
    			this.min = heap2.min;
    	}
    	
    	// adding respectively the number of trees, marked nodes, and size of heap2 to the current heap 
    	this.numberOfMarkedNodes += heap2.numberOfMarkedNodes;
    	this.numberOfTrees += heap2.numberOfTrees; 
    	this.size += heap2.size();
    	
    	heap2.min = null; 
    }

   /**
    * public int size()
    *
    * Return the number of elements in the heap
    *   
    */
    public int size() {
    	return this.size;
    }
    	
    /**
    * public int[] countersRep()
    *
    * Return a counters array, where the value of the i-th entry is the number of trees of order i in the heap. 
    * 
    */
    public int[] countersRep() {
    	if (this.empty())
    		return null;
    	
    	// the maximum degree of a node (and a root in particular) in a fibonacci heap is at most log(n) in the "golden ratio" base
    	int[] arr = new int[(int)(Math.log(this.size()) / Math.log((1 + Math.sqrt(5)) / 2))];
        HeapNode currRoot = this.min;
        
        do {
        	arr[currRoot.rank]++;
        	currRoot = currRoot.next;
        } while (currRoot != this.min); 
    	
    	return arr;
    }
	
   /**
    * public void delete(HeapNode x)
    *
    * Deletes the node x from the heap. 
    *
    */
    public void delete(HeapNode x) {
    	if (this.empty())
    		return;
    	
    	// to delete a heap-node we first make it the minimum node of the heap, and then call the deleteMin function
    	decreaseKey(x,Integer.MIN_VALUE);
    	deleteMin();
    }
    
   /**
    * public void decreaseKey(HeapNode x, int delta)
    *
    * The function decreases the key of the node x by delta. The structure of the heap should be updated
    * to reflect this chage (for example, the cascading cuts procedure should be applied if needed).
    */
    public void decreaseKey(HeapNode x, int delta) {
    	x.key -= delta;
    	HeapNode y = x.parent;
    	
    	if (y == null) { // if the heap-node x is a root, after its value is decreased, we only make it the minimum if needed 
    		if ((x != this.min) && (x.key < this.min.key)) 
    			this.min = x;
    	}
    	else if (x.key < y.key) { // if the heap-node x is not a root, we cut him from its parent and then start a cascading-cut process
    		cut(x,y);
    		cascadingCut(y); 
    	}
    }
    
    // disconnecting the tree that its root is the heap-node x from the tree that its root is the heap-node y (y is the parent of x)
    private void cut(HeapNode x, HeapNode y) {
    	if (y.child == x) {
    		if (x.next == x)
    			y.child = null;
    		else {
    			y.child = x.next;
    			x.prev.next = x.next;
        		x.next.prev = x.prev;
    		}
    	}
    	else {
    		x.prev.next = x.next;
    		x.next.prev = x.prev;
    	}
    	
    	y.rank--;
    	
    	// the heap-node x is added to the roots chain after being cut off
    	x.parent = null;
    	x.prev = this.min.prev;
    	this.min.prev.next = x;
    	x.next = this.min;
    	this.min.prev = x;
    	
    	// updating the relevant static fields of the class when a tree is cut off from it's parent
    	numberOfCuts++;
    	this.numberOfTrees++;
    	
    	if (x.mark) { // roots are unmarked by definition
    		x.mark = false;
    		numberOfMarkedNodes--;
    	}
    	
    	if (x.key < this.min.key)
    		this.min = x;
    }
    
    /* decides if to make a cut or stop the cutting proccess: 
     * the proccess ends when the heap-node y is either a root or an unmarked node (and then it gets marked).
     * otherwise, the function keeps the cutting proccess recursively. 
     */
    private void cascadingCut(HeapNode y) {
    	HeapNode z = y.parent;
    	
    	if (z != null) {
    		if (!y.mark) {
    			y.mark = true;
    			numberOfMarkedNodes++;
    		}
    		else {
    			cut(y,z);
    			cascadingCut(z);
    		}
    	}
    }
    
   /**
    * public int potential() 
    *
    * This function returns the current potential of the heap, which is:
    * Potential = #trees + 2*#marked
    * The potential equals to the number of trees in the heap plus twice the number of marked nodes in the heap. 
    */
    public int potential() {   
    	return (this.numberOfTrees + (2 * this.numberOfMarkedNodes));
    }

   /**
    * public static int totalLinks() 
    *
    * This static function returns the total number of link operations made during the run-time of the program.
    * A link operation is the operation which gets as input two trees of the same rank, and generates a tree of 
    * rank bigger by one, by hanging the tree which has larger value in its root on the tree which has smaller value 
    * in its root.
    */
    public static int totalLinks() {    
    	return numberOfLinks;
    }

   /**
    * public static int totalCuts() 
    *
    * This static function returns the total number of cut operations made during the run-time of the program.
    * A cut operation is the operation which diconnects a subtree from its parent (during decreaseKey/delete methods). 
    */
    public static int totalCuts() {    
    	return numberOfCuts;
    }
    
   /**
    * public class HeapNode
    * 
    * If you wish to implement classes other than FibonacciHeap
    * (for example HeapNode), do it in this file, not in 
    * another file 
    *  
    */
    public class HeapNode {
    	
    	int key;
    	int rank;
    	boolean mark;
    	HeapNode child;
    	HeapNode parent;
    	HeapNode next = this;
    	HeapNode prev = this; 	
    	
    	public HeapNode(int key) {
    		this.key = key;
    	}
    	
    	public int getKey() {
    	    return this.key;
        }
    	
    }
    
}
