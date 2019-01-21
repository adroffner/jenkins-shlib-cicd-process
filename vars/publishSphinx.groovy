/*
  Module that dynamically resolves the project folder based on the __init__.py and publishes sphinx documentation to the hosting server.

  * Required Plugins:

  "Pipeline Utility Steps"

  Requires script approval for:
    method java.io.File getParent
    staticMethod org.codehaus.groovy.runtime.DefaultGroovyMethods getText java.io.File
    new java.io.File java.lang.String
    java.lang.String
    staticField java.io.File separator
*/

def call(String imageName) {
  def fullImageName = buildDockerImage.fullImageName(imageName)
  script {
      sh """
        if [ ! -d "${env.WORKSPACE}/documentation" ]; then
          mkdir ${env.WORKSPACE}/documentation
        fi
      """

      // Find the project directory!
      def initFile = findFiles(glob: "**${File.separator}__init__.py")
      initFilePath = "${initFile[0].path}"
      def projectFolder = new File(initFilePath).parent

    	sh """ 
        chmod 777 ${env.WORKSPACE}/documentation \\
        && sudo docker run --rm \\
        --user="www-data" \\
        --entrypoint="/home/bin/build_sphinx.sh" \\
        --volume="${env.WORKSPACE}/documentation:/home/app/greta_api/documentation/" ${fullImageName} "${projectFolder}"
      """
  }
}