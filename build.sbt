name := "rest-swagger-app"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
filters
,"org.xerial" % "sqlite-jdbc" % "3.8.6" 
,"com.typesafe.play" %% "play-slick" % "1.1.1" 
,"com.typesafe.play" %% "play-slick-evolutions" % "1.1.1" 
,"io.swagger" %% "swagger-play2" % "1.5.1" 
,"org.webjars" % "swagger-ui" % "2.1.4" 
,javaWs ) 

libraryDependencies += evolutions

routesGenerator := InjectedRoutesGenerator
