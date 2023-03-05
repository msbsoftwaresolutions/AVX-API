package net.arvaux.core.util.sequences;

public class IntegerSequence implements Sequence<Integer> {
    private int current;
    private int interval;
    private int stop;
    private int start;
    private int iteration;
    private Runnable onRotation;

    public IntegerSequence(int start, int stop) {
        this(start, stop, 1);
    }

    public IntegerSequence(int start, int stop, int interval) {
        this(start, stop, interval, null);
    }

    public IntegerSequence(int start, int stop, int interval, Runnable onRotation) {
        this.start = start;
        this.stop = stop;
        this.interval = interval;
        this.current = start - interval;
        this.onRotation = onRotation;
    }

    @Override
    public Integer get() {
        if (this.current < 0) {
            return 0;
        }

        return this.current;
    }

    @Override
    public Integer next() {
        int current = this.current;
        this.current += this.interval;
        if (this.current > this.stop) {
            this.current = 0;
        }

        if ((current < this.start && this.start <= this.current) || (this.current == 0 && this.start == 0)) {
            if (this.iteration > 0 && this.onRotation != null) {
                this.onRotation.run();
            }

            this.iteration++;
        }

        return this.current;
    }

    public int getIteration() {
        return iteration;
    }

    @Override
    public Integer getInterval() {
        return interval;
    }

    @Override
    public Integer getStop() {
        return stop;
    }

    @Override
    public Integer getStart() {
        return start;
    }

    @Override
    public void add(Integer object) {
        this.current += object;
    }

    @Override
    public void reset() {
        this.current = this.start;
        this.iteration = 0;
    }

    @Override
    public void onIteration(Runnable runnable) {
        this.onRotation = runnable;
    }
}
