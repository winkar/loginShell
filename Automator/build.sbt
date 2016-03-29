name := "Automator"

organization := "com.winkar"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.11.7"

libraryDependencies += "log4j" % "log4j" % "1.2.14"
libraryDependencies += "junit" % "junit" % "4.12"
libraryDependencies += "io.appium" % "java-client" % "3.4.0"
libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "1.0.3"
libraryDependencies += "com.github.scopt" %% "scopt" % "3.4.0"

resolvers += Resolver.sonatypeRepo("public")

//ivyXML :=
//  <dependencies>
//    <dependency>
//      <groupId>junit</groupId>
//      <artifactId>junit</artifactId>
//      <version>3.8.1</version>
//      <scope>test</scope>
//    </dependency>
//    <dependency>
//      <groupId>io.appium</groupId>
//      <artifactId>java-client</artifactId>
//      <version>3.3.0</version>
//    </dependency>
//    <dependency>
//      <groupId>log4j</groupId>
//      <artifactId>log4j</artifactId>
//      <version>1.2.17</version>
//    </dependency>
//  </dependencies>

