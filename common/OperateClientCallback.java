package common;

import java.util.UUID;

/**
 * Callback used when user tries operating an existing client
 */
public interface OperateClientCallback {
	/**
	 * Called when the operation succeeds
	 * @param clientId indicates which client is being operated
	 */
	void handleSuccess(UUID clientId);
	
	/**
	 * Called when the operation fails
	 * @param clientId clientId indicates which client is being operated
	 * @param error shows why new client fails to be created
	 */
	void handleFailure(UUID clientId, Exception error);
}