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
package io.beapi.api.domain.service;

import io.beapi.api.domain.Hook;
import io.beapi.api.repositories.HookRepository;
import io.beapi.api.domain.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

// todo: rename as ExchangeService : encompasses both request/response methods for interceptor
@Service
public class HookService implements IHook {

	HookRepository hookrepo;

	public HookService(HookRepository hookrepo) {
		this.hookrepo = hookrepo;
	}

	public Hook save(Hook hook){
		hookrepo.save(hook);
		hookrepo.flush();
		return hook;
	}

	//@Override
	public void deleteById(Long id){
		hookrepo.deleteById(id);
		hookrepo.flush();
	}


	//@Override
	public Optional<Hook> findById(Long id) {
		return hookrepo.findById(id);
	}

	//@Override
	public List<Hook> findByUser(User user){
		// TODO Auto-generated method stub
		List<Hook> hooks = hookrepo.findByUser(user);
		return hooks;
	}

	/*
	* NOTE : we use 'service' rather than endpoint to declare a 'controller'.
	* This enables us to use PUT/POST/DELETE for controller to report
	* all changes.
	 */
	public Hook findByServiceAndUser(String service, User user){
		Hook hook = hookrepo.findByServiceAndUser(service,user);
		return hook;
	}

	/*
	* NOTE : we use 'service' rather than endpoint to declare a 'controller'.
	* This enables us to use PUT/POST/DELETE for controller to report
	* all changes.
	 */
	public List<Hook> findByEnabledTrueAndService(String service){
		List<Hook> hooks = hookrepo.findByEnabledTrueAndService(service);
		return hooks;
	}
}
