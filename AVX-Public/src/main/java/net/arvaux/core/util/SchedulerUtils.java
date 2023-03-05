package net.arvaux.core.util;

import net.arvaux.core.Main;
import net.arvaux.core.entity.player.Callback;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Callable;

public class SchedulerUtils {

    public static void runLater(long delay, Runnable runnable) {
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), runnable, delay);
    }

    public static void runLaterAsync(long delay, Runnable runnable) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getInstance(), runnable, delay);
    }

    public static void runAtInterval(long interval, Runnable... tasks) {
        runAtInterval(0L, interval, tasks);
    }

    public static void runAtInterval(long delay, long interval, Runnable... tasks) {
        new BukkitRunnable() {
            private int index;

            @Override
            public void run() {
                if (this.index >= tasks.length) {
                    this.cancel();
                    return;
                }

                tasks[index].run();
                index++;
            }
        }.runTaskTimer(Main.getInstance(), delay, interval);
    }

    public static void repeat(int repetitions, long interval, Runnable task, Runnable onComplete) {
        new BukkitRunnable() {
            private int index;

            @Override
            public void run() {
                index++;
                if (this.index > repetitions) {
                    this.cancel();
                    if (onComplete == null) {
                        return;
                    }

                    onComplete.run();
                    return;
                }

                task.run();
            }
        }.runTaskTimer(Main.getInstance(), 0L, interval);
    }

    public static void repeatAsync(int repetitions, long interval, Runnable task, Runnable onComplete) {
        new BukkitRunnable() {
            private int index;

            @Override
            public void run() {
                index++;
                if (this.index > repetitions) {
                    this.cancel();
                    if (onComplete == null) {
                        return;
                    }

                    UtilConcurrency.runSync(onComplete);
                    return;
                }

                task.run();
            }
        }.runTaskTimerAsynchronously(Main.getInstance(), 0L, interval);
    }

    public static void repeatWhile(long interval, Callable<Boolean> predicate, Runnable task, Runnable onComplete) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    if (!predicate.call()) {
                        this.cancel();
                        if (onComplete == null) {
                            return;
                        }

                        onComplete.run();
                        return;
                    }

                    task.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.runTaskTimer(Main.getInstance(), 0L, interval);
    }

    public static void repeatWhileAsync(long interval, Callable<Boolean> predicate, Runnable task, Runnable onComplete) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    if (!predicate.call()) {
                        this.cancel();
                        if (onComplete == null) {
                            return;
                        }

                        onComplete.run();
                        return;
                    }

                    task.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.runTaskTimerAsynchronously(Main.getInstance(), 0L, interval);
    }

    public interface Task {

        void start(Runnable onComplete);
    }

    public static class TaskBuilder {

        private Queue<Task> taskList;

        public TaskBuilder() {
            this.taskList = new LinkedList<>();
        }

        public TaskBuilder append(TaskBuilder builder) {
            if (builder == null) {
                return this;
            }

            this.taskList.addAll(builder.taskList);
            return this;
        }

        public TaskBuilder appendDelay(long delay) {
            this.taskList.add(onComplete -> SchedulerUtils.runLater(delay, onComplete));
            return this;
        }

        public TaskBuilder appendTask(Runnable task) {
            this.taskList.add(onComplete ->
            {
                task.run();
                onComplete.run();
            });

            return this;
        }

        public TaskBuilder appendTask(Task task) {
            this.taskList.add(task);
            return this;
        }

        public TaskBuilder appendDelayedTask(long delay, Runnable task) {
            this.taskList.add(onComplete -> SchedulerUtils.runLater(delay, () -> {
                task.run();
                onComplete.run();
            }));

            return this;
        }

        public TaskBuilder appendTasks(long delay, long interval, Runnable... tasks) {
            this.taskList.add(onComplete -> {
                Runnable[] runnables = Arrays.copyOf(tasks, tasks.length + 1);
                runnables[runnables.length - 1] = onComplete;
                SchedulerUtils.runAtInterval(delay, interval, runnables);
            });

            return this;
        }

        public TaskBuilder appendRepeatingTask(int repetitions, long interval, Runnable task) {
            this.taskList.add(onComplete -> SchedulerUtils.repeat(repetitions, interval, task, onComplete));
            return this;
        }

        public TaskBuilder appendConditionalRepeatingTask(long interval, Callable<Boolean> predicate, Runnable task) {
            this.taskList.add(onComplete -> SchedulerUtils.repeatWhile(interval, predicate, task, onComplete));
            return this;
        }

        public TaskBuilder waitOrFail(long wait, Callable<Boolean> predicate, Callback<Boolean> result) {
            this.taskList.add(onComplete -> new BukkitRunnable() {
                private int ticks;

                @Override
                public void run() {
                    try {
                        if (!predicate.call()) {
                            cancel();

                            if (result != null) {
                                result.call(false);
                            }

                            onComplete.run();
                            return;
                        }

                        if (++this.ticks < wait) {
                            return;
                        }

                        cancel();
                        if (result != null) {
                            result.call(true);
                        }

                        onComplete.run();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.runTaskTimer(Main.getInstance(), 1L, 1L));
            return this;
        }

        public TaskBuilder waitFor(Callable<Boolean> predicate) {
            this.taskList.add(onComplete -> new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        if (!predicate.call()) {
                            return;
                        }

                        this.cancel();
                        onComplete.run();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.runTaskTimer(Main.getInstance(), 0L, 1L));
            return this;
        }

        public TaskBuilder waitFor(Callable<Boolean> predicate, long timeOut, Runnable onTimeOut) {
            this.taskList.add(onComplete -> new BukkitRunnable() {
                private int ticks;

                @Override
                public void run() {
                    try {
                        if (this.ticks++ > timeOut) {
                            if (onTimeOut != null) {
                                onTimeOut.run();
                            }

                            onComplete.run();
                            this.cancel();
                            return;
                        }

                        if (!predicate.call()) {
                            return;
                        }

                        this.cancel();
                        onComplete.run();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.runTaskTimer(Main.getInstance(), 0L, 1L));
            return this;
        }

        public void runTasks() {
            this.startNext();
        }

        private void startNext() {
            Task task = this.taskList.poll();
            if (task == null) {
                return;
            }

            task.start(this::startNext);
        }
    }
}
