/*
  Module that dynamically finds the server_config.py file and returns the production URL of the API to publish documentation for.

  * Required Plugins:

  "Pipeline Utility Steps"

  Requires script approval for:
    method java.io.File getParent
    staticMethod org.codehaus.groovy.runtime.DefaultGroovyMethods getText java.io.File
    new java.io.File java.lang.String
    java.lang.String
    staticField java.io.File separator
*/


def call(serverConfig="server_config.py") {
  // try {
      println("inside find server name")
      // Read contents of serverConfig python file.
      server_config = findFiles(glob: "**${File.separator}${serverConfig}")
      print(server_config)
      server_config_path = "${server_config[0].path}"
      println("server config path")
      println(server_config_path)
      def projectName = new File(server_config_path).parent
      println("project name")
      println(projectName)
      baseDir = "${env.WORKSPACE}${File.separator}${projectName}"
      println("baseDir")
      println(baseDir)
      filePath = "${baseDir}${File.separator}${serverConfig}"
      println("printing file pat")
      println(filePath)
      

      // def server_config_contents = readFile "${filePath}"
      server_config_contents = new File(filePath).text
      println("server config contents")
      println(server_config_contents)

      // // Parse serverName (hostname and port) string from contents.
      def serverName = "${server_config_contents}" =~ /([^'"\s]*[.]web[.][^'"\s]*:(?!8100)\d+)/
      serverName = serverName[0][0] // assume first match is right
      println(serverName)
      return serverName
  // }
  // catch (Exception e) {
      // println(e.getMessage())
      currentBuild.result = "UNSTABLE"
      // throw e
  // }
}
