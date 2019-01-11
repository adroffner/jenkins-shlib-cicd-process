def call(String imageName) {
  def fullImageName = buildDockerImage.fullImageName(imageName)
  script {
      sh """
        if [ ! -d "${env.WORKSPACE}/documentation"]; then
          mkdir ${env.WORKSPACE}/documentation
        fi
      """

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