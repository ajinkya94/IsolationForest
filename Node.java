public class Node
{ 
    double p;
    int q;
    Node left, right;
    int size;
    
    
    Node(double p, int q, Node left, Node right) 
    { 
        //key = item; 
    	this.p=p;
    	this.q=q;
        this.left = left;
        this.right = right;
    }
    
    void set_size(int size){
    	this.size = size;
    }
    int get_size() {
    	return size;
    }
}