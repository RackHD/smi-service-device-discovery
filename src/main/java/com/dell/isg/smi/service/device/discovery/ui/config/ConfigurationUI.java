package com.dell.isg.smi.service.device.discovery.ui.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.viritin.button.ConfirmButton;
import org.vaadin.viritin.components.DisclosurePanel;
import org.vaadin.viritin.label.RichText;

import com.dell.isg.smi.service.device.discovery.config.DiscoveryDeviceConfig;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.io.Files;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.ValueChangeMode;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

/**
 * This UI is the application entry point. A UI may either represent a browser
 * window (or tab) or some part of a html page where a Vaadin application is
 * embedded.
 * <p>
 * The UI is initialized using {@link #init(VaadinRequest)}. This method is
 * intended to be overridden to add component to the user interface and
 * initialize non-component functionality.
 */
@SuppressWarnings("deprecation")
@Title("Discovery Configuration UI")
@Theme("valo")
@SpringUI(path = "/config-ui")
public class ConfigurationUI extends UI {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Autowired
	public ConfigDeviceTypeHandler configHandler;
	private Grid<ConfigDeviceType> grid = new Grid<>(ConfigDeviceType.class);
	private TextField filterGroup = new TextField();
	private ConfigDeviceTypeForm configDeviceTypeForm = new ConfigDeviceTypeForm(this);
    private Button resetBtn = new ConfirmButton(VaadinIcons.FLIP_V,
            "Are you sure you want to reset to default configuration?", this::reset);

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationUI.class.getName());

	@Override
	protected void init(VaadinRequest vaadinRequest) {
		final VerticalLayout layout = new VerticalLayout();
		
        HorizontalLayout titleBar = new HorizontalLayout();
        titleBar.setWidth("100%");

        Label title = new Label("CONFIGURATION UI: ");
        title.addStyleName(ValoTheme.LABEL_HUGE);
        titleBar.addComponent(title);
        Label titleComment = new Label("for Discovery");
        titleComment.addStyleName(ValoTheme.LABEL_LIGHT);
        titleComment.setSizeUndefined();
        titleBar.addComponent(titleComment);
        titleBar.setExpandRatio(title, 1.0f); 
		
		filterGroup.setPlaceholder("Filter by Group...");
		filterGroup.addValueChangeListener(e -> updateList());
		filterGroup.setValueChangeMode(ValueChangeMode.LAZY);

		Button clearFilterTextBtn = new Button(VaadinIcons.CLOSE_SMALL);
		clearFilterTextBtn.setDescription("Clear the current filter");
		clearFilterTextBtn.addClickListener(e -> filterGroup.clear());

		CssLayout filtering = new CssLayout();
		filtering.addComponents(filterGroup, clearFilterTextBtn);
		filtering.setStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);

		resetBtn.setCaption("Reset back to orginal configuration.");

		DisclosurePanel aboutConfig = new DisclosurePanel("About Discovery Configuration",
				new RichText().withMarkDownResource("/config-readme.md"));
		HorizontalLayout toolbar = new HorizontalLayout(aboutConfig, filtering, resetBtn);
		grid.setColumns("group", "name", "enabled");

		HorizontalLayout main = new HorizontalLayout(grid, configDeviceTypeForm);
		main.setSizeFull();
		grid.setSizeFull();
		grid.setHeight("450");
		main.setExpandRatio(grid, 1);
		
		final ConfigUploader receiver = new ConfigUploader(); 
		Upload upload = new Upload("Upload Configuration", receiver);
		upload.setImmediateMode(false);
		upload.addSucceededListener(receiver);
		upload.setButtonCaption("Upload Now");
		
		HorizontalLayout uploader = new HorizontalLayout(upload);
		uploader.setSizeFull();
		layout.addComponents(titleBar , toolbar, main, uploader);
		updateList();
		setContent(layout);
		configDeviceTypeForm.setVisible(false);
		grid.asSingleSelect().addValueChangeListener(event -> {
			if (event.getValue() == null) {
				configDeviceTypeForm.setVisible(false);
			} else {
				configDeviceTypeForm.setConfigDeviceType(event.getValue());
			}
		});
	}

	public void updateList() {
		List<ConfigDeviceType> configDeviceTypeList = configHandler.findByNameLikeIgnoreCase(filterGroup.getValue());
		grid.setItems(configDeviceTypeList);
	}
	
	public void reset(){
		grid.asSingleSelect().clear();
		configHandler.reset();
		updateList();
    	Notification.show("Reset to deafult :",
                "Default YML Configuration applied successfully !!!. ",
                Notification.Type.HUMANIZED_MESSAGE);
	}
	
	class ConfigUploader implements Receiver, SucceededListener {
	    private static final long serialVersionUID = -1276759102490466761L;
	    public File file;
	    public OutputStream receiveUpload(String filename, String mimeType) {
	    	FileOutputStream fileOutputStream = null;
	        try {
	        	if (!FilenameUtils.isExtension(filename,"yml")) {
	        		throw new IOException("Invalid file extention.");
	        	}
	            File tempDir = Files.createTempDir();
	        	file = new File(tempDir+File.separator+filename);
	        	fileOutputStream = new FileOutputStream(file);
	        } catch (Exception e) {
	        	logger.error("Unable to upload the YML file.");
	        	Notification.show("Error :",
	                    "Invalid file !!!. Please upload valid YML configuration file !!!. ",
	                    Notification.Type.ERROR_MESSAGE);
	        }
	        return fileOutputStream; 
	    }

	    public void uploadSucceeded(SucceededEvent event) {
	    	try {
	    		updateConfig();
				Notification.show("Configuration Upload :",
		                "YML Configuration applied successfully !!!. ",
		                Notification.Type.HUMANIZED_MESSAGE);
			} catch (Exception e) {
				logger.error("Unable to parse the YML file.");
				Notification.show("Error :",
	                    "Parsing error !!!. Unable to parse the YML configuration file !!!. ",
	                    Notification.Type.ERROR_MESSAGE);
			}
	    	
	    	
	    }
	    
	    public synchronized void updateConfig() throws Exception{
	        try {
	        	ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
	        	mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	        	DiscoveryDeviceConfig discoveryConfig = mapper.readValue(file, DiscoveryDeviceConfig.class);
	    		configHandler.loadFrom(discoveryConfig);
	    		updateList();
	        } catch (Exception e) {
	            throw e;
	        }
	    }
	};

}



