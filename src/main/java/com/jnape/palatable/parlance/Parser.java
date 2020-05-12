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
import com.jnape.palatable.lambda.monad.transformer.builtin.ReaderT;

import static com.jnape.palatable.lambda.monad.transformer.builtin.EitherT.eitherT;
import static com.jnape.palatable.lambda.monad.transformer.builtin.ReaderT.readerT;

public interface Parser<A, F, B> extends ParserT<Identity<?>, A, F, B> {

    Either<F, B> parse(A a);

    @Override
    default ReaderT<A, EitherT<Identity<?>, F, ?>, B> runParserT() {
        return readerT(a -> eitherT(new Identity<>(parse(a))));
    }

    @Override
    default <F2, C> Parser<A, F2, C> biMap(Fn1<? super F, ? extends F2> lFn,
                                           Fn1<? super B, ? extends C> rFn) {
        return parser(ParserT.super.biMap(lFn, rFn));
    }

    @Override
    default <F2> Parser<A, F2, B> biMapL(Fn1<? super F, ? extends F2> fn) {
        return parser(ParserT.super.biMapL(fn));
    }

    @Override
    default <C> Parser<A, F, C> biMapR(Fn1<? super B, ? extends C> fn) {
        return parser(ParserT.super.biMapR(fn));
    }

    @Override
    default <C> Parser<Tuple2<C, A>, F, Tuple2<C, B>> cartesian() {
        return parser(ParserT.super.cartesian());
    }

    @Override
    default Parser<A, F, Tuple2<A, B>> carry() {
        return parser(ParserT.super.carry());
    }

    @Override
    default <Z, C> Parser<Z, F, C> diMap(Fn1<? super Z, ? extends A> lFn,
                                         Fn1<? super B, ? extends C> rFn) {
        return parser(ParserT.super.diMap(lFn, rFn));
    }

    @Override
    default <Z> Parser<Z, F, B> diMapL(Fn1<? super Z, ? extends A> fn) {
        return parser(ParserT.super.diMapL(fn));
    }

    @Override
    default <C> Parser<A, F, C> diMapR(Fn1<? super B, ? extends C> fn) {
        return parser(ParserT.super.diMapR(fn));
    }

    @Override
    default <Z> Parser<Z, F, B> contraMap(Fn1<? super Z, ? extends A> fn) {
        return parser(ParserT.super.contraMap(fn));
    }

    @Override
    default <C> Parser<A, F, C> and(ParserT<Identity<?>, B, F, C> other) {
        return parser(ParserT.super.and(other));
    }

    @Override
    default Parser<A, F, B> or(ParserT<Identity<?>, A, F, B> other,
                               Fn2<? super F, ? super F, ? extends F> combineF) {
        return parser(ParserT.super.or(other, combineF));
    }

    @Override
    default <C> Parser<A, F, C> trampolineM(
            Fn1<? super B, ? extends MonadRec<RecursiveResult<B, C>, ParserT<Identity<?>, A, F, ?>>> fn) {
        return parser(ParserT.super.trampolineM(fn));
    }

    @Override
    default <C> Parser<A, F, C> flatMap(
            Fn1<? super B, ? extends Monad<C, ParserT<Identity<?>, A, F, ?>>> f) {
        return parser(ParserT.super.flatMap(f));
    }

    @Override
    default <C> Parser<A, F, C> zip(
            Applicative<Fn1<? super B, ? extends C>, ParserT<Identity<?>, A, F, ?>> appFn) {
        return parser(ParserT.super.zip(appFn));
    }

    @Override
    default <C> Parser<A, F, C> pure(C c) {
        return parser(ParserT.super.pure(c));
    }

    @Override
    default <C> Parser<A, F, C> fmap(Fn1<? super B, ? extends C> fn) {
        return parser(ParserT.super.fmap(fn));
    }

    @Override
    default <C> Lazy<? extends Parser<A, F, C>> lazyZip(
            Lazy<? extends Applicative<Fn1<? super B, ? extends C>, ParserT<Identity<?>, A, F, ?>>> lazyAppFn) {
        return ParserT.super.lazyZip(lazyAppFn).fmap(Parser::parser);
    }

    @Override
    default <C> Parser<A, F, C> discardL(Applicative<C, ParserT<Identity<?>, A, F, ?>> appB) {
        return parser(ParserT.super.discardL(appB));
    }

    @Override
    default <C> Parser<A, F, B> discardR(Applicative<C, ParserT<Identity<?>, A, F, ?>> appB) {
        return parser(ParserT.super.discardR(appB));
    }

    static <A, F, B> Parser<A, F, B> parser(ParserT<Identity<?>, A, F, B> parserT) {
        return a -> parserT.runParserT()
                .<EitherT<Identity<?>, F, B>>runReaderT(a)
                .<Identity<Either<F, B>>>runEitherT()
                .runIdentity();
    }

    static <A, F, B> Parser<A, F, B> parser(Fn1<? super A, ? extends Either<F, B>> parseFn) {
        return parseFn::apply;
    }
}
