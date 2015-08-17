name          := "spray-upload"

version       := "0.1"

organization  := "xyz.hyperreal"

scalaVersion  := "2.11.7"

scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-language:postfixOps", "-encoding", "utf8")

resolvers += "Hyperreal Repository" at "https://dl.bintray.com/edadma/maven"

libraryDependencies ++= {
	val akkaV = "2.3.12"
	val sprayV = "1.3.3"
	Seq(
		"io.spray"          %% "spray-can"     % sprayV,
		"io.spray"          %% "spray-routing" % sprayV,
		"io.spray"          %% "spray-json"    % "1.3.2",
		"io.spray"          %% "spray-testkit" % sprayV   % "test",
		"com.typesafe.akka" %% "akka-actor"    % akkaV,
		"com.typesafe.akka" %% "akka-testkit"  % akkaV    % "test",
		"org.specs2"        %% "specs2-core"   % "2.3.11" % "test"
	)
}

libraryDependencies += "ca.hyperreal" %% "spray-cookiebaker" % "0.1"

libraryDependencies += "org.scalatra.scalate" %% "scalate-core" % "1.7.0"

libraryDependencies ++= Seq(
	"org.slf4j" % "slf4j-api" % "1.7.7",
	"org.slf4j" % "slf4j-simple" % "1.7.7"
	)

libraryDependencies ++= Seq(
	"org.webjars" % "bootstrap" % "3.3.5",
//	"org.webjars" % "font-awesome" % "4.2.0",
	"org.webjars" % "angularjs" % "1.4.3",
	"org.webjars" % "nervgh-angular-file-upload" % "1.1.5-1"
)

libraryDependencies ++= Seq(
	"com.typesafe.slick" %% "slick" % "3.0.1",
	"com.typesafe.slick" %% "slick-codegen" % "3.0.1",
	"com.h2database" % "h2" % "1.4.188",
	"joda-time" % "joda-time" % "2.7",
	"org.joda" % "joda-convert" % "1.7",
	"com.github.tototoshi" %% "slick-joda-mapper" % "2.0.0"
	)

//mainClass in assembly := Some( "ca.hyperreal.PACKAGE.Boot" )

jarName in assembly := name.value + "-" + version.value + ".jar"

Revolver.settings

//lazy val root = (project in file(".")).enablePlugins(SbtTwirl)


seq(sassSettings : _*)

(resourceGenerators in Compile) <+= (SassKeys.sass in Compile)

SassKeys.sassOutputStyle in (Compile, SassKeys.sass) := 'compressed

(compile in Compile) <<= compile in Compile dependsOn (SassKeys.sass in Compile)


seq(coffeeSettings: _*)

(resourceManaged in (Compile, CoffeeKeys.coffee)) <<= (crossTarget in Compile)(_ / "classes" / "public" / "js")

(CoffeeKeys.bare in (Compile, CoffeeKeys.coffee)) := true

publishMavenStyle := true

publishTo := Some( Resolver.sftp( "private", "hyperreal.ca", "/var/www/hyperreal.ca/maven2" ) )

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

licenses := Seq("MIT" -> url("http://opensource.org/licenses/MIT"))

homepage := Some(url("https://github.com/edadma/" + name.value))

pomExtra := (
  <scm>
    <url>git@github.com:edadma/{name.value}.git</url>
    <connection>scm:git:git@github.com:edadma/{name.value}.git</connection>
  </scm>
  <developers>
    <developer>
      <id>edadma</id>
      <name>Edward A. Maxedon, Sr.</name>
      <url>https://github.com/edadma</url>
    </developer>
  </developers>)
