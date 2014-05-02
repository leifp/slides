
//// Transformations (return a new RDD)

/**
 * Return a new RDD by applying a function to all elements of this RDD.
 */
def map[U: ClassTag](f: T => U): RDD[U]

/**
 *  Return a new RDD by first applying a function to all elements of this
 *  RDD, and then flattening the results.
 */
def flatMap[U: ClassTag](f: T => TraversableOnce[U]): RDD[U]

/**
 * Return a new RDD containing only the elements that satisfy a predicate.
 */
def filter(f: T => Boolean): RDD[T]

/**
 * Return a new RDD containing the distinct elements in this RDD.
 */
def distinct(numPartitions: Int): RDD[T]
def distinct(): RDD[T]

/**
 * Return a new RDD that has exactly numPartitions partitions.
 *
 * Can increase or decrease the level of parallelism in this RDD. Internally, this uses
 * a shuffle to redistribute data.
 *
 * If you are decreasing the number of partitions in this RDD, consider using `coalesce`,
 * which can avoid performing a shuffle.
 */
def repartition(numPartitions: Int): RDD[T]

/**
 * Return a new RDD that is reduced into `numPartitions` partitions.
 *
 * This results in a narrow dependency, e.g. if you go from 1000 partitions
 * to 100 partitions, there will not be a shuffle, instead each of the 100
 * new partitions will claim 10 of the current partitions.
 *
 * However, if you're doing a drastic coalesce, e.g. to numPartitions = 1,
 * this may result in your computation taking place on fewer nodes than
 * you like (e.g. one node in the case of numPartitions = 1). To avoid this,
 * you can pass shuffle = true. This will add a shuffle step, but means the
 * current upstream partitions will be executed in parallel (per whatever
 * the current partitioning is).
 *
 * Note: With shuffle = true, you can actually coalesce to a larger number
 * of partitions. This is useful if you have a small number of partitions,
 * say 100, potentially with a few partitions being abnormally large. Calling
 * coalesce(1000, shuffle = true) will result in 1000 partitions with the
 * data distributed using a hash partitioner.
 */
def coalesce(numPartitions: Int, shuffle: Boolean = false): RDD[T]

/**
 * Return a sampled subset of this RDD.
 */
def sample(withReplacement: Boolean, fraction: Double, seed: Int): RDD[T]

/**
 * Return the union of this RDD and another one. Any identical elements will appear multiple
 * times (use `.distinct()` to eliminate them).
 */
def union(other: RDD[T]): RDD[T]
def ++(other: RDD[T]): RDD[T]

/**
 * Return an RDD created by coalescing all elements within each partition into an array.
 */
def glom(): RDD[Array[T]]

/**
 * Return the Cartesian product of this RDD and another one, that is, the RDD of all pairs of
 * elements (a, b) where a is in `this` and b is in `other`.
 */
def cartesian[U: ClassTag](other: RDD[U]): RDD[(T, U)]

/**
 * Return an RDD of grouped items.
 */
def groupBy[K: ClassTag](f: T => K): RDD[(K, Seq[T])]
def groupBy[K: ClassTag](f: T => K, numPartitions: Int): RDD[(K, Seq[T])]
def groupBy[K: ClassTag](f: T => K, p: Partitioner): RDD[(K, Seq[T])]

/**
 * Return an RDD created by piping elements to a forked external process.
 */
def pipe(command: String): RDD[String]
def pipe(command: String, env: Map[String, String]): RDD[String]
/**
 * Return an RDD created by piping elements to a forked external process.
 * The print behavior can be customized by providing two functions.
 *
 * @param command command to run in forked process.
 * @param env environment variables to set.
 * @param printPipeContext Before piping elements, this function is called as an oppotunity
 *                         to pipe context data. Print line function (like out.println) will be
 *                         passed as printPipeContext's parameter.
 * @param printRDDElement Use this function to customize how to pipe elements. This function
 *                        will be called with each RDD element as the 1st parameter, and the
 *                        print line function (like out.println()) as the 2nd parameter.
 *                        An example of pipe the RDD data of groupBy() in a streaming way,
 *                        instead of constructing a huge String to concat all the elements:
 *                        def printRDDElement(record:(String, Seq[String]), f:String=>Unit) =
 *                          for (e <- record._2){f(e)}
 * @return the result RDD
 */
def pipe(
    command: Seq[String],
    env: Map[String, String] = Map(),
    printPipeContext: (String => Unit) => Unit = null,
    printRDDElement: (T, String => Unit) => Unit = null): RDD[String]

/**
 * Return a new RDD by applying a function to each partition of this RDD.
 */
def mapPartitions[U: ClassTag](
    f: Iterator[T] => Iterator[U], preservesPartitioning: Boolean = false): RDD[U]

/**
 * Return a new RDD by applying a function to each partition of this RDD, while tracking the index
 * of the original partition.
 */
def mapPartitionsWithIndex[U: ClassTag](
    f: (Int, Iterator[T]) => Iterator[U], preservesPartitioning: Boolean = false): RDD[U]

/**
 * Return a new RDD by applying a function to each partition of this RDD. This is a variant of
 * mapPartitions that also passes the TaskContext into the closure.
 */
def mapPartitionsWithContext[U: ClassTag](
    f: (TaskContext, Iterator[T]) => Iterator[U],
    preservesPartitioning: Boolean = false): RDD[U]

/**
 * Maps f over this RDD, where f takes an additional parameter of type A.  This
 * additional parameter is produced by constructA, which is called in each
 * partition with the index of that partition.
 */
def mapWith[A: ClassTag, U: ClassTag]
    (constructA: Int => A, preservesPartitioning: Boolean = false)
    (f: (T, A) => U): RDD[U]

/**
 * FlatMaps f over this RDD, where f takes an additional parameter of type A.  This
 * additional parameter is produced by constructA, which is called in each
 * partition with the index of that partition.
 */
def flatMapWith[A: ClassTag, U: ClassTag]
    (constructA: Int => A, preservesPartitioning: Boolean = false)
    (f: (T, A) => Seq[U]): RDD[U]

/**
 * Filters this RDD with p, where p takes an additional parameter of type A.  This
 * additional parameter is produced by constructA, which is called in each
 * partition with the index of that partition.
 */
def filterWith[A: ClassTag](constructA: Int => A)(p: (T, A) => Boolean): RDD[T]

/**
 * Zips this RDD with another one, returning key-value pairs with the first element in each RDD,
 * second element in each RDD, etc. Assumes that the two RDDs have the *same number of
 * partitions* and the *same number of elements in each partition* (e.g. one was made through
 * a map on the other).
 */
def zip[U: ClassTag](other: RDD[U]): RDD[(T, U)]

/**
 * Zip this RDD's partitions with one (or more) RDD(s) and return a new RDD by
 * applying a function to the zipped partitions. Assumes that all the RDDs have the
 * *same number of partitions*, but does *not* require them to have the same number
 * of elements in each partition.
 */
def zipPartitions[B: ClassTag, V: ClassTag]
    (rdd2: RDD[B], preservesPartitioning: Boolean)
    (f: (Iterator[T], Iterator[B]) => Iterator[V]): RDD[V]

def zipPartitions[B: ClassTag, V: ClassTag]
    (rdd2: RDD[B])
    (f: (Iterator[T], Iterator[B]) => Iterator[V]): RDD[V]

def zipPartitions[B: ClassTag, C: ClassTag, V: ClassTag]
    (rdd2: RDD[B], rdd3: RDD[C], preservesPartitioning: Boolean)
    (f: (Iterator[T], Iterator[B], Iterator[C]) => Iterator[V]): RDD[V]

def zipPartitions[B: ClassTag, C: ClassTag, V: ClassTag]
    (rdd2: RDD[B], rdd3: RDD[C])
    (f: (Iterator[T], Iterator[B], Iterator[C]) => Iterator[V]): RDD[V]

def zipPartitions[B: ClassTag, C: ClassTag, D: ClassTag, V: ClassTag]
    (rdd2: RDD[B], rdd3: RDD[C], rdd4: RDD[D], preservesPartitioning: Boolean)
    (f: (Iterator[T], Iterator[B], Iterator[C], Iterator[D]) => Iterator[V]): RDD[V]

def zipPartitions[B: ClassTag, C: ClassTag, D: ClassTag, V: ClassTag]
    (rdd2: RDD[B], rdd3: RDD[C], rdd4: RDD[D])
    (f: (Iterator[T], Iterator[B], Iterator[C], Iterator[D]) => Iterator[V]): RDD[V]

//// Actions

/**
 * Applies f to each element of this RDD, where f takes an additional parameter of type A.
 * This additional parameter is produced by constructA, which is called in each
 * partition with the index of that partition.
 */
def foreachWith[A: ClassTag](constructA: Int => A)(f: (T, A) => Unit)

/**
 * Return a sampled subset of this RDD.
 */
def takeSample(withReplacement: Boolean, num: Int, seed: Int): Array[T]

//// Double Functions

  /** Add up the elements in this RDD. */
  def sum(): Double

  /**
   * Return a [[org.apache.spark.util.StatCounter]] object that captures the mean, variance and count
   * of the RDD's elements in one operation.
   */
  def stats(): StatCounter

  /** Compute the mean of this RDD's elements. */
  def mean(): Double = stats().mean

  /** Compute the variance of this RDD's elements. */
  def variance(): Double = stats().variance

  /** Compute the standard deviation of this RDD's elements. */
  def stdev(): Double = stats().stdev

  /** 
   * Compute the sample standard deviation of this RDD's elements (which corrects for bias in
   * estimating the standard deviation by dividing by N-1 instead of N).
   */
  def sampleStdev(): Double = stats().sampleStdev

  /**
   * Compute the sample variance of this RDD's elements (which corrects for bias in
   * estimating the variance by dividing by N-1 instead of N).
   */
  def sampleVariance(): Double = stats().sampleVariance

  /** (Experimental) Approximate operation to return the mean within a timeout. */
  def meanApprox(timeout: Long, confidence: Double = 0.95): PartialResult[BoundedDouble]

  /** (Experimental) Approximate operation to return the sum within a timeout. */
  def sumApprox(timeout: Long, confidence: Double = 0.95): PartialResult[BoundedDouble]

  /**
   * Compute a histogram of the data using bucketCount number of buckets evenly
   *  spaced between the minimum and maximum of the RDD. For example if the min
   *  value is 0 and the max is 100 and there are two buckets the resulting
   *  buckets will be [0, 50) [50, 100]. bucketCount must be at least 1
   * If the RDD contains infinity, NaN throws an exception
   * If the elements in RDD do not vary (max == min) always returns a single bucket.
   */
  def histogram(bucketCount: Int): Pair[Array[Double], Array[Long]]
  /**
   * Compute a histogram using the provided buckets. The buckets are all open
   * to the left except for the last which is closed
   *  e.g. for the array
   *  [1, 10, 20, 50] the buckets are [1, 10) [10, 20) [20, 50]
   *  e.g 1<=x<10 , 10<=x<20, 20<=x<50
   *  And on the input of 1 and 50 we would have a histogram of 1, 0, 0 
   * 
   * Note: if your histogram is evenly spaced (e.g. [0, 10, 20, 30]) this can be switched
   * from an O(log n) inseration to O(1) per element. (where n = # buckets) if you set evenBuckets
   * to true.
   * buckets must be sorted and not contain any duplicates.
   * buckets array must be at least two elements 
   * All NaN entries are treated the same. If you have a NaN bucket it must be
   * the maximum value of the last position and all NaN entries will be counted
   * in that bucket.
   */
  def histogram(buckets: Array[Double], evenBuckets: Boolean = false): Array[Long]

//// Pair Functions
/**
 * Extra functions available on RDDs of (key, value) pairs through an implicit conversion.
 * Import `org.apache.spark.SparkContext._` at the top of your program to use these functions.
 */

  /**
   * Generic function to combine the elements for each key using a custom set of aggregation
   * functions. Turns an RDD[(K, V)] into a result of type RDD[(K, C)], for a "combined type" C
   * Note that V and C can be different -- for example, one might group an RDD of type
   * (Int, Int) into an RDD of type (Int, Seq[Int]). Users provide three functions:
   *
   * - `createCombiner`, which turns a V into a C (e.g., creates a one-element list)
   * - `mergeValue`, to merge a V into a C (e.g., adds it to the end of a list)
   * - `mergeCombiners`, to combine two C's into a single one.
   *
   * In addition, users can control the partitioning of the output RDD, and whether to perform
   * map-side aggregation (if a mapper can produce multiple items with the same key).
   */
  def combineByKey[C](createCombiner: V => C,
      mergeValue: (C, V) => C,
      mergeCombiners: (C, C) => C,
      partitioner: Partitioner,
      mapSideCombine: Boolean = true,
      serializerClass: String = null): RDD[(K, C)]
  /**
   * Simplified version of combineByKey that hash-partitions the output RDD.
   */
  def combineByKey[C](createCombiner: V => C,
      mergeValue: (C, V) => C,
      mergeCombiners: (C, C) => C,
      numPartitions: Int): RDD[(K, C)]
  /**
   * Simplified version of combineByKey that hash-partitions the resulting RDD using the
   * existing partitioner/parallelism level.
   */
  def combineByKey[C](createCombiner: V => C, mergeValue: (C, V) => C, mergeCombiners: (C, C) => C)


  /**
   * Merge the values for each key using an associative function and a neutral "zero value" which
   * may be added to the result an arbitrary number of times, and must not change the result
   * (e.g., Nil for list concatenation, 0 for addition, or 1 for multiplication.).
   */
  def foldByKey(zeroValue: V, partitioner: Partitioner)(func: (V, V) => V): RDD[(K, V)]
  /**
   * Merge the values for each key using an associative function and a neutral "zero value" which
   * may be added to the result an arbitrary number of times, and must not change the result
   * (e.g., Nil for list concatenation, 0 for addition, or 1 for multiplication.).
   */
  def foldByKey(zeroValue: V, numPartitions: Int)(func: (V, V) => V): RDD[(K, V)]
  /**
   * Merge the values for each key using an associative function and a neutral "zero value" which
   * may be added to the result an arbitrary number of times, and must not change the result
   * (e.g., Nil for list concatenation, 0 for addition, or 1 for multiplication.).
   */
  def foldByKey(zeroValue: V)(func: (V, V) => V): RDD[(K, V)]

  /**
   * Merge the values for each key using an associative reduce function. This will also perform
   * the merging locally on each mapper before sending results to a reducer, similarly to a
   * "combiner" in MapReduce. Output will be hash-partitioned with the existing partitioner/
   * parallelism level.
   */
  def reduceByKey(func: (V, V) => V): RDD[(K, V)]
  /**
   * Merge the values for each key using an associative reduce function. This will also perform
   * the merging locally on each mapper before sending results to a reducer, similarly to a
   * "combiner" in MapReduce. Output will be hash-partitioned with numPartitions partitions.
   */
  def reduceByKey(func: (V, V) => V, numPartitions: Int): RDD[(K, V)]
  /**
   * Merge the values for each key using an associative reduce function. This will also perform
   * the merging locally on each mapper before sending results to a reducer, similarly to a
   * "combiner" in MapReduce.
   */
  def reduceByKey(partitioner: Partitioner, func: (V, V) => V): RDD[(K, V)]

  /**
   * Merge the values for each key using an associative reduce function, but return the results
   * immediately to the master as a Map. This will also perform the merging locally on each mapper
   * before sending results to a reducer, similarly to a "combiner" in MapReduce.
   */
  def reduceByKeyLocally(func: (V, V) => V): Map[K, V]

  /** Count the number of elements for each key, and return the result to the master as a Map. */
  def countByKey(): Map[K, Long]

  /**
   * (Experimental) Approximate version of countByKey that can return a partial result if it does
   * not finish within a timeout.
   */
  def countByKeyApprox(timeout: Long, confidence: Double = 0.95)

  /**
   * Return approximate number of distinct values for each key in this RDD.
   * The accuracy of approximation can be controlled through the relative standard deviation
   * (relativeSD) parameter, which also controls the amount of memory used. Lower values result in
   * more accurate counts but increase the memory footprint and vise versa. Uses the provided
   * Partitioner to partition the output RDD.
   */
  def countApproxDistinctByKey(relativeSD: Double, partitioner: Partitioner): RDD[(K, Long)]

  /**
   * Return approximate number of distinct values for each key in this RDD.
   * The accuracy of approximation can be controlled through the relative standard deviation
   * (relativeSD) parameter, which also controls the amount of memory used. Lower values result in
   * more accurate counts but increase the memory footprint and vise versa. HashPartitions the
   * output RDD into numPartitions.
   *
   */
  def countApproxDistinctByKey(relativeSD: Double, numPartitions: Int): RDD[(K, Long)]

  /**
   * Return approximate number of distinct values for each key this RDD.
   * The accuracy of approximation can be controlled through the relative standard deviation
   * (relativeSD) parameter, which also controls the amount of memory used. Lower values result in
   * more accurate counts but increase the memory footprint and vise versa. The default value of
   * relativeSD is 0.05. Hash-partitions the output RDD using the existing partitioner/parallelism
   * level.
   */
  def countApproxDistinctByKey(relativeSD: Double = 0.05): RDD[(K, Long)]

  /**
   * Group the values for each key in the RDD into a single sequence. Allows controlling the
   * partitioning of the resulting key-value pair RDD by passing a Partitioner.
   */
  def groupByKey(partitioner: Partitioner): RDD[(K, Seq[V])]
  /**
   * Group the values for each key in the RDD into a single sequence. Hash-partitions the
   * resulting RDD with into `numPartitions` partitions.
   */
  def groupByKey(numPartitions: Int): RDD[(K, Seq[V])]
  /**
   * Group the values for each key in the RDD into a single sequence. Hash-partitions the
   * resulting RDD with the existing partitioner/parallelism level.
   */
  def groupByKey(): RDD[(K, Seq[V])]

  /**
   * Return a copy of the RDD partitioned using the specified partitioner.
   */
  def partitionBy(partitioner: Partitioner): RDD[(K, V)]

  /**
   * Return an RDD containing all pairs of elements with matching keys in `this` and `other`. Each
   * pair of elements will be returned as a (k, (v1, v2)) tuple, where (k, v1) is in `this` and
   * (k, v2) is in `other`. Uses the given Partitioner to partition the output RDD.
   */
  def join[W](other: RDD[(K, W)], partitioner: Partitioner): RDD[(K, (V, W))]
  /**
   * Return an RDD containing all pairs of elements with matching keys in `this` and `other`. Each
   * pair of elements will be returned as a (k, (v1, v2)) tuple, where (k, v1) is in `this` and
   * (k, v2) is in `other`. Performs a hash join across the cluster.
   */
  def join[W](other: RDD[(K, W)]): RDD[(K, (V, W))]
  /**
   * Return an RDD containing all pairs of elements with matching keys in `this` and `other`. Each
   * pair of elements will be returned as a (k, (v1, v2)) tuple, where (k, v1) is in `this` and
   * (k, v2) is in `other`. Performs a hash join across the cluster.
   */
  def join[W](other: RDD[(K, W)], numPartitions: Int): RDD[(K, (V, W))]

  /**
   * Perform a left outer join of `this` and `other`. For each element (k, v) in `this`, the
   * resulting RDD will either contain all pairs (k, (v, Some(w))) for w in `other`, or the
   * pair (k, (v, None)) if no elements in `other` have key k. Uses the given Partitioner to
   * partition the output RDD.
   */
  def leftOuterJoin[W](other: RDD[(K, W)], partitioner: Partitioner): RDD[(K, (V, Option[W]))]
  /**
   * Perform a left outer join of `this` and `other`. For each element (k, v) in `this`, the
   * resulting RDD will either contain all pairs (k, (v, Some(w))) for w in `other`, or the
   * pair (k, (v, None)) if no elements in `other` have key k. Hash-partitions the output
   * using the existing partitioner/parallelism level.
   */
  def leftOuterJoin[W](other: RDD[(K, W)]): RDD[(K, (V, Option[W]))]


  /**
   * Perform a right outer join of `this` and `other`. For each element (k, w) in `other`, the
   * resulting RDD will either contain all pairs (k, (Some(v), w)) for v in `this`, or the
   * pair (k, (None, w)) if no elements in `this` have key k. Uses the given Partitioner to
   * partition the output RDD.
   */
  def rightOuterJoin[W](other: RDD[(K, W)], partitioner: Partitioner)

  /**
   * Pass each value in the key-value pair RDD through a map function without changing the keys;
   * this also retains the original RDD's partitioning.
   */
  def mapValues[U](f: V => U): RDD[(K, U)]

  /**
   * Pass each value in the key-value pair RDD through a flatMap function without changing the
   * keys; this also retains the original RDD's partitioning.
   */
  def flatMapValues[U](f: V => TraversableOnce[U]): RDD[(K, U)]

  /**
   * For each key k in `this` or `other`, return a resulting RDD that contains a tuple with the
   * list of values for that key in `this` as well as `other`.
   */
  def cogroup[W](other: RDD[(K, W)], partitioner: Partitioner): RDD[(K, (Seq[V], Seq[W]))]
  /**
   * For each key k in `this` or `other1` or `other2`, return a resulting RDD that contains a
   * tuple with the list of values for that key in `this`, `other1` and `other2`.
   */
  def cogroup[W1, W2](other1: RDD[(K, W1)], other2: RDD[(K, W2)], partitioner: Partitioner)
  /**
   * For each key k in `this` or `other`, return a resulting RDD that contains a tuple with the
   * list of values for that key in `this` as well as `other`.
   */
  def cogroup[W](other: RDD[(K, W)]): RDD[(K, (Seq[V], Seq[W]))]
  /**
   * For each key k in `this` or `other1` or `other2`, return a resulting RDD that contains a
   * tuple with the list of values for that key in `this`, `other1` and `other2`.
   */
  def cogroup[W1, W2](other1: RDD[(K, W1)], other2: RDD[(K, W2)])
  /**
   * For each key k in `this` or `other`, return a resulting RDD that contains a tuple with the
   * list of values for that key in `this` as well as `other`.
   */
  def cogroup[W](other: RDD[(K, W)], numPartitions: Int): RDD[(K, (Seq[V], Seq[W]))]
  /**
   * For each key k in `this` or `other1` or `other2`, return a resulting RDD that contains a
   * tuple with the list of values for that key in `this`, `other1` and `other2`.
   */
  def cogroup[W1, W2](other1: RDD[(K, W1)], other2: RDD[(K, W2)], numPartitions: Int)

  /**
   * Return an RDD with the pairs from `this` whose keys are not in `other`.
   *
   * Uses `this` partitioner/partition size, because even if `other` is huge, the resulting
   * RDD will be <= us.
   */
  def subtractByKey[W: ClassTag](other: RDD[(K, W)]): RDD[(K, V)]
  /** Return an RDD with the pairs from `this` whose keys are not in `other`. */
  def subtractByKey[W: ClassTag](other: RDD[(K, W)], numPartitions: Int): RDD[(K, V)]
  /** Return an RDD with the pairs from `this` whose keys are not in `other`. */
  def subtractByKey[W: ClassTag](other: RDD[(K, W)], p: Partitioner): RDD[(K, V)]

  /**
   * Return an RDD with the keys of each tuple.
   */
  def keys: RDD[K] = self.map(_._1)

  /**
   * Return an RDD with the values of each tuple.
   */
  def values: RDD[V] = self.map(_._2)

//// Pair Actions

  /**
   * Return the key-value pairs in this RDD to the master as a Map.
   */
  def collectAsMap(): Map[K, V]

  /**
   * Return the list of values in the RDD for key `key`. This operation is done efficiently if the
   * RDD has a known partitioner by only searching the partition that the key maps to.
   */
  def lookup(key: K): Seq[V]

//// SequenceFile Functions
/**
 * Extra functions available on RDDs of (key, value) pairs to create a Hadoop SequenceFile,
 * through an implicit conversion. Note that this can't be part of PairRDDFunctions because
 * we need more implicit parameters to convert our keys and values to Writable.
 *
 * Import `org.apache.spark.SparkContext._` at the top of their program to use these functions.
 */

  /**
   * Output the RDD as a Hadoop SequenceFile using the Writable types we infer from the RDD's key
   * and value types. If the key or value are Writable, then we use their classes directly;
   * otherwise we map primitive types such as Int and Double to IntWritable, DoubleWritable, etc,
   * byte arrays to BytesWritable, and Strings to Text. The `path` can be on any Hadoop-supported
   * file system.
   */
  def saveAsSequenceFile(path: String, codec: Option[Class[_ <: CompressionCodec]] = None)

//// Ordered Functions
/**
 * Extra functions available on RDDs of (key, value) pairs where the key is sortable through
 * an implicit conversion. Import `org.apache.spark.SparkContext._` at the top of your program to
 * use these functions. They will work with any key type that has a `scala.math.Ordered`
 * implementation.
 */

  /**
   * Sort the RDD by key, so that each partition contains a sorted range of the elements. Calling
   * `collect` or `save` on the resulting RDD will return or output an ordered list of records
   * (in the `save` case, they will be written to multiple `part-X` files in the filesystem, in
   * order of the keys).
   */
  def sortByKey(ascending: Boolean = true, numPartitions: Int = self.partitions.size): RDD[P]

