def call(String imageName) {
  def fullImageName = buildDockerImage.fullImageName(imageName)
  script {
      sh """
        if [ ! -d "${env.WORKSPACE}/documentation" ]; then
          mkdir ${env.WORKSPACE}/documentation
        fi
      """


      def initFile = findFiles(glob: "**${File.separator}__init__.py")
      initFilePath = "${initFile[0].path}"
      println(initFilePath)
      def projectName = new File(initFilePath).parent
      println(projectName)

    	sh """ 
        chmod 777 ${env.WORKSPACE}/documentation \\
        && sudo docker run --rm \\
        --user="www-data" \\
        --entrypoint="/home/bin/build_sphinx.sh" \\
        --volume="${env.WORKSPACE}/documentation:/home/app/greta_api/documentation/" ${fullImageName} "Purple" \\
        && echo "After sudo docker run" \\
        && pwd \\
        && ls \\
        && ls ${env.WORKSPACE}/documentation
      """
  }
}