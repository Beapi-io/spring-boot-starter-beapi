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
 * Used in conjunction with ApiParams in created params holder for use by ApiDescriptor
 *
 * @author Owen Rubel
 * @see ApiParams
 * @see ApiDescriptor
 *
 */
class ParamsDescriptor{

	/**
	 * String holder for paramType (ie String, Integer, etc)
	 */
	String paramType

	/**
	 * String holder for keyType, if this param is read in as a key (OPTIONAL)
	 */
	String keyType

	/**
	 * String holder for param name
	 */
	String name

	/**
	 * String holder for idReference. If keyType is equal to 'FKEY'/'PKEY', this will contain the domain/entity that it references (OPTIONAL)
	 */
	String idReferences

	/**
	 * Brief description of param; used for apidocs
	 */
	String description = ""

	/**
	 * String representing Mockdata; used in testing
	 */
	String mockData

	/**
	 * list of all param values
	 */
	ParamsDescriptor[] values = []

	static constraints = { 
		paramType(nullable:false,maxSize:100,inList: ["STRING","DATE","LONG","BOOLEAN","FLOAT","BIGDECIMAL","MAP","LIST","COMPOSITE"])
		keyType(nullable:true,maxSize:100,inList: ["PRIMARY","FOREIGN","INDEX"])
		name(nullable:false,maxSize:100)
		idReferences(maxSize:100, validator: { val, obj ->
			if(keyType['FOREIGN','PRIMARY','INDEX'].contains(keyType)) {
			  return true
			}else {
			  return ['nullable']
			}
		})
		description(nullable:false,maxSize:1000)
		mockData(nullable:false)
		values(nullable:true)
	} 
}
