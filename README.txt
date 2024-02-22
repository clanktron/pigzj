Pigzj

This program implements a parallelized gzip in java.

Reads from stdin and outputs compressed output stdout.

Flags:
-p - number of processors to use

Parallelization is achieved with multithreading. A custom class ProcessQueue is used to track the state of the program, while worker threads are used to compress the input in parallel. ProcessQueue contains a blocking queue that is used to load in the input in 128Kib blocks. Worker threads pop this queue, perform compression at level 6, and load the result into a concurrent hashmap that is used as an output queue. The main thread is responsible for filling/writing from the queue(s).

Benchmarks:
$ input=/usr/local/cs/jdk-21.0.2/lib/modules
$ time gzip <$input >gzip.gz

real    0m8.534s
user    0m7.748s
sys     0m0.113s

$ time pigz <$input >pigz.gz

real    0m2.701s
user    0m8.160s
sys     0m0.049s

$ time java Pigzj <$input >Pigzj.gz

real    0m2.480s
user    0m8.084s
sys     0m0.302s

$ ls -l gzip.gz pigz.gz Pigzj.gz
-rw-r--r-- 1 robert csugrad  9111605 Feb 21 23:23 Pigzj.gz
-rw-r--r-- 1 robert csugrad 47109893 Feb 21 23:20 gzip.gz
-rw-r--r-- 1 robert csugrad 47008845 Feb 21 23:24 pigz.gz
