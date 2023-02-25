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

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.io.File
import groovy.text.GStringTemplateEngine

class BeapiCli {

	private LinkedHashMap scaffoldArg = [:]
	private LinkedHashMap domainArg = [:]

	public BeapiCli(Set<String> args) {
		parse(args)
	}

	private void parse(Set<String> args) {
		ArrayList validArgKeys = ['controller','connector','domain']
		ArrayList scaffoldKeys = ['controller','connector']
		ArrayList domainKey = ['domain']
		LinkedHashMap vars = [:]
		args.each(){
			ArrayList temp = it.split('=')
			if(validArgKeys.contains(temp[0])){
				if(temp[1] ==~ /[a-z][a-z0-9_]*(\.[a-z0-9_]+)+[0-9a-z_]/) {
					if(scaffoldKeys.contains(temp[0])){
						if(scaffoldArg==[:]) {
							scaffoldArg[temp[0]] = temp[1]
						}else{
							System.err << "Scaffold value for '"+scaffoldArg[0]+"' has already been set. Send ONLY ONE OF controller/connector with a domain setting."
							System.exit 1
						}
					}
					if(domainKey.contains(temp[0])){
						if(domainArg==[:]){
							domainArg[temp[0]] = temp[1]
						}else{
							System.err << "Domain sent twice. Send only domain setting only once."
							System.exit 1
						}
					}
					println("${scaffoldArg[0]} = ${scaffoldArg[1]}")
					println("${domainArg[0]} = ${domainArg[1]}")
				}else{
					System.err << "Invalid package name. Package name for '"+temp[0]+"' is not recognized as a valid package name"
					System.exit 1
				}
			}else{
				System.err << "Invalid ARG sent. Please provide ARG values of \'controller/connector\' and \'domain\'."
				System.exit 1
			}
		}

		if(domainArg==[:]){
			System.err << "No valid domain value sent. Please try again."
			System.exit 1
		}

		if(domainArg==[:]){
			System.err << "No valid scaffold value sent (ie controller/connector). Please try again."
			System.exit 1
		}
	}

	// NOTE : This has to be called separately in the 'runner'
	public scaffold(){
		switch(scaffoldArg[0].toLowerCase()){
			case 'controller':
				createController()
				break;
			case 'connector':
				createConnector()
				break;
			default:
				System.err << "Unrecognized arg. Please try again."
				System.exit 1
		}
	}

	private void createController(){
		// look for 'entity'' first and try to get match
		entityManager.getMetamodel().getEntities();

		// next make sure controller does not exist

	}

	private void createDomain(){

	}

	private void createConnector(){

	}

	private void checkDirectory(contDir) {
		def ant = new AntBuilder()
		def cfile = new File(contDir)
		if (!cfile.exists()) {
			ant.mkdir(dir: contDir)
		}
		return
	}

	private boolean fileExists(String path){
		def cfile = new File(path)
		if (cfile.exists()) {
			return true
		}
		return false
	}

	private LinkedHashMap hibernateTypeConverter(String type){
		switch(type){
			case 'class org.hibernate.type.CharacterType':
				return ['Character':'java.lang.Character']
				break
			case 'class org.hibernate.type.NumericBooleanType':
			case 'class org.hibernate.type.YesNoType':
			case 'class org.hibernate.type.TrueFalseType':
			case 'class org.hibernate.type.BooleanType':
				return ['Boolean':'java.lang.Boolean']
				break
			case 'class org.hibernate.type.ByteType':
				return ['Byte':'java.lang.Byte']
				break
			case 'class org.hibernate.type.ShortType':
				return ['Short':'java.lang.Short']
				break
			case 'class org.hibernate.type.IntegerTypes':
				return ['Integer':'java.lang.Integer']
				break
			case 'class org.hibernate.type.LongType':
				return ['Long':'java.lang.Long']
				break
			case 'class org.hibernate.type.FloatType':
				return ['Float':'java.lang.Float']
				break
			case 'class org.hibernate.type.DoubleType':
				return ['Double':'java.lang.Double']
				break
			case 'class org.hibernate.type.BigIntegerType':
				return ['BigInteger':'java.math.BigInteger']
				break
			case 'class org.hibernate.type.BigDecimalType':
				return ['BigDecimal':'java.math.BigDecimal']
				break
			case 'class org.hibernate.type.TimestampType':
				return ['Timestamp':'java.sql.Timestamp']
				break
			case 'class org.hibernate.type.TimeType':
				return ['Time':'java.sql.Time']
				break
			case 'class org.hibernate.type.CalendarDateType':
			case 'class org.hibernate.type.DateType':
				return ['Date':'java.sql.Date']
				break
			case 'class org.hibernate.type.CalendarType':
				return ['Calendar':'java.util.Calendar']
				break
			case 'class org.hibernate.type.CurrencyType':
				return ['Currency':'java.util.Currency']
				break
			case 'class org.hibernate.type.LocaleType':
				return ['Locale':'java.util.Locale']
				break
			case 'class org.hibernate.type.TimeZoneType':
				return ['TimeZone':'java.util.TimeZone']
				break
			case 'class org.hibernate.type.UrlType':
				return ['URL':'java.net.URL']
				break
			case 'class org.hibernate.type.ClassType':
				return ['Class':'java.lang.Class']
				break
			case 'class org.hibernate.type.MaterializedBlobType':
			case 'class org.hibernate.type.BlobType':
				return ['Blob':'java.sql.Blob']
				break
			case 'class org.hibernate.type.ClobType':
				return ['Clob':'java.sql.Clob']
				break
			case 'class org.hibernate.type.PostgresUUIDType':
			case 'class org.hibernate.type.UUIDBinaryType':
				return ['UUID':'java.util.UUID']
				break
			case 'class org.hibernate.type.TextType':
			case 'class org.hibernate.type.StringType':
			default:
				return ['String':'java.lang.String']
				break
		}
	}
}

