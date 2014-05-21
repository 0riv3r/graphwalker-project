package org.graphwalker.core.model;

/*
 * #%L
 * GraphWalker Core
 * %%
 * Copyright (C) 2011 - 2014 GraphWalker
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import java.util.*;

import static org.graphwalker.core.model.Edge.RuntimeEdge;
import static org.graphwalker.core.model.Vertex.RuntimeVertex;

/**
 * @author Nils Olsson
 */
public final class Model implements Builder<Model.RuntimeModel> {

    private final List<Vertex> vertices = new ArrayList<>();
    private final List<Edge> edges = new ArrayList<>();

    public Model addVertex(Vertex vertex) {
        vertices.add(vertex);
        return this;
    }

    public Model addEdge(Edge edge) {
        edges.add(edge);
        if (null != edge.getSourceVertex() && !vertices.contains(edge.getSourceVertex())) {
            vertices.add(edge.getSourceVertex());
        }
        if (null != edge.getTargetVertex() && !vertices.contains(edge.getTargetVertex())) {
            vertices.add(edge.getTargetVertex());
        }
        return this;
    }

    public List<Vertex> getVertices() {
        return vertices;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    @Override
    public RuntimeModel build() {
        return new RuntimeModel(this);
    }

    public static class RuntimeModel {

        private static final List<RuntimeEdge> EMPTY_LIST = Collections.unmodifiableList(Arrays.<RuntimeEdge>asList());

        private final List<RuntimeVertex> vertices;
        private final List<RuntimeEdge> edges;
        private final Map<RuntimeVertex, List<RuntimeEdge>> vertexEdgeCache;
        private final List<Element> elements;

        private RuntimeModel(Model model) {
            this.vertices = BuilderFactory.build(model.getVertices());
            this.edges = BuilderFactory.build(model.getEdges());
            this.vertexEdgeCache = createVertexEdgeCache();
            this.elements = createElementCache();
        }

        public List<RuntimeVertex> getVertices() {
            return vertices;
        }

        public List<RuntimeVertex> findVertices(String name) {
            // TODO: don't loop over all the vertices every time
            List<RuntimeVertex> result = new ArrayList<>();
            for (RuntimeVertex vertex: vertices) {
                if (vertex.hasName() && vertex.getName().equals(name)) {
                    result.add(vertex);
                }
            }
            return result;
        }

        public List<RuntimeEdge> getInEdges(RuntimeVertex vertex) {
            // TODO: don't loop over all the vertices every time
            List<RuntimeEdge> result = new ArrayList<>();
            for (RuntimeEdge edge: edges) {
                if (edge.getTargetVertex().equals(vertex)) {
                    result.add(edge);
                }
            }
            return result;
        }

      public List<RuntimeEdge> getEdges() {
            return edges;
        }

        public List<RuntimeEdge> getEdges(RuntimeVertex vertex) {
            List<RuntimeEdge> edges = vertexEdgeCache.get(vertex);
            return null != edges ? edges: EMPTY_LIST;
        }

        public List<RuntimeEdge> findEdges(String name) {
            // TODO: don't loop over all the vertices every time
            List<RuntimeEdge> result = new ArrayList<>();
            for (RuntimeEdge edge: edges) {
                if (edge.hasName() && edge.getName().equals(name)) {
                    result.add(edge);
                }
            }
            return result;
        }

        public List<Element> getElements() {
            return elements;
        }

        public List<Element> getElements(Element element) {
            if (element instanceof RuntimeVertex) {
                RuntimeVertex vertex = (RuntimeVertex)element;
                List<Element> edges = new ArrayList<>();
                // TODO: We have to handle edges that get "removed" due to guards
                for (RuntimeEdge edge: getEdges(vertex)) {
                    if (!edge.isBlocked()) {
                        edges.add(edge);
                    }
                }
                return edges;
            } else {
                RuntimeEdge edge = (RuntimeEdge)element;
                return Arrays.<Element>asList(edge.getTargetVertex());
            }
        }

        private List<Element> createElementCache() {
            List<Element> elements = new ArrayList<>();
            elements.addAll(vertices);
            elements.addAll(edges);
            return Collections.unmodifiableList(elements);
        }

        private Map<RuntimeVertex, List<RuntimeEdge>> createVertexEdgeCache() {
            Map<RuntimeVertex, List<RuntimeEdge>> vertexEdgeCache = new HashMap<>();
            for (RuntimeEdge edge: edges) {
                RuntimeVertex vertex = edge.getSourceVertex();
                if (null != vertex) {
                    if (!vertexEdgeCache.containsKey(vertex)) {
                        vertexEdgeCache.put(vertex, new ArrayList<RuntimeEdge>());
                    }
                    vertexEdgeCache.get(vertex).add(edge);
                }
            }
            Map<RuntimeVertex, List<RuntimeEdge>> unmodifiableVertexEdgeCache = new HashMap<>();
            for (RuntimeVertex vertex: vertexEdgeCache.keySet()) {
                unmodifiableVertexEdgeCache.put(vertex, Collections.unmodifiableList(vertexEdgeCache.get(vertex)));
            }
            return Collections.unmodifiableMap(unmodifiableVertexEdgeCache);
        }
    }
}
