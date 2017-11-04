package common;

import java.util.*;

public abstract class ConnectorUser implements ConnectorCallback  {
	protected Connector connector;
	
	protected Optional<Exception> startConnector() {
		if (connector == null || !connector.isAlive()) {
			try {
				connector = new Connector(this);
				connector.start();
			} catch (Exception e) {
				connector = null;
				return Optional.of(e);
			}
		}
		return Optional.empty();
	}
}
