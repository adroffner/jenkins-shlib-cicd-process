/*
  Module that dynamically finds the server_config.py file and returns the production URL of the API to publish documentation for.

  * Required Plugins:

  "Pipeline Utility Steps"

  Requires script approval for:
    method java.io.File getParent
    staticMethod org.codehaus.groovy.runtime.DefaultGroovyMethods getText java.io.File
    new java.io.File java.lang.String
    java.lang.String
*/

def call(serverConfig="server_config.py") {
  try {
      server_config = findFiles(glob: "**/${serverConfig}")
      server_config_path = "${server_config[0].path}"
      def projectName = new File(server_config_path).parent
      baseDir = "${env.WORKSPACE}/$projectName"
      server_config_contents = new File("${baseDir}/${serverConfig.py}").text
      def serverName = "${server_config_contents}"=~['"].*\.web\..*:\d+['"]/
      serverName = serverName[0].replaceAll("\"", "").replaceAll("'", "")
      println ("Server name found in server_config.py: ${serverName}")
      return serverName
  }
  catch (Exception) {
      currentBuild.result = "UNSTABLE"
  }
}