package com.msi.manning.network.data

import android.os.{Bundle, Handler, Message}
import android.util.Log

import com.msi.manning.network.Constants
import com.msi.manning.network.util.StringUtils

import org.apache.http.{HttpEntity, HttpException, HttpRequest,
                        HttpRequestInterceptor, HttpResponse, NameValuePair,
                        ProtocolVersion, StatusLine}
import org.apache.http.auth.{AuthScope, UsernamePasswordCredentials}
import org.apache.http.client.{HttpClient, ResponseHandler}
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.{HttpGet, HttpPost, HttpRequestBase}
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.message.{BasicHttpResponse, BasicNameValuePair}
import org.apache.http.protocol.{HTTP, HttpContext}

import java.io.{IOException, UnsupportedEncodingException}

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer

/**
 * Wrapper to help make HTTP requests easier - after all, we want to make it nice for the people.
 * 
 * TODO cookies TODO multi-part binary data
 * 
 * @author charliecollins
 * 
 */
class HTTPRequestHelper(responseHandler: ResponseHandler[String]) {
  import HTTPRequestHelper._  // companion object

  /**
   * Perform an HTTP GET operation.
   * 
   */
  def performGet(url: String, user: String, pass: String,
                 additionalHeaders: Map[String, String]) {
    performRequest(null, url, user, pass, additionalHeaders, null, GET_TYPE)
  }

  /**
   * Perform an HTTP POST operation with specified content type.
   * 
   */
  def performPost(contentType: String, url: String, user: String, pass: String,
                  additionalHeaders: Map[String, String],
                  params: Map[String, String]) {
    performRequest(contentType, url, user, pass, additionalHeaders, params, POST_TYPE)
  }

  /**
   * Perform an HTTP POST operation with a default conent-type of
   * "application/x-www-form-urlencoded."
   * 
   */
  def performPost(url: String, user: String, pass: String,
                  additionalHeaders: Map[String, String],
                  params: Map[String, String]) {
    performRequest(MIME_FORM_ENCODED, url, user, pass, additionalHeaders, params,
                   POST_TYPE)
  }

  /**
   * Private heavy lifting method that performs GET or POST with supplied url, user, pass, data,
   * and headers.
   * 
   * @param contentType
   * @param url
   * @param user
   * @param pass
   * @param headers
   * @param params
   * @param requestType
   */
  private def performRequest(contentType: String, url: String, user: String, pass: String,
                             headers: Map[String, String],
                             params: Map[String, String], requestType: Int) {

    Log.d(Constants.LOGTAG, " " + CLASSTAG + " making HTTP request to url - " + url)

    // establish HttpClient
    val client = new DefaultHttpClient()

    // add user and pass to client credentials if present
    if ((user != null) && (pass != null)) {
      Log.d(Constants.LOGTAG, " " + CLASSTAG +
            " user and pass present, adding credentials to request")
      client.getCredentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(user, pass));
    }

    // process headers using request interceptor
    val sendHeaders = new collection.mutable.HashMap[String, String]()
    if (headers != null && headers.size > 0) {
      sendHeaders ++= headers
    }
    if (requestType == POST_TYPE) {
      sendHeaders.put(CONTENT_TYPE, contentType)
    }
    if (sendHeaders.size > 0) {
      client addRequestInterceptor new HttpRequestInterceptor() {

        @throws(classOf[HttpException])
        @throws(classOf[IOException])
        def process(request: HttpRequest, context: HttpContext) {
          for (key <- sendHeaders.keySet) {
            if (!request.containsHeader(key)) {
               Log.d(Constants.LOGTAG, " " + CLASSTAG + " adding header: " + key + " | "
                                + sendHeaders(key))
               request.addHeader(key, sendHeaders(key))
            }
          }
        }
      }
    }

    // handle POST or GET request respectively
    if (requestType == POST_TYPE) {
      Log.d(Constants.LOGTAG, " " + CLASSTAG + " performRequest POST")
      val method = new HttpPost(url)

      // data - name/value params
      var nvps: ListBuffer[NameValuePair] = null
      if (params != null && params.size > 0) {
        nvps = new ListBuffer[NameValuePair]()
        for (key <- params.keySet) {
          Log.d(Constants.LOGTAG, " " + CLASSTAG + " adding param: " + key + " | "
               + params(key))
          nvps += new BasicNameValuePair(key, params(key))
        }
      }
      if (nvps != null) {
        try {
          method setEntity new UrlEncodedFormEntity(nvps, HTTP.UTF_8)
        } catch {
          case e: UnsupportedEncodingException =>
            Log.e(Constants.LOGTAG, " " + CLASSTAG, e)
        }
      }
      execute(client, method)
    } else if (requestType == GET_TYPE) {
      Log.d(Constants.LOGTAG, " " + CLASSTAG + " performRequest GET")
      val method = new HttpGet(url)
      execute(client, method)
    }
  }
    
  /**
   * Once the client and method are established, execute the request. 
   * 
   * @param client
   * @param method
   */
  private def execute(client: HttpClient, method: HttpRequestBase) {
    Log.d(Constants.LOGTAG, " " + CLASSTAG + " execute invoked")
        
    // create a response specifically for errors (in case)
    val errorResponse = 
      new BasicHttpResponse(new ProtocolVersion("HTTP_ERROR", 1, 1), 500, "ERROR")
        
    try {
      client.execute(method, responseHandler)
      Log.d(Constants.LOGTAG, " " + CLASSTAG + " request completed")
    } catch {
      case e: Exception =>
        Log.e(Constants.LOGTAG, " " + CLASSTAG, e)
        errorResponse setReasonPhrase e.getMessage
        try {
          this.responseHandler handleResponse errorResponse
        } catch {
          case ex: Exception =>
            Log.e(Constants.LOGTAG, " " + CLASSTAG, ex)
        }
    }
  }

}

object HTTPRequestHelper {
  private final val CLASSTAG = classOf[HTTPRequestHelper].getSimpleName

  private final val POST_TYPE = 1
  private final val GET_TYPE = 2
  private final val CONTENT_TYPE = "Content-Type"
  final val MIME_FORM_ENCODED = "application/x-www-form-urlencoded"
  final val MIME_TEXT_PLAIN = "text/plain"

  /**
   * Static utility method to create a default ResponseHandler that sends a Message to the passed
   * in Handler with the response as a String, after the request completes.
   * 
   * @param handler
   * @return
   */
  def getResponseHandlerInstance(handler: Handler): ResponseHandler[String] =
    new ResponseHandler[String]() {
      def handleResponse(response: HttpResponse): String = {
        val message = handler.obtainMessage
        val bundle = new Bundle()
        val status = response.getStatusLine();
        Log.d(Constants.LOGTAG, " " + CLASSTAG + " statusCode - " + status.getStatusCode)
        Log.d(Constants.LOGTAG, " " + CLASSTAG + " statusReasonPhrase - " + status.getReasonPhrase)
        val entity = response.getEntity
        var result: String = null
        if (entity != null) {
          try {
            result = StringUtils.inputStreamToString(entity.getContent)
            bundle.putString("RESPONSE", result)
            message setData bundle
            handler sendMessage message
          } catch {
            case e: IOException =>
             Log.e(Constants.LOGTAG, " " + CLASSTAG, e)
             bundle.putString("RESPONSE", "Error - " + e.getMessage());
             message setData bundle
             handler sendMessage message
          }
        } else {
          Log.w(Constants.LOGTAG, " " + CLASSTAG +
                " empty response entity, HTTP error occurred")
          bundle.putString("RESPONSE", "Error - " + response.getStatusLine.getReasonPhrase)
          message setData bundle
          handler sendMessage message
        }
        result
      }
    }

}
