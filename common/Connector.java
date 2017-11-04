package common;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.*;

public class Connector extends Thread {
	private final Selector selector;
	private volatile boolean exit;
	private ConnectorCallback callback;
	private final Map<UUID, Client> clients;

	private BlockingQueue<Task> pendingTasks;

	private static void log(String text) {
		System.out.println(text);
	}

	private interface Client {
		SelectableChannel getChannel();
	}

	private class ListeningClient implements Client {
		final ServerSocketChannel channel;

		ListeningClient(ServerSocketChannel channel) {
			this.channel = channel;
		}

		@Override
		public SelectableChannel getChannel() {
			return channel;
		}

		SocketChannel accept() throws IOException {
			return channel.accept();
		}
	}

	private class ReadWriteClient implements Client {
		final SocketChannel channel;
		final ByteBuffer readBuf;
		final List<SendMessageTask> taskList;

		ReadWriteClient(SocketChannel channel) {
			this.channel = channel;
			this.readBuf = ByteBuffer.allocate(256);
			this.taskList = new LinkedList<>();
		}

		@Override
		public SelectableChannel getChannel() {
			return channel;
		}

		/**
		 * Read message from the client
		 * 
		 * @return Optional.empty if the underlying client is closed
		 * @throws IOException
		 */
		Optional<String> read() throws IOException {
			readBuf.clear();
			int count = channel.read(readBuf);
			if (count < 0) {
				return Optional.empty();
			}
			readBuf.flip();
			return Optional.of(new String(readBuf.array(), 0, readBuf.limit()));
		}

		/**
		 * Add one new message sending task to this client list
		 * 
		 * @param task
		 */
		void add(SendMessageTask task) {
			channel.keyFor(selector).interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
			taskList.add(task);
		}

		// Write one message per round
		void write() {
			Iterator<SendMessageTask> it = taskList.iterator();
			if (it.hasNext()) {
				SendMessageTask task = it.next();

				try {
					ByteBuffer writeBuf = ByteBuffer.wrap(task.message.getBytes());
					channel.write(writeBuf);
					while (writeBuf.hasRemaining()) {
						// We still have some remaining data for current task
						channel.write(writeBuf);
					}
					task.ocCallback.handleSuccess(task.clientId);
				} catch (Exception e) {
					task.ocCallback.handleFailure(task.clientId, e);
				}

				// We finish this task
				it.remove();
			}

			if (taskList.isEmpty()) {
				log("Remove OP_WRITE for channel " + channel);
				// Remove OP_WRITE since we don't have data to be written
				channel.keyFor(selector).interestOps(SelectionKey.OP_READ);
			}
		}
	}

	private interface Task {
		void execute();
	}

	private class ListenTask implements Task {
		String address;
		CreateClientCallback ccCallback;

		ListenTask(String address, CreateClientCallback ccCallback) {
			this.address = address;
			this.ccCallback = ccCallback;
		}

		@Override
		public void execute() {
			log("execute ListenTask");
			try {
				ServerSocketChannel socketChannel = ServerSocketChannel.open();
				socketChannel.socket().bind(Utils.parseSocketAddress(address));
				UUID clientId = register(new ListeningClient(socketChannel), SelectionKey.OP_ACCEPT);
				ccCallback.handleSuccess(clientId);
			} catch (Exception e) {
				ccCallback.handleFailure(e);
			}
		}
	}

	private class ConnectTask implements Task {
		String address;
		CreateClientCallback ccCallback;

		ConnectTask(String address, CreateClientCallback ccCallback) {
			this.address = address;
			this.ccCallback = ccCallback;
		}

		@Override
		public void execute() {
			log("execute ConnectTask");
			try {
				SocketChannel socketChannel = SocketChannel.open();
				socketChannel.connect(Utils.parseSocketAddress(address));
				UUID clientId = register(new ReadWriteClient(socketChannel), SelectionKey.OP_READ);
				ccCallback.handleSuccess(clientId);
			} catch (Exception e) {
				ccCallback.handleFailure(e);
			}
		}
	}

	private class SendMessageTask implements Task {
		UUID clientId;
		String message;
		OperateClientCallback ocCallback;

		SendMessageTask(UUID clientId, String message, OperateClientCallback ocCallback) {
			this.clientId = clientId;
			this.message = message;
			this.ocCallback = ocCallback;
		}

		@Override
		public void execute() {
			log("execute SendMessageTask for " + clientId);
			try {
				Client client = clients.get(clientId);
				if (client == null) {
					throw new IllegalArgumentException("Illegal client id " + clientId);
				}
				if (client.getClass() != ReadWriteClient.class) {
					throw new UnsupportedOperationException("Client " + client.getClass() + " doesn't support sending message");
				}
				((ReadWriteClient) client).add(this);

				// We are unable to know if this message could be sent out
				// successfully later since we just added the task into the
				// task queue of corresponding client.
				// ocCallback.handleSuccess(clientId);
			} catch (Exception e) {
				ocCallback.handleFailure(clientId, e);
			}

		}
	}

	private class CloseClientTask implements Task {
		UUID clientId;
		OperateClientCallback ocCallback;

		CloseClientTask(UUID clientId, OperateClientCallback ocCallback) {
			this.clientId = clientId;
			this.ocCallback = ocCallback;
		}

		@Override
		public void execute() {
			log("execute CloseClientTask for " + clientId);
			try {
				Client client = clients.remove(clientId);
				if (client == null) {
					throw new IllegalArgumentException("Illegal client id " + clientId);
				}

				log("closing client " + clientId);
				client.getChannel().close();
				log("closed client " + clientId);

				ocCallback.handleSuccess(clientId);
			} catch (Exception e) {
				ocCallback.handleFailure(clientId, e);
			}

		}
	}

	public Connector(ConnectorCallback callback) throws IOException {
		this.selector = Selector.open();
		this.exit = false;
		this.callback = callback;
		this.clients = new HashMap<>();
		this.pendingTasks = new LinkedBlockingQueue<>();
	}

	private UUID register(Client client, int ops) throws IOException {
		UUID clientId = UUID.randomUUID();
		client.getChannel().configureBlocking(false);
		client.getChannel().register(selector, ops, clientId);
		clients.put(clientId, client);
		return clientId;
	}

	public void listen(String address, CreateClientCallback ccCallback) {
		addPendingTask(new ListenTask(address, ccCallback));
	}

	public void connect(String address, CreateClientCallback ccCallback) {
		addPendingTask(new ConnectTask(address, ccCallback));
	}

	public void sendMessage(UUID clientId, String message, OperateClientCallback ocCallback) {
		addPendingTask(new SendMessageTask(clientId, message, ocCallback));
	}

	public void close(UUID clientId, OperateClientCallback ocCallback) {
		addPendingTask(new CloseClientTask(clientId, ocCallback));
	}
	
	private void addPendingTask(Task task) {
		pendingTasks.add(task);
		selector.wakeup();
	}

	// Tell connector to exit and wait it to complete exit
	public void safelyExit() {
		exit = true;
		selector.wakeup();
		try {
			join();
		} catch (InterruptedException e) {
			// ignore this
		}
	}

	@Override
	public void run() {
		log("Connector started");

		while (!exit) {
			// Handle any pending tasks first
			Task pendingTask;
			while ((pendingTask = pendingTasks.poll()) != null) {
				pendingTask.execute();
			}

			try {
				selector.select();
			} catch (Exception e) {
				callback.handleException(e, true);
				exit = true;
				break;
			}

			Iterator<SelectionKey> it = selector.selectedKeys().iterator();
			while (it.hasNext()) {
				SelectionKey selectionKey = it.next();

				UUID clientId = (UUID) selectionKey.attachment();
				try {
					if (selectionKey.isAcceptable()) {
						log("Connector acceptable for " + clientId);

						SocketChannel clientChannel = ((ListeningClient) clients.get(clientId)).accept();
						UUID newClientId = register(new ReadWriteClient(clientChannel), SelectionKey.OP_READ);

						callback.handleAcceptedClient(newClientId);

					} else if (selectionKey.isReadable()) {
						log("Connector readable for " + clientId);

						clientId = (UUID) selectionKey.attachment();
						ReadWriteClient client = (ReadWriteClient) clients.get(clientId);
						Optional<String> message = client.read();
						if (message.isPresent()) {
							callback.handleRead(clientId, message.get());
						} else {
							log("Connector read - client closed");
							client.channel.close();
							clients.remove(clientId);
							callback.handleClose(clientId);
						}

					} else if (selectionKey.isValid() && selectionKey.isWritable()) {
						log("Connector writable for " + clientId);
						clientId = (UUID) selectionKey.attachment();
						((ReadWriteClient) clients.get(clientId)).write();
					}
				} catch (Exception e) {
					callback.handleException(clientId, e);
				}

				it.remove();
			}
		}

		try {
			selector.close();
		} catch (Exception e) {
			log("(ignored) unable to close selector: " + e.getMessage());
		}

		log("Connector still has " + clients.size() + " channels before exiting");
		clients.forEach((id, c) -> {
			try {
				c.getChannel().close();
			} catch (Exception e) {
				log("(ignored) unable to close client " + id + " before exiting: " + e.getMessage());
			}
		});
		clients.clear();

		log("Connector exited");
	}

}
