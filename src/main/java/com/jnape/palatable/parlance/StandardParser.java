package com.jnape.palatable.parlance;

import com.jnape.palatable.lambda.adt.Either;
import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.Fn2;
import com.jnape.palatable.lambda.functions.recursion.RecursiveResult;
import com.jnape.palatable.lambda.functor.Applicative;
import com.jnape.palatable.lambda.functor.builtin.Identity;
import com.jnape.palatable.lambda.functor.builtin.Lazy;
import com.jnape.palatable.lambda.monad.Monad;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.lambda.monad.transformer.builtin.EitherT;

public interface StandardParser<A, B> extends Parser<A, Failures, B> {

    @Override
    Either<Failures, B> parse(A a);

    default StandardParser<A, B> or(ParserT<Identity<?>, A, Failures, B> other) {
        return or(other, (x, y) -> Failures.nested("Expected one of the following to pass", x.append(y)));
    }

    @Override
    default <C> StandardParser<A, C> biMapR(Fn1<? super B, ? extends C> fn) {
        return standardParser(Parser.super.biMapR(fn));
    }

    @Override
    default <C> StandardParser<Tuple2<C, A>, Tuple2<C, B>> cartesian() {
        return standardParser(Parser.super.cartesian());
    }

    @Override
    default StandardParser<A, Tuple2<A, B>> carry() {
        return standardParser(Parser.super.carry());
    }

    @Override
    default <Z, C> StandardParser<Z, C> diMap(Fn1<? super Z, ? extends A> lFn, Fn1<? super B, ? extends C> rFn) {
        return standardParser(Parser.super.diMap(lFn, rFn));
    }

    @Override
    default <Z> StandardParser<Z, B> diMapL(Fn1<? super Z, ? extends A> fn) {
        return standardParser(Parser.super.diMapL(fn));
    }

    @Override
    default <C> StandardParser<A, C> diMapR(Fn1<? super B, ? extends C> fn) {
        return standardParser(Parser.super.diMapR(fn));
    }

    @Override
    default <Z> StandardParser<Z, B> contraMap(Fn1<? super Z, ? extends A> fn) {
        return standardParser(Parser.super.contraMap(fn));
    }

    @Override
    default <C> StandardParser<A, C> and(ParserT<Identity<?>, B, Failures, C> other) {
        return standardParser(Parser.super.and(other));
    }

    @Override
    default StandardParser<A, B> or(ParserT<Identity<?>, A, Failures, B> other,
                                    Fn2<? super Failures, ? super Failures, ? extends Failures> combineF) {
        return standardParser(Parser.super.or(other, combineF));
    }

    @Override
    default <C> StandardParser<A, C> trampolineM(
            Fn1<? super B, ? extends MonadRec<RecursiveResult<B, C>, ParserT<Identity<?>, A, Failures, ?>>> fn) {
        return standardParser(Parser.super.trampolineM(fn));
    }

    @Override
    default <C> StandardParser<A, C> flatMap(
            Fn1<? super B, ? extends Monad<C, ParserT<Identity<?>, A, Failures, ?>>> f) {
        return standardParser(Parser.super.flatMap(f));
    }

    @Override
    default <C> StandardParser<A, C> zip(
            Applicative<Fn1<? super B, ? extends C>, ParserT<Identity<?>, A, Failures, ?>> appFn) {
        return standardParser(Parser.super.zip(appFn));
    }

    @Override
    default <C> StandardParser<A, C> pure(C c) {
        return standardParser(Parser.super.pure(c));
    }

    @Override
    default <C> StandardParser<A, C> fmap(Fn1<? super B, ? extends C> fn) {
        return standardParser(Parser.super.fmap(fn));
    }

    @Override
    default <C> Lazy<? extends StandardParser<A, C>> lazyZip(
            Lazy<? extends Applicative<Fn1<? super B, ? extends C>, ParserT<Identity<?>, A, Failures, ?>>> lazyAppFn) {
        return Parser.super.lazyZip(lazyAppFn).fmap(StandardParser::standardParser);

    }

    @Override
    default <C> StandardParser<A, C> discardL(Applicative<C, ParserT<Identity<?>, A, Failures, ?>> appB) {
        return standardParser(Parser.super.discardL(appB));
    }

    @Override
    default <C> StandardParser<A, B> discardR(Applicative<C, ParserT<Identity<?>, A, Failures, ?>> appB) {
        return standardParser(Parser.super.discardR(appB));
    }

    static <A, B> StandardParser<A, B> standardParser(ParserT<Identity<?>, A, Failures, B> parserT) {
        return a -> parserT.runParserT()
                .<EitherT<Identity<?>, Failures, B>>runReaderT(a)
                .<Identity<Either<Failures, B>>>runEitherT()
                .runIdentity();
    }
}
