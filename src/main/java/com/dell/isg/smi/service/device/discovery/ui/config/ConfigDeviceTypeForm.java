package com.dell.isg.smi.service.device.discovery.ui.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.viritin.button.MButton;
import org.vaadin.viritin.fields.MTextField;

import com.vaadin.data.Binder;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.FontAwesome;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.ValoTheme;

@UIScope
@SpringComponent
public class ConfigDeviceTypeForm extends FormLayout {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Autowired
	public ConfigDeviceTypeHandler configHandler;
	private ConfigDeviceType configDeviceType;
	private ConfigurationUI configuratorUI;
	private Binder<ConfigDeviceType> binder = new Binder<>(ConfigDeviceType.class);

	private TextField group = new TextField("Group :");
	private TextField name = new TextField("Device Type :");
	private TextField identifyBy = new TextField("Identified By :");
	private TextArea identifiers = new TextArea("Identifiers :");
	private TextField username = new TextField("User :");
	private PasswordField password = new PasswordField("Password :");
	private CheckBox enabled = new CheckBox("Discovery Enabled ");
	private Button save = new MButton();
	private Button cancel = new MButton();

	public ConfigDeviceTypeForm(ConfigurationUI configuratorUI) {
		this.configuratorUI = configuratorUI;
		group.setEnabled(false);
		name.setEnabled(false);
		identifyBy.setEnabled(false);
		identifiers.setEnabled(false);
		identifiers.setWordWrap(true);
		save.setIcon(VaadinIcons.CHECK_CIRCLE,"Save");
		cancel.setIcon(VaadinIcons.CLOSE_CIRCLE,"Cancel");
		setSizeUndefined();
		HorizontalLayout buttons = new HorizontalLayout(save, cancel);
		
		Panel formPanel = new Panel("Global Credential ");
		formPanel.addStyleName("mypanelexample");
		formPanel.setSizeUndefined(); // Shrink to fit content
		// Create the content
		FormLayout content = new FormLayout();
		content.addStyleName("mypanelcontent");
		content.addComponents(group, name, username, password, enabled, buttons);
		//content.addComponents(group, name, identifyBy, identifiers, username, password, enabled, buttons);
		content.setSizeUndefined(); // Shrink to fit
		content.setMargin(true);
		formPanel.setContent(content);
		setSizeUndefined();
		addComponents(formPanel);
		save.setStyleName(ValoTheme.BUTTON_PRIMARY);
		save.setClickShortcut(KeyCode.ENTER);
		binder.bindInstanceFields(this);
		save.addClickListener(e -> this.save());
		cancel.addClickListener(e -> this.cancel());
	}

	public void setConfigDeviceType(ConfigDeviceType configDeviceType) {
		this.configDeviceType = configDeviceType;
		binder.setBean(configDeviceType);
		setVisible(true);
		group.selectAll();
	}

	private void cancel() {
		configuratorUI.updateList();
		setVisible(false);
	}

	private void save() {
		try {
			configHandler.save(configDeviceType);
		} catch (Exception e) {

		}
		configuratorUI.updateList();
		setVisible(false);
		Notification.show("Saved :",
                "Configuration modification to " +configDeviceType.name + " was successfull !!!. ",
                Notification.Type.HUMANIZED_MESSAGE);
	}

}
