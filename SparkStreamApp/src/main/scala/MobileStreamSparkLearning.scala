import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.clustering.StreamingKMeans
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}

import java.util

/**
 * Case class for the MobileData
 * @param uid Unique identifier
 * @param longitude Longitude value of the mobile device
 * @param latitude Latitude value of the mobile device
 */
case class MobileData(uid: String, longitude: Float, latitude: Float)

/**
 * The MobileData object to store and parse mobile string data
 */
object MobileData {
  def parseStringValue(mobileEntry: String): MobileData = {
    // Example to parse:
    // 2021-08-17 19:27:27.652,66dd4af6-9e6c-4dd5-9877-956461647dfe,1.770576528292556,9.920800321591638,8.691135259810395,-77.94833255851199,55.09200814968813,0
    val entries = mobileEntry.split(",")
    MobileData(entries(1), entries(5).toFloat, entries(6).toFloat)
  }
}

object MobileStreamSparkLearning {
  val spark = SparkSession.builder()
    .appName("MobileStreamLearner")
    .master("local[*]")
    .getOrCreate()

  // We batch the incoming data every 5 seconds
  val ssc = new StreamingContext(spark.sparkContext, Seconds(5))
  val kafkaParams: Map[String, Object] = Map(
    "bootstrap.servers" -> "my-kafka-cluster.default.svc.cluster.local:9092",
    "key.serializer" -> classOf[StringSerializer], // send data to kafka
    "value.serializer" -> classOf[StringSerializer],
    "group.id" -> "use_a_separate_group_id_for_each_stream",
    "key.deserializer" -> classOf[StringDeserializer], // receiving data from kafka
    "value.deserializer" -> classOf[StringDeserializer],
    "auto.offset.reset" -> "latest",
    "enable.auto.commit" -> false.asInstanceOf[Object]
  )

  val kafkaTopic = "mobile"

  def readFromKafka() = {
    val topics = Array(kafkaTopic)
    // Distribute the partitions evenly across the Spark cluster.
    val kafkaDStream = KafkaUtils.createDirectStream(ssc,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String, String](topics, kafkaParams)
    )

    // We are only interested in the value field
    val processedValueStream = kafkaDStream.map(record => (record.value()))

    // Now we create a stream of MobileData values
    val processedModelStream = processedValueStream.map(value => MobileData.parseStringValue(value))

    // From the mobile data, we generate now the continuous learning data
    val trainingMobileDataStream = processedModelStream.map(mobileData => Vectors.dense(Array(0.0, mobileData.longitude, mobileData.longitude)))

    // We generate from the train data stream a test data stream, but we map to constant value to make a prediction from a consistent poinz of view
    val testingMobileStream = processedModelStream.map(mobileData => (1.0, Vectors.dense(Array(0.0, 55.0, 45.0))))

    // Build the k-means model for clustering
    val kMeansModel = new StreamingKMeans()
      .setK(5)
      .setDecayFactor(1.0)
      .setRandomCenters(3.toInt, 0.0)

    // Train on training stream
    kMeansModel.trainOn(trainingMobileDataStream)

    // Do predictions on the test data
    val predictions = kMeansModel.predictOnValues(testingMobileStream)
    val printPredictions = predictions.map(prediction => "New Prediction is: " + prediction.toString())

    // We print each prediction of the prediction stream into the console
    // This information could be used for a report in later activities
    println("Predictions will start to stream")
    printPredictions.foreachRDD(rdd => {
      rdd.collect().foreach(content => print("Prediction is: " + content.toString))
    })

    // Start on streaming context and wait for termination
    ssc.start()
    ssc.awaitTermination()
  }

  def main(args: Array[String]): Unit = {
    readFromKafka()
  }
}
