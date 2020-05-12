package com.jnape.palatable.parlance;

import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.Fn2;
import com.jnape.palatable.lambda.functions.recursion.RecursiveResult;
import com.jnape.palatable.lambda.functor.Applicative;
import com.jnape.palatable.lambda.functor.builtin.Lazy;
import com.jnape.palatable.lambda.monad.Monad;
import com.jnape.palatable.lambda.monad.MonadRec;

import static com.jnape.palatable.parlance.Failures.nested;

public interface StandardParserT<M extends MonadRec<?, M>, A, B> extends ParserT<M, A, Failures, B> {

    default StandardParserT<M, A, B> or(ParserT<M, A, Failures, B> other) {
        return or(other, (x, y) -> nested("Expected one of the following to pass", x.append(y)));
    }

    @Override
    default <C> StandardParserT<M, A, C> biMapR(Fn1<? super B, ? extends C> fn) {
        throw new UnsupportedOperationException();
    }

    @Override
    default <C> StandardParserT<M, Tuple2<C, A>, Tuple2<C, B>> cartesian() {
        throw new UnsupportedOperationException();
    }

    @Override
    default StandardParserT<M, A, Tuple2<A, B>> carry() {
        throw new UnsupportedOperationException();
    }

    @Override
    default <Z, C> StandardParserT<M, Z, C> diMap(Fn1<? super Z, ? extends A> lFn, Fn1<? super B, ? extends C> rFn) {
        throw new UnsupportedOperationException();
    }

    @Override
    default <Z> StandardParserT<M, Z, B> diMapL(Fn1<? super Z, ? extends A> fn) {
        throw new UnsupportedOperationException();
    }

    @Override
    default <C> StandardParserT<M, A, C> diMapR(Fn1<? super B, ? extends C> fn) {
        throw new UnsupportedOperationException();
    }

    @Override
    default <Z> StandardParserT<M, Z, B> contraMap(Fn1<? super Z, ? extends A> fn) {
        throw new UnsupportedOperationException();
    }

    @Override
    default <C> StandardParserT<M, A, C> and(ParserT<M, B, Failures, C> other) {
        throw new UnsupportedOperationException();
    }

    @Override
    default StandardParserT<M, A, B> or(ParserT<M, A, Failures, B> other,
                                        Fn2<? super Failures, ? super Failures, ? extends Failures> combineF) {
        throw new UnsupportedOperationException();
    }

    @Override
    default <C> StandardParserT<M, A, C> trampolineM(
            Fn1<? super B, ? extends MonadRec<RecursiveResult<B, C>, ParserT<M, A, Failures, ?>>> fn) {
        throw new UnsupportedOperationException();
    }

    @Override
    default <C> StandardParserT<M, A, C> flatMap(Fn1<? super B, ? extends Monad<C, ParserT<M, A, Failures, ?>>> f) {
        throw new UnsupportedOperationException();
    }

    @Override
    default <C> StandardParserT<M, A, C> zip(
            Applicative<Fn1<? super B, ? extends C>, ParserT<M, A, Failures, ?>> appFn) {
        throw new UnsupportedOperationException();
    }

    @Override
    default <C> StandardParserT<M, A, C> pure(C c) {
        throw new UnsupportedOperationException();
    }

    @Override
    default <C> StandardParserT<M, A, C> fmap(Fn1<? super B, ? extends C> fn) {
        throw new UnsupportedOperationException();
    }

    @Override
    default <C> Lazy<? extends StandardParserT<M, A, C>> lazyZip(
            Lazy<? extends Applicative<Fn1<? super B, ? extends C>, ParserT<M, A, Failures, ?>>> lazyAppFn) {
        throw new UnsupportedOperationException();
    }

    @Override
    default <C> StandardParserT<M, A, C> discardL(Applicative<C, ParserT<M, A, Failures, ?>> appB) {
        throw new UnsupportedOperationException();
    }

    @Override
    default <C> StandardParserT<M, A, B> discardR(Applicative<C, ParserT<M, A, Failures, ?>> appB) {
        throw new UnsupportedOperationException();
    }

    static <M extends MonadRec<?, M>, A, B> StandardParserT<M, A, B> standardParserT(
            ParserT<M, A, Failures, B> parserT) {
        return parserT::runParserT;
    }
}
