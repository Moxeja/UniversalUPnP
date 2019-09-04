package com.moxeja.uupnp;

import org.fourthline.cling.DefaultUpnpServiceConfiguration;
import org.fourthline.cling.model.Namespace;
import org.fourthline.cling.transport.impl.AsyncServletStreamServerConfigurationImpl;
import org.fourthline.cling.transport.impl.AsyncServletStreamServerImpl;
import org.fourthline.cling.transport.impl.jetty.JettyServletContainer;
import org.fourthline.cling.transport.impl.jetty.StreamClientConfigurationImpl;
import org.fourthline.cling.transport.impl.jetty.StreamClientImpl;
import org.fourthline.cling.transport.spi.NetworkAddressFactory;
import org.fourthline.cling.transport.spi.StreamClient;
import org.fourthline.cling.transport.spi.StreamServer;

public class JettyUPnPConfiguration extends DefaultUpnpServiceConfiguration {

	@Override
	protected Namespace createNamespace() {
		return new Namespace("/upnp");
	}
	
	@Override
	public StreamClient<?> createStreamClient() {
		return new StreamClientImpl(
				new StreamClientConfigurationImpl(getSyncProtocolExecutorService()));
	}
	
	@Override
	public StreamServer<?> createStreamServer(NetworkAddressFactory networkAddressFactory) {
		return new AsyncServletStreamServerImpl(new AsyncServletStreamServerConfigurationImpl(
				JettyServletContainer.INSTANCE, networkAddressFactory.getStreamListenPort()));
	}
}
