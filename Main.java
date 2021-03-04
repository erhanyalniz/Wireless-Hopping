// Erhan Yalnız - 150117905
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Formatter;

public class Main {
	
	// This array will hold x, y and ranges of laptops read from file. (nx3 array) 
	static double[][] laptops;
	
	// This will be adjacency matrix of every laptop on network. (nxn graph)
	static int[][] network;
	
	// Read file "filename" and build nx3 double array. (x, y, range in order)
	static double[][] readFile(String filename) {
		double[][] result = null;
		// This will store length n.
		int size;
		
		try {
			// Open file "filename".
			File file = new File(filename);
			Scanner sc = new Scanner(file);
			// First line contains size n.
			size = sc.nextInt();
			sc.nextLine();
			// Initialize array.
			result = new double[size][3];
			
			// Iterate for each line over file.
			for(int i=0; i < size; i++) {
				// Read the line and split by "\t".
				String[] buffer = sc.nextLine().split("\t");

				// Parse splitted values.
				result[i][0] = Double.parseDouble(buffer[0]);
				result[i][1] = Double.parseDouble(buffer[1]);
				result[i][2] = Double.parseDouble(buffer[2]);
			}
			sc.close();
		} catch (FileNotFoundException e) {
			System.out.println("File \"" + filename + "\" doesn't exist.");
			e.printStackTrace();
			System.exit(-1);
		}
		
		return result;
	}
	
	// Function to write array to file "filename".
	static void writeFile(String filename, int[] inputArray) {
		try {
			Formatter f = new Formatter(filename);
			// Iterate through each element in array and write to file.
			for(int i=0; i < inputArray.length; i++) {
				f.format("%d\n", inputArray[i]);
			}
			f.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// Check if laptop "a" is reaching laptop "b". Both parameters are arrays with length 3.
	static boolean isReachable(double[] a, double[] b) {
		return Math.sqrt(Math.pow(a[0] - b[0], 2) + Math.pow(a[1] - b[1], 2)) <= a[2] + b[2];
	}
	
	// Check distance between laptop "a" between laptop "b". Both parameters are arrays with length 3.
	static double distance(double[] a, double[] b) {
		return Math.sqrt(Math.pow(a[0] - b[0], 2) + Math.pow(a[1] - b[1], 2));
	}
	
	// Build the graph out of given nx3 array. "array" here contains n laptops with x, y and range values.
	static int[][] buildNetwork(double[][] array) {
		// Initialize resulting graph to size nxn.
		int[][] graph = new int[array.length][array.length];
		// Iterate through each element of "graph" to build adjacency matrix.
		for(int i = 0; i < array.length; i++) {
			for(int j = 0;j < array.length; j++) {
				// Don't check if the node links to itself. 
				if(j == i) {
					continue;
				}
				
				// If laptop array[i] is reachable by laptop[j] then make sure to enter adjacency values to "graph". 
				if(isReachable(array[i], array[j])) {
					// Show that i'th element and j'th element is connected.
					// 1 represents i'th and j'th node are connected.
					// 0 represents i'th and j'th node are not connected.
					graph[i][j] = 1;
					graph[j][i] = 1;
					// Increment total nearby connected laptops to i'th element.
					graph[i][i]++;
				}
			}
		}
		
		// Return the resulting "graph".
		return graph;
	}
	
	// Calculate the hop distance from i to j recursively. "dist" here is used and incremented in recursive calls.
	// Estimating O(n^n)
	static int h(int i, int j, int dist) {
		// If "i" and "j" are same we already hopped to element we wanted so return calculated hop distance.
		if(i == j) {
			return dist;
		}
		
		// If we hopped more then maximum hopping count, number of elements in whole network, then we are looping over same elements.
		// So return maximum integer to show the loop does not end and clear the function call stack.
		if(dist > network.length - 1) {
			return Integer.MAX_VALUE;
		}
		
		// So to implement a breadth first search function has to call on closest laptop.
		// Here "sorted" array will help make this possible by holding values of each nearby laptop sorted by distance.
		int[] sorted = new int[network[i][i]];
		int index = 0;
		
		// Iterate through i'th row of adjacency matrix to get connected nodes.
		// O(n) on estimate. (n here is laptop count on whole network.)
		for(int ii = 0; ii < network.length; ii++) {
			// Don't add same element as i'th element to sorted.
			if(ii == i) {
				continue;
			}
			
			// If ii'th column of i'th row is 0 this means two elements are not connected.
			if(network[i][ii] == 0) {
				continue;
			}
			
			// If there is target j'th element in nearby connected laptops then hop to it, returning the function.
			if(ii == j) {
				return dist + 1;
			}
			
			// If the all predicates above are wrong then this is a proper element to search using breadth first search.
			// Add it to list to sort by distance later.
			sorted[index++] = ii;
		}
		
		// Bubblesort each element in "sorted" array containing indexes of nearby laptops.
		// O(n^2) on average. (n here is laptop count on whole network.)
		for(int ii = 0; ii < sorted.length - 1; ii++) {
			for(int jj = 0; jj < sorted.length - ii - 1; jj++) {
				// Get distances of current element and element after it.
				double dist1 = distance(laptops[i], laptops[sorted[jj]]);
				double dist2 = distance(laptops[i], laptops[sorted[jj + 1]]);
				// Swap elements in "sorted" array if element after current is closer.
				if(dist1 > dist2) {
					int temp = sorted[jj];
					sorted[jj] = sorted[jj + 1];
					sorted[jj + 1] = temp;
				}
			}
		}
		
		// Assume there is no way to reach j'th element from i'th element, setting min hop distance to maximum integer. 
		int min = Integer.MAX_VALUE;
		// Iterate through each hoppable elements in nearby laptops: "sorted" array.
		// Calculate hop distance for each of them recursively.
		// For the loop Big O estimate is O(n) but this executes recursively on each call of "h()".
		for(int ii = 0; ii < sorted.length; ii++) {
			int temp = h(sorted[ii], j, dist+1);
			if(temp < min) {
				min = temp;
			}
		}
		
		// Return calculated minimum hop distance from each combination of jumping to nearby laptops.
		return min;
	}
	
	public static void main(String[] args) {
		// Exit with error code -1, if no file is given as commandline argument.
		if(args.length < 1) {
			System.out.println("No input filename has given...");
			System.exit(-1);
		}
		
		// Get the filename for input file.
		String filename = args[0];
		
		// Read each laptop from input file. Save it to "laptops" array.
		laptops = readFile(filename);
		
		// Build adjacency matrix from laptops read from input file. Save it to "network" array.
		network = buildNetwork(laptops);
		
		// This array will hold each one of the calculated hop distance: h(1,i)
		int[] hops = new int[laptops.length];
		
		// Calculate all hop distances for h(1,i).
		for(int i = 0; i < hops.length; i++) {
			// Recursively call h(1,i) starting from hop distance 0.
			hops[i] = h(0, i, 0);
			
			// If element cannot be reachable from 1st element then hop distance is maximum integer.
			// Change it to 0 for the required output format.
			hops[i] = hops[i] == Integer.MAX_VALUE ? 0: hops[i];
		}
		
		// Write array "hops" to file.
		writeFile("output.txt", hops);
	}
}
