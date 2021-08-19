import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions.{explode, split}
import org.apache.spark.sql.{Encoder, Encoders}

case class MobileData(uid: String, longitude: Float, latitude: Float)

object MobileData {
  def parseStringValue(mobileEntry: String): MobileData = {
    // Example to parse:
    // 2021-08-17 19:27:27.652,66dd4af6-9e6c-4dd5-9877-956461647dfe,1.770576528292556,9.920800321591638,8.691135259810395,-77.94833255851199,55.09200814968813,0
    val entries = mobileEntry.split(",")
    MobileData(entries(1), entries(5).toFloat, entries(6).toFloat)
  }
}

object SparkStreamingConsumeKafka {
  def main(args: Array[String]): Unit = {
    val spark:SparkSession = SparkSession.builder()
      .master("local[*]")
      .appName("SparkStreamingAnalyseMobileData")
      .getOrCreate()

    import spark.implicits._
    spark.sparkContext.setLogLevel("ERROR")

    val mobileDf = spark.readStream
      .format("kafka")
      //.option("kafka.bootstrap.servers", "my-kafka-cluster.default.svc.cluster.local:9092")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("subscribe", "mobile")
      .option("startingOffsets", "earliest")
      .load()

    mobileDf.printSchema()
    val dataStream = mobileDf.selectExpr("CAST(value AS STRING)").as[(String)]
    val mobileDataStream = dataStream.map(value => MobileData.parseStringValue(value))


    val consoleOutput = mobileDataStream.writeStream
      .outputMode("append")
      .format("console")
      .start()
    consoleOutput.awaitTermination()

    // TODO: Do KMeans clustering of mobile longitude and latitude to generate report
 }
}
