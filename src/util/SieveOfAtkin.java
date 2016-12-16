package util;

import java.util.ArrayList;
import java.util.List;

/**
 * Class handling Sieve of Atkin prime generation.
 */
public class SieveOfAtkin {
    //A long list of primes.
    private static final List<Integer> primes = getPrimes(25000000);

    //The current prime number we are at.
    private static int counter = 0;

    /**
     * Get the next prime number.
     *
     * @return The next prime number after the previously taken prime number.
     */
    public static Long getNextPrime() {
        return Long.valueOf(primes.get(counter++));
    }

    /**
     * Calculate all primes between 1 and the limit.
     *
     * @param limit The limiting size of the prime.
     * @return List of integer primes.
     */
    private static List<Integer> getPrimes(int limit) {
        System.out.println("Initializing prime generation.");
        System.out.println("\tCreating prime table for all values < " + limit + ".");

        //Initialize the sieve.
        boolean[] isPrime = new boolean[limit + 1];
        isPrime[2] = true;
        isPrime[3] = true;

        //The max value we iterate to.
        int root = (int) Math.ceil(Math.sqrt(limit));

        //Generate candidate primes.
        for (int x = 1; x < root; x++) {
            for (int y = 1; y < root; y++) {
                int n = 4 * x * x + y * y;
                if (n <= limit && (n % 12 == 1 || n % 12 == 5))
                    isPrime[n] = !isPrime[n];
                n = 3 * x * x + y * y;
                if (n <= limit && n % 12 == 7)
                    isPrime[n] = !isPrime[n];
                n = 3 * x * x - y * y;
                if ((x > y) && (n <= limit) && (n % 12 == 11))
                    isPrime[n] = !isPrime[n];
            }
        }

        //Eliminate values by sieving.
        for (int i = 5; i <= root; i++) {
            if (isPrime[i]) {
                for (int j = i * i; j < limit; j += i * i) {
                    isPrime[j] = false;
                }
            }
        }


        //Create the prime number list.
        List<Integer> primes = new ArrayList<>();

        //Add the primes to the prime list.
        for (int i = 2; i < isPrime.length; i++) {
            if (isPrime[i]) {
                primes.add(i);
            }
        }

        System.out.println("\tPrime table creation finished, generated " + primes.size() + " primes.");

        return primes;
    }
}
