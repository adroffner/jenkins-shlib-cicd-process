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
import groovy.json.JsonSlurperClassic

@NonCPS
def jsonParse(def json) {
    new groovy.json.JsonSlurperClassic().parseText(json)
}

def swaggerHost() {
  return "http://micro.web.att.com:8045"
}

def postRestAPI(url, inputPayload) {
  transient post = new URL(url).openConnection();
  inputPayload = JsonOutput.toJson(inputPayload)
  println("Posting to host: ${url}")
  println(inputPayload)
  post.setRequestMethod("POST")
  post.setDoOutput(true)
  post.setRequestProperty("Content-Type", "application/json")
  post.getOutputStream().write(inputPayload.getBytes("UTF-8"));
  def statusCode = post.getResponseCode();
  if(statusCode.equals(200)) {
    response = jsonParse(post.getInputStream().text)
    return [statusCode, response]
  } else {
    currentBuild.result = "UNSTABLE"
    response = jsonParse(post.getErrorStream().text)
    return [statusCode, response]
  }
}

def call(String serverName) {
  def swaggerAPIHost = swaggerHost() + "/swagger/createWikiEntry"

  (status, response) = postRestAPI(swaggerAPIHost, [current_server_name: serverName])
  

  if (status == 200 ) {
    println('Swagger publish successful')
  } else {
    println('Swagger publish step encountered an error.')
  }
  println(response)
}
