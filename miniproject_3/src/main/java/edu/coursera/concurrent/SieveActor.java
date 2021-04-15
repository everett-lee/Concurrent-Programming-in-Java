package edu.coursera.concurrent;

import edu.rice.pcdp.Actor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static edu.rice.pcdp.PCDP.finish;

/**
 * An actor-based implementation of the Sieve of Eratosthenes.
 *
 * TODO Fill in the empty SieveActorActor actor class below and use it from
 * countPrimes to determine the number of primes <= limit.
 */
public final class SieveActor extends Sieve {
    static int MAX_LOCAL_PRIMES = 50;

    /**
     * {@inheritDoc}
     *
     * TODO Use the SieveActorActor class to calculate the number of primes <=
     * limit in parallel. You might consider how you can model the Sieve of
     * Eratosthenes as a pipeline of actors, each corresponding to a single
     * prime number.
     */
    @Override
    public int countPrimes(final int limit) {
        final SieveActorActor actor = new SieveActorActor();
        List<Integer> listWithTwo = new ArrayList<>();
        listWithTwo.add(2);
        actor.localPrimes = listWithTwo;
        actor.count = 1;

        finish(() -> {
            IntStream.iterate(3, i -> i + 2)
                    .boxed()
                    .takeWhile(i -> i <= limit)
                    .forEach(actor::send);
            actor.send(0);

        });

        return getPrimeCount(actor, 0);
    }

    private int getPrimeCount(SieveActorActor actor, int count) {
        if (actor == null) {
            return count;
        }
        return getPrimeCount(actor.next, count + actor.count);
    }

    /**
     * An actor class that helps implement the Sieve of Eratosthenes in
     * parallel.
     */
    public static final class SieveActorActor extends Actor {
        SieveActorActor next = null;
        int count = 0;
        List<Integer> localPrimes = new ArrayList<>();

        /**
         * Process a single message sent to this actor.
         * <p>
         * TODO complete this method.
         *
         * @param msg Received message
         */
        @Override
        public void process(final Object msg) {
            int candidate = (int) msg;

            if (candidate <= 0) {
                if (next != null) {
                    next.send(0);
                }
                return;
            }

            if (isMultiple(candidate)) {
                return;
            }

            if (count < MAX_LOCAL_PRIMES) {
                localPrimes.add(candidate);
                count += 1;
                return;
            }

            if (next == null) {
                next = new SieveActorActor();
            }

            next.send(candidate);
        }

        private boolean isMultiple(int n) {
            for (int lp : localPrimes) {
                if (n % lp == 0) {
                    return true;
                }
            }

            return false;
        }
    }
}
