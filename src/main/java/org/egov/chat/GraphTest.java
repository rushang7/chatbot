package org.egov.chat;

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.io.*;
import org.jgrapht.traverse.*;

import java.io.FileWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.server.ExportException;
import java.util.Iterator;

public class GraphTest {

    public static void main(String args[]) throws Exception {

        Graph<String, DefaultEdge> stringGraph = createStringGraph();

        // note undirected edges are printed as: {<v1>,<v2>}
        System.out.println("-- toString output");
        //@example:toString:begin
        System.out.println(stringGraph.toString());
        //@example:toString:end
        System.out.println();

        //@example:traverse:begin

        // create a graph based on URI objects
        Graph<URI, DefaultEdge> hrefGraph = createHrefGraph();

        // find the vertex corresponding to www.jgrapht.org
        //@example:findVertex:begin
        URI start = hrefGraph
                .vertexSet().stream().filter(uri -> uri.getHost().equals("www.jgrapht.org")).findAny()
                .get();
        //@example:findVertex:end

        //@example:traverse:end

        // perform a graph traversal starting from that vertex
        System.out.println("-- traverseHrefGraph output");
        traverseHrefGraph(hrefGraph, start);
        System.out.println();

        System.out.println("-- renderHrefGraph output");
        renderHrefGraph(hrefGraph);
        System.out.println();

        Writer writer = new FileWriter("GRAPH_ADJACENCY_LIST.csv");

//        CSVExporter<URI, DefaultEdge> csvExporter = new CSVExporter<>(vertexLabelProvider, CSVFormat.ADJACENCY_LIST, ',') ;

        CSVExporter<String, DefaultEdge> csvExporter = new CSVExporter<>(s -> s, CSVFormat.ADJACENCY_LIST, ',');

        csvExporter.exportGraph(stringGraph, writer);

    }


    /**
     * Creates a toy directed graph based on URI objects that represents link structure.
     *
     * @return a graph based on URI objects.
     */
    private static Graph<URI, DefaultEdge> createHrefGraph()
            throws URISyntaxException
    {
        //@example:uriCreate:begin

        Graph<URI, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);

        URI google = new URI("http://www.google.com");
        URI wikipedia = new URI("http://www.wikipedia.org");
        URI jgrapht = new URI("http://www.jgrapht.org");

        // add the vertices
        g.addVertex(google);
        g.addVertex(wikipedia);
        g.addVertex(jgrapht);

        // add edges to create linking structure
        g.addEdge(jgrapht, wikipedia);
        g.addEdge(google, jgrapht);
        g.addEdge(google, wikipedia);
        g.addEdge(wikipedia, google);

        //@example:uriCreate:end

        return g;
    }

    /**
     * Traverse a graph in depth-first order and print the vertices.
     *
     * @param hrefGraph a graph based on URI objects
     *
     * @param start the vertex where the traversal should start
     */
    private static void traverseHrefGraph(Graph<URI, DefaultEdge> hrefGraph, URI start)
    {
        //@example:traverse:begin
        Iterator<URI> iterator = new DepthFirstIterator<>(hrefGraph, start);
        while (iterator.hasNext()) {
            URI uri = iterator.next();
            System.out.println(uri);
        }
        //@example:traverse:end
    }


    static ComponentNameProvider<URI> vertexIdProvider = new ComponentNameProvider<URI>()
    {
        public String getName(URI uri)
        {
            return uri.getHost().replace('.', '_');
        }
    };
    static ComponentNameProvider<URI> vertexLabelProvider = new ComponentNameProvider<URI>()
    {
        public String getName(URI uri)
        {
            return uri.toString();
        }
    };


    /**
     * Render a graph in DOT format.
     *
     * @param hrefGraph a graph based on URI objects
     */
    private static void renderHrefGraph(Graph<URI, DefaultEdge> hrefGraph)
            throws ExportException, org.jgrapht.io.ExportException {
        //@example:render:begin

        // use helper classes to define how vertices should be rendered,
        // adhering to the DOT language restrictions

        GraphExporter<URI, DefaultEdge> exporter =
                new DOTExporter<>(vertexIdProvider, vertexLabelProvider, null);
        Writer writer = new StringWriter();
        exporter.exportGraph(hrefGraph, writer);
        System.out.println(writer.toString());
        //@example:render:end
    }

    /**
     * Create a toy graph based on String objects.
     *
     * @return a graph based on String objects.
     */
    private static Graph<String, DefaultEdge> createStringGraph()
    {
        Graph<String, DefaultEdge> g = new DirectedMultigraph<>(DefaultEdge.class);

        String v = "root";

        String v10 = "pgr.create.start";
        String v11 = "pgr.create.complaintType";
        String v12 = "pgr.create.locality";
        String v13 = "pgr.create.complaintDetails";
        String v14 = "pgr.create.photo";
        String v15 = "pgr.create.location";
        String v16 = "pgr.create.address";
        String v17 = "pgr.create.landmark";
        String v18 = "prg.create.end";

        String v20 = "pt.renew.start";
        String v21 = "pt.renew.propertyId";
        String v22 = "pt.renew.end";


        // add the vertices
        g.addVertex(v);

        g.addVertex(v10);
        g.addVertex(v11);
        g.addVertex(v12);
        g.addVertex(v13);
        g.addVertex(v14);
        g.addVertex(v15);
        g.addVertex(v16);
        g.addVertex(v17);
        g.addVertex(v18);

        g.addVertex(v20);
        g.addVertex(v21);
        g.addVertex(v22);



        // add edges to create a circuit
        g.addEdge(v, v10);

        g.addEdge(v10, v11);
        g.addEdge(v11, v12);
        g.addEdge(v12, v13);
        g.addEdge(v13, v14);
        g.addEdge(v14, v15);
        g.addEdge(v15, v16);
        g.addEdge(v16, v17);
        g.addEdge(v17, v18);

        g.addEdge(v, v20);

        g.addEdge(v20, v21);
        g.addEdge(v21, v22);

        return g;
    }

}
