package com.github.murphye.customer.repository;

import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import com.datastax.driver.mapping.Result;
import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.Single;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class AbstractRepository<I, E, A> {

    private final TypeToken<E> typeToken = new TypeToken<E>(getClass()) { };
    private final Type type = typeToken.getType();

    protected Mapper<E> mapper;

    protected A accessor;

    protected void init(Session session) {
        MappingManager manager = new MappingManager(session);
        this.mapper = manager.mapper(getMapperClass());
        this.accessor = manager.createAccessor(getAccessorClass());
    }

    public Single<Void> add(E e) {
        System.out.println("add");
        return Single.fromFuture(mapper.saveAsync(e));
    }

    public Single<Void> remove(E e) {
        return Single.fromFuture(mapper.deleteAsync(e));
    }

    public Single<E> find(I id) {
        return Single.fromFuture(mapper.getAsync(id));
    }


    protected Flowable<E> toFlowable(ListenableFuture<Result<E>> resultListenableFuture) {
        return Flowable.create(emitter -> {
            Futures.transformAsync(resultListenableFuture, iterateResult(emitter,1));
        }, BackpressureStrategy.BUFFER);
    }

    /**
     * Iterates through the results in pages, and emits the values in process.
     * @param emitter The FlowableEmitter
     * @param page
     * @return
     */
    private AsyncFunction<Result<E>, Result<E>> iterateResult(final FlowableEmitter<E> emitter, final int page) {
        return result -> {
            // How far we can go without triggering the blocking fetch:
            int remainingInPage = result.getAvailableWithoutFetching();

            //System.out.printf("Starting page %d (%d rows)%n", page, remainingInPage);

            while(result.getAvailableWithoutFetching() > 0 && result.iterator().hasNext()) {
                E e = result.iterator().next();
                //System.out.println("Emit      " + e);
                emitter.onNext(e);
            }

            //System.out.printf("Done page %d%n", page);

            if (result.getExecutionInfo().getPagingState() == null) { // Last page
                //System.out.println("Done iterating");
                emitter.onComplete();
                return Futures.immediateFuture(result);
            } else {
                ListenableFuture<Result<E>> moreResultsFuture = result.fetchMoreResults();
                return Futures.transformAsync(moreResultsFuture, iterateResult(emitter, page + 1));
            }
        };
    }

    @SuppressWarnings("unchecked")
    private Class<E> getMapperClass() {
        try {
            String className = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1].getTypeName();
            Class<?> clazz = Class.forName(className);
            return (Class<E>) clazz;
        } catch (Exception e) {
            throw new IllegalStateException("Class is not parametrized with generic type!!! Please use extends <> ");
        }
    }

    @SuppressWarnings("unchecked")
    private Class<A> getAccessorClass() {
        try {
            String className = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[2].getTypeName();
            Class<?> clazz = Class.forName(className);
            return (Class<A>) clazz;
        } catch (Exception e) {
            throw new IllegalStateException("Class is not parametrized with generic type!!! Please use extends <> ");
        }
    }
}
