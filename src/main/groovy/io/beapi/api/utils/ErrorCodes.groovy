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

/**
 * Class is used as holder for all error messages (except custom messages) so that we can standardize on all error messages
 *
 * This needs to be expanded for I18N
 *
 * @author Owen Rubel
 */
public final class ErrorCodes{

	/**
	 * Static error codes for standardization of error messages.
	 @return LinkedHashMap representing the error messages and their keys
	 */
	public static LinkedHashMap<String,LinkedHashMap> en = [
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
		'423':['short':'Account Locked','long':'Account is currently locked. Please check your email for a verification code to unlock'],
		// Server Error
		'500':['short':'Internal Server Error','long':''],
		'501':['short':'Not Implemented','long':'The server either does not recognize the request method, or it lacks the ability to fulfill the request'],
		'502':['short':'Bad Gateway','long':'The server was acting as a gateway or proxy and received an invalid response from the upstream server'],
		'503':['short':'Service Unavailable','long':'The server is currently unavailable'],
		'504':['short':'Gateway Timeout','long':'The server was acting as a gateway or proxy and did not receive a timely response from the upstream server'],
		'505':['short':'HTTP Version Not Supported','long':'The server does not support the HTTP protocol version used in the request']
	]

	public static LinkedHashMap<String,LinkedHashMap> zh = [
			// Informational
			'100':['short':'继续','long':'服务器已收到请求标头，客户端应继续发送请求正文'],
			'101':['short':'切换协议','long':'请求者要求服务器切换协议'],
			// Success
			'200':['short':'好的/成功','long':'请求成功'],
			'201':['short':'创建/成功','long':'请求已完成，新资源已创建'],
			'202':['short':'已接受/成功','long':'请求已被接受处理，但处理尚未完成'],
			'203':['short':'非权威信息/成功','long':'请求已成功处理，但返回的信息可能来自其他来源'],
			'204':['short':'没有内容/成功','long':'请求已成功处理，但未返回任何内容'],
			'205':['short':'重置内容/成​​功','long':'请求已成功处理，但未返回任何内容，需要请求者重置文档视图'],
			'206':['short':'部分内容/成功','long':'由于客户端发送了范围标头，服务器仅传送部分资源'],
			// Redirect
			'300':['short':'多重选择/重定向','long':'用户可以选择一个链接并转到该位置'],
			'301':['short':'永久移动/重定向','long':'请求的页面已移动到新的 URL '],
			'302':['short':'找到/重定向','long':'请求的页面已暂时移动到新的 URL '],
			'303':['short':'查看其他/重定向','long':'可以在不同的 URL 下找到请求的页面'],
			'304':['short':'未修改/重定向','long':'表示自上次请求以来所请求的页面没有被修改过'],
			'307':['short':'临时重定向/重定向','long':'请求的页面已暂时移动到新的 URL'],
			// Client Error
			'400':['short':'错误的请求','long':'由于语法错误，无法满足请求'],
			'401':['short':'未经授权','long':'身份验证失败或令牌超时。请尝试重新登录'],
			'403':['short':'禁止','long':'该请求是合法请求，但服务器拒绝响应它'],
			'404':['short':'未找到','long':'无法找到请求的页面，但将来可能会再次可用'],
			'405':['short':'不允许的方法','long':'使用该页面不支持的请求方法向页面发出请求'],
			'406':['short':'不可接受','long':'服务器只能生成不被客户端接受的响应'],
			'407':['short':'需要代理身份验证','long':'客户端必须首先向代理验证自己的身份'],
			'408':['short':'请求超时','long':'服务器等待请求超时'],
			'409':['short':'冲突','long':'由于请求中存在冲突，请求无法完成'],
			'410':['short':'走了','long':'请求的页面不再可用'],
			'411':['short':'所需长度','long':'“内容长度”未定义。如果没有它，服务器将不会接受请求 '],
			'412':['short':'前提条件失败','long':'请求中给出的前提条件被服务器评估为 false'],
			'413':['short':'请求实体太大','long':'服务器不会接受该请求，因为请求实体太大'],
			'414':['short':'请求 URI 太长','long':'服务器不会接受请求，因为 URL 太长'],
			'415':['short':'不支持的媒体类型','long':'服务器不会接受请求，因为媒体类型不受支持'],
			'416':['short':'请求的范围无法满足','long':'客户端已请求文件的一部分，但服务器无法提供该部分'],
			'417':['short':'期望落空','long':'服务器无法满足Expect请求头字段的要求'],
			'422':['short':'无法处理的实体','long':'服务器理解内容类型和语法，但无法处理包含的指令'],
			'423':['short':'帐户被锁定','long':'帐户目前已被锁定。请检查您的电子邮件以获取验证码以解锁'],
			// Server Error
			'500':['short':'内部服务器错误','long':'内部服务器错误'],
			'501':['short':'未实施','long':'服务器无法识别请求方法，或者缺乏满足请求的能力'],
			'502':['short':'坏网关','long':'服务器充当网关或代理并从上游服务器收到无效响应'],
			'503':['short':'暂停服务','long':'服务器当前不可用'],
			'504':['short':'网关超时','long':'服务器充当网关或代理，没有收到上游服务器的及时响应'],
			'505':['short':'不支持 HTTP 版本','long':'服务器不支持请求中使用的HTTP协议版本']
	]

	public static LinkedHashMap<String,LinkedHashMap> es = [
			// Informational
			'100':['short':'Continuar','long':'El servidor ha recibido los encabezados de la solicitud y el cliente debe proceder a enviar el cuerpo de la solicitud.'],
			'101':['short':'Protocolos de conmutación','long':'El solicitante ha pedido al servidor que cambie de protocolo.'],
			// Success
			'200':['short':'OK / exitoso','long':'Solicitud exitosa'],
			'201':['short':'Creado / exitoso','long':'La solicitud se ha cumplido y se ha creado un nuevo recurso.'],
			'202':['short':'Aceptado / exitoso','long':'La solicitud ha sido aceptada para su procesamiento, pero el procesamiento no se ha completado'],
			'203':['short':'Información no autorizada/Exitosa','long':'La solicitud se procesó correctamente, pero devuelve información que puede provenir de otra fuente.'],
			'204':['short':'Sin contenido/Exitoso','long':'La solicitud se procesó correctamente, pero no devuelve ningún contenido.'],
			'205':['short':'Restablecer contenido/exitoso','long':'La solicitud se procesó correctamente, pero no devuelve ningún contenido y requiere que el solicitante restablezca la vista del documento.'],
			'206':['short':'Contenido parcial/Exitoso','long':'El servidor entrega solo una parte del recurso debido a un encabezado de rango enviado por el cliente'],
			// Redirect
			'300':['short':'Múltiples opciones/Redirección','long':'El usuario puede seleccionar un enlace e ir a esa ubicación.'],
			'301':['short':'Movido permanentemente/Redirección','long':'La página solicitada se ha movido a una nueva URL. '],
			'302':['short':'Encontrado / Redirección','long':'La página solicitada se ha movido temporalmente a una nueva URL. '],
			'303':['short':'Ver Otros/Redirección','long':'La página solicitada se puede encontrar en una URL diferente.'],
			'304':['short':'No modificado / redirección','long':'Indica que la página solicitada no ha sido modificada desde la última vez que se solicitó.'],
			'307':['short':'Redirección temporal/Redirección','long':'La página solicitada se ha movido temporalmente a una nueva URL.'],
			// Client Error
			'400': ['short': 'Solicitud Incorrecta', 'long': 'La solicitud no se puede cumplir debido a una sintaxis incorrecta'],
			'401': ['short': 'No Autorizado', 'long': 'La autenticación falló o el token expiró. Intente iniciar sesión nuevamente'],
			'403': ['short': 'Prohibido', 'long': 'La solicitud era legal, pero el servidor se niega a responder'],
			'404': ['short': 'No Encontrado', 'long': 'La página solicitada no se pudo encontrar, pero puede estar disponible en el futuro'],
			'405': ['short': 'Método No Permitido', 'long': 'Se realizó una solicitud con un método no compatible por la página'],
			'406': ['short': 'No Aceptable', 'long': 'El servidor solo puede generar una respuesta no aceptada por el cliente'],
			'407': ['short': 'Autenticación de Proxy Requerida', 'long': 'El cliente debe autenticarse primero con el proxy'],
			'408': ['short': 'Tiempo de Espera Agotado', 'long': 'El servidor agotó el tiempo de espera para la solicitud'],
			'409': ['short': 'Conflicto', 'long': 'La solicitud no se pudo completar debido a un conflicto en la solicitud'],
			'410': ['short': 'Ya No Disponible', 'long': 'La página solicitada ya no está disponible'],
			'411': ['short': 'Longitud Requerida', 'long': 'El "Content-Length" no está definido. El servidor no aceptará la solicitud sin él'],
			'412': ['short': 'Fallo en la Precondición', 'long': 'La precondición dada en la solicitud fue evaluada como falsa por el servidor'],
			'413': ['short': 'Entidad de Solicitud Demasiado Grande', 'long': 'El servidor no aceptará la solicitud porque la entidad es demasiado grande'],
			'414': ['short': 'URI de Solicitud Demasiado Larga', 'long': 'El servidor no aceptará la solicitud porque la URL es demasiado larga'],
			'415': ['short': 'Tipo de Medio No Compatible', 'long': 'El servidor no aceptará la solicitud porque el tipo de medio no es compatible'],
			'416': ['short': 'Rango Solicitado No Satisfactorio', 'long': 'El cliente ha solicitado una porción del archivo, pero el servidor no puede proporcionarla'],
			'417': ['short': 'Expectativa Fallida', 'long': 'El servidor no puede cumplir con los requisitos del encabezado de solicitud Expect'],
			'422': ['short': 'Entidad No Procesable', 'long': 'El servidor entiende el tipo de contenido y la sintaxis, pero no pudo procesar las instrucciones contenidas'],
			'423': ['short': 'Cuenta Bloqueada', 'long': 'La cuenta está actualmente bloqueada. Verifique su correo electrónico para un código de verificación y desbloquearla'],
			// Server Error
			'500': ['short': 'Error Interno del Servidor', 'long': ''],
			'501': ['short': 'No Implementado', 'long': 'El servidor no reconoce el método de solicitud o no tiene la capacidad para cumplirla'],
			'502': ['short': 'Puerta de Enlace Incorrecta', 'long': 'El servidor actuaba como una puerta de enlace o proxy y recibió una respuesta inválida del servidor aguas arriba'],
			'503': ['short': 'Servicio No Disponible', 'long': 'El servidor no está disponible actualmente'],
			'504': ['short': 'Tiempo de Espera de la Puerta de Enlace Agotado', 'long': 'El servidor actuaba como una puerta de enlace o proxy y no recibió una respuesta a tiempo del servidor aguas arriba'],
			'505': ['short': 'Versión HTTP No Compatible', 'long': 'El servidor no admite la versión del protocolo HTTP utilizada en la solicitud']
	]

	public static LinkedHashMap<String,LinkedHashMap> fr = [
			// Informational
			'100': ['short': 'Continuer', 'long': 'Le serveur a reçu les en-têtes de la requête et le client doit continuer à envoyer le corps de la requête'],
			'101': ['short': 'Changement de Protocole', 'long': 'Le client a demandé au serveur de changer de protocole'],

			// Success
			'200': ['short': 'OK/Réussi', 'long': 'La requête a réussi'],
			'201': ['short': 'Créé/Réussi', 'long': 'La requête a été satisfaite et une nouvelle ressource a été créée'],
			'202': ['short': 'Accepté/Réussi', 'long': 'La requête a été acceptée pour traitement, mais celui-ci n’est pas encore terminé'],
			'203': ['short': 'Information Non Authoritative/Réussi', 'long': 'La requête a été traitée avec succès, mais renvoie des informations pouvant provenir d’une autre source'],
			'204': ['short': 'Aucun Contenu/Réussi', 'long': 'La requête a été traitée avec succès, mais ne retourne aucun contenu'],
			'205': ['short': 'Réinitialiser le Contenu/Réussi', 'long': 'La requête a été traitée avec succès, ne retourne aucun contenu et nécessite que le client réinitialise l’affichage du document'],
			'206': ['short': 'Contenu Partiel/Réussi', 'long': 'Le serveur ne délivre qu’une partie de la ressource en raison d’un en-tête de plage envoyé par le client'],

			// Redirect
			'300': ['short': 'Choix Multiples/Redirection', 'long': 'L’utilisateur peut sélectionner un lien et se rendre à cet emplacement.'],
			'301': ['short': 'Déplacé de Façon Permanente/Redirection', 'long': 'La page demandée a été déplacée vers une nouvelle URL.'],
			'302': ['short': 'Trouvé/Redirection', 'long': 'La page demandée a été déplacée temporairement vers une nouvelle URL.'],
			'303': ['short': 'Voir Autre/Redirection', 'long': 'La page demandée peut être trouvée sous une autre URL.'],
			'304': ['short': 'Non Modifié/Redirection', 'long': 'Indique que la page demandée n’a pas été modifiée depuis la dernière requête.'],
			'307': ['short': 'Redirection Temporaire/Redirection', 'long': 'La page demandée a été déplacée temporairement vers une nouvelle URL.'],
			// Client Error
			'400': ['short': 'Mauvaise Requête', 'long': 'La requête ne peut pas être traitée en raison d’une syntaxe incorrecte'],
			'401': ['short': 'Non Autorisé', 'long': 'L’authentification a échoué ou le jeton a expiré. Veuillez essayer de vous reconnecter'],
			'403': ['short': 'Interdit', 'long': 'La requête est valide, mais le serveur refuse d’y répondre'],
			'404': ['short': 'Non Trouvé', 'long': 'La page demandée est introuvable, mais elle pourrait être disponible ultérieurement'],
			'405': ['short': 'Méthode Non Autorisée', 'long': 'Une requête a été effectuée avec une méthode non prise en charge par cette page'],
			'406': ['short': 'Non Acceptable', 'long': 'Le serveur ne peut générer qu’une réponse non acceptée par le client'],
			'407': ['short': 'Authentification Proxy Requise', 'long': 'Le client doit d’abord s’authentifier auprès du proxy'],
			'408': ['short': 'Délai d’Attente de la Requête Expiré', 'long': 'Le serveur a dépassé le délai d’attente pour la requête'],
			'409': ['short': 'Conflit', 'long': 'La requête n’a pas pu être complétée en raison d’un conflit'],
			'410': ['short': 'Disparu', 'long': 'La page demandée n’est plus disponible'],
			'411': ['short': 'Longueur Requise', 'long': 'Le champ "Content-Length" n’est pas défini. Le serveur n’acceptera pas la requête sans celui-ci'],
			'412': ['short': 'Échec de la Précondition', 'long': 'La précondition fournie dans la requête a été évaluée comme fausse par le serveur'],
			'413': ['short': 'Entité de Requête Trop Grande', 'long': 'Le serveur ne peut pas accepter la requête, car l’entité de la requête est trop grande'],
			'414': ['short': 'URI de Requête Trop Longue', 'long': 'Le serveur ne peut pas accepter la requête, car l’URL est trop longue'],
			'415': ['short': 'Type de Média Non Pris en Charge', 'long': 'Le serveur ne peut pas accepter la requête, car le type de média n’est pas pris en charge'],
			'416': ['short': 'Plage Demandée Non Satisfaisante', 'long': 'Le client a demandé une portion du fichier, mais le serveur ne peut pas fournir cette portion'],
			'417': ['short': 'Échec de l’Expectation', 'long': 'Le serveur ne peut pas répondre aux exigences de l’en-tête Expect de la requête'],
			'422': ['short': 'Entité Non Traitée', 'long': 'Le serveur comprend le type de contenu et la syntaxe, mais n’a pas pu traiter les instructions contenues'],
			'423': ['short': 'Compte Verrouillé', 'long': 'Le compte est actuellement verrouillé. Veuillez vérifier votre e-mail pour obtenir un code de vérification afin de le déverrouiller'],
			// Server Error
			'500': ['short': 'Erreur Interne du Serveur', 'long': ''],
			'501': ['short': 'Non Implémenté', 'long': 'Le serveur ne reconnaît pas la méthode de requête ou n’a pas la capacité de satisfaire la requête'],
			'502': ['short': 'Mauvaise Passerelle', 'long': 'Le serveur agissait en tant que passerelle ou proxy et a reçu une réponse invalide du serveur en amont'],
			'503': ['short': 'Service Indisponible', 'long': 'Le serveur est actuellement indisponible'],
			'504': ['short': 'Délai d’Attente de la Passerelle Expiré', 'long': 'Le serveur agissait en tant que passerelle ou proxy et n’a pas reçu de réponse à temps du serveur en amont'],
			'505': ['short': 'Version HTTP Non Prise en Charge', 'long': 'Le serveur ne prend pas en charge la version du protocole HTTP utilisée dans la requête']
	]

	public static LinkedHashMap<String,LinkedHashMap> de = [
			// Informational
			'100': ['short': 'Weiter', 'long': 'Der Server hat die Anfrage-Header erhalten, und der Client sollte fortfahren, den Anfrage-Body zu senden'],
			'101': ['short': 'Protokollwechsel', 'long': 'Der Anforderer hat den Server gebeten, das Protokoll zu wechseln'],
			// Success
			'200': ['short': 'OK/Erfolgreich', 'long': 'Die Anfrage war erfolgreich'],
			'201': ['short': 'Erstellt/Erfolgreich', 'long': 'Die Anfrage wurde erfüllt, und eine neue Ressource wurde erstellt'],
			'202': ['short': 'Akzeptiert/Erfolgreich', 'long': 'Die Anfrage wurde zur Verarbeitung angenommen, aber die Verarbeitung ist noch nicht abgeschlossen'],
			'203': ['short': 'Nicht maßgebliche Information/Erfolgreich', 'long': 'Die Anfrage wurde erfolgreich verarbeitet, aber die zurückgegebenen Informationen stammen möglicherweise aus einer anderen Quelle'],
			'204': ['short': 'Kein Inhalt/Erfolgreich', 'long': 'Die Anfrage wurde erfolgreich verarbeitet, gibt aber keinen Inhalt zurück'],
			'205': ['short': 'Inhalt zurücksetzen/Erfolgreich', 'long': 'Die Anfrage wurde erfolgreich verarbeitet, gibt aber keinen Inhalt zurück und erfordert, dass der Anforderer die Dokumentansicht zurücksetzt'],
			'206': ['short': 'Teilinhalt/Erfolgreich', 'long': 'Der Server liefert nur einen Teil der Ressource aufgrund eines Bereich-Headers, der vom Client gesendet wurde'],
			// Redirect
			'300': ['short': 'Mehrere Auswahlmöglichkeiten/Umleitung', 'long': 'Der Benutzer kann einen Link auswählen und zu diesem Ort wechseln'],
			'301': ['short': 'Dauerhaft verschoben/Umleitung', 'long': 'Die angeforderte Seite wurde auf eine neue URL verschoben'],
			'302': ['short': 'Gefunden/Umleitung', 'long': 'Die angeforderte Seite wurde vorübergehend auf eine neue URL verschoben'],
			'303': ['short': 'Siehe andere/Umleitung', 'long': 'Die angeforderte Seite ist unter einer anderen URL zu finden'],
			'304': ['short': 'Nicht geändert/Umleitung', 'long': 'Gibt an, dass die angeforderte Seite seit der letzten Anfrage nicht geändert wurde'],
			'307': ['short': 'Temporäre Umleitung/Umleitung', 'long': 'Die angeforderte Seite wurde vorübergehend auf eine neue URL verschoben'],
			// Client Error
			"400": ["short": "Fehlerhafte Anfrage", "long": "Die Anfrage kann aufgrund fehlerhafter Syntax nicht erfüllt werden"],
			"401": ["short": "Nicht autorisiert", "long": "Entweder ist die Authentifizierung fehlgeschlagen oder das Token ist abgelaufen. Bitte melden Sie sich erneut an"],
			"403": ["short": "Verboten", "long": "Die Anfrage war rechtlich zulässig, aber der Server verweigert die Antwort"],
			"404": ["short": "Nicht gefunden", "long": "Die angeforderte Seite konnte nicht gefunden werden, ist aber möglicherweise in der Zukunft wieder verfügbar"],
			"405": ["short": "Methode nicht erlaubt", "long": "Eine Anfrage wurde mit einer Methode gestellt, die von dieser Seite nicht unterstützt wird"],
			"406": ["short": "Nicht akzeptabel", "long": "Der Server kann nur eine Antwort generieren, die vom Client nicht akzeptiert wird"],
			"407": ["short": "Proxy-Authentifizierung erforderlich", "long": "Der Client muss sich zuerst beim Proxy authentifizieren"],
			"408": ["short": "Zeitüberschreitung der Anfrage", "long": "Der Server hat zu lange auf die Anfrage gewartet"],
			"409": ["short": "Konflikt", "long": "Die Anfrage konnte aufgrund eines Konflikts in der Anfrage nicht abgeschlossen werden"],
			"410": ["short": "Nicht mehr verfügbar", "long": "Die angeforderte Seite ist nicht mehr verfügbar"],
			"411": ["short": "Länge erforderlich", "long": "Die \"Content-Length\" ist nicht definiert. Der Server akzeptiert die Anfrage ohne sie nicht"],
			"412": ["short": "Vorbedingung fehlgeschlagen", "long": "Die in der Anfrage gegebene Vorbedingung wurde vom Server als falsch bewertet"],
			"413": ["short": "Anfrageentität zu groß", "long": "Der Server akzeptiert die Anfrage nicht, da die Anfrageentität zu groß ist"],
			"414": ["short": "Anfrage-URI zu lang", "long": "Der Server akzeptiert die Anfrage nicht, da die URL zu lang ist"],
			"415": ["short": "Nicht unterstützter Medientyp", "long": "Der Server akzeptiert die Anfrage nicht, da der Medientyp nicht unterstützt wird"],
			"416": ["short": "Angeforderter Bereich nicht erfüllbar", "long": "Der Client hat einen Teil der Datei angefordert, aber der Server kann diesen Teil nicht bereitstellen"],
			"417": ["short": "Erwartung fehlgeschlagen", "long": "Der Server kann die Anforderungen des Expect-Request-Header-Felds nicht erfüllen"],
			"422": ["short": "Nicht verarbeitbare Entität", "long": "Der Server versteht den Inhaltstyp und die Syntax, konnte aber die enthaltenen Anweisungen nicht verarbeiten"],
			"423": ["short": "Konto gesperrt", "long": "Das Konto ist derzeit gesperrt. Bitte überprüfen Sie Ihre E-Mail auf einen Verifizierungscode zur Entsperrung"],
			// Server Error
			"500": ["short": "Interner Serverfehler", "long": ""],
			"501": ["short": "Nicht implementiert", "long": "Der Server erkennt entweder die Anfragemethode nicht oder er ist nicht in der Lage, die Anfrage zu erfüllen"],
			"502": ["short": "Fehlerhaftes Gateway", "long": "Der Server fungierte als Gateway oder Proxy und erhielt eine ungültige Antwort vom nachgelagerten Server"],
			"503": ["short": "Dienst nicht verfügbar", "long": "Der Server ist derzeit nicht verfügbar"],
			"504": ["short": "Gateway-Zeitüberschreitung", "long": "Der Server fungierte als Gateway oder Proxy und erhielt keine rechtzeitige Antwort vom nachgelagerten Server"],
			"505": ["short": "HTTP-Version nicht unterstützt", "long": "Der Server unterstützt die in der Anfrage verwendete HTTP-Protokollversion nicht"]
	]

	/**
	 * Static error codes for standardization of error messages.
	 @return LinkedHashMap representing the error messages and their keys
	 */
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
			'423':['short':'Account Locked','long':'Account is currently locked. Please check your email for a verification code to unlock'],
// Server Error
			'500':['short':'Internal Server Error','long':''],
			'501':['short':'Not Implemented','long':'The server either does not recognize the request method, or it lacks the ability to fulfill the request'],
			'502':['short':'Bad Gateway','long':'The server was acting as a gateway or proxy and received an invalid response from the upstream server'],
			'503':['short':'Service Unavailable','long':'The server is currently unavailable'],
			'504':['short':'Gateway Timeout','long':'The server was acting as a gateway or proxy and did not receive a timely response from the upstream server'],
			'505':['short':'HTTP Version Not Supported','long':'The server does not support the HTTP protocol version used in the request']
	]

}
