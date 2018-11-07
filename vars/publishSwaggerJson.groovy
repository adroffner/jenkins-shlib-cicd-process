/* 
  Module accepting a server name that will post to the Swagger API that will publish swagger documentation and API gateway onloading based on the contents of the JSON

  Requires script approval for:
    URL
    JsonOutput
    openConnection
    getInputStream
    getOuputStream
    getErrorStream
    setDoOutput
    setRequestProperty
    getResponseCode
    text
*/
import groovy.json.JsonOutput

def swaggerHost() {
  return "http://micro.dev.att.com:8045"
}


def postRestAPI(url, inputPayload) {
  def post = new URL(url).openConnection();
  inputPayload = JsonOutput.toJson(inputPayload)
  println("Posting to host: ${url}")
  println(inputPayload)
  post.setRequestMethod("POST")
  post.setDoOutput(true)
  post.setRequestProperty("Content-Type", "application/json")
  post.getOutputStream().write(inputPayload.getBytes("UTF-8"));
  return post
}



def call(String serverName) {
  swaggerAPIHost = swaggerHost() + "/swagger/createWikiEntry"

  post = postRestAPI(swaggerAPIHost, [current_server_name: serverName]])
  def statusCode = post.getResponseCode();
  println(statusCode);
  if(statusCode.equals(200)) {
    println('Swagger publish successful')
    println(post.getInputStream().text)
  }
    println('Swagger publish step encountered an error.')
    println(post.getErrorStream().text)
    currentBuild.result = "UNSTABLE"
}
