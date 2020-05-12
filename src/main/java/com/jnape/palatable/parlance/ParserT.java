package com.jnape.palatable.parlance;

import com.jnape.palatable.lambda.adt.Either;
import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.Fn2;
import com.jnape.palatable.lambda.functions.recursion.RecursiveResult;
import com.jnape.palatable.lambda.functor.Applicative;
import com.jnape.palatable.lambda.functor.Cartesian;
import com.jnape.palatable.lambda.functor.builtin.Lazy;
import com.jnape.palatable.lambda.monad.Monad;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.lambda.monad.transformer.builtin.EitherT;
import com.jnape.palatable.lambda.monad.transformer.builtin.ReaderT;

import static com.jnape.palatable.lambda.adt.Either.right;
import static com.jnape.palatable.lambda.monad.transformer.builtin.EitherT.eitherT;
import static com.jnape.palatable.lambda.monad.transformer.builtin.ReaderT.readerT;

public interface ParserT<M extends MonadRec<?, M>, A, F, B> extends
        MonadRec<B, ParserT<M, A, F, ?>>,
        Cartesian<A, B, ParserT<M, ?, F, ?>> {

    ReaderT<A, EitherT<M, F, ?>, B> runParserT();

    @Override
    default <C> ParserT<M, Tuple2<C, A>, F, Tuple2<C, B>> cartesian() {
        return () -> runParserT().cartesian();
    }

    @Override
    default ParserT<M, A, F, Tuple2<A, B>> carry() {
        return (ParserT<M, A, F, Tuple2<A, B>>) Cartesian.super.carry();
    }

    @Override
    default <Z, C> ParserT<M, Z, F, C> diMap(Fn1<? super Z, ? extends A> lFn, Fn1<? super B, ? extends C> rFn) {
        return () -> runParserT().diMap(lFn, rFn);
    }

    @Override
    default <Z> ParserT<M, Z, F, B> diMapL(Fn1<? super Z, ? extends A> fn) {
        return (ParserT<M, Z, F, B>) Cartesian.super.<Z>diMapL(fn);
    }

    @Override
    default <C> ParserT<M, A, F, C> diMapR(Fn1<? super B, ? extends C> fn) {
        return (ParserT<M, A, F, C>) Cartesian.super.<C>diMapR(fn);
    }

    @Override
    default <Z> ParserT<M, Z, F, B> contraMap(Fn1<? super Z, ? extends A> fn) {
        return (ParserT<M, Z, F, B>) Cartesian.super.<Z>contraMap(fn);
    }

    default <C> ParserT<M, A, F, C> and(ParserT<M, B, F, C> other) {
        return () -> runParserT().and(other.runParserT());
    }

    default ParserT<M, A, F, B> or(ParserT<M, A, F, B> other, Fn2<? super F, ? super F, ? extends F> combineF) {
        return () -> readerT(a -> {
            MonadRec<Either<F, B>, M> mefb = runParserT().<EitherT<M, F, B>>runReaderT(a).runEitherT();
            return eitherT(mefb.flatMap(fOrB -> fOrB.match(
                    f -> other.runParserT().<EitherT<M, F, B>>runReaderT(a).<F>biMapL(combineF.apply(f)).runEitherT(),
                    b -> mefb.pure(right(b))
            )));
        });
    }

    @Override
    default <C> ParserT<M, A, F, C> trampolineM(
            Fn1<? super B, ? extends MonadRec<RecursiveResult<B, C>, ParserT<M, A, F, ?>>> fn) {
        return () -> runParserT().trampolineM(b -> fn.apply(b).<ParserT<M, A, F, RecursiveResult<B, C>>>coerce().runParserT());
    }

    @Override
    default <C> ParserT<M, A, F, C> flatMap(Fn1<? super B, ? extends Monad<C, ParserT<M, A, F, ?>>> f) {
        return () -> runParserT().flatMap(b -> f.apply(b).<ParserT<M, A, F, C>>coerce().runParserT());
    }

    @Override
    default <C> ParserT<M, A, F, C> zip(Applicative<Fn1<? super B, ? extends C>, ParserT<M, A, F, ?>> appFn) {
        return () -> runParserT().zip(appFn.<ParserT<M, A, F, Fn1<? super B, ? extends C>>>coerce().runParserT());
    }

    @Override
    default <C> ParserT<M, A, F, C> pure(C c) {
        return () -> runParserT().pure(c);
    }

    @Override
    default <C> ParserT<M, A, F, C> fmap(Fn1<? super B, ? extends C> fn) {
        return () -> runParserT().fmap(fn);
    }

    @Override
    default <C> Lazy<? extends ParserT<M, A, F, C>> lazyZip(
            Lazy<? extends Applicative<Fn1<? super B, ? extends C>, ParserT<M, A, F, ?>>> lazyAppFn) {
        return runParserT().lazyZip(lazyAppFn.fmap(app -> app.<ParserT<M, A, F, Fn1<? super B, ? extends C>>>coerce().runParserT()))
                .fmap(parseFn -> () -> parseFn);
    }

    @Override
    default <C> ParserT<M, A, F, C> discardL(Applicative<C, ParserT<M, A, F, ?>> appB) {
        return MonadRec.super.discardL(appB).coerce();
    }

    @Override
    default <C> ParserT<M, A, F, B> discardR(Applicative<C, ParserT<M, A, F, ?>> appB) {
        return MonadRec.super.discardR(appB).coerce();
    }

    static <M extends MonadRec<?, M>, A, F, B> ParserT<M, A, F, B> parserT(
            Fn1<? super A, ? extends MonadRec<Either<F, B>, M>> parseFn) {
        return () -> readerT(parseFn.fmap(EitherT::eitherT));
    }
}
