package net.arvaux.core.util.sequences;

import java.util.Arrays;

public class ExplicitSequence<T> implements Sequence<T> {

    private T[] values;
    private Sequence<Integer> index;

    @SafeVarargs
    public ExplicitSequence(T... values) {
        this(null, values);
    }

    @SafeVarargs
    public ExplicitSequence(Runnable onIteration, T... values) {
        this.values = values;
        this.index = new IntegerSequence(0, values.length - 1, 1, onIteration);
    }

    public T[] getValues() {
        return values;
    }

    public Sequence<Integer> getIndex() {
        return index;
    }

    @Override
    public T get() {
        return this.getValues()[this.getIndex().get()];
    }

    @Override
    public T next() {
        return this.getValues()[this.getIndex().next()];
    }

    @Override
    public T getInterval() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(T object) {
        this.values = Arrays.copyOf(this.getValues(), this.getValues().length + 1);
        this.getValues()[this.getValues().length - 1] = object;
    }

    @Override
    public T getStop() {
        return this.getValues()[this.getValues().length - 1];
    }

    @Override
    public T getStart() {
        return this.getValues()[0];
    }

    @Override
    public void reset() {
        this.getIndex().reset();
    }

    @Override
    public void onIteration(Runnable runnable) {

    }
}
