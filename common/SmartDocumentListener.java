package common;

import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class SmartDocumentListener implements DocumentListener {
	private JTextField _field;
	private JButton _button;
	
	public SmartDocumentListener(JTextField field_, JButton button_) {
		_field = field_;
		_button = button_;
	}
	
	@Override
	public void insertUpdate(DocumentEvent e) {
		_button.setEnabled(!_field.getText().isEmpty());
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		_button.setEnabled(!_field.getText().isEmpty());
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		_button.setEnabled(!_field.getText().isEmpty());
	}
}
