import java.util.LinkedList;
import java.util.Queue;

class CustomThreadPool {
    private final Queue<Runnable> taskQueue = new LinkedList<>();
    private final WorkerThread[] workerThreads;
    private volatile boolean isRunning = true;

    public CustomThreadPool(int initialSize) {
        workerThreads = new WorkerThread[initialSize];
        for (int i = 0; i < initialSize; i++) {
            workerThreads[i] = new WorkerThread("Worker-" + i);
            workerThreads[i].start();
        }
    }

    public synchronized void submitTask(Runnable task) {
        if (isRunning) {
            taskQueue.offer(task); // Add task to the queue
            notify(); // Notify a waiting worker thread
        }
    }

    public synchronized void shutdown() {
        isRunning = false;
        for (WorkerThread worker : workerThreads) {
            worker.interrupt(); // Interrupt each worker thread
        }
    }

    private class WorkerThread extends Thread {
        public WorkerThread(String name) {
            super(name);
        }

        @Override
        public void run() {
            while (isRunning) {
                Runnable task;
                synchronized (CustomThreadPool.this) {
                    while (taskQueue.isEmpty()) {
                        try {
                            CustomThreadPool.this.wait(); // Wait for a new task
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                    task = taskQueue.poll(); // Get the next task from the queue
                }
                if (task != null) {
                    task.run(); // Execute the task
                }
            }
        }
    }
}

public class CustomThreadPoolExample {
    public static void main(String[] args) {
        CustomThreadPool threadPool = new CustomThreadPool(3);

        // Submit 10 tasks to the thread pool
        for (int i = 1; i <= 10; i++) {
            int taskNumber = i;
            threadPool.submitTask(() -> {
                System.out.println(Thread.currentThread().getName() + " executing task " + taskNumber);
                try {
                    Thread.sleep(500); // Simulate task workload
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        // Shutdown the thread pool
        threadPool.shutdown();
    }
}

