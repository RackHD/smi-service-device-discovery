package com.dell.isg.smi.service.device.discovery.ui.demo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.viritin.button.ConfirmButton;
import org.vaadin.viritin.button.MButton;
import org.vaadin.viritin.components.DisclosurePanel;
import org.vaadin.viritin.fields.MTextField;
import org.vaadin.viritin.form.AbstractForm;
import org.vaadin.viritin.grid.MGrid;
import org.vaadin.viritin.label.RichText;
import org.vaadin.viritin.layouts.MHorizontalLayout;

import com.dell.isg.smi.commons.model.common.Credential;
import com.dell.isg.smi.commons.model.common.DevicesIpsRequest;
import com.dell.isg.smi.commons.model.device.discovery.DiscoverDeviceRequest;
import com.dell.isg.smi.commons.model.device.discovery.DiscoverIPRangeDeviceRequests;
import com.dell.isg.smi.commons.model.device.discovery.DiscoverdDeviceResponse;
import com.dell.isg.smi.commons.model.device.discovery.DiscoveryDeviceGroupEnum;
import com.dell.isg.smi.service.device.discovery.manager.IDiscoveryManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.RadioButtonGroup;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TwinColSelect;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Runo;
import com.vaadin.ui.themes.ValoTheme;

import de.codecentric.vaadin.copy2clipboard.Copy2ClipboardButton;

@Title("Discovery Request Builder UI")
@SpringUI(path = "/builder-ui")
@Theme("valo")
public class RequestBuilderUI extends UI {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8881224699705009785L;
	@Autowired
	DeviceRangeRequestHandler deviceRangeRequestHandler;

	DeviceRangeRequestForm deviceRangeRequestForm = new DeviceRangeRequestForm(this);

	@Autowired
	IDiscoveryManager discoveryManager;

	private MGrid<DeviceRangeRequest> grid = new MGrid<>(DeviceRangeRequest.class)
			.withProperties("startIp", "endIp", "deviceGroups").withColumnHeaders("Strat IP:", "End IP:", "Groups:")
			.withFullWidth();

	private RadioButtonGroup<String> endpoint = new RadioButtonGroup<>("Please select service endpoint..");
	private Panel rangePanel = new Panel();
	private Panel ipsPanel = new Panel();
	private TextArea requestAreaIps = new TextArea("Request JSON : ");
	private TextArea requestAreaRange = new TextArea("Request JSON : ");
	private TextArea responseArea = new TextArea("Response JSON : ");

	private Button addNew = new MButton(VaadinIcons.PLUS, this::add);
	private Button edit = new MButton(VaadinIcons.PENCIL, this::edit);
	private Button delete = new ConfirmButton(VaadinIcons.TRASH, "Are you sure you want to delete the entry?",
			this::remove);

	private DiscoverIPRangeDeviceRequests discoverIPRangeDeviceRequests = new DiscoverIPRangeDeviceRequests();
	private DevicesIpsRequest devicesIpsRequest = new DevicesIpsRequest();
	private ObjectWriter objectWritter = new ObjectMapper().writer().withDefaultPrettyPrinter();

	@Override
	protected void init(VaadinRequest vaadinRequest) {

		deviceRangeRequestForm.setSavedHandler(new AbstractForm.SavedHandler<DeviceRangeRequest>() {
			@Override
			public void onSave(DeviceRangeRequest rangeRequest) {
				try {
					deviceRangeRequestHandler.addRange(rangeRequest);
					onDeviceRangeRequestModified();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		VerticalLayout main = new VerticalLayout();
		HorizontalLayout center = new HorizontalLayout();
		HorizontalLayout titleBar = new HorizontalLayout();
		titleBar.setWidth("100%");

		// Title
		Label title = new Label("REQUEST BUILDER UI: ");
		title.addStyleName(ValoTheme.LABEL_HUGE);
		titleBar.addComponent(title);
		Label titleComment = new Label("for Discovery");
		titleComment.addStyleName(ValoTheme.LABEL_LIGHT);
		titleComment.setSizeUndefined();
		titleBar.addComponent(titleComment);
		titleBar.setExpandRatio(title, 1.0f);

		CssLayout endpointLayout = new CssLayout();

		endpoint.setItems("ips", "range");
		endpointLayout.addComponent(endpoint);

		HorizontalLayout aboutLayout = new HorizontalLayout();
		// About
		DisclosurePanel aboutConfig = new DisclosurePanel("About Discovery Request Builder",
				new RichText().withMarkDownResource("/request_builder.md"));

		aboutLayout.setWidth("100%");
		aboutLayout.addComponents(aboutConfig);

		HorizontalLayout rangeLayout = new HorizontalLayout();
		rangeLayout.setWidth("100%");
		rangeLayout.addComponents(buildLeftPanel(), buildCenterRangePanel(), buildRightRangeRequestPanel());
		rangePanel.setContent(rangeLayout);
		rangePanel.setVisible(false);

		HorizontalLayout ipsLayout = new HorizontalLayout();
		ipsLayout.setWidth("100%");
		ipsLayout.addComponents(buildLeftPanel(), buildCenterIpsPanel(), buildRightIpsRequestPanel());
		ipsPanel.setContent(ipsLayout);
		ipsPanel.setVisible(false);

		center.addComponents(rangePanel, ipsPanel);
		center.setSizeFull();

		main.addComponents(titleBar, aboutLayout, endpointLayout, center);
		endpoint.addValueChangeListener(event -> {
			if (event.getValue() == "range") {
				rangePanel.setVisible(true);
				ipsPanel.setVisible(false);
			} else {
				rangePanel.setVisible(false);
				ipsPanel.setVisible(true);
			}
			buildRequest(event.getValue());
		});
		setContent(main);

	}

	private Panel buildLeftPanel() {
		Panel leftPanel = new Panel();
		leftPanel.addStyleName(Runo.PANEL_LIGHT);
		leftPanel.setSizeFull();
		VerticalLayout leftPanelLayout = new VerticalLayout();
		leftPanelLayout.setSizeFull();
		CheckBox globalCredential = new CheckBox("Global Credential ");
		globalCredential.setValue(false);
		Panel credPanel = new Panel();
		TextField globalUsername = new MTextField("Username :");
		PasswordField globalPassword = new PasswordField("Password :");
		VerticalLayout credLayout = new VerticalLayout();
		credLayout.addComponents(globalUsername, globalPassword);
		credPanel.setContent(credLayout);
		credPanel.setVisible(false);
		globalCredential.addValueChangeListener(event -> {
			if (event.getValue() == false) {
				credPanel.setVisible(false);
			} else {
				credPanel.setVisible(true);
			}
		});

		Button next = new MButton();
		next.setIcon(VaadinIcons.ARROW_CIRCLE_RIGHT, "Next");
		next.addClickListener(event -> {
			if (globalCredential.getValue() == true && StringUtils.isEmpty(globalUsername.getValue())) {
				Notification.show("Username :", "Cannot be null or empty !!!. ", Notification.Type.ERROR_MESSAGE);
			} else {
				Credential credential = new Credential();
				credential.setUserName(globalUsername.getValue());
				credential.setPassword(globalPassword.getValue());
				if (StringUtils.equals(endpoint.getValue(), "range")) {
					discoverIPRangeDeviceRequests.setCredential(credential);
				} else if (StringUtils.equals(endpoint.getValue(), "ips")) {
					devicesIpsRequest.setCredential(credential);
				}
			}
			buildRequest(endpoint.getValue());
		});
		leftPanelLayout.addComponents(globalCredential, credPanel, next);
		leftPanelLayout.setSizeFull();
		leftPanel.setSizeFull();
		leftPanel.setContent(leftPanelLayout);
		return leftPanel;

	}

	private Panel buildCenterIpsPanel() {
		Panel ipsPanel = new Panel();

		ipsPanel.addStyleName(ValoTheme.PANEL_BORDERLESS);
		ipsPanel.setSizeFull();
		VerticalLayout ipsLayout = new VerticalLayout();
		TwinColSelect<String> deviceGroups = new TwinColSelect<String>("Select the group(s) for Discovery :");
		String[] discoverGroupNames = Stream.of(DiscoveryDeviceGroupEnum.values()).map(DiscoveryDeviceGroupEnum::name)
				.toArray(String[]::new);
		deviceGroups.setItems(discoverGroupNames);
		TextField ipList = new MTextField("IP List [',' has delimiter]:");
		Label ipListLabel = new Label("Sample IP List :");
		ipListLabel.setValue("Example: - 100.68.123.31,100.68.124.95,100.68.123.35");
		ipList.setSizeFull();
		ipList.setWidth("400");
		ipsLayout.setSizeFull();
		ipsLayout.setMargin(true);
		ipsPanel.setContent(ipsLayout);
		ipsPanel.setSizeFull();
		ipsPanel.setStyleName(ValoTheme.PANEL_BORDERLESS);

		Button next = new MButton();
		next.setIcon(VaadinIcons.ARROW_CIRCLE_RIGHT, "Next");
		next.addClickListener(event -> {
			if (StringUtils.isEmpty(ipList.getValue())) {
				Notification.show("IP List :", "Cannot be null or empty !!!. ", Notification.Type.ERROR_MESSAGE);
			} else {
				String[] ips = ipList.getValue().split(",");
				devicesIpsRequest.setIps(ips);
			}
			buildRequest(endpoint.getValue());
		});

		deviceGroups.addValueChangeListener(event -> {
			String[] deviceType = null;
			if (deviceGroups.getSelectedItems().size() == 0) {
				devicesIpsRequest.setDeviceType(deviceType);
			} else {
				deviceType = deviceGroups.getSelectedItems().stream().toArray(String[]::new);
				devicesIpsRequest.setDeviceType(deviceType);
			}
			buildRequest(endpoint.getValue());
		});

		ipsLayout.addComponents(deviceGroups, ipList, ipListLabel, next);

		return ipsPanel;
	}

	private Panel buildRightIpsRequestPanel() {
		Panel requestPanel = new Panel();
		requestPanel.addStyleName(ValoTheme.PANEL_BORDERLESS);
		requestPanel.setSizeUndefined();
		VerticalLayout requestViewerLayout = new VerticalLayout();
		requestAreaIps.setSizeFull();
		requestAreaIps.setCaption("Request Viewer:");
		requestAreaIps.setHeight("350");
		requestAreaIps.setWidth("430");
		requestAreaIps.setEnabled(false);
		Copy2ClipboardButton copy = new Copy2ClipboardButton();
		copy.setCaption("Copy2Clipboard");
		copy.setIcon(VaadinIcons.COPY_O, "Copy");
		copy.addClickListener(event -> {
			{
				copy.setClipboardText(requestAreaIps.getValue());
				Notification.show("Request copy :", "Copied request to the clipboard !!!. " + requestAreaIps.getValue(),
						Notification.Type.HUMANIZED_MESSAGE);
			}
		});

		Button process = new MButton();
		process.setCaption("Run");
		process.setIcon(VaadinIcons.FILE_PROCESS, "Process");
		process.addClickListener(event -> {
			List<DiscoverdDeviceResponse> response = null;
			try {
				if (StringUtils.equals(endpoint.getValue(), "range")) {
					response = discoveryManager.discover(discoverIPRangeDeviceRequests);
				} else if (StringUtils.equals(endpoint.getValue(), "ips")) {
					if (ArrayUtils.isEmpty(devicesIpsRequest.getIps())) {
						Notification.show("IP List :", "Cannot be null or empty !!!. ",
								Notification.Type.ERROR_MESSAGE);
					} else {
						response = discoveryManager.discover(devicesIpsRequest);
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (response != null) {
				buildResponse(response);
			}

		});

		Button swagger = new MButton("Open Swagger");
		swagger.addClickListener(event -> {
			getUI().getPage().open("http://localhost:46002/swagger-ui.html", "Swagger-UI");
		});
		requestViewerLayout.addComponents(requestAreaIps, new MHorizontalLayout(copy, process, swagger));
		requestViewerLayout.setSizeFull();
		requestPanel.setContent(requestViewerLayout);
		requestPanel.setStyleName(ValoTheme.PANEL_BORDERLESS);
		requestPanel.setSizeFull();
		return requestPanel;

	}
	
	private Panel buildRightRangeRequestPanel() {
		Panel requestPanel = new Panel();
		requestPanel.addStyleName(ValoTheme.PANEL_BORDERLESS);
		requestPanel.setSizeUndefined();
		VerticalLayout requestViewerLayout = new VerticalLayout();
		requestAreaRange.setSizeFull();
		requestAreaRange.setCaption("Request Viewer:");
		requestAreaRange.setHeight("350");
		requestAreaRange.setWidth("430");
		requestAreaRange.setEnabled(false);
		Copy2ClipboardButton copy = new Copy2ClipboardButton();
		copy.setCaption("Copy2Clipboard");
		copy.setIcon(VaadinIcons.COPY_O, "Copy");
		copy.addClickListener(event -> {
			{
				copy.setClipboardText(requestAreaIps.getValue());
				Notification.show("Request copy :", "Copied request to the clipboard !!!. " + requestAreaIps.getValue(),
						Notification.Type.HUMANIZED_MESSAGE);
			}
		});

		Button process = new MButton();
		process.setCaption("Run");
		process.setIcon(VaadinIcons.FILE_PROCESS, "Process");
		process.addClickListener(event -> {
			List<DiscoverdDeviceResponse> response = null;
			try {
				if (StringUtils.equals(endpoint.getValue(), "range")) {
					response = discoveryManager.discover(discoverIPRangeDeviceRequests);
				} else if (StringUtils.equals(endpoint.getValue(), "ips")) {
					if (ArrayUtils.isEmpty(devicesIpsRequest.getIps())) {
						Notification.show("IP List :", "Cannot be null or empty !!!. ",
								Notification.Type.ERROR_MESSAGE);
					} else {
						response = discoveryManager.discover(devicesIpsRequest);
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (response != null) {
				buildResponse(response);
			}

		});

		Button swagger = new MButton("Open Swagger");
		swagger.addClickListener(event -> {
			getUI().getPage().open("http://localhost:46002/swagger-ui.html", "Swagger-UI");
		});
		requestViewerLayout.addComponents(requestAreaRange, new MHorizontalLayout(copy, process, swagger));
		requestViewerLayout.setSizeFull();
		requestPanel.setContent(requestViewerLayout);
		requestPanel.setStyleName(ValoTheme.PANEL_BORDERLESS);
		requestPanel.setSizeFull();
		return requestPanel;

	}

	private Panel buildCenterRangePanel() {
		Panel rangePanel = new Panel();
		VerticalLayout rangeForm = new VerticalLayout();
		grid.asSingleSelect().addValueChangeListener(e -> {
			boolean hasSelection = !grid.getSelectedItems().isEmpty();
			edit.setEnabled(hasSelection);
			delete.setEnabled(hasSelection);
		});

		Button next = new MButton();
		next.setIcon(VaadinIcons.ARROW_CIRCLE_RIGHT, "Next");
		next.addClickListener(event -> {
			if (grid.getSelectedItems().size() <= 0) {
				Notification.show("Range :", "You should define atleast one range !!!. ",
						Notification.Type.ERROR_MESSAGE);
			} else {
				Set<DiscoverDeviceRequest> discoverDeviceRequestSet = transformDeviceRangeRequest(
						grid.getSelectedItems());
				discoverIPRangeDeviceRequests.setDiscoverIpRangeDeviceRequests(discoverDeviceRequestSet);
			}
			buildRequest(endpoint.getValue());
		});

		updateList();
		rangeForm.addComponents(new MHorizontalLayout(addNew, edit, delete), grid, next);
		rangeForm.setExpandRatio(grid, 1);
		rangeForm.setSizeFull();
		rangeForm.setMargin(true);
		rangePanel.addStyleName(ValoTheme.PANEL_BORDERLESS);
		rangePanel.setStyleName(ValoTheme.PANEL_BORDERLESS);
		rangePanel.setSizeFull();
		rangePanel.setContent(rangeForm);
		return rangePanel;

	}

	private Set<DiscoverDeviceRequest> transformDeviceRangeRequest(Set<DeviceRangeRequest> rangeSet) {
		Set<DiscoverDeviceRequest> discoveryRangeRequestList = new HashSet<DiscoverDeviceRequest>();
		for (DeviceRangeRequest rangeRequest : rangeSet) {
			DiscoverDeviceRequest discoverDeviceRequest = new DiscoverDeviceRequest();
			discoverDeviceRequest.setDeviceType(rangeRequest.getDeviceGroups().stream().toArray(String[]::new));
			discoverDeviceRequest.setDeviceStartIp(rangeRequest.getStartIp());
			discoverDeviceRequest.setDeviceEndIp(rangeRequest.getEndIp());
			Credential credential = new Credential();
			credential.setUserName(rangeRequest.getUsername());
			credential.setPassword(rangeRequest.getPassword());
			discoverDeviceRequest.setCredential(credential);
			discoveryRangeRequestList.add(discoverDeviceRequest);
		}
		return discoveryRangeRequestList;
	}

	public void add(ClickEvent clickEvent) {
		edit(new DeviceRangeRequest());
	}

	public void edit(ClickEvent e) {
		edit(grid.asSingleSelect().getValue());
	}

	public void remove() {
		deviceRangeRequestHandler.delete(grid.asSingleSelect().getValue());
		grid.deselectAll();
		onDeviceRangeRequestModified();

	}

	protected void edit(final DeviceRangeRequest deviceRangeRequestEntry) {
		deviceRangeRequestForm.setEntity(deviceRangeRequestEntry);
		deviceRangeRequestForm.openInModalPopup();
	}

	public void onDeviceRangeRequestModified() {
		updateList();
		deviceRangeRequestForm.closePopup();
	}

	private void buildRequest(String endpoint) {
		String value = "";
		try {
			if (StringUtils.equals(endpoint, "range")) {
				value = objectWritter.writeValueAsString(discoverIPRangeDeviceRequests);
				requestAreaRange.setValue(value);
			} else if (StringUtils.equals(endpoint, "ips")) {
				value = objectWritter.writeValueAsString(devicesIpsRequest);
				requestAreaIps.setValue(value);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private void buildResponse(List<DiscoverdDeviceResponse> responseList) {
		String value = "";
		try {
			value = objectWritter.writeValueAsString(responseList);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// responseArea.setValue(value);
		Window subWindow = new Window("Response Window:");
		VerticalLayout subContent = new VerticalLayout();
		subWindow.setContent(subContent);
		responseArea.setCaption("Response:");
		responseArea.setWidth("450");
		responseArea.setHeight("500");
		responseArea.setEnabled(false);
		responseArea.setValue(value);
		subContent.addComponent(responseArea);
		subContent.setComponentAlignment(responseArea, Alignment.MIDDLE_CENTER);
		subWindow.center();
		subWindow.setWidth("600");
		subWindow.setHeight("650");
		addWindow(subWindow);
	}

	public void updateList() {
		grid.setRows(deviceRangeRequestHandler.getRangeSet());
	}

}