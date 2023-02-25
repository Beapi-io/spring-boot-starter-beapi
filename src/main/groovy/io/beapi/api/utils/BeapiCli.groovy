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


class BeapiCli {

	private LinkedHashMap args

	public BeapiCli(Set<String> args) {
		parse(args)
	}

	private void parse(Set<String> args) {
		ArrayList validArgKeys = ['controller','connector','domain']
		LinkedHashMap vars = [:]
		args.each(){
			ArrayList temp = it.split('=')
			if(validArgKeys.contains(temp[0])){
				if(temp[1] ==~ /[a-z][a-z0-9_]*(\.[a-z0-9_]+)+[0-9a-z_]/) {
					println("${temp[0]} = ${temp[1]}")
					vars[temp[0]] = temp[1]
				}else{
					System.err << "Invalid package name. Package name for '"+temp[0]+"' is not recognized as a valid package name"
					System.exit 1
				}
			}else{
				System.err << "Invalid ARG sent. Please provide ARG values of \'controller/connector\' and \'domain\'."
				System.exit 1
			}
		}
		this.args = vars
	}

}

