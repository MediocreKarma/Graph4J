/*
 * Copyright (C) 2023 Cristian Frăsinaru and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.graph4j.alg.cut;

import org.graph4j.Graph;
import org.graph4j.measures.GraphMeasures;
import org.graph4j.util.VertexSet;

/**
 * {@inheritDoc}
 *
 * <p>
 * There is no guarantee that the vertex separator created by the greedy
 * algorithm is of minimum size.
 *
 * @author Cristian Frăsinaru
 *
 */
public class GreedyVertexSeparator extends VertexSeparatorBase
        implements VertexSeparatorAlgorithm {

    private VertexSeparator solution;

    public GreedyVertexSeparator(Graph graph) {
        super(graph);
    }

    public GreedyVertexSeparator(Graph graph, int maxShoreSize) {
        super(graph, maxShoreSize);
    }

    /**
     * Computes and returns a vertex separator set. There is no guarantee that
     * the vertex separator returned is of minimum size. The right shore may be
     * empty (in case of complete graphs, for example).
     *
     * @return a vertex separator set.
     */
    @Override
    public VertexSeparator getSeparator() {
        if (solution != null) {
            return solution;
        }
        int v = GraphMeasures.minDegreeVertex(graph);
        leftShore = new VertexSet(graph);
        leftShore.add(v);
        separator = new VertexSet(graph, graph.neighbors(v));

        rightShore = new VertexSet(graph, graph.vertices());
        rightShore.remove(v);
        rightShore.removeAll(separator.vertices());

        //grow the left shore
        while (leftShore.size() < maxShoreSize && rightShore.size() > maxShoreSize) {
            v = choose(separator, rightShore);
            leftShore.add(v);
            separator = neighborhood(leftShore);
            rightShore = new VertexSet(graph, graph.vertices());
            rightShore.removeAll(leftShore.vertices());
            rightShore.removeAll(separator.vertices());
        }

        //reduce the right shore, to not exceed the maximum size
        while (rightShore.size() > maxShoreSize) {
            v = rightShore.pop();
            separator.add(v);
        }
        solution = new VertexSeparator(separator, leftShore, rightShore);
        assert solution.isValid();
        return solution;
    }

    //heuristic for choosing the vertex that will be moved to leftShore
    private int choose(VertexSet sep, VertexSet right) {
        int minVertex = -1;
        int min = Integer.MAX_VALUE;
        for (int u : sep.vertices()) {
            var nb = right.intersection(graph.neighbors(u));
            if (nb.size() < min) {
                min = nb.size();
                minVertex = u;
            }
        }
        for (int u : right.vertices()) {
            var nb = right.intersection(graph.neighbors(u));
            if (nb.size() < min) {
                min = nb.size();
                minVertex = u;
            }
        }
        return minVertex;
    }

    private VertexSet neighborhood(VertexSet set) {
        var nb = new VertexSet(graph);
        for (int v : set.vertices()) {
            nb.addAll(graph.neighbors(v));
        }
        nb.removeAll(set.vertices());
        return nb;
    }

}
