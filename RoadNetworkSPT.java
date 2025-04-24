// Luke Molony - C23339021 - Simplified Version
// Dijkstra's algorithm for shortest path on a road network.
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class RoadNetworkSPT {
    static class Heap {
        private int[] a;
        private int[] hPos;
        private long[] dist;
        private int N;

        public Heap(int maxSize, long[] _dist, int[] _hPos) {
            N = 0;
            a = new int[maxSize + 1];
            dist = _dist;
            hPos = _hPos;
        }

        public boolean isEmpty() {
            return N == 0;
        }

        public void siftUp(int k) {
            int v = a[k];
            while (k > 1 && dist[v] < dist[a[k / 2]]) {
                a[k] = a[k / 2];
                hPos[a[k]] = k;
                k = k / 2;
            }
            a[k] = v;
            hPos[v] = k;
        }

        public void siftDown(int k) {
            int v = a[k];
            int j;

            while (2 * k <= N) {
                j = 2 * k;
                if (j < N && dist[a[j + 1]] < dist[a[j]]) {
                    j++;
                }
                if (dist[v] > dist[a[j]]) {
                    a[k] = a[j];
                    hPos[a[k]] = k;
                    k = j;
                } else {
                    break;
                }
            }
            a[k] = v;
            hPos[v] = k;
        }

        public void insert(int x) {
            if (N >= a.length - 1) {
                System.err.println("Heap capacity exceeded. Consider increasing maxSize.");
                return;
            }
            a[++N] = x;
            siftUp(N);
        }

        public int remove() {
            if (isEmpty()) {
                System.err.println("Attempting to remove from an empty heap.");
                return -1; // throw exception
            }
            int v = a[1];
            hPos[v] = -1; // Mark as removed
            a[1] = a[N--];
            if (N > 0) {
                hPos[a[1]] = 1;
                siftDown(1);
            }
            return v;
        }

        public boolean contains(int v) {
            // A node is in the heap if its hPos is > 0 (since heap uses 1-based indexing)
            if (v >= 0 && v < hPos.length) {
                return hPos[v] > 0;
            }
            return false; // Node ID out of bounds for hPos array
        }
    }

    // Represents the graph using adjacency lists.
    static class Graph {

        static class Node {
            public int vert;
            public int wgt;
            public Node next;
        }

        private int V, E;
        private Node[] adj;
        private Node z; // Sentinel node

        // Reads graph from file, assuming node IDs <= header V
        public Graph(String graphFile) throws IOException, NumberFormatException, IllegalArgumentException {
            long startTime = System.currentTimeMillis();
            System.out.println("Reading graph file: " + graphFile + "...");

            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(graphFile));
                String line;
                String[] parts;

                // header line: <numVertices> <numEdges>
                line = reader.readLine();
                if (line == null) {
                    throw new IOException("Graph file is empty or missing header line.");
                }
                parts = line.trim().split("\\s+");
                if (parts.length < 2) {
                    throw new NumberFormatException("Invalid header format. Expected: <numVertices> <numEdges>");
                }
                V = Integer.parseInt(parts[0]);
                E = Integer.parseInt(parts[1]);

                System.out.println("Vertices (from header): " + V + ", Edges (from header): " + E);
                if (V <= 0) {
                    throw new IllegalArgumentException("Number of vertices must be positive.");
                }

                // Initialize adjacency lists
                z = new Node();
                z.next = z;
                adj = new Node[V + 1]; // Use V + 1 for 1-based indexing
                for (int i = 1; i <= V; ++i) {
                    adj[i] = z; // Initialize lists to sentinel
                }

                // edge lines: <source> <destination> <weight>
                int edgesRead = 0;
                while ((line = reader.readLine()) != null) {
                    parts = line.trim().split("\\s+");
                    if (parts.length >= 3) {
                        try {
                            int u = Integer.parseInt(parts[0]);
                            int v = Integer.parseInt(parts[1]);
                            int wgt = Integer.parseInt(parts[2]);

                            // Validate node IDs against declared V
                            if (u <= 0 || u > V || v <= 0 || v > V) {
                               System.err.println("Warning: Skipping edge with out-of-bounds node ID (must be 1 to " + V + "): " + line);
                               continue;
                            }
                            if (wgt < 0) {
                               System.err.println("Warning: Skipping edge with negative weight" + line);
                               continue;
                            }

                            // Add edge u -> v to adjacency list of u
                            Node t = new Node();
                            t.vert = v;
                            t.wgt = wgt;
                            t.next = adj[u];
                            adj[u] = t;
                            edgesRead++;

                        } catch (NumberFormatException e) {
                            System.err.println("Warning: Skipping malformed line during edge reading: " + line + " - " + e.getMessage());
                        }
                    } else if (!line.trim().isEmpty()){ // Ignore empty lines
                       System.err.println("Warning: Skipping incomplete edge line: " + line);
                    }
                }

                long endTime = System.currentTimeMillis();
                System.out.println("Graph construction complete. Edges processed: " + edgesRead);
                System.out.println("Time taken for graph construction: " + (endTime - startTime) + " ms");

            } finally {
                if (reader != null) {
                reader.close();
                }
            }
        }

        public int getV() {
            return V;
        }

        // Implements Dijkstra's algorithm to find the shortest path.
        public PathResult SPT_Dijkstra(int startNode, int endNode) {
            // basic validation check in main, but good to keep here too
            if (startNode <= 0 || startNode > V || endNode <= 0 || endNode > V) {
                System.err.println("Error: Start or end node index is out of bounds (1 to " + V + ").");
                return null; // fail
            }

            // Check if start node exists and has edges, unless it's the destination
            // adj[startNode] == z means no outgoing edges
            if (adj[startNode] == z && startNode != endNode) {
                System.out.println("Warning: Start node " + startNode + " has no outgoing edges. Path to " + endNode + " is impossible.");
                return new PathResult(Long.MAX_VALUE, Collections.emptyList());
            }


            long[] dist = new long[V + 1]; // Distances from startNode
            int[] parent = new int[V + 1]; // Parent pointers for path reconstruction
            int[] hPos = new int[V + 1]; // Heap positions for decrease-key

            Arrays.fill(dist, Long.MAX_VALUE); // Initialize distances to infinity
            Arrays.fill(parent, 0); // Initialize parent pointers
            Arrays.fill(hPos, 0); // Initialize heap positions (0 or less means not in heap)

            dist[startNode] = 0; // Distance from startNode to itself is 0

            Heap heap = new Heap(V, dist, hPos); // Create the priority queue
            heap.insert(startNode); // Add the start node to the heap

            System.out.println("\nStarting Dijkstra's Algorithm from " + startNode + " to " + endNode + "...");

            int nodesVisited = 0;
            while (!heap.isEmpty()) {
                int u = heap.remove(); // Extract vertex with minimum distance
                nodesVisited++;

                // If we reached the destination, we can stop
                if (u == endNode) {
                    System.out.println("Destination node " + endNode + " reached.");
                    break;
                }

                // If the extracted node has infinite distance, it's unreachable.
                // Any nodes reachable only through this node are also unreachable.
                if (dist[u] == Long.MAX_VALUE) {
                    System.out.println("Node " + u + " is unreachable (infinite distance extracted), stopping.");
                    break;
                }

                // Iterate through neighbors of u
                for (Node edge = adj[u]; edge != z; edge = edge.next) {
                    int v = edge.vert;
                    int weight = edge.wgt;

                    // Relaxation step: If a shorter path to v is found through u
                    if (dist[u] != Long.MAX_VALUE && dist[u] + weight < dist[v]) {
                        long newDist = dist[u] + weight;

                        dist[v] = newDist; // Update distance
                        parent[v] = u; // Set parent of v to u

                        // Update vertex v in the priority queue
                        if (!heap.contains(v)) {
                            heap.insert(v); // If v is not in heap, add it
                        } else {
                            heap.siftUp(hPos[v]); // If v is in heap, its distance decreased, sift up
                        }
                    }
                }
            }

            System.out.println("Dijkstra's algorithm finished. Nodes visited: " + nodesVisited);

            // Reconstruct path if endNode is reachable
            if (dist[endNode] == Long.MAX_VALUE) {
                System.out.println("No path found from " + startNode + " to " + endNode + ".");
                return new PathResult(Long.MAX_VALUE, Collections.emptyList()); // Indicate no path
            } else {
                List<Integer> path = new ArrayList<>();
                int currentNode = endNode;

                // Backtrack from endNode using parent array
                while (currentNode != 0) {
                    path.add(currentNode);
                    if (currentNode == startNode) break; // Stop when we reach the start node

                    currentNode = parent[currentNode];
                }
                Collections.reverse(path); // Reverse to get path from start to end

                return new PathResult(dist[endNode], path);
            }
        }
    }

    // Helper class to store the result of the pathfinding.
    static class PathResult {
        final long distance;
        final List<Integer> path;

        PathResult(long distance, List<Integer> path) {
            this.distance = distance;
            this.path = path;
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String graphFileName;
        int startNode, endNode;

        System.out.print("Enter the graph file name (e.g., example.txt): ");
        graphFileName = scanner.nextLine();

        // Get start node input
        System.out.print("Enter the starting node number: ");
        while (!scanner.hasNextInt()) {
            System.out.print("Invalid input. Please enter an integer for the starting node: ");
            scanner.next(); // Consume the invalid input
        }
        startNode = scanner.nextInt();
        scanner.nextLine(); // Consume the newline character

        // Get destination node input
        System.out.print("Enter the destination node number: ");
        while (!scanner.hasNextInt()) {
            System.out.print("Invalid input. Please enter an integer for the destination node: ");
            scanner.next(); // Consume the invalid input
        }
        endNode = scanner.nextInt();
        scanner.nextLine(); // Consume the newline character

        try {
            Runtime runtime = Runtime.getRuntime();
            long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
            long startTime = System.currentTimeMillis();

            Graph graph = new Graph(graphFileName);

            // Validate user-provided nodes against the graph's vertex count
            if (startNode <= 0 || startNode > graph.getV() || endNode <= 0 || endNode > graph.getV()) {
                System.err.println("Error: Start or end node index is out of the valid range [1, " + graph.getV() + "] for the loaded graph.");
                // No need to run Dijkstra if input is invalid
            } else {
                // Run Dijkstra's algorithm
                PathResult result = graph.SPT_Dijkstra(startNode, endNode);

                long endTime = System.currentTimeMillis();
                long memoryAfter = runtime.totalMemory() - runtime.freeMemory();

                System.out.println("\n--- Results ---");
                if (result != null && result.distance != Long.MAX_VALUE) {
                    System.out.println("Shortest distance from " + startNode + " to " + endNode + ": " + result.distance);
                    System.out.println("Path (" + result.path.size() + " nodes):");
                    // Print path, limit length for large graphs
                    int nodesToPrint = Math.min(result.path.size(), 50); // Print up to 50 nodes
                    for (int i = 0; i < nodesToPrint; i++) {
                        System.out.print(result.path.get(i) + (i < nodesToPrint - 1 ? " -> " : ""));
                    }
                    if (result.path.size() > nodesToPrint) {
                        System.out.print(" ...");
                    }
                    System.out.println();
                } else {
                    // this covers cases where result is null (invalid input nodes)
                    // or result.distance is Long.MAX_VALUE (no path found)
                    System.out.println("No path found between " + startNode + " and " + endNode + ", or invalid nodes provided.");
                }

                System.out.println("\n--- Performance ---");
                System.out.println("Total execution time: " + (endTime - startTime) + " ms");
                System.out.println("Estimated memory usage for graph and algorithm: " + (memoryAfter - memoryBefore) / (1024 * 1024) + " MB");
            }


        } catch (IOException e) {
            System.err.println("Error reading graph file: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Error parsing numbers in graph file or input: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
        } catch (OutOfMemoryError e) {
            System.err.println("Critical Error: Out of memory. The graph might be too large for the available Java heap space.");
        } finally {
            scanner.close();
        }
    }
}