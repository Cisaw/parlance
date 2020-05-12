package com.jnape.palatable.parlance;

import org.junit.Test;

import static com.jnape.palatable.parlance.Failures.multiple;
import static com.jnape.palatable.parlance.Failures.nested;
import static com.jnape.palatable.parlance.Failures.single;
import static org.junit.Assert.assertEquals;

public class FailuresTest {

    @Test
    public void append() {
        assertEquals(multiple(single("foo"), single("bar")),
                     single("foo").append(single("bar")));

        assertEquals(multiple(single("foo"), multiple(single("bar"), single("baz"))),
                     single("foo").append(multiple(single("bar"), single("baz"))));

        assertEquals(multiple(single("foo"), multiple(single("bar"), single("baz"))),
                     multiple(single("foo"), single("bar")).append(single("baz")));

        assertEquals(multiple(multiple(single("foo"), single("bar")), multiple(single("baz"), single("quux"))),
                     multiple(single("foo"), single("bar")).append(multiple(single("baz"), single("quux"))));

        assertEquals(multiple(single("foo"), nested("bar", single("baz"))),
                     single("foo").append(nested("bar", single("baz"))));

        assertEquals(multiple(single("foo"), nested("bar", single("baz"))),
                     single("foo").append(nested("bar", single("baz"))));
    }

    @Test
    public void multipleDeforesting() {
        assertEquals(multiple(single("foo"), single("bar"), single("baz")),
                     multiple(multiple(single("foo"), single("bar")),
                              single("baz")));

        assertEquals(multiple(single("foo"), single("bar"), single("baz")),
                     multiple(multiple(single("foo"), single("bar")),
                              single("baz")));

        assertEquals(multiple(single("foo"), single("bar"), single("baz")),
                     multiple(single("foo"), multiple(single("bar"), single("baz"))));

        assertEquals(multiple(single("foo"), single("bar"), single("baz"), single("quux")),
                     multiple(multiple(single("foo"), single("bar")), multiple(single("baz"), single("quux"))));
    }
}