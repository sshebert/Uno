package Game.Shared;

import java.util.Collection;
public class CyclicLinkedList<T> {
	private boolean leftToRight;
	private CLLNode<T> root;//only used in the constructor
	private CLLNode<T> current;
	private int count;
	
	//force an item, even if it is null
	private CyclicLinkedList() {
		root = new CLLNode<T>(null);
		root.left = root;
		root.right = root;
		leftToRight = true;
		current = root;
		count = 1;
	}
	public CyclicLinkedList(T data ) {
		this();
		root.data = data;
	}
	//adds elements in the order specified by the collection, which may be arbitrary
	public CyclicLinkedList(Collection<T> c) {
		this();
		boolean first = true;
		for (T item : c) {
			if (first) {
				first = false;
				root.data = item;
			}
			else {
				add(item);
			}
		}
	}
	//inserts the element between the current element and the next element
	public void add(T data) {
		CLLNode<T> newNode = new CLLNode<T>(data);
		//point the new node at its neighbors
		newNode.setPrev(current);
		newNode.setNext(current.next());
		//point the neighbors at the new node
		current.next().setPrev(newNode);
		current.setNext(newNode);
		//update the current pointer
		current = newNode;
		//update the counter
		count++;
	}
	//removes the first instance found of the specified data
	//returns true if it deleted an element
	//returns false if it didn't find the element,
	//or there aren't enough elements to delete one and still have a current node
	public boolean remove(T data) {
		//don't let all items be deleted
		if (count == 1) return false;
		CLLNode<T> temp = current;
		if (seek(data)) {
			//the current node has the specified data, so remove references to it
			current.prev().setNext(current.next());
			current.next().setPrev(current.prev());
			//if we deleted the previously-current node, set current to the previous node 
			if (current == temp) temp = temp.prev();
			current = temp;
			return true;
		}
		else {
			//keep the current node from changing when we didn't remove anything
			current = temp;
			return false;
		}
	}
	//makes traversal occur in the opposite direction
	//does not change any underlying structure
	public void flipDirection() {
		leftToRight = !leftToRight;
	}
	//sets the current position to the first element it finds with matching data
	//returns true if it found such an element, or false otherwise
	public boolean seek(T data) {
		//searching for null
		if (data == null) {
			//we match immediately
			if (current.data == null) return true;
			//scan as many other nodes as necessary
			CLLNode<T> first = current;
			for (current = current.next(); current != first; current = current.next()) {
				if (current.data == null) return true;
			}
		}
		//searching for an actual item
		else {
			//we match immediately
			if (data.equals(current.data)) return true;
			//scan as many other nodes as necessary
			CLLNode<T> first = current;
			for (current = current.next(); current != first; current = current.next()) {
				if (data.equals(current.data)) return true;
			}
		}
		//no such element found
		return false;
	}
	//for grabbing the next element
	//updates the current position
	public T next() {
		current = current.next();
		return current.data;
	}
	//returns the data at the current position
	public T current() {
		return current.data;
	}
	//for grabbing the next element without updating the current position
	public T peek() {
		return current.next().data;
	}
	//returns the number of elements
	public int size() {
		return count;
	}
	
	//used to store data
	private class CLLNode<S extends T> {
		public S data;
		private CLLNode<S> left;
		private CLLNode<S> right;
		public CLLNode(S data) {
			this.data = data;
			left = right = null;
		}
		//for abstracting direction
		public CLLNode<S> next() {
			return leftToRight ? right : left;
		}
		public CLLNode<S> prev() {
			return leftToRight ? left : right;
		}
		public void setNext(CLLNode<S> node) {
			if (leftToRight) right = node;
			else left = node;
		}
		public void setPrev(CLLNode<S> node) {
			if (leftToRight) left = node;
			else right = node;
		}
	}
}
