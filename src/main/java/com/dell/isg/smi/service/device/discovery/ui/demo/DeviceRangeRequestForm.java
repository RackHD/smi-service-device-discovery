package com.dell.isg.smi.service.device.discovery.ui.demo;

import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.viritin.form.AbstractForm;
import org.vaadin.viritin.layouts.MFormLayout;
import org.vaadin.viritin.layouts.MVerticalLayout;

import com.dell.isg.smi.commons.model.device.discovery.DiscoveryDeviceGroupEnum;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TwinColSelect;
import com.vaadin.ui.VerticalLayout;

@UIScope
@SpringComponent
public class DeviceRangeRequestForm extends AbstractForm<DeviceRangeRequest> {

	private static final long serialVersionUID = -5957848856809398440L;
	@Autowired
	DeviceRangeRequestHandler deviceRangeRequestHandler;
	private RequestBuilderUI requestBuilderUI;
	private TwinColSelect<String> deviceGroups = new TwinColSelect<String>("Select the group(s) for Discovery :");
	private TextField startIp = new TextField("Start IP :");
	private TextField endIp = new TextField("End IP :");
	private CheckBox credential = new CheckBox("Local Credential ");
	private TextField username = new TextField("Username :");
	private PasswordField password = new PasswordField("Password :");
	private Panel credPanel = new Panel();

	public DeviceRangeRequestForm(RequestBuilderUI requestBuilderUI) {
        super(DeviceRangeRequest.class);
        this.requestBuilderUI = requestBuilderUI;
		String[] discoverGroupNames = Stream.of(DiscoveryDeviceGroupEnum.values()).map(DiscoveryDeviceGroupEnum::name)
				.toArray(String[]::new);
		deviceGroups.setItems(discoverGroupNames);
		
		VerticalLayout credLayout = new VerticalLayout();
		credLayout.addComponents(username, password);
		credPanel.setContent(credLayout);
		credPanel.setVisible(false);
		
		credential.addValueChangeListener(event -> {
			if (event.getValue() == false) {
				credPanel.setVisible(false);
			} else {
				credPanel.setVisible(true);
			}
		});
//        setSavedHandler(rangeRequest -> {
//        	try {
//				deviceRangeRequestHandler.addRange(rangeRequest);
//				requestBuilderUI.onPersonModified();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//        });
        
        setSizeUndefined();
	}
	

	@Override
	protected Component createContent() {
		return new MVerticalLayout(
                new MFormLayout(
                		deviceGroups,
                		startIp,
                		endIp,
                		credential,
                		credPanel
                ).withWidth(""),
                getToolbar()
        ).withWidth("");
	}

}
