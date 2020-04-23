
lazy val root = (project in file("."))
  .enablePlugins(RiffRaffArtifact)
  .settings(
    name := "invoicing-api",
    description := "Zuora Invoice management for supporters (refund, etc.)",
    version := "0.1.0",
    organization := "com.gu",
    organizationName := "The Guardian",
    scalaVersion := "2.13.1",
    libraryDependencies ++= List(
      "org.scalaj"      %% "scalaj-http"  % "2.4.2",
      "com.lihaoyi"     %% "upickle"      % "1.1.0",
      "org.scalameta"   %% "munit"        % "0.7.1"   % Test,
      "com.gu"          %% "spy"          % "0.1.1"   % "provided",
    ),
    assemblyJarName := "invoicing-api.jar",
    assemblyExcludedJars in assembly := minimiseScalaAwsLambdaPackageSize.value,
    unmanagedJars in Compile += file("lib/scala-min.jar"),
    riffRaffPackageType := assembly.value,
    riffRaffUploadArtifactBucket := Option("riffraff-artifact"),
    riffRaffUploadManifestBucket := Option("riffraff-builds"),
    riffRaffManifestProjectName := "support:invoicing-api",
    riffRaffArtifactResources += (file("cfn.yaml"), "cfn/cfn.yaml")
  )

def minimiseScalaAwsLambdaPackageSize = Def.task {
  val excludedDeps = List(
    "scala-reflect",
    "scala-compiler",
//    "scala-library",
  )
  (fullClasspath in assembly)
    .value
    .filter(dep => excludedDeps.exists(dep.data.getName.contains))
}

lazy val deployAwsLambda = taskKey[Unit]("Execute the shell script")

deployAwsLambda := {
  import scala.sys.process._
  "aws lambda update-function-code --function-name invoicing-api-refund-CODE --zip-file fileb://target/scala-2.13/invoicing-api.jar --profile membership --region eu-west-1" !
}

commands += Command.command("d") { state =>
  "assembly" :: "deployAwsLambda" :: state
}


