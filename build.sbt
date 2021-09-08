
lazy val scala212 = "2.12.14"
lazy val scala3   = "3.0.0"
lazy val supportedScalaVersions = List(
  scala212,
  scala3
)

val Java11 = "adopt@1.11"

lazy val shexsVersion          = "0.1.93"
lazy val srdfVersion           = "0.1.102"
lazy val utilsVersion          = "0.1.98"
lazy val documentVersion       = "0.0.32"

// Dependency versions
lazy val catsVersion           = "2.6.1"
lazy val catsEffectVersion     = "3.2.2"
lazy val circeVersion          = "0.14.1"
lazy val declineVersion        = "2.1.0"
lazy val fs2Version            = "3.0.4"
lazy val jenaVersion           = "4.1.0"
lazy val jacksonVersion        = "2.12.3"
// lazy val log4jVersion          = "2.14.1"
lazy val munitVersion          = "0.7.27"
lazy val munitEffectVersion    = "1.0.5"
lazy val slf4jVersion          = "1.7.31"
// lazy val pprintVersion         = "0.6.6"
// lazy val scalaCollCompatVersion  = "2.5.0"
lazy val wikidataToolkitVersion = "0.12.1"

// Dependency modules
lazy val catsCore          = "org.typelevel"              %% "cats-core"           % catsVersion
lazy val catsKernel        = "org.typelevel"              %% "cats-kernel"         % catsVersion
lazy val catsEffect        = "org.typelevel"              %% "cats-effect"         % catsEffectVersion
lazy val circeCore         = "io.circe"                   %% "circe-core"          % circeVersion
lazy val circeGeneric      = "io.circe"                   %% "circe-generic"       % circeVersion
lazy val circeParser       = "io.circe"                   %% "circe-parser"        % circeVersion
lazy val decline           = "com.monovore"               %% "decline"             % declineVersion
lazy val declineEffect     = "com.monovore"               %% "decline-effect"      % declineVersion
lazy val fs2               = "co.fs2"                     %% "fs2-core"            % fs2Version
lazy val fs2io             = "co.fs2"                     %% "fs2-io"              % fs2Version
lazy val jenaArq           = "org.apache.jena"            % "jena-arq"             % jenaVersion
lazy val jenaFuseki        = "org.apache.jena"            % "jena-fuseki-main"     % jenaVersion
lazy val jacksonScala      = ""                           % ""
lazy val munit             = "org.scalameta"              %% "munit"               % munitVersion
lazy val munitEffect       = "org.typelevel"              %% "munit-cats-effect-3" % munitEffectVersion
lazy val slf4j_api         = "org.slf4j"                   % "slf4j-api"           % slf4jVersion
lazy val slf4j_log4j12     = "org.slf4j"                   % "slf4j-log4j12"       % slf4jVersion

lazy val wdtk_dumpfiles   = "org.wikidata.wdtk" % "wdtk-dumpfiles"   % wikidataToolkitVersion
lazy val wdtk_wikibaseapi = "org.wikidata.wdtk" % "wdtk-wikibaseapi" % wikidataToolkitVersion
lazy val wdtk_datamodel   = "org.wikidata.wdtk" % "wdtk-datamodel"   % wikidataToolkitVersion
lazy val wdtk_rdf         = "org.wikidata.wdtk" % "wdtk-rdf"         % wikidataToolkitVersion
lazy val wdtk_storage     = "org.wikidata.wdtk" % "wdtk-storage"     % wikidataToolkitVersion
lazy val wdtk_util        = "org.wikidata.wdtk" % "wdtk-util"        % wikidataToolkitVersion

// lazy val scalaCollCompat   = "org.scala-lang.modules"     %% "scala-collection-compat" % scalaCollCompatVersion

// WESO components
lazy val document          = "es.weso"                    %% "document"        % documentVersion
lazy val srdf              = "es.weso"                    %% "srdf"            % srdfVersion
lazy val srdfJena          = "es.weso"                    %% "srdfjena"        % srdfVersion
lazy val srdf4j            = "es.weso"                    %% "srdf4j"          % srdfVersion
lazy val utils             = "es.weso"                    %% "utils"           % utilsVersion
lazy val shex              = "es.weso"                    %% "shex"            % shexsVersion

// lazy val pprint            = "com.lihaoyi"                %% "pprint"        % pprintVersion

lazy val MUnitFramework = new TestFramework("munit.Framework")

ThisBuild / githubWorkflowJavaVersions := Seq(Java11)

lazy val wdsubRoot = project
  .in(file("."))
  .enablePlugins(
    DockerPlugin,
    ScalaUnidocPlugin,
    SiteScaladocPlugin,
    AsciidoctorPlugin,
    SbtNativePackager,
    WindowsPlugin,
    JavaAppPackaging,
    LauncherJarPlugin
    )
    .enablePlugins(BuildInfoPlugin)
  .settings(
    commonSettings,
    packagingSettings,
//    wixSettings,
    universalSettings,
    dockerSettings
   )
  .aggregate(wdsub, docs)
  .dependsOn(wdsub)
  .settings(
    libraryDependencies ++= Seq(
      catsCore,
      catsKernel,
      catsEffect,
      decline,
      declineEffect,
      srdf,
      srdfJena, shex,
//      pprint,
    ),
    fork := true,
    ThisBuild / turbo := true,
    ThisBuild / crossScalaVersions := supportedScalaVersions,
//    Compile / run / mainClass := Some("es.weso.shexs.Main"),
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "buildinfo"
  )

lazy val wdsub = project
  .in(file("modules/wdsub"))
  .settings(
    crossScalaVersions := supportedScalaVersions,
    commonSettings
  )
  .dependsOn()
  .settings(
    libraryDependencies ++= Seq(
      circeCore,
      circeGeneric,
      circeParser,
      catsEffect,
//      pprint,
      fs2, fs2io,
      utils     % "test -> test; compile -> compile",
      srdf, srdfJena % Test,
      shex,
      wdtk_dumpfiles, 
      wdtk_wikibaseapi,
      slf4j_api, slf4j_log4j12
    ),
    testFrameworks += MUnitFramework
  )

lazy val docs = project
  .in(file("wdsub-docs"))
  .settings(
    noPublishSettings,
    mdocSettings,
    ScalaUnidoc / unidoc / unidocProjectFilter := inAnyProject -- inProjects(noDocProjects: _*)
   )
  .dependsOn(wdsub)
  .enablePlugins(MdocPlugin, DocusaurusPlugin, ScalaUnidocPlugin)

lazy val mdocSettings = Seq(
  mdocVariables := Map(
    "VERSION" -> version.value
  ),
  ScalaUnidoc / unidoc / unidocProjectFilter := inProjects(wdsub),
  ScalaUnidoc / unidoc / target := (LocalRootProject / baseDirectory).value / "website" / "static" / "api",
  cleanFiles += (ScalaUnidoc / unidoc / target).value,
  docusaurusCreateSite := docusaurusCreateSite
    .dependsOn(Compile / unidoc)
    .value,
  docusaurusPublishGhpages :=
    docusaurusPublishGhpages
      .dependsOn(Compile / unidoc)
      .value,
  ScalaUnidoc / unidoc / scalacOptions ++= Seq(
    "-doc-source-url", s"https://github.com/weso/wdsub/tree/v${(ThisBuild / version).value}€{FILE_PATH}.scala",
    "-sourcepath", (LocalRootProject / baseDirectory).value.getAbsolutePath,
    "-doc-title", "wdsub",
    "-doc-version", s"v${(ThisBuild / version).value}"
  )
)

lazy val noPublishSettings = publish / skip := true


/* ********************************************************
 ******************** Grouped Settings ********************
 **********************************************************/

lazy val noDocProjects = Seq[ProjectReference](
  )

lazy val sharedDependencies = Seq(
  libraryDependencies ++= Seq(
   munit % Test,
   munitEffect % Test
  ),
  testFrameworks += new TestFramework("munit.Framework")
)

lazy val packagingSettings = Seq(
  mainClass in Compile := Some("es.weso.wdsubmain.Main"),
  mainClass in assembly := Some("es.weso.wdsubmain.Main"),
  test in assembly := {},
  assemblyJarName in assembly := "wdsub.jar",
  packageSummary in Linux := name.value,
  packageSummary in Windows := name.value,
  packageDescription := name.value
)

lazy val compilationSettings = Seq(
  scalacOptions ++= Seq(
    "-deprecation",                      // Emit warning and location for usages of deprecated APIs.
    "-encoding", "utf-8",                // Specify character encoding used by source files.
    "-language:_",
    "-unchecked",                        // Enable additional warnings where generated code depends on assumptions.
    "-Ywarn-dead-code",                  // Warn when dead code is identified.
  ) ++ (if (priorTo2_13(scalaVersion.value))
  Seq(
    "-Yno-adapted-args",
    "-Xfuture"
  )
else
  Seq(
    "-Ymacro-annotations"
  ))

  // format: on
)

def priorTo2_13(scalaVersion: String): Boolean =
  CrossVersion.partialVersion(scalaVersion) match {
    case Some((2, minor)) if minor < 13 => true
    case _                              => false
  }

/*lazy val wixSettings = Seq(
  wixProductId := "39b564d5-d381-4282-ada9-87244c76e14b",
  wixProductUpgradeId := "6a710435-9af4-4adb-a597-98d3dd0bade1"
)*/

lazy val universalSettings = Seq(
  Universal / name := "wdsub",
  maintainer := "Jose Emilio Labra Gayo"
)


import com.typesafe.sbt.packager.docker.DockerChmodType

lazy val dockerSettings = Seq(
  dockerRepository := Some("wesogroup"), 
  Docker / packageName := "wdsub",
  dockerBaseImage := "openjdk:11",
  dockerAdditionalPermissions ++= Seq((DockerChmodType.UserGroupWriteExecute, "/tmp")),
  Docker / daemonUserUid := Some("0"),
  Docker / daemonUser    := "root"
//  dockerUsername := Some("wesogroup")
)


lazy val warnUnusedImport = Seq(
  scalacOptions in (Compile, console) ~= { _.filterNot(Set("-Ywarn-unused-import", "-Ywarn-unused:imports")) },
  scalacOptions in (Test, console) := (scalacOptions in (Compile, console)).value
)

lazy val commonSettings = compilationSettings ++ sharedDependencies ++ Seq(
  coverageHighlighting := priorTo2_13(scalaVersion.value),
  organization := "es.weso",
  sonatypeProfileName := ("es.weso"),
  homepage            := Some(url("https://github.com/weso/wdsub")),
  licenses            := Seq("MIT" -> url("http://opensource.org/licenses/MIT")),
  scmInfo             := Some(ScmInfo(url("https://github.com/weso/wdsub"), "scm:git:git@github.com:weso/wdsub.git")),
  autoAPIMappings     := true,
  apiURL              := Some(url("http://weso.github.io/wdsub/latest/api/")),
  autoAPIMappings     := true,
  developers := List(
    Developer(
      id="labra",
      name="Jose Emilio Labra Gayo",
      email="jelabra@gmail.com",
      url=url("https://weso.labra.es")
    ))
) ++ warnUnusedImport
