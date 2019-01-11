def call(String imageName) {
  def fullImageName = buildDockerImage.fullImageName(imageName)
  script {
    	sh """rmdir ${env.WORKSPACE}/docs \\
        && mkdir ${env.WORKSPACE}/docs \\
&& chmod 777 ${env.WORKSPACE}/docs \\
&& sudo docker run --rm \\
	--user="www-data" \\
	--entrypoint="/home/bin/build_sphinx.sh" \\
	--volume="${env.WORKSPACE}/docs:/home/bin/app/greta_api/documentation/_build/html" ${fullImageName}
"""
  }
}