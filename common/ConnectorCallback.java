package common;

import java.util.UUID;

/**
 * This interface defines the necessary callback functions used 
 * by Connector class when it need to notify user of what happens 
 * during handling response from net-work. The client of connection 
 * means the socket channel managed by Connector.
 * 
 * @author will
 *
 */
public interface ConnectorCallback {
	/**
	 * Unexpected error happens and user need to check if it's a serious one
	 * @param error exception that wraps the error info
	 * @param serious indicates if a serious error happens. If it's 
	 * serious, Connector thread would die and all current clients 
	 * would be closed.
	 */
	void handleException(Exception error, boolean serious);
	
	/**
	 * Error happens when operating specific client
	 * @param clientId identifies the client that causes the error
	 * @param error exception that wraps the error info
	 */
	void handleException(UUID clientId, Exception error);
	
	/**
	 * Notify that a new client has been accepted
	 * @param clientId identifies the new client
	 */
	void handleAcceptedClient(UUID clientId);
	
	/**
	 * Notify that a new message was read from a client
	 * @param clientId identifies the new client
	 * @param message content read from client
	 */
	void handleRead(UUID clientId, String message);
	
	/**
	 * Notify that a client has closed its connection
	 * @param clientId identifies the new client
	 */
	void handleClose(UUID clientId);
}