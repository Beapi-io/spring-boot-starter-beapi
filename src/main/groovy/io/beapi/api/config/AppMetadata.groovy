package io.beapi.api.config

import org.springframework.stereotype.Component


@Component
public class AppMetadata{

	/**
	 *
	 * @return
	 * @throws IOException
	 */
	public String getAppVersion(){
		ClassLoader classLoader = getClass().getClassLoader();
		URL incoming = classLoader.getResource("META-INF/build-info.properties")

		String version;
		if (incoming != null) {
			Properties properties = new Properties();
			properties.load(incoming.openStream());
			version = properties.getProperty('build.version')
		}
		return version
	}

}
