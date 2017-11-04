package server;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Panel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Optional;
import java.util.UUID;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import common.ConnectorUser;
import common.CreateClientCallback;
import common.OperateClientCallback;
import common.SmartDocumentListener;

public class ServerUI extends ConnectorUser {
	private static String UITitle = "NIO Server";

	private JFrame mainFrame;
	private JTextField uriTF;
	private JButton listenBtn, stopBtn;
	private JTabbedPane clientTabs;
	
	private UUID listeningClientId;

	private void showError(String message, String title) {
		JOptionPane.showMessageDialog(mainFrame, message, title, JOptionPane.ERROR_MESSAGE);
	}

	private void listen() {
		Optional<Exception> error = startConnector();
		if (error.isPresent()) {
			showError("Error: " + error.get().getMessage(), "Failed to create connector");
			return;
		}
		
		System.out.println("Create server socket now ...");
		
		connector.listen(uriTF.getText(), new CreateClientCallback() {
			@Override
			public void handleSuccess(UUID clientId) {
				listeningClientId = clientId;
				mainFrame.setTitle(UITitle + " - Listened");
				setControlStatus(true);
			}

			@Override
			public void handleFailure(Exception error) {
				showError("Error: " + error.getMessage(), "Failed to listen");
			}
		});
	}

	private void stop() {
		connector.close(listeningClientId, new OperateClientCallback() {
			@Override
			public void handleSuccess(UUID clientId) {
				listeningClientId = null;
				mainFrame.setTitle(UITitle + " - Listen stopped");
				setControlStatus(false);
			}

			@Override
			public void handleFailure(UUID clientId, Exception error) {
				showError("Error: " + error.getMessage(), "Failed to stop listening");
			}
		});
	}

	private void setControlStatus(boolean listened) {
		uriTF.setEditable(!listened);
		listenBtn.setEnabled(!listened && !uriTF.getText().isEmpty());
		stopBtn.setEnabled(listened);
	}

	private void show() {
		mainFrame = new JFrame(UITitle);
		mainFrame.setSize(500, 400);
		mainFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent windowEvent) {
				if (connector != null) {
					connector.safelyExit();
				}
				System.exit(0);
			}
		});

		mainFrame.setLayout(new BorderLayout(5, 5));

		JPanel connPanel = new JPanel(new BorderLayout());
		JLabel connLabel = new JLabel("URI:");
		connPanel.add(connLabel, BorderLayout.WEST);
		uriTF = new JTextField("localhost:2626");
		connPanel.add(uriTF, BorderLayout.CENTER);
		JPanel connButtons = new JPanel(new FlowLayout());
		listenBtn = new JButton("Listen");
		listenBtn.addActionListener(e -> listen());
		connButtons.add(listenBtn);
		stopBtn = new JButton("Stop");
		stopBtn.addActionListener(e -> stop());
		connButtons.add(stopBtn);
		connPanel.add(connButtons, BorderLayout.EAST);

		uriTF.getDocument().addDocumentListener(new SmartDocumentListener(uriTF, listenBtn));

		mainFrame.add(connPanel, BorderLayout.NORTH);

		clientTabs = new JTabbedPane();
		mainFrame.add(clientTabs, BorderLayout.CENTER);

		setControlStatus(false);

		mainFrame.setVisible(true);
	}

	class ClientTab extends Panel {
		private static final long serialVersionUID = 1L;

		private UUID clientId;
		private JTextArea chatTA;
		private JTextField msgTF;
		private JButton sendBtn;
		private boolean disconnected = false;

		public ClientTab(UUID id) {
			super(new BorderLayout());

			clientId = id;

			chatTA = new JTextArea();
			chatTA.setEditable(false);
			add(chatTA, BorderLayout.CENTER);

			JPanel msgPanel = new JPanel(new BorderLayout());

			msgTF = new JTextField();
			msgPanel.add(msgTF, BorderLayout.CENTER);

			Panel btnPanel = new Panel(new FlowLayout());
			sendBtn = new JButton("Send");
			sendBtn.setEnabled(false);
			sendBtn.addActionListener(e -> {
				connector.sendMessage(clientId, msgTF.getText(), new OperateClientCallback() {

					@Override
					public void handleSuccess(UUID clientId) {
						chatTA.setText(chatTA.getText() + "Me: " + msgTF.getText() + "\n");
						// When we set it to empty, send button would be disabled auto by doc listener
						msgTF.setText("");
					}

					@Override
					public void handleFailure(UUID clientId, Exception error) {
						showError("Error: " + error.getMessage(), "Failed to send message");
					}
					
				});
			});
			btnPanel.add(sendBtn);

			msgTF.getDocument().addDocumentListener(new SmartDocumentListener(msgTF, sendBtn));

			JButton closeButton = new JButton("Close");
			closeButton.addActionListener(e -> {
				if (!disconnected) {
					connector.close(clientId, new OperateClientCallback() {
						@Override
						public void handleSuccess(UUID clientId) {
							clientTabs.remove(ClientTab.this);
						}

						@Override
						public void handleFailure(UUID clientId, Exception error) {
							showError("Error: " + error.getMessage(), "Failed to close connection");
						}
					});
				}
			});
			btnPanel.add(closeButton);

			msgPanel.add(btnPanel, BorderLayout.EAST);

			add(msgPanel, BorderLayout.SOUTH);
		}

		void receiveMsg(String msg) {
			chatTA.setText(chatTA.getText() + "He: " + msg + "\n");
		}

		void disconnect() {
			if (!disconnected) {
				disconnected = true;
				msgTF.setEditable(false);
				chatTA.setText(chatTA.getText() + "(disconnected)");
				sendBtn.setEnabled(false);
			}
		}
	}

	public static void main(String[] args) {
		new ServerUI().show();
	}

	@Override
	public void handleException(Exception e, boolean serious) {
		SwingUtilities.invokeLater(() -> {
			if (serious) {
				connector = null; // connector died
				setControlStatus(false);
			}
			showError("Error: " + e.getMessage(), "Serious Error");
		});
	}

	@Override
	public void handleException(UUID clientId, Exception error) {
		SwingUtilities.invokeLater(() -> showError("Error: " + error.getMessage(), "Client Error"));
	}

	@Override
	public void handleAcceptedClient(UUID clientId) {
		SwingUtilities.invokeLater(() -> clientTabs.add(new ClientTab(clientId)));
	}

	@Override
	public void handleRead(UUID clientId, String message) {
		SwingUtilities.invokeLater(() -> {
			for (Component c : clientTabs.getComponents()) {
				ClientTab panel = (ClientTab) c;
				if (panel.clientId == clientId) {
					panel.receiveMsg(message);
				}
			}
		});
	}

	@Override
	public void handleClose(UUID clientId) {
		SwingUtilities.invokeLater(() -> {
			for (Component c : clientTabs.getComponents()) {
				ClientTab panel = (ClientTab) c;
				if (panel.clientId.equals(clientId)) {
					panel.disconnect();
				}
			}
		});
	}

}
