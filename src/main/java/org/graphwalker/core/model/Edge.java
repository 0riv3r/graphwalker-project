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

import static org.graphwalker.core.model.Vertex.RuntimeVertex;

/**
 * @author Nils Olsson
 */
public final class Edge extends CachedBuilder<Edge.RuntimeEdge> {

    private String id;
    private String name;
    private Vertex sourceVertex;
    private Vertex targetVertex;
    private Guard guard;
    private final List<Action> actions = new ArrayList<>();
    private final Set<Requirement> requirements = new HashSet<>();
    private Double weight = 1.0;

    public Edge setId(String id) {
        this.id = id;
        invalidateCache();
        return this;
    }

    public String getId() {
        return id;
    }

    public Edge setName(String name) {
        this.name = name;
        invalidateCache();
        return this;
    }

    public String getName() {
        return name;
    }

    public Edge addRequirement(Requirement requirement) {
        this.requirements.add(requirement);
        invalidateCache();
        return this;
    }

    public Edge addRequirements(Set<Requirement> requirements) {
        this.requirements.addAll(requirements);
        invalidateCache();
        return this;
    }

    public Edge setSourceVertex(Vertex vertex) {
        this.sourceVertex = vertex;
        invalidateCache();
        return this;
    }

    public Vertex getSourceVertex() {
        return sourceVertex;
    }

    public Edge setTargetVertex(Vertex vertex) {
        this.targetVertex = vertex;
        invalidateCache();
        return this;
    }

    public Vertex getTargetVertex() {
        return targetVertex;
    }

    public Edge setGuard(Guard guard) {
        this.guard = guard;
        invalidateCache();
        return this;
    }

    public Guard getGuard() {
        return guard;
    }

    public Set<Requirement> getRequirements() {
        return requirements;
    }

    public Edge addAction(Action action) {
        this.actions.add(action);
        invalidateCache();
        return this;
    }

    public Edge addActions(List<Action> actions) {
        this.actions.addAll(actions);
        invalidateCache();
        return this;
    }

    public List<Action> getActions() {
        return actions;
    }

    public Double getWeight() {
        return weight;
    }

    public Edge setWeight(Double weight) {
        this.weight = weight;
        invalidateCache();
        return this;
    }

    public String toString() {
        return "{ id: " + getId() + ", name: " + getName() + "}";
    }

    @Override
    protected RuntimeEdge createCache() {
        return new RuntimeEdge(this);
    }

    public static final class RuntimeEdge extends BaseElement {

        private final RuntimeVertex sourceVertex;
        private final RuntimeVertex targetVertex;
        private final Guard guard;
        private final Double weight;

        private RuntimeEdge(Edge edge) {
            super(edge.getId(), edge.getName(), edge.getActions(), edge.getRequirements());
            this.sourceVertex = build(edge.getSourceVertex());
            this.targetVertex = build(edge.getTargetVertex());
            this.guard = edge.getGuard();
            this.weight = edge.getWeight();
        }

        private <T> T build(Builder<T> builder) {
            return (null!=builder?builder.build():null);
        }

        public RuntimeVertex getSourceVertex() {
            return sourceVertex;
        }

        public RuntimeVertex getTargetVertex() {
            return targetVertex;
        }

        public Guard getGuard() {
            return guard;
        }

        public Double getWeight() {
            return weight;
        }

        @Override
        public void accept(ElementVisitor visitor) {
            visitor.visit(this);
        }

    }
}
