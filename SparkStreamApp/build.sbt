name := "SparkStreamingAnalyseMobileData"

version := "0.1"

scalaVersion := "2.12.14"

libraryDependencies += "org.apache.spark" % "spark-streaming_2.12" % "3.1.2"
libraryDependencies += "org.apache.spark" %% "spark-core" % "3.1.2"
libraryDependencies += "org.apache.spark" %% "spark-sql" % "3.1.2"
libraryDependencies += "org.apache.spark" %% "spark-mllib" % "3.1.2"
libraryDependencies += "org.apache.spark" %% "spark-streaming-kafka-0-10" % "3.1.2"
libraryDependencies += "org.apache.spark" %% "spark-sql-kafka-0-10" % "3.1.2"
libraryDependencies += "org.apache.logging.log4j" % "log4j-api" % "2.14.1"
libraryDependencies += "org.apache.logging.log4j" % "log4j-core" % "2.14.1"

artifactName := { (sv: ScalaVersion, module: ModuleID, artifact: Artifact) =>
  "SparkStreamingAnalyseMobileData" + "." + artifact.extension
}

