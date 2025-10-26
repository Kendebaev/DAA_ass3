package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.io.InputStream;

public class KruskalsAlgorithm {



    /** Class representing an edge for JSON deserialization. */
    public static class Edge {
        public String source;
        public String destination;
        public int weight;
    }

    /** Class to hold the entire graph structure from the JSON file. */
    public static class GraphData {
        public String graphName;
        public String number_OF_Edge_and_Vertices;
        public List<Edge> edges;
    }


    private static class DisjointSetUnion {

        private final Map<String, String> parent = new HashMap<>();
        private long operationCount = 0;


        public void makeSet(Set<String> vertices) {
            for (String v : vertices) {
                parent.put(v, v);
                operationCount += 1;
            }
        }


        public String find(String v) {
            operationCount += 1;


            if (!parent.get(v).equals(v)) {

                parent.put(v, find(parent.get(v)));
                operationCount += 1; // Recursive call/map update
            }
            return parent.get(v);
        }


        public boolean union(String v1, String v2) {
            String root1 = find(v1);
            String root2 = find(v2);

            operationCount += 1; // Comparison
            if (!root1.equals(root2)) {

                parent.put(root1, root2);
                operationCount += 1; // Map update
                return true;
            }
            return false;
        }

        public long getOperationCount() {
            return operationCount;
        }
    }

    // structure for MST Result
    private static class PriorityEdge {
        String source;
        String destination;
        int weight;

        public PriorityEdge(String src, String dest, int weight) {
            this.source = src;
            this.destination = dest;
            this.weight = weight;
        }

        @Override
        public String toString() {
            return String.format("%s --(%d)--> %s", source, weight, destination);
        }
    }

    // Class to the MST and the Operation Count
    public static class MSTResult {
        public List<PriorityEdge> mstEdges;
        public long operationCount;

        public MSTResult(List<PriorityEdge> edges, long count) {
            this.mstEdges = edges;
            this.operationCount = count;
        }
    }

    // Kruskal's Algorithm

    public static MSTResult findMinimumSpanningTree(GraphData graph) {

        long opCount = 0;
        List<PriorityEdge> mst = new ArrayList<>();

        //  Identify all unique vertices
        Set<String> vertices = new HashSet<>();
        for (Edge e : graph.edges) {
            vertices.add(e.source);
            vertices.add(e.destination);
            opCount += 2; // Set additions
        }

        //  empty graph case
        if (vertices.isEmpty()) {
            return new MSTResult(mst, opCount);
        }

        // sort all edges by weight

        List<Edge> sortedEdges = new ArrayList<>(graph.edges);
        sortedEdges.sort(Comparator.comparingInt(e -> e.weight));
        opCount += sortedEdges.size() * Math.log(sortedEdges.size());

        //  Initialize Disjoint Set Union  structure
        DisjointSetUnion dsu = new DisjointSetUnion();
        dsu.makeSet(vertices);
        opCount += dsu.getOperationCount();

        int edgesAdded = 0;
        int numVertices = vertices.size();

        // iterate through sorted edges
        for (Edge edge : sortedEdges) {
            opCount += 1; // Edge iteration


            boolean unionOccurred = dsu.union(edge.source, edge.destination);
            opCount += dsu.getOperationCount();

            if (unionOccurred) {
                // Add edge to MST
                mst.add(new PriorityEdge(edge.source, edge.destination, edge.weight));
                edgesAdded++;
                opCount += 1; // List add

                // MST is complete when |V| - 1 edges are found
                opCount += 1; // Comparison
                if (edgesAdded == numVertices - 1) {
                    break;
                }
            }
        }

        if (edgesAdded < numVertices - 1) {
            System.err.println("Warning: MST does not span the entire graph (graph may be disconnected).");
        }

        return new MSTResult(mst, opCount);
    }

    //  Main

    public static void main(String[] args) {
        //change here file name

        String filename = "ExtraLargeGraph_1.json";  //HERE

        //HERE

        try (InputStream is = KruskalsAlgorithm.class.getClassLoader().getResourceAsStream(filename)) {

            if (is == null) {
                System.err.println("Error: The file '" + filename + "' was not found in the classpath (e.g., src/main/resources/).");
                return;
            }

            ObjectMapper mapper = new ObjectMapper();
            GraphData graph = mapper.readValue(is, GraphData.class);

            if (graph.edges.isEmpty()) {
                System.out.println("Graph is empty. MST is zero length.");
                return;
            }

            // TIME  START
            long startTime = System.currentTimeMillis();


            MSTResult result = findMinimumSpanningTree(graph);

            // TIME  ENDS
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;

            List<PriorityEdge> mstResult = result.mstEdges;
            long operationCount = result.operationCount;

            int totalWeight = mstResult.stream().mapToInt(e -> e.weight).sum();

            // Print the results
            System.out.println("------------------------------------");
            System.out.println("Minimum Spanning Tree (MST) for: " + graph.graphName);

            if (graph.number_OF_Edge_and_Vertices != null) {
                System.out.println(graph.number_OF_Edge_and_Vertices);
            }


            System.out.println("Algorithm: Kruskal's ");
            System.out.println("------------------------------------");

            for (PriorityEdge edge : mstResult) {
                System.out.println("  - " + edge);
            }

            System.out.println("------------------------------------");
            System.out.println("Total MST Weight: " + totalWeight);
            System.out.println("Execution Time: " + executionTime + " ms");
            System.out.println("Total Key Operations: " + operationCount);
            System.out.println("------------------------------------");


        } catch (Exception e) {
            System.err.println("Error reading or parsing JSON file: " + filename);
            e.printStackTrace();
        }
    }
}