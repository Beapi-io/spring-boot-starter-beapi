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


import java.beans.BeanInfo
import java.beans.PropertyDescriptor
import java.beans.Introspector
import java.lang.reflect.InvocationTargetException
import groovy.transform.CompileStatic

/**
 *
 * Params Object. Used in conjunction with ParamsDescriptor in creating params object for use by ApiDescriptor
 * @author Owen Rubel
 *
 * @see ParamsDescriptor
 * @see ApiDescriptor
 *
 */
@CompileStatic
class ApiParams{

	ParamsDescriptor param
	
	private static final INSTANCE = new ApiParams()
	
	static getInstance(){ return INSTANCE }

	/**
	 * Empty Constructor
	 */
	private ApiParams() {}

	
	LinkedHashMap toObject(){
		LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>()
		BeanInfo info = Introspector.getBeanInfo(param.getClass())
		for (PropertyDescriptor descriptor : info.getPropertyDescriptors()) {
			try{
	            Object propertyValue = descriptor.getReadMethod().invoke(param)
	            if (propertyValue != null) {
					if(!['metaClass','class','errors','values'].contains(descriptor.getName())){
						result.put(descriptor.getName(),propertyValue)
					}
	            }
			}catch (final IllegalArgumentException e){
				throw new Exception("[ApiParams :: toObject] : IllegalArgumentException - full stack trace follows:",e)
			}catch (final IllegalAccessException e){
				throw new Exception("[ApiParams :: toObject] : IllegalAccessException - full stack trace follows:",e)
			}catch (final InvocationTargetException e){
				throw new Exception("[ApiParams :: toObject] : InvocationTargetException - full stack trace follows:",e)
			}
		}
		
		return result
	}

	/**
	 *
	 * @param data
	 * @return
	 */
	ApiParams setMockData(String data){
		this.param.mockData = data
		return this
	}
	
	ApiParams setDescription(String data){
		this.param.description = data
		return this
	}

	ApiParams setKey(String data){
		this.param.keyType = data
		return this
	}


	ApiParams hasParams(ParamsDescriptor[] values){
		this.param.values = values
		return this
	}

	ApiParams setReference(String data){
		this.param.idReferences = data
		return this
	}

	ApiParams setParam(String type,String name){
		this.param = new ParamsDescriptor(paramType:"${type}",name:"${name}")
		return this
	}
}
