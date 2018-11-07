/* 
  Module accepting a server name that will post to the Swagger API that will publish swagger documentation and API gateway onloading based on the contents of the JSON

  Requires script approval for:
    URL
    JsonOutput
    method java.net.URL openConnection
    method java.net.URLConnection getInputStream
    method java.net.URLConnection getOutputStream
    java.net.HttpURLConnection getErrorStream
    method java.net.URLConnection setDoOutput boolean
    method java.net.URLConnection setRequestProperty java.lang.String 
    method java.net.URLConnection getResponseCode
    method java.net.URLConnection getOutputStream.text
    method java.io.OutputStream write byte[]
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
  statusCode = post.getResponseCode();
  if(statusCode.equals(200)) {
    return [statusCode, post.getInputStream()]
  } else {
    currentBuild.result = "UNSTABLE"
    return [statusCode, post.getErrorStream()]
  }
}

def call(String serverName) {
  swaggerAPIHost = swaggerHost() + "/swagger/createWikiEntry"

  postResponse = postRestAPI(swaggerAPIHost, [current_server_name: serverName]])
  
  if(postResponse[0] == 200 ) {
    println('Swagger publish successful')
  } else {
    println('Swagger publish step encountered an error.')
  }
  println(postResponse[1].text)
}
