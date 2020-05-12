package com.jnape.palatable.parlance;

import com.jnape.palatable.lambda.adt.coproduct.CoProduct3;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.shoki.api.Collection;
import com.jnape.palatable.shoki.api.Natural;
import com.jnape.palatable.shoki.impl.StrictQueue;

import java.util.Objects;

import static com.jnape.palatable.lambda.adt.hlist.HList.tuple;
import static com.jnape.palatable.lambda.functions.Fn0.fn0;
import static com.jnape.palatable.lambda.functions.builtin.fn2.Into.into;
import static com.jnape.palatable.lambda.functions.recursion.RecursiveResult.recurse;
import static com.jnape.palatable.lambda.functions.recursion.RecursiveResult.terminate;
import static com.jnape.palatable.lambda.functions.recursion.Trampoline.trampoline;
import static com.jnape.palatable.shoki.impl.StrictQueue.strictQueue;

public abstract class Failures implements
        CoProduct3<Failures.Single, Failures.Multiple, Failures.Nested, Failures> {

    private Failures() {
    }

    public final Failures append(Failures failures) {
        return projectB()
                .match(fn0(() -> failures.projectB()
                               .match(fn0(() -> multiple(this, failures)),
                                      ys -> multiple(this, ys))),
                       xs -> failures.projectB()
                               .match(fn0(() -> xs.add(failures)),
                                      ys -> xs.addAll(ys.failures())));
    }

    public static Failures single(String failure) {
        return new Single(failure);
    }

    public static Failures multiple(Failures first, Failures second, Failures... more) {
        return new Multiple(trampoline(
                into((out, in) -> in.head().match(
                        fn0(() -> terminate(out)),
                        next -> next.projectB().match(
                                fn0(() -> recurse(tuple(out.snoc(next), in.tail()))),
                                multiple -> recurse(tuple(out, multiple.failures().snocAll(in.tail())))))),
                tuple(StrictQueue.<Failures>strictQueue(), strictQueue(more).cons(second).cons(first))));
    }

    public static Failures nested(String preamble, Failures nested) {
        return new Nested(preamble, nested);
    }

    public static final class Single extends Failures {
        private final String failure;

        private Single(String failure) {
            this.failure = failure;
        }

        public String failure() {
            return failure;
        }

        @Override
        public <R> R match(Fn1<? super Single, ? extends R> aFn,
                           Fn1<? super Multiple, ? extends R> bFn,
                           Fn1<? super Nested, ? extends R> cFn) {
            return aFn.apply(this);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Single single = (Single) o;
            return failure.equals(single.failure);
        }

        @Override
        public int hashCode() {
            return Objects.hash(failure);
        }

        @Override
        public String toString() {
            return "Single{" +
                    "failure='" + failure + '\'' +
                    '}';
        }
    }

    public static final class Multiple extends Failures {
        private final StrictQueue<Failures> failures;

        private Multiple(StrictQueue<Failures> failures) {
            this.failures = failures;
        }

        public StrictQueue<Failures> failures() {
            return failures;
        }

        public Multiple add(Failures failures) {
            return new Multiple(this.failures.snoc(failures));
        }

        public Multiple addAll(Collection<Natural, Failures> failures) {
            return new Multiple(this.failures.snocAll(failures));
        }

        @Override
        public <R> R match(Fn1<? super Single, ? extends R> aFn,
                           Fn1<? super Multiple, ? extends R> bFn,
                           Fn1<? super Nested, ? extends R> cFn) {
            return bFn.apply(this);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Multiple multiple = (Multiple) o;
            return failures.equals(multiple.failures);
        }

        @Override
        public int hashCode() {
            return Objects.hash(failures);
        }

        @Override
        public String toString() {
            return "Multiple{" +
                    "failures=" + failures +
                    '}';
        }
    }

    public static final class Nested extends Failures {
        private final String   preamble;
        private final Failures nested;

        private Nested(String preamble, Failures nested) {
            this.preamble = preamble;
            this.nested   = nested;
        }

        public String preamble() {
            return preamble;
        }

        public Failures nested() {
            return nested;
        }

        @Override
        public <R> R match(Fn1<? super Single, ? extends R> aFn,
                           Fn1<? super Multiple, ? extends R> bFn,
                           Fn1<? super Nested, ? extends R> cFn) {
            return cFn.apply(this);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Nested nested1 = (Nested) o;
            return preamble.equals(nested1.preamble) &&
                    nested.equals(nested1.nested);
        }

        @Override
        public int hashCode() {
            return Objects.hash(preamble, nested);
        }

        @Override
        public String toString() {
            return "Nested{" +
                    "preamble='" + preamble + '\'' +
                    ", nested=" + nested +
                    '}';
        }
    }
}
