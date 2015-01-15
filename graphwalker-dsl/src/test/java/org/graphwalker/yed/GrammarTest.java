package org.graphwalker.yed;

/*
 * #%L
 * GraphWalker text parser
 * %%
 * Copyright (C) 2005 - 2014 GraphWalker
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

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.graphwalker.dsl.yed.YEdEdgeParser;
import org.graphwalker.dsl.yed.YEdLabelLexer;
import org.graphwalker.dsl.yed.YEdVertexParser;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.Is.is;

/**
 * @author Nils Olsson
 */
public class GrammarTest {

    private List<String> vertices = Arrays.asList(
            "word1",
            "word1 BLOCKED",
            "word1\nBLOCKED",
            "word1 INIT: x=y;",
            "word1 INIT:x=y;",
            "word1 INIT: x=y;z=0;",
            "word1\n INIT: x=y;",
            "word1 REQTAG:UC02 3.4.1",
            "word1 REQTAG:UC02 3.4.1, UC02 3.4.2",
            "word1\nREQTAG:UC02 3.4.1",
            "word1\nREQTAG:UC02 3.4.1, UC02 3.4.2",
            "word1.word2",
            "word1;word2",
            "word1.word2;word3",
            "word1;word2.word3",
            "word1.word2.word3",
            "word1.word2.word3;word1.word2.word3;word1.word2.word3",
            "word1 // comment",
            "word1\n// my one line comment\nBLOCKED"
    );

    private List<String> edges = Arrays.asList(
            "word1",
            "word1[x=>y]",
            "word1\n[x=>y]",
            "word1/x=y;",
            "word1\n/x=y;",
            "word1[x=>y]/x=y;",
            "word1\n[x=>y]\n/x=y;",
            "word1.word2",
            "word1;word2",
            "word1.word2;word3",
            "word1;word2.word3",
            "word1.word2.word3",
            "word1.word2.word3;word1.word2.word3;word1.word2.word3",
            "word1 // comment",
            "word1\n// my one line comment\n[x>y]"
    );

    @Test
    public void testVertexParser() {
        for (String vertex : vertices) {
            ANTLRInputStream inputStream = new ANTLRInputStream(vertex);
            YEdLabelLexer lexer = new YEdLabelLexer(inputStream);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            YEdVertexParser parser = new YEdVertexParser(tokens);
            YEdVertexParser.ParseContext context = parser.parse();
            Assert.assertThat("Could not parse: " + vertex, parser.getNumberOfSyntaxErrors(), is(0));
        }
    }

    @Test
    public void testEdgeParser() {
        for (String edge : edges) {
            ANTLRInputStream inputStream = new ANTLRInputStream(edge);
            YEdLabelLexer lexer = new YEdLabelLexer(inputStream);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            YEdEdgeParser parser = new YEdEdgeParser(tokens);
            YEdEdgeParser.ParseContext context = parser.parse();
            Assert.assertThat("Could not parse: " + edge, parser.getNumberOfSyntaxErrors(), is(0));
        }
    }
}
