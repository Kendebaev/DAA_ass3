package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.io.InputStream;

public class PrimsAlgorithm {




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

    //  Class to the MST and the Operation Count
    public static class MSTResult {
        public List<PriorityEdge> mstEdges;
        public long operationCount;

        public MSTResult(List<PriorityEdge> edges, long count) {
            this.mstEdges = edges;
            this.operationCount = count;
        }
    }

    //  Prim's Algorithm

    public static MSTResult findMinimumSpanningTree(GraphData graph, String startVertex) {

        long opCount = 0;


        Map<String, List<Edge>> adj = new HashMap<>();
        Set<String> vertices = new HashSet<>();


        for (Edge e : graph.edges) {
            opCount += 4; // 2 adds to map/list, 2 adds to set (amortized O(1))
            adj.computeIfAbsent(e.source, k -> new ArrayList<>()).add(e);
            vertices.add(e.source);

            adj.computeIfAbsent(e.destination, k -> new ArrayList<>()).add(new Edge() {{
                source = e.destination;
                destination = e.source;
                weight = e.weight;
            }});
            vertices.add(e.destination);
        }

        opCount += 1; // Comparison
        if (!vertices.contains(startVertex)) {
            throw new IllegalArgumentException("Start vertex " + startVertex + " not found in graph.");
        }

        // setup
        List<PriorityEdge> mst = new ArrayList<>();
        Set<String> inMST = new HashSet<>();
        PriorityQueue<PriorityEdge> pq = new PriorityQueue<>(Comparator.comparingInt(e -> e.weight));

        // Start from the specified vertex
        inMST.add(startVertex);
        opCount += 1;


        for (Edge e : adj.getOrDefault(startVertex, Collections.emptyList())) {
            opCount += 1;
            if (!inMST.contains(e.destination)) {

                pq.offer(new PriorityEdge(e.source, e.destination, e.weight));
                opCount += 1;
            }
        }


        while (!pq.isEmpty() && inMST.size() < vertices.size()) {
            opCount += 2; // PQ.isEmpty() check and inMST.size() comparison


            PriorityEdge minEdge = pq.poll();
            opCount += 1;

            String newVertex = null;


            opCount += 4;
            if (inMST.contains(minEdge.source) && !inMST.contains(minEdge.destination)) {
                newVertex = minEdge.destination;
            } else if (inMST.contains(minEdge.destination) && !inMST.contains(minEdge.source)) {
                newVertex = minEdge.source;
            }

            opCount += 1;
            if (newVertex == null) {
                continue;
            }

            // adding to MST and Set
            mst.add(minEdge);
            inMST.add(newVertex);
            opCount += 2; // List addition and Set addition

            // Loop 4: Adding new edges to PQ
            for (Edge e : adj.getOrDefault(newVertex, Collections.emptyList())) {
                opCount += 1; // Set lookup (inMST.contains)
                if (!inMST.contains(e.destination)) {
                    // PQ insertion
                    pq.offer(new PriorityEdge(newVertex, e.destination, e.weight));
                    opCount += 1;
                }
            }
        }


        opCount += 1;
        if (inMST.size() < vertices.size()) {
            System.err.println("Warning: MST does not span the entire graph (graph may be disconnected).");
        }

        // Return the MST edges and the total operation count
        return new MSTResult(mst, opCount);
    }

    // Main

    public static void main(String[] args) {



        //HERE CHANGE THE JSON FILE NAME
        String filename = "ExtraLargeGraph_1.json";
        //HERE


        try (InputStream is = PrimsAlgorithm.class.getClassLoader().getResourceAsStream(filename)) {

            if (is == null) {
                System.err.println("Error: The file '" + filename + "' was not found in the classpath (e.g., src/main/resources/).");
                return;
            }

            ObjectMapper mapper = new ObjectMapper();
            GraphData graph = mapper.readValue(is, GraphData.class);

            // HERE
            String startingVertex = "№1";  //change here to letter IN SMALL JSON
            //HERE
            if (graph.edges.isEmpty()) {
                System.out.println("Graph is empty. MST is zero length.");
                return;
            }

            // TIME
            long startTime = System.currentTimeMillis();

            // Find the MST
            MSTResult result = findMinimumSpanningTree(graph, startingVertex);

            //  TIME ENDS
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;

            // result object
            List<PriorityEdge> mstResult = result.mstEdges;
            long operationCount = result.operationCount;

            int totalWeight = mstResult.stream().mapToInt(e -> e.weight).sum();

            // Print the results
            System.out.println("------------------------------------");
            System.out.println("Minimum Spanning Tree (MST) for: " + graph.graphName);

            if (graph.number_OF_Edge_and_Vertices != null) {
                System.out.println(graph.number_OF_Edge_and_Vertices);
            }

            System.out.println("Starting Vertex: " + startingVertex);
            System.out.println("------------------------------------");

            for (PriorityEdge edge : mstResult) {
                System.out.println("  - " + edge);
            }

            System.out.println("------------------------------------");
            System.out.println("Total MST Weight: " + totalWeight);

            // --- NEW OUTPUT LINES ---
            System.out.println("Execution Time: " + executionTime + " ms");
            System.out.println("Total Key Operations: " + operationCount);
            System.out.println("------------------------------------");


        } catch (IllegalArgumentException e) {
            System.err.println("Configuration Error: " + e.getMessage());
            e.printStackTrace();
        }
        catch (Exception e) {
            System.err.println("Error reading or parsing JSON file: " + filename);
            e.printStackTrace();
        }
    }
}