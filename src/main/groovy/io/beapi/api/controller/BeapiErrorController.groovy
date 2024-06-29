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
package io.beapi.api.controller

import io.beapi.api.service.PrincipleService

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.RequestDispatcher
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.stereotype.Controller
import org.springframework.boot.web.servlet.error.ErrorController
import io.beapi.api.utils.ErrorCodes
import org.springframework.web.servlet.HandlerMapping;
//import jakarta.servlet.RequestDispatcher;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@CrossOrigin
class BeapiErrorController implements ErrorController {


    @RequestMapping("/error")
    @ResponseBody
    public void error(HttpServletRequest request,  HttpServletResponse response) {
        //do something like logging
        println("### forwarded to errorController : ")


        println("URI:"+request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI))
        println("FORWARD URI:"+request.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI))
        println("INCLUDE URI:"+request.getAttribute(RequestDispatcher.INCLUDE_REQUEST_URI))

        println("exception : "+request.getAttribute(RequestDispatcher.ERROR_EXCEPTION))
        println("exception : "+request.getSession().getAttribute(RequestDispatcher.ERROR_EXCEPTION))


        println("exception type : "+request.getAttribute(RequestDispatcher.ERROR_EXCEPTION_TYPE))
        println("error_message : "+request.getAttribute(RequestDispatcher.ERROR_MESSAGE))
        println(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE))

        println("CONTEXT PATH:"+request.getAttribute(RequestDispatcher.FORWARD_CONTEXT_PATH))
        println(request.getAttribute(RequestDispatcher.FORWARD_PATH_INFO))



        final Exception exception = (Exception) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        println("### Exception : "+exception)

        //def servletCtx = this.ctx.getServletContext()
        //def rd = servletCtx?.getRequestDispatcher(newPath)

        //for (StackTraceElement ste : exception.getStackTrace()) {
        //    System.out.println(ste.getClassName());
        //}

        Object status = request.getSession().getAttribute(RequestDispatcher.ERROR_STATUS_CODE)
        println('status : '+status)
        if (status != null) {
            String statusCode = status.toString()
            String uri = request.getRequestURI()
            response.setStatus(Integer.valueOf(statusCode))
            String message = "{\"timestamp\":\""+System.currentTimeMillis()+"\",\"status\":\""+statusCode+"\",\"error\":\""+ErrorCodes.codes[statusCode]['short']+"\",\"message\": \""+ErrorCodes.codes[statusCode]['long']+"\",\"path\":\""+uri+"\"}"
            response.getWriter().write(message)
            //response.writer.flush()
        }
        //return
    }


}
