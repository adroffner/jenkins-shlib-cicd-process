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
      println(initFilePath)
      def projectFolder = new File(initFilePath).parent
      println(projectFolder)

    	sh """ 
        chmod 777 ${env.WORKSPACE}/documentation \\
        && sudo docker run --rm \\
        --user="www-data" \\
        --entrypoint="/home/bin/build_sphinx.sh" \\
        --volume="${env.WORKSPACE}/documentation:/home/app/greta_api/documentation/" ${fullImageName} "${projectFolder}"
      """
  }
}