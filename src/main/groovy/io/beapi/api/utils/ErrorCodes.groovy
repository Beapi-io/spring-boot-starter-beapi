/*
 * Copyright 2013-2022 Owen Rubel
 * API Chaining(R) 2022 Owen Rubel
 *
 * Licensed under the AGPL v2 License;
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author Owen Rubel (orubel@gmail.com)
 *
 */
package io.beapi.api.utils

public final class ErrorCodes{

	public static LinkedHashMap<String,LinkedHashMap> codes = [
// Informational
			'100':['short':'Continue','long':'The server has received the request headers, and the client should proceed to send the request body'],
			'101':['short':'Switching Protocols','long':'The requester has asked the server to switch protocols'],
// Success
			'200':['short':'OK/Successful','long':'Request succeeded'],
			'201':['short':'Created/Successful','long':'The request has been fulfilled, and a new resource has been created '],
			'202':['short':'Accepted/Successful','long':'The request has been accepted for processing, but the processing has not been completed'],
			'203':['short':'Non-Authoritative Information/Successful','long':'The request has been successfully processed, but is returning information that may be from another source'],
			'204':['short':'No Content/Successful','long':'The request has been successfully processed, but is not returning any content'],
			'205':['short':'Reset Content/Successful','long':'The request has been successfully processed, but is not returning any content, and requires that the requester reset the document view'],
			'206':['short':'Partial Content/Successful','long':'The server is delivering only part of the resource due to a range header sent by the client'],
// Redirect
			'300':['short':'Multiple Choices/Redirection','long':'The user can select a link and go to that location.'],
			'301':['short':'Moved Permanently/Redirection','long':'The requested page has moved to a new URL '],
			'302':['short':'Found/Redirection','long':'The requested page has moved temporarily to a new URL '],
			'303':['short':'See Other/Redirection','long':'The requested page can be found under a different URL'],
			'304':['short':'Not Modified/Redirection','long':'Indicates the requested page has not been modified since last requested'],
			'307':['short':'Temporary Redirect/Redirection','long':'The requested page has moved temporarily to a new URL'],
// Client Error
			'400':['short':'Bad Request','long':'The request cannot be fulfilled due to bad syntax'],
			'401':['short':'Unauthorized','long':'Either authentication failed or token timed out. Please try logging in again'],
			'403':['short':'Forbidden','long':'The request was a legal request, but the server is refusing to respond to it'],
			'404':['short':'Not Found','long':'The requested page could not be found but may be available again in the future'],
			'405':['short':'Method Not Allowed','long':'A request was made of a page using a request method not supported by that page'],
			'406':['short':'Not Acceptable','long':'The server can only generate a response that is not accepted by the client'],
			'407':['short':'Proxy Authentication Required','long':'The client must first authenticate itself with the proxy'],
			'408':['short':'Request Timeout','long':'The server timed out waiting for the request'],
			'409':['short':'Conflict','long':'The request could not be completed because of a conflict in the request'],
			'410':['short':'Gone','long':'The requested page is no longer available'],
			'411':['short':'Length Required','long':'The "Content-Length" is not defined. The server will not accept the request without it '],
			'412':['short':'Precondition Failed','long':'The precondition given in the request evaluated to false by the server'],
			'413':['short':'Request Entity Too Large','long':'The server will not accept the request, because the request entity is too large'],
			'414':['short':'Request-URI Too Long','long':'The server will not accept the request, because the URL is too long'],
			'415':['short':'Unsupported Media Type','long':'The server will not accept the request, because the media type is not supported '],
			'416':['short':'Requested Range Not Satisfiable','long':'The client has asked for a portion of the file, but the server cannot supply that portion'],
			'417':['short':'Expectation Failed','long':'The server cannot meet the requirements of the Expect request-header field'],
			'422':['short':'Unprocessable Entity','long':'The server understands content-type and syntax but was unable to process contained instructions.'],
// Server Error
			'500':['short':'Internal Server Error','long':''],
			'501':['short':'Not Implemented','long':'The server either does not recognize the request method, or it lacks the ability to fulfill the request'],
			'502':['short':'Bad Gateway','long':'The server was acting as a gateway or proxy and received an invalid response from the upstream server'],
			'503':['short':'Service Unavailable','long':'The server is currently unavailable'],
			'504':['short':'Gateway Timeout','long':'The server was acting as a gateway or proxy and did not receive a timely response from the upstream server'],
			'505':['short':'HTTP Version Not Supported','long':'The server does not support the HTTP protocol version used in the request']
	]

}
