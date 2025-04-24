# Road Network Shortest Path Finder

This project implements Dijkstra's algorithm to find the shortest path between two points on a large-scale road network. It was developed as part of a larger algorithms assignment focusing on graphs.

The program is designed to handle real-world graph data, specifically the complete California road network dataset from the [9th DIMACS Implementation Challenge](http://www.diag.uniroma1.it/challenge9/).

## Features

* **Graph Loading:** Reads graph data from a specified file.
* **Dijkstra's Algorithm:** Computes the shortest path and distance from a source node to a destination node using a priority queue (implemented as a binary heap).
* **Path Output:** Displays the shortest distance and the sequence of nodes forming the path (output is limited to the first 50 nodes for large paths).
* **Performance Metrics:** Reports the time taken for graph construction and Dijkstra's algorithm execution, as well as estimated memory usage.
* **Error Handling:** Includes basic validation for input nodes and graph file format issues.

## Dataset

The program utilizes the California road network dataset from the 9th DIMACS Implementation Challenge. This dataset represents roads as vertices and intersections as edges, with edge weights representing the distance. The full dataset is substantial, containing:

* **Vertices:** 1,890,815
* **Edges:** 4,657,742

A custom Python script (`datacleaning.py`) was used to process the raw data into a format suitable for this Java program.

## Input File Format

The graph data file should be a plain text file with the following format:

* The first line is the header: `<number_of_vertices> <number_of_edges>`
* Subsequent lines represent edges: `<source_node> <destination_node> <weight>`

Nodes are expected to be 1-indexed integers. Edge weights should be non-negative integers.

## Notes

* The output path is limited to the first 50 nodes for readability on the console. The code can be modified to output the full path to a file if needed.
* I am definately going to revisit this project in the future as it was rushed, and I believe I could improve it substantially given more time.
