package common;

import java.util.UUID;

/**
 * Callback used when user requests creating new client
 */
public interface CreateClientCallback {
	/**
	 * Called when new client is successfully created
	 * @param clientId new client id returned 
	 */
	void handleSuccess(UUID clientId);
	
	/**
	 * Called when creating a new client failed
	 * @param error shows why new client fails to be created
	 */
	void handleFailure(Exception error);
}