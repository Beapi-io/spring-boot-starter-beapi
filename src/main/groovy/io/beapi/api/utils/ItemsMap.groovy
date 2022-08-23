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
 * @author https://www.onlinetutorialspoint.com/java/jaxb-map-to-xml-conversion-example.html
 *
 */
package io.beapi.api.utils

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.Map;

@XmlRootElement(name = "api")
@XmlAccessorType(XmlAccessType.FIELD)
public class ApiMap {

    private Map<String, ?> apiMap = new HashMap<>();

    public Map<String, ?> getApiMap() {
        return apiMap;
    }

    public void setItemsMap(Map<String, ?> ApiMap) {
        this.apiMap = apiMap;
    }
}