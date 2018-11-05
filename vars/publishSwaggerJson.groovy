/* 
  Module accepting a server name that will post to the Swagger API that will publish swagger documentation and API gateway onloading based on the contents of the JSON
*/
import groovy.json.JsonOutput

def call(String serverName, String swaggerHost="http://127.0.0.1:8045") {
  swaggerHost = "${swaggerHost}/swagger/createWikiEntry"
  def post = new URL(swaggerHost).openConnection();
    message = [current_server_name: serverName]
    message = JsonOutput.toJson(message)
    println("Swagger host: ${swaggerHost}")
    println(message)
    post.setRequestMethod("POST")
    post.setDoOutput(true)
    post.setRequestProperty("Content-Type", "application/json")
    post.getOutputStream().write(message.getBytes("UTF-8"));
    def statusCode = post.getResponseCode();
    println(statusCode);
    if(statusCode.equals(200)) {
      println('Swagger publish successful')
      println(post.getInputStream().text)
  }
      println('Swagger publish step encountered an error.')
      println(post.getErrorStream().text;
}
