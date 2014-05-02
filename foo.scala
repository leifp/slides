
//// Transformations (return a new RDD)
def map[U](f: T => U): RDD[U]
def flatMap[U](f: T => TraversableOnce[U]): RDD[U]
def filter(f: T => Boolean): RDD[T]
def distinct(numPartitions: Int): RDD[T]
def distinct(): RDD[T]
def sample(withReplacement: Boolean, fraction: Double, seed: Int): RDD[T]
def ++, union(other: RDD[T]): RDD[T]
def ++(other: RDD[T]): RDD[T]
def groupBy[K](f: T => K): RDD[(K, Seq[T])]
def repartition(numPartitions: Int): RDD[T]
def coalesce(numPartitions: Int, shuffle: Boolean = false): RDD[T]
def glom(): RDD[Array[T]]
def pipe(command: String, env: Map[String, String]): RDD[String]
def cartesian[U](other: RDD[U]): RDD[(T, U)]

def mapPartitions[U](
    f: Iterator[T] => Iterator[U], preservesPartitioning: Boolean = false): RDD[U]
def mapPartitionsWithIndex[U](
    f: (Int, Iterator[T]) => Iterator[U], preservesPartitioning: Boolean = false): RDD[U]
def mapPartitionsWithContext[U](
    f: (TaskContext, Iterator[T]) => Iterator[U],
    preservesPartitioning: Boolean = false): RDD[U]

def mapWith[A, U]
    (constructA: Int => A, preservesPartitioning: Boolean = false)
    (f: (T, A) => U): RDD[U]
def flatMapWith[A, U]
    (constructA: Int => A, preservesPartitioning: Boolean = false)
    (f: (T, A) => Seq[U]): RDD[U]
def filterWith[A](constructA: Int => A)(p: (T, A) => Boolean): RDD[T]

def zip[U](other: RDD[U]): RDD[(T, U)]
def zipPartitions[B, V]
    (rdd2: RDD[B])
    (f: (Iterator[T], Iterator[B]) => Iterator[V]): RDD[V]
def zipPartitions[B, C, V]
    (rdd2: RDD[B], rdd3: RDD[C])
    (f: (Iterator[T], Iterator[B], Iterator[C]) => Iterator[V]): RDD[V]
def zipPartitions[B, C, D, V]
    (rdd2: RDD[B], rdd3: RDD[C], rdd4: RDD[D])
    (f: (Iterator[T], Iterator[B], Iterator[C], Iterator[D]) => Iterator[V]): RDD[V]
//// Pair Functions
  def combineByKey[C](VtoC: V => C, addCV: (C, V) => C, addCC: (C, C) => C): RDD[(K,C)]
  def foldByKey(zeroValue: V)(func: (V, V) => V): RDD[(K, V)]
  def reduceByKey(func: (V, V) => V): RDD[(K, V)]
  def countApproxDistinctByKey(relativeSD: Double, partitioner: Partitioner): RDD[(K, Long)]
  def countApproxDistinctByKey(relativeSD: Double, numPartitions: Int): RDD[(K, Long)]
  def countApproxDistinctByKey(relativeSD: Double = 0.05): RDD[(K, Long)]
  def groupByKey(partitioner: Partitioner): RDD[(K, Seq[V])]
  def groupByKey(numPartitions: Int): RDD[(K, Seq[V])]
  def groupByKey(): RDD[(K, Seq[V])]
  def partitionBy(partitioner: Partitioner): RDD[(K, V)]
  def join[W](other: RDD[(K, W)]): RDD[(K, (V, W))]
  def leftOuterJoin[W](other: RDD[(K, W)]):  RDD[(K, (V, Option[W]))]
  def rightOuterJoin[W](other: RDD[(K, W)]): RDD[(K, (Option[V], W))]
  def mapValues[U](f: V => U): RDD[(K, U)]
  def flatMapValues[U](f: V => TraversableOnce[U]): RDD[(K, U)]
  def cogroup[W1, W2](other1: RDD[(K, W1)], other2: RDD[(K, W2)]): RDD[(K, (Seq[V], Seq[W1], Seq[W2]))]
  // like set diff
  def subtractByKey[W](other: RDD[(K, W)]): RDD[(K, V)]
  def keys: RDD[K]
  def values: RDD[V]
  def sortByKey(ascending: Boolean = true, numPartitions: Int = self.partitions.size): RDD[P]

//// Actions

def foreachWith[A](constructA: Int => A)(f: (T, A) => Unit)
def takeSample(withReplacement: Boolean, num: Int, seed: Int): Array[T]
/// Double Actions
  def sum(): Double
  def stats(): StatCounter
  def mean(): Double = stats().mean
  def variance(): Double = stats().variance
  def stdev(): Double = stats().stdev
  def sampleStdev(): Double = stats().sampleStdev
  def sampleVariance(): Double = stats().sampleVariance
  def meanApprox(timeout: Long, confidence: Double = 0.95): PartialResult[BoundedDouble]
  def sumApprox(timeout: Long, confidence: Double = 0.95): PartialResult[BoundedDouble]
  def histogram(bucketCount: Int): Pair[Array[Double], Array[Long]]
  def histogram(buckets: Array[Double], evenBuckets: Boolean = false): Array[Long]
/// Pair Actions
  def reduceByKeyLocally(func: (V, V) => V): Map[K, V]
  def countByKey(): Map[K, Long]
  def countByKeyApprox(timeout: Long, confidence: Double = 0.95)
  def collectAsMap(): Map[K, V]
  def lookup(key: K): Seq[V]
  def saveAsSequenceFile(path: String, codec: Option[Class[_ <: CompressionCodec]] = None)
