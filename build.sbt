val Http4sVersion          = "0.23.23"
val CirceVersion           = "0.14.5"
val MunitVersion           = "0.7.29"
val LogbackVersion         = "1.4.11"
val MunitCatsEffectVersion = "1.0.7"

lazy val root = (project in file("."))
  .settings(
    organization                     := "com.ghurtchu",
    name                             := "loadbalancer",
    scalaVersion                     := "3.3.0",
    libraryDependencies ++= Seq(
      "org.http4s"            %% "http4s-ember-server" % Http4sVersion,
      "org.http4s"            %% "http4s-ember-client" % Http4sVersion,
      "org.http4s"            %% "http4s-circe"        % Http4sVersion,
      "org.http4s"            %% "http4s-dsl"          % Http4sVersion,
      "io.circe"              %% "circe-generic"       % CirceVersion,
      "org.scalameta"         %% "munit"               % MunitVersion           % Test,
      "org.typelevel"         %% "munit-cats-effect-3" % MunitCatsEffectVersion % Test,
      "ch.qos.logback"         % "logback-classic"     % LogbackVersion         % Runtime,
      "com.github.pureconfig" %% "pureconfig-core"     % "0.17.4",
    ),
    assembly / assemblyMergeStrategy := {
      case "module-info.class" => MergeStrategy.discard
      case x                   => (assembly / assemblyMergeStrategy).value.apply(x)
    },
    assembly / mainClass             := Some("Main"),
    assembly / assemblyJarName       := "lb.jar",
  )
