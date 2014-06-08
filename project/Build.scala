import sbt._
import Keys._
import com.github.retronym.SbtOneJar

object CMSExportBuild extends Build {

  lazy val buildSettings = Defaults.coreDefaultSettings ++ Seq(
    organization         := "fi.pyppe",
    organizationHomepage := Some(url("http://www.pyppe.fi")),
    version              := "0.1-SNAPSHOT",
    scalaVersion         := "2.11.1",
    exportJars           := true
  )

  lazy val root = Project(
    id = "cms-export",
    base = file("."),
    settings = buildSettings ++
      Seq(libraryDependencies ++= Seq(
        "commons-dbcp"                %  "commons-dbcp"          % "1.4",
        "org.scalikejdbc"             %% "scalikejdbc"           % "2.0.1",
        "mysql"                       %  "mysql-connector-java"  % "5.1.30",

        "com.typesafe.scala-logging"  %% "scala-logging-slf4j"   % "2.1.2",
        "ch.qos.logback"              %  "logback-classic"       % "1.0.13",

        "org.joda"                    %  "joda-convert"          % "1.5",
        "joda-time"                   %  "joda-time"             % "2.3"
      )) ++
      SbtOneJar.oneJarSettings
  )

}
