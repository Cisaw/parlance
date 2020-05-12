package com.jnape.palatable.parlance.combinators;

import com.jnape.palatable.lambda.adt.hlist.HList;
import com.jnape.palatable.lambda.adt.hlist.HList.HCons;
import com.jnape.palatable.lambda.adt.hlist.HList.HNil;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.specialized.Pure;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.lambda.monad.transformer.builtin.EitherT;
import com.jnape.palatable.lambda.monad.transformer.builtin.ReaderT;
import com.jnape.palatable.lambda.semigroup.Semigroup;
import com.jnape.palatable.parlance.Failures;
import com.jnape.palatable.parlance.ParserT;

import static com.jnape.palatable.lambda.adt.Either.left;
import static com.jnape.palatable.lambda.adt.hlist.HList.nil;
import static com.jnape.palatable.lambda.functions.builtin.fn1.Constantly.constantly;
import static com.jnape.palatable.lambda.functions.builtin.fn1.Downcast.downcast;
import static com.jnape.palatable.lambda.monad.transformer.builtin.EitherT.eitherT;
import static com.jnape.palatable.lambda.monad.transformer.builtin.EitherT.pureEitherT;
import static com.jnape.palatable.lambda.monad.transformer.builtin.ReaderT.pureReaderT;
import static com.jnape.palatable.lambda.monad.transformer.builtin.ReaderT.readerT;
import static com.jnape.palatable.parlance.Failures.nested;

public final class AllOf<M extends MonadRec<?, M>, A, F, B extends HList> implements ParserT<M, A, F, B> {

    private final ParserT<M, A, F, B>         delegate;
    private final Semigroup<F>                semigroupF;
    private final Fn1<? super F, ? extends F> finisherF;

    private AllOf(ParserT<M, A, F, B> delegate,
                  Semigroup<F> semigroupF,
                  Fn1<? super F, ? extends F> finisherF) {
        this.delegate   = delegate;
        this.semigroupF = semigroupF;
        this.finisherF  = finisherF;
    }

    public <H, C extends HCons<H, B>> AllOf<M, A, F, C> cons(ParserT<M, A, F, H> parserT) {
        return new AllOf<>(() -> readerT(a -> eitherT(
                delegate.runParserT().<EitherT<M, F, B>>runReaderT(a).runEitherT()
                        .zip(parserT.runParserT().<EitherT<M, F, H>>runReaderT(a).runEitherT().fmap(
                                fOrH -> fOrB -> fOrB.match(
                                        fs -> left(fOrH.match(f -> semigroupF.apply(fs, f), constantly(fs))),
                                        b -> fOrH.fmap(h -> downcast(HList.cons(h, b)))))))), semigroupF, finisherF);
    }

    @Override
    public ReaderT<A, EitherT<M, F, ?>, B> runParserT() {
        return delegate.runParserT();
    }

    public static <M extends MonadRec<?, M>, A, F> AllOf<M, A, F, HNil> allOf(
            Pure<M> pureM,
            Semigroup<F> failuresSemigroup,
            Fn1<? super F, ? extends F> finishFailures) {
        Pure<ReaderT<A, EitherT<M, F, ?>, ?>> pureParseFn = pureReaderT(pureEitherT(pureM));
        return new AllOf<>(() -> pureParseFn.<HNil, ReaderT<A, EitherT<M, F, ?>, HNil>>apply(nil()),
                           failuresSemigroup, finishFailures);
    }

    public static <M extends MonadRec<?, M>, A> AllOf<M, A, Failures, HNil> allOf(Pure<M> pureM) {
        return allOf(pureM,
                     Failures::append,
                     f -> f.projectB().fmap(fs -> nested("Expected all of the following to pass", fs)).orElse(f));
    }
}
