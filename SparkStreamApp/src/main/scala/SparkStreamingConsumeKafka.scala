import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{explode, split}

object SparkStreamingConsumeKafka {
  def main(args: Array[String]): Unit = {
    val spark:SparkSession = SparkSession.builder()
      .master("local[*]")
      .appName("SparkStreamingAnalyseMobileData")
      .getOrCreate()

    spark.sparkContext.setLogLevel("ERROR")

    val df = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "my-kafka-cluster.default.svc.cluster.local:9092")
      .option("subscribe", "mobile_data")
      .option("startingOffsets", "earliest")
      .load()

    df.printSchema()

    val mobile_data_row = df.selectExpr("CAST(value AS STRING)")

    mobile_data_row.writeStream
      .format("console")
      .outputMode("complete")
      .start()
      .awaitTermination()

    // TODO: Do KMeans clustering of mobile longitude and latitude to generate report
  }
}
