package client;

import static common.Utils.require;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Optional;
import java.util.UUID;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import common.ConnectorUser;
import common.CreateClientCallback;
import common.OperateClientCallback;
import common.SmartDocumentListener;

public class ClientUI extends ConnectorUser {
	private static String UITitle = "NIO Client";

	private JFrame mainFrame;
	private JTextField uriTF;
	private JButton connBtn, disconnBtn;

	private JTextArea chatTA;

	private JTextField msgTF;
	private JButton sendBtn;

	private UUID clientId;
	
	private void showError(String message, String title) {
		JOptionPane.showMessageDialog(mainFrame, message, title, JOptionPane.ERROR_MESSAGE);
	}

	private void connect() {
		Optional<Exception> error = startConnector();
		if (error.isPresent()) {
			showError("Error: " + error.get().getMessage(), "Failed to create connector");
			return;
		}

		connector.connect(uriTF.getText(), new CreateClientCallback() {
			@Override
			public void handleSuccess(UUID clientId) {
				ClientUI.this.clientId = clientId;
				mainFrame.setTitle(UITitle + " - Connected");
				setControlStatus(true);
			}

			@Override
			public void handleFailure(Exception error) {
				showError("Error: " + error.getMessage(), "Failed to connect");
			}
		});
	}

	private void disconnect() {
		connector.close(clientId, new OperateClientCallback() {
			@Override
			public void handleSuccess(UUID clientId) {
				ClientUI.this.clientId = null;
				mainFrame.setTitle(UITitle + " - Disconnected");
				setControlStatus(false);
			}

			@Override
			public void handleFailure(UUID clientId, Exception error) {
				showError("Error: " + error.getMessage(), "Failed to disconnect");
			}
		});
	}

	private void setControlStatus(boolean connected) {
		uriTF.setEditable(!connected);
		connBtn.setEnabled(!connected && !uriTF.getText().isEmpty());
		disconnBtn.setEnabled(connected);
		msgTF.setEditable(connected);
		sendBtn.setEnabled(connected && !msgTF.getText().isEmpty());
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

		BorderLayout layout = new BorderLayout();
		layout.setHgap(5);
		layout.setVgap(5);
		mainFrame.setLayout(layout);

		JPanel connPanel = new JPanel(new BorderLayout());

		JLabel connLabel = new JLabel("URI:");
		connPanel.add(connLabel, BorderLayout.WEST);

		uriTF = new JTextField("localhost:2626");
		connPanel.add(uriTF, BorderLayout.CENTER);

		JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		connBtn = new JButton("Connect");
		connBtn.addActionListener(e -> connect());
		btnPanel.add(connBtn);
		disconnBtn = new JButton("Disconnect");
		disconnBtn.addActionListener(e -> disconnect());
		btnPanel.add(disconnBtn);
		connPanel.add(btnPanel, BorderLayout.EAST);

		// Bind uriText and connButton
		uriTF.getDocument().addDocumentListener(new SmartDocumentListener(uriTF, connBtn));

		mainFrame.add(connPanel, BorderLayout.NORTH);

		chatTA = new JTextArea();
		chatTA.setEditable(false);
		mainFrame.add(chatTA, BorderLayout.CENTER);

		JPanel msgPanel = new JPanel(new BorderLayout());
		msgTF = new JTextField();
		msgPanel.add(msgTF, BorderLayout.CENTER);
		sendBtn = new JButton("Send");
		sendBtn.addActionListener(e -> {
			connector.sendMessage(clientId, msgTF.getText(), new OperateClientCallback() {
				@Override
				public void handleSuccess(UUID clientId) {
					SwingUtilities.invokeLater(() -> {
						require(ClientUI.this.clientId == clientId);
						chatTA.setText(chatTA.getText() + "Me: " + msgTF.getText() + "\n");
						// When we set it to empty, send button would be disabled auto by doc listener
						msgTF.setText("");
					});
				}

				@Override
				public void handleFailure(UUID clientId, Exception error) {
					require(ClientUI.this.clientId == clientId);
					showError("Error: " + error.getMessage(), "Failed to send message");
				}
			});
		});
		msgPanel.add(sendBtn, BorderLayout.EAST);
		mainFrame.add(msgPanel, BorderLayout.SOUTH);

		// Bind msgText and msgText
		msgTF.getDocument().addDocumentListener(new SmartDocumentListener(msgTF, sendBtn));

		setControlStatus(false);

		mainFrame.setVisible(true);
	}

	public static void main(String[] args) {
		new ClientUI().show();
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
		throw new UnsupportedOperationException("accept is not supported");
	}

	@Override
	public void handleRead(UUID clientId, String message) {
		SwingUtilities.invokeLater(() -> chatTA.setText(chatTA.getText() + "He: " + message + "\n"));
	}

	@Override
	public void handleClose(UUID clientId) {
		// We need to re-connect to the server
		SwingUtilities.invokeLater(() -> setControlStatus(false));
	}

}
