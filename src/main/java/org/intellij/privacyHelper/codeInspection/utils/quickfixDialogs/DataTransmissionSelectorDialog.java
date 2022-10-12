package org.intellij.privacyHelper.codeInspection.utils.quickfixDialogs;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.intellij.privacyHelper.codeInspection.annotations.AnnotationHolder;
import org.intellij.privacyHelper.codeInspection.utils.*;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.intellij.privacyHelper.codeInspection.utils.Constants.*;

public class DataTransmissionSelectorDialog extends DialogWrapper {

    static final String noDataTransmission = "No user data transmitted here";
    static final String noAvailableId = "No available id. Please add @DataAccess where your app accesses user data.";
    private final Callback callback;
    private final AnnotationHolder preFilledAnnotation;
    private final Project project;

    private JCheckBoxList dataIdList;
    private JCheckBox noDataTransmissionCheckBox;
    private SharingPanel sharingPanel;
    private CollectionPanel collectionPanel;

    public interface Callback {
        void onOk(AnnotationHolder filledAnnotation);

        void onCancel();
    }

    @Override
    protected void doOKAction() {
        if (!noDataTransmissionCheckBox.isSelected() && dataIdList.getSelectedValuesList().isEmpty()) {
            JOptionPane.showMessageDialog(this.getContentPanel(),
                    "Please select at least one data id or check the no data transmission checkbox.");
            return;
        }
        if (noDataTransmissionCheckBox.isSelected()) {
            AnnotationHolder filledAnnotation =
                    CodeInspectionUtil.createEmptyAnnotationHolderByType(CoconutAnnotationType.NotPersonalDataTransmission);
            super.doOKAction();
            callback.onOk(filledAnnotation);
            return;
        }
        if (!collectionPanel.isCompleteSelection() && !sharingPanel.isCompleteSelection()) {
            JOptionPane.showMessageDialog(this.getContentPanel(),
                    "Please answer all data collection and sharing questions.");
            return;
        }
        if (!collectionPanel.isCompleteSelection()) {
            JOptionPane.showMessageDialog(this.getContentPanel(), "Please answer all data collection questions.");
            return;
        }
        if (!sharingPanel.isCompleteSelection()) {
            JOptionPane.showMessageDialog(this.getContentPanel(), "Please answer all data sharing questions.");
            return;
        }
        AnnotationHolder filledAnnotation =
                CodeInspectionUtil.createEmptyAnnotationHolderByType(CoconutAnnotationType.DataTransmission);

        ArrayList<String> selectedIds = new ArrayList<>();
        for (int i = 0; i < dataIdList.getModel().getSize(); i++) {
            if (dataIdList.getModel().getElementAt(i).isSelected()) {
                selectedIds.add(dataIdList.getModel().getElementAt(i).getText().split(" \\(")[0]);
            }
        }
        ArrayList<String> collectionAttributes = collectionPanel.getAttributes();
        ArrayList<String> sharingAttributes = sharingPanel.getAttributes();
        if (collectionAttributes.contains("collectionAttribute." + CollectionAttribute.TransmittedOffDevice_False) &&
                sharingAttributes.contains("sharingAttribute." + SharingAttribute.SharedWithThirdParty_False)) {
            filledAnnotation =
                    CodeInspectionUtil.createEmptyAnnotationHolderByType(CoconutAnnotationType.NotPersonalDataTransmission);
            super.doOKAction();
            callback.onOk(filledAnnotation);
            return;
        }
        filledAnnotation.put(fieldDataTransmissionAccessIdList, selectedIds);
        filledAnnotation.put(fieldDataTransmissionCollectionAttributeList, collectionAttributes);
        filledAnnotation.put(fieldDataTransmissionSharingAttributeList, sharingAttributes);

        super.doOKAction();
        callback.onOk(filledAnnotation);
    }

    @Override
    public void doCancelAction() {
        super.doCancelAction();
        callback.onCancel();
    }

    public DataTransmissionSelectorDialog(@Nullable Project project, AnnotationHolder preFilledAnnotation,
                                          Callback callback) {
        super(project, false);
        this.project = project;
        this.preFilledAnnotation = preFilledAnnotation;
        this.callback = callback;
        setTitle("Annotate Data Transmission Instance");
        init();
        setOKButtonText("OK");
        setCancelButtonText("Cancel");
    }

    static class JCheckBoxList extends JBList<JCheckBox> {
        protected Border noFocusBorder = JBUI.Borders.empty(1);

        @Override
        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            for (int i = 0 ; i < getModel().getSize() ; i++) {
                getModel().getElementAt(i).setEnabled(enabled);
            }
        }

        public void clear() {
            setSelectedIndices(new int[0]);
            for (int i = 0 ; i < getModel().getSize() ; i++) {
                getModel().getElementAt(i).setSelected(false);
            }
        }

        public JCheckBoxList() {
            setCellRenderer(new CellRenderer());
            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    int index = locationToIndex(e.getPoint());
                    if (index != -1) {
                        JCheckBox checkbox = getModel().getElementAt(index);
                        if (checkbox.isEnabled()) {
                            checkbox.setSelected(!checkbox.isSelected());
                            repaint();
                        }
                    }
                }
            });
            setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        }

        public JCheckBoxList(ListModel<JCheckBox> model){
            this();
            setModel(model);
        }

        protected class CellRenderer implements ListCellRenderer<JCheckBox> {
            public Component getListCellRendererComponent(
                    JList<? extends JCheckBox> list, JCheckBox value, int index,
                    boolean isSelected, boolean cellHasFocus) {

                //Drawing checkbox, change the appearance here
                value.setBackground(isSelected ? getSelectionBackground()
                        : getBackground());
                value.setForeground(isSelected ? getSelectionForeground()
                        : getForeground());
                value.setEnabled(isEnabled());
                value.setFont(getFont());
                value.setFocusPainted(false);
                value.setBorderPainted(true);
                value.setBorder(isSelected ? UIManager
                        .getBorder("List.focusCellHighlightBorder") : noFocusBorder);
                return value;
            }
        }
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel dataIdPanel = new JPanel();
        dataIdPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Data Access Ids"));
        dataIdPanel.setLayout(new BoxLayout(dataIdPanel, BoxLayout.X_AXIS));

        JPanel dataIdCheckBoxPanel = new JPanel();
        dataIdCheckBoxPanel.setLayout(new BoxLayout(dataIdCheckBoxPanel, BoxLayout.Y_AXIS));
        JTextArea idTextArea = new JTextArea("What are the access id(s) of the data source that you are transmitting data from?");
        idTextArea.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        idTextArea.setEditable(false);
        idTextArea.setBackground(UIUtil.getPanelBackground());
        noDataTransmissionCheckBox = new JCheckBox(noDataTransmission);
        idTextArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        noDataTransmissionCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        dataIdCheckBoxPanel.add(idTextArea);
        dataIdCheckBoxPanel.add(noDataTransmissionCheckBox);

        DefaultListModel<JCheckBox> model = new DefaultListModel<>();
        dataIdList = new JCheckBoxList(model);
        dataIdList.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        ArrayList<String> idOptions = CodeInspectionUtil.getAllUniqueSourceIds(project);
        if (idOptions.isEmpty()) {
            idOptions.add("No available id. Please add @DataAccess where your app accesses user data.");
            dataIdList.setEnabled(false);
        }
        for (String id : idOptions) {
            PersonalDataGroup[] dataGroups = CodeInspectionUtil.getDataTypesByAccessIds(new String[]{id}, project);
            model.addElement(new JCheckBox(String.format("%s (%s)", id,
                    Arrays.stream(dataGroups).map(PersonalDataGroup::name).sorted().collect(Collectors.joining(", ")))));
        }

        noDataTransmissionCheckBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                dataIdList.setEnabled(false);
                dataIdList.clear();
            } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                if (idOptions.size() > 1 || !noAvailableId.equals(idOptions.get(0))) {
                    dataIdList.setEnabled(true);
                }
            }
        });

        Font font = dataIdList.getFont();
        font = new Font(font.getName(), font.getStyle(), font.getSize() + 1);
        idTextArea.setFont(font);

        dataIdPanel.add(dataIdCheckBoxPanel);
        JScrollPane dataIdScrollPane = new JBScrollPane(dataIdList);
        dataIdScrollPane.setPreferredSize(new Dimension(650, 100));
        dataIdScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        dataIdPanel.add(dataIdScrollPane);

        panel.add(dataIdPanel);

        panel.add(Box.createVerticalGlue());

        collectionPanel = new CollectionPanel(font);

        sharingPanel = new SharingPanel(font);

        JPanel collectionSharingPanel = new JPanel();
        collectionSharingPanel.setLayout(new BoxLayout(collectionSharingPanel, BoxLayout.Y_AXIS));
        collectionSharingPanel.add(collectionPanel);
        collectionSharingPanel.add(sharingPanel);

        panel.add(collectionSharingPanel);

        for (int i = 0 ; i < model.getSize() ; i++) {
            model.getElementAt(i).addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED || e.getStateChange() == ItemEvent.DESELECTED) {
                    boolean anySelected = false;
                    for (int j = 0 ; j < dataIdList.getModel().getSize() ; j++) {
                        anySelected |= dataIdList.getModel().getElementAt(j).isSelected();
                    }
                    if (anySelected) {
                        collectionPanel.setEnabled(true);
                        sharingPanel.setEnabled(true);
                    } else {
                        collectionPanel.setEnabled(false);
                        sharingPanel.setEnabled(false);
                    }
                }
            });

        }

//        JPanel resultPanel = new JPanel();
//        resultPanel.setBorder(BorderFactory.createTitledBorder(
//                BorderFactory.createEtchedBorder(), "Result"));
//        JTextArea resultTextArea = new JTextArea(generateDataUsageDescription());
//        resultTextArea.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
//        resultTextArea.setEditable(false);
//        resultTextArea.setBackground(UIUtil.getPanelBackground());
//        // set font color to blue
//        Map<TextAttribute, Object> textAttributeMap = new HashMap<>();
//        textAttributeMap.put(TextAttribute.FOREGROUND, JBColor.BLUE);
//        resultTextArea.setFont(font.deriveFont(textAttributeMap));
//        resultPanel.add(resultTextArea);
//
//        panel.add(resultPanel);

        return panel;
    }

    private String generateDataUsageDescription() {
        return "Nationality data is collected and shared here. Data sharing can be exempt from being disclosed on the safety label because 2 sharing criteria is matched.";
    }

    class SharingPanel extends JPanel {
        JRadioButton sharedYes;
        JRadioButton sharedNo;
        ButtonGroup sharedGroup;

        PurposePanel sharedPurposeListPanel;

        JRadioButton serviceProviderYes;
        JRadioButton serviceProviderNo;
        ButtonGroup serviceProviderGroup;

        JRadioButton legalYes;
        JRadioButton legalNo;
        ButtonGroup legalGroup;

        JRadioButton userSharingYes;
        JRadioButton userSharingNo;
        ButtonGroup userSharingGroup;

        JRadioButton anonymousYes;
        JRadioButton anonymousNo;
        ButtonGroup anonymousGroup;

        public SharingPanel(Font font) {
            setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), "Data sharing"));
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            boolean initialEnabled = false;

            JPanel sharedQuestionPanel = new JPanel();
            sharedYes = new JRadioButton("Yes");
            sharedNo = new JRadioButton("No");
            if (preFilledAnnotation.containsPair(fieldDataTransmissionSharingAttributeList,
                    "sharingAttribute." + SharingAttribute.SharedWithThirdParty_True)) {
                sharedYes.setSelected(true);
                initialEnabled = true;
            }

            sharedGroup = CreateBinaryQuestionPanel(this, font, sharedQuestionPanel, sharedYes, sharedNo,
                    initialEnabled,
                    "Does your app share the data with any third party (including sharing other apps)?");

            JPanel sharingPurposePanel = new JPanel();

            sharedPurposeListPanel = new PurposePanel(initialEnabled);

            CreatePurposeSelectionPanel(this, font, sharingPurposePanel, sharedPurposeListPanel,
                    "Select data sharing purpose(s):");

            JTextArea sharingExemptTextArea = new JTextArea("Check data sharing exemption criteria below:");
            sharingExemptTextArea.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 10));
            sharingExemptTextArea.setEditable(false);
            sharingExemptTextArea.setBackground(UIUtil.getPanelBackground());
            Map<TextAttribute, Integer> fontAttributes = new HashMap<>();
            fontAttributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
            sharingExemptTextArea.setFont(font.deriveFont(fontAttributes));
            add(sharingExemptTextArea);

            JPanel serviceProviderQuestionPanel = new JPanel();
            serviceProviderYes = new JRadioButton("Yes");
            serviceProviderNo = new JRadioButton("No");
            serviceProviderGroup = CreateBinaryQuestionPanel(this, font, serviceProviderQuestionPanel,
                    serviceProviderYes, serviceProviderNo, initialEnabled,
                    "Does the third party only process the data on behalf of the developer and based on the developer’s instructions?");

            JPanel legalQuestionPanel = new JPanel();
            legalYes = new JRadioButton("Yes");
            legalNo = new JRadioButton("No");
            legalGroup = CreateBinaryQuestionPanel(this, font, legalQuestionPanel,
                    legalYes, legalNo, initialEnabled,
                    "Is the data only shared for legal purposes such as in response to a legal obligation or government requests?");

            JPanel userSharingQuestionPanel = new JPanel();
            userSharingYes = new JRadioButton("Yes");
            userSharingNo = new JRadioButton("No");
            userSharingGroup = CreateBinaryQuestionPanel(this, font, userSharingQuestionPanel,
                    userSharingYes, userSharingNo, initialEnabled,
                    "Is the data shared only after getting the user's consent or initiated by the user?");

            JPanel anonymousQuestionPanel = new JPanel();
            anonymousYes = new JRadioButton("Yes");
            anonymousNo = new JRadioButton("No");
            anonymousGroup = CreateBinaryQuestionPanel(this, font, anonymousQuestionPanel,
                    anonymousYes, anonymousNo, initialEnabled,
                    "Is the data fully anonymized before sharing (i.e., can be reassociated with the user)?");

            sharedYes.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    setSharingAttributeQuestionEnabled(true);
                }
            });
            sharedNo.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    setSharingAttributeQuestionEnabled(false);
                }
            });
        }

        public void setEnabled(boolean enabled) {
            if (enabled) {
                setSharingQuestionEnabled(true);
            } else {
                setSharingQuestionEnabled(false);
                setSharingAttributeQuestionEnabled(false);
            }
        }

        public void setSharingQuestionEnabled(boolean enabled) {
            if (enabled) {
                sharedYes.setEnabled(true);
                sharedNo.setEnabled(true);
            } else {
                sharedYes.setEnabled(false);
                sharedNo.setEnabled(false);
                sharedGroup.clearSelection();
            }
        }

        public void setSharingAttributeQuestionEnabled(boolean enabled) {
            if (enabled) {
                sharedPurposeListPanel.setEnabled(true);
                serviceProviderYes.setEnabled(true);
                serviceProviderNo.setEnabled(true);
                legalYes.setEnabled(true);
                legalNo.setEnabled(true);
                userSharingYes.setEnabled(true);
                userSharingNo.setEnabled(true);
                anonymousYes.setEnabled(true);
                anonymousNo.setEnabled(true);
            } else {
                sharedPurposeListPanel.setEnabled(false);
                sharedPurposeListPanel.clear();
                serviceProviderYes.setEnabled(false);
                serviceProviderNo.setEnabled(false);
                legalYes.setEnabled(false);
                legalNo.setEnabled(false);
                userSharingYes.setEnabled(false);
                userSharingNo.setEnabled(false);
                anonymousYes.setEnabled(false);
                anonymousNo.setEnabled(false);
                serviceProviderGroup.clearSelection();
                legalGroup.clearSelection();
                userSharingGroup.clearSelection();
                anonymousGroup.clearSelection();
            }
        }

        public ArrayList<String> getAttributes() {
            ArrayList<String> attributes = new ArrayList<>();
            if (sharedNo.isSelected()) {
                attributes.add(SharingAttribute.SharedWithThirdParty_False);
            } else {
                attributes.add(SharingAttribute.SharedWithThirdParty_True);
                attributes.addAll(sharedPurposeListPanel.getSelectedPurposes(false));
                attributes.add(serviceProviderYes.isSelected() ?
                        SharingAttribute.OnlySharedWithServiceProviders_True : SharingAttribute.OnlySharedWithServiceProviders_False);
                attributes.add(legalYes.isSelected() ?
                        SharingAttribute.OnlySharedForLegalPurposes_True : SharingAttribute.OnlySharedForLegalPurposes_False);
                attributes.add(userSharingYes.isSelected() ?
                        SharingAttribute.OnlyInitiatedByUser_True : SharingAttribute.OnlyInitiatedByUser_False);
                attributes.add(userSharingYes.isSelected() ?
                        SharingAttribute.OnlyAfterGettingUserConsent_True : SharingAttribute.OnlyAfterGettingUserConsent_False);
                attributes.add(anonymousYes.isSelected() ?
                        SharingAttribute.OnlyTransferringAnonymousData_True : SharingAttribute.OnlyTransferringAnonymousData_False);
            }
            return attributes.stream().map(s -> "sharingAttribute." + s).collect(Collectors.toCollection(ArrayList::new));
        }

        public boolean isCompleteSelection() {
            if (sharedNo.isSelected()) {
                return true;
            }
            if (sharedPurposeListPanel.getSelectedPurposes(false).isEmpty()) {
                return false;
            }
            if (serviceProviderGroup.getSelection() == null) {
                return false;
            }
            if (legalGroup.getSelection() == null) {
                return false;
            }
            if (userSharingGroup.getSelection() == null) {
                return false;
            }
            return anonymousGroup.getSelection() != null;
        }
    }

    static class PurposePanel extends JPanel {
        private final JCheckBox appFunctionality;
        private final JCheckBox analytics;
        private final JCheckBox accountManagement;
        private final JCheckBox advertisingOrMarketing;
        private final JCheckBox developerCommunications;
        private final JCheckBox fraudPreventionAndSecurityAndCompliance;
        private final JCheckBox personalization;
        public PurposePanel(boolean initialEnabled) {
            super(new FlowLayout(FlowLayout.LEFT));
            appFunctionality = new JCheckBox(CoconutUIUtil.prettifyCamelCaseString(
                    CollectionAttribute.ForAppFunctionality.split("\\.")[1]));
            appFunctionality.setEnabled(initialEnabled);
            add(appFunctionality);
            analytics = new JCheckBox(CoconutUIUtil.prettifyCamelCaseString(
                    CollectionAttribute.ForAnalytics.split("\\.")[1]));
            analytics.setEnabled(initialEnabled);
            add(analytics);
            accountManagement = new JCheckBox(CoconutUIUtil.prettifyCamelCaseString(
                    CollectionAttribute.ForAccountManagement.split("\\.")[1]));
            accountManagement.setEnabled(initialEnabled);
            add(accountManagement);
            advertisingOrMarketing = new JCheckBox(CoconutUIUtil.prettifyCamelCaseString(
                    CollectionAttribute.ForAdvertisingOrMarketing.split("\\.")[1]));
            advertisingOrMarketing.setEnabled(initialEnabled);
            add(advertisingOrMarketing);
            developerCommunications = new JCheckBox(CoconutUIUtil.prettifyCamelCaseString(
                    CollectionAttribute.ForDeveloperCommunications.split("\\.")[1]));
            developerCommunications.setEnabled(initialEnabled);
            add(developerCommunications);
            fraudPreventionAndSecurityAndCompliance = new JCheckBox(CoconutUIUtil.prettifyCamelCaseString(
                    CollectionAttribute.ForFraudPreventionAndSecurityAndCompliance.split("\\.")[1]));
            fraudPreventionAndSecurityAndCompliance.setEnabled(initialEnabled);
            add(fraudPreventionAndSecurityAndCompliance);
            personalization = new JCheckBox(CoconutUIUtil.prettifyCamelCaseString(
                    CollectionAttribute.ForPersonalization.split("\\.")[1]));
            personalization.setEnabled(initialEnabled);
            add(personalization);
        }

        public void setEnabled(boolean enabled) {
            appFunctionality.setEnabled(enabled);
            analytics.setEnabled(enabled);
            accountManagement.setEnabled(enabled);
            advertisingOrMarketing.setEnabled(enabled);
            developerCommunications.setEnabled(enabled);
            fraudPreventionAndSecurityAndCompliance.setEnabled(enabled);
            personalization.setEnabled(enabled);
        }

        public void clear() {
            appFunctionality.setSelected(false);
            analytics.setSelected(false);
            accountManagement.setSelected(false);
            advertisingOrMarketing.setSelected(false);
            developerCommunications.setSelected(false);
            fraudPreventionAndSecurityAndCompliance.setSelected(false);
            personalization.setSelected(false);
        }

        public ArrayList<String> getSelectedPurposes(boolean isCollection) {
            ArrayList<String> purposes = new ArrayList<>();
            if (isCollection) {
                if (appFunctionality.isSelected()) {
                    purposes.add(CollectionAttribute.ForAppFunctionality);
                }
                if (analytics.isSelected()) {
                    purposes.add(CollectionAttribute.ForAnalytics);
                }
                if (accountManagement.isSelected()) {
                    purposes.add(CollectionAttribute.ForAccountManagement);
                }
                if (advertisingOrMarketing.isSelected()) {
                    purposes.add(CollectionAttribute.ForAdvertisingOrMarketing);
                }
                if (developerCommunications.isSelected()) {
                    purposes.add(CollectionAttribute.ForDeveloperCommunications);
                }
                if (fraudPreventionAndSecurityAndCompliance.isSelected()) {
                    purposes.add(CollectionAttribute.ForFraudPreventionAndSecurityAndCompliance);
                }
                if (personalization.isSelected()) {
                    purposes.add(CollectionAttribute.ForPersonalization);
                }
            } else {
                if (appFunctionality.isSelected()) {
                    purposes.add(SharingAttribute.ForAppFunctionality);
                }
                if (analytics.isSelected()) {
                    purposes.add(SharingAttribute.ForAnalytics);
                }
                if (accountManagement.isSelected()) {
                    purposes.add(SharingAttribute.ForAccountManagement);
                }
                if (advertisingOrMarketing.isSelected()) {
                    purposes.add(SharingAttribute.ForAdvertisingOrMarketing);
                }
                if (developerCommunications.isSelected()) {
                    purposes.add(SharingAttribute.ForDeveloperCommunications);
                }
                if (fraudPreventionAndSecurityAndCompliance.isSelected()) {
                    purposes.add(SharingAttribute.ForFraudPreventionAndSecurityAndCompliance);
                }
                if (personalization.isSelected()) {
                    purposes.add(SharingAttribute.ForPersonalization);
                }
            }
            return purposes;
        }
    }

    class CollectionPanel extends JPanel {
        JRadioButton collectedYes;
        JRadioButton collectedNo;
        ButtonGroup collectedGroup;

        PurposePanel collectedPurposeListPanel;

        JRadioButton optional;
        JRadioButton required;
        ButtonGroup optionalRequiredGroup;

        JRadioButton storedYes;
        JRadioButton storedNo;
        ButtonGroup storedGroup;

        JRadioButton encryptedYes;
        JRadioButton encryptedNo;
        ButtonGroup encryptedGroup;

        JRadioButton userDeleteYes;
        JRadioButton userDeleteNo;
        ButtonGroup userDeleteGroup;

        JRadioButton userToUserEncryptionYes;
        JRadioButton userToUserEncryptionNo;
        ButtonGroup userToUserEncryptionGroup;

        CollectionPanel(Font font) {
            setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), "Data collection"));
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            boolean initialEnabled = false;

            JPanel collectedQuestionPanel = new JPanel();
            collectedYes = new JRadioButton("Yes");
            collectedNo = new JRadioButton("No");
            if (preFilledAnnotation.containsPair(fieldDataTransmissionCollectionAttributeList,
                    "collectionAttribute."+CollectionAttribute.TransmittedOffDevice_True)) {
                collectedYes.setSelected(true);
                initialEnabled = true;
            }
            collectedGroup = CreateBinaryQuestionPanel(this, font, collectedQuestionPanel, collectedYes, collectedNo,
                    initialEnabled, "Does your app transmit the data out of the device?");

            JPanel collectedPurposePanel = new JPanel();

            collectedPurposeListPanel = new PurposePanel(initialEnabled);
            CreatePurposeSelectionPanel(this, font, collectedPurposePanel, collectedPurposeListPanel,
                    "Select data collection purpose(s):");

            JPanel handlingQuestionPanel = new JPanel();
            optional = new JRadioButton("Optional");
            required = new JRadioButton("Required");
            optionalRequiredGroup = CreateBinaryQuestionPanel(this, font, handlingQuestionPanel,
                    optional, required, initialEnabled,
                    "Is the data collection optional or required for users?");

            JPanel storedQuestionPanel = new JPanel();
            storedYes = new JRadioButton("Yes");
            storedNo = new JRadioButton("No");
            storedGroup = CreateBinaryQuestionPanel(this, font, storedQuestionPanel,
                    storedYes, storedNo, initialEnabled,
                    "Is the data stored off the device?");
            if (preFilledAnnotation.containsPair(fieldDataTransmissionCollectionAttributeList,
                    "collectionAttribute."+CollectionAttribute.NotStoredInBackend_False)) {
                storedYes.setSelected(true);
            }

            JPanel encryptedQuestionPanel = new JPanel();
            encryptedYes = new JRadioButton("Yes");
            encryptedNo = new JRadioButton("No");
            encryptedGroup = CreateBinaryQuestionPanel(this, font, encryptedQuestionPanel,
                    encryptedYes, encryptedNo, initialEnabled,
                    "Is the data encrypted in transit?");

            JPanel userDeleteQuestionPanel = new JPanel();
            userDeleteYes = new JRadioButton("Yes");
            userDeleteNo = new JRadioButton("No");
            userDeleteGroup = CreateBinaryQuestionPanel(this, font, userDeleteQuestionPanel,
                    userDeleteYes, userDeleteNo, initialEnabled,
                    "Does your app provide a way for users to request deletion of the data??");

            JTextArea collectionExemptTextArea = new JTextArea("Check data collection exemption criteria below:");
            collectionExemptTextArea.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 10));
            collectionExemptTextArea.setEditable(false);
            collectionExemptTextArea.setBackground(UIUtil.getPanelBackground());
            Map<TextAttribute, Integer> fontAttributes = new HashMap<>();
            fontAttributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
            collectionExemptTextArea.setFont(font.deriveFont(fontAttributes));
            add(collectionExemptTextArea);

            JPanel userToUserEncryptionQuestionPanel = new JPanel();
            userToUserEncryptionYes = new JRadioButton("Yes");
            userToUserEncryptionNo = new JRadioButton("No");
            userToUserEncryptionGroup = CreateBinaryQuestionPanel(this, font,
                    userToUserEncryptionQuestionPanel, userToUserEncryptionYes, userToUserEncryptionNo,
                    initialEnabled, "Is the data encrypted and decrypted on the user end?");

            collectedYes.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    setCollectionAttributeQuestionEnabled(true);
                }
            });
            collectedNo.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    setCollectionAttributeQuestionEnabled(false);
                }
            });
        }

        public void setEnabled(boolean enabled) {
            if (enabled) {
                setCollectionQuestionEnabled(true);
            } else {
                setCollectionQuestionEnabled(false);
                setCollectionAttributeQuestionEnabled(false);
            }
        }

        public void setCollectionQuestionEnabled(boolean enabled) {
            if (enabled) {
                collectedYes.setEnabled(true);
                collectedNo.setEnabled(true);
            } else {
                collectedYes.setEnabled(false);
                collectedNo.setEnabled(false);
                collectedGroup.clearSelection();
            }
        }

        public void setCollectionAttributeQuestionEnabled(boolean enabled) {
            if (enabled) {
                collectedPurposeListPanel.setEnabled(true);
                optional.setEnabled(true);
                required.setEnabled(true);
                storedYes.setEnabled(true);
                storedNo.setEnabled(true);
                encryptedYes.setEnabled(true);
                encryptedNo.setEnabled(true);
                userDeleteYes.setEnabled(true);
                userDeleteNo.setEnabled(true);
                userToUserEncryptionYes.setEnabled(true);
                userToUserEncryptionNo.setEnabled(true);
            } else {
                collectedPurposeListPanel.setEnabled(false);
                collectedPurposeListPanel.clear();
                optional.setEnabled(false);
                required.setEnabled(false);
                storedYes.setEnabled(false);
                storedNo.setEnabled(false);
                encryptedYes.setEnabled(false);
                encryptedNo.setEnabled(false);
                userDeleteYes.setEnabled(false);
                userDeleteNo.setEnabled(false);
                userToUserEncryptionYes.setEnabled(false);
                userToUserEncryptionNo.setEnabled(false);
                optionalRequiredGroup.clearSelection();
                storedGroup.clearSelection();
                encryptedGroup.clearSelection();
                userDeleteGroup.clearSelection();
                userToUserEncryptionGroup.clearSelection();
            }
        }

        public ArrayList<String> getAttributes() {
            ArrayList<String> attributes = new ArrayList<>();
            if (collectedNo.isSelected()) {
                attributes.add(CollectionAttribute.TransmittedOffDevice_False);
            } else {
                attributes.add(CollectionAttribute.TransmittedOffDevice_True);
                attributes.addAll(collectedPurposeListPanel.getSelectedPurposes(true));
                attributes.add(optional.isSelected() ?
                        CollectionAttribute.OptionalCollection_True : CollectionAttribute.OptionalCollection_False);
                attributes.add(storedYes.isSelected() ?
                        CollectionAttribute.NotStoredInBackend_False : CollectionAttribute.NotStoredInBackend_True);
                attributes.add(encryptedYes.isSelected() ?
                        CollectionAttribute.EncryptionInTransit_True : CollectionAttribute.EncryptionInTransit_False);
                attributes.add(userDeleteYes.isSelected() ?
                        CollectionAttribute.UserRequestDelete_True : CollectionAttribute.UserRequestDelete_False);
                attributes.add(userToUserEncryptionYes.isSelected() ?
                        CollectionAttribute.UserToUserEncryption_True : CollectionAttribute.UserToUserEncryption_False);
            }
            return attributes.stream().map(s -> "collectionAttribute." + s).collect(Collectors.toCollection(ArrayList::new));
        }

        public boolean isCompleteSelection() {
            if (collectedNo.isSelected()) {
                return true;
            }
            if (collectedPurposeListPanel.getSelectedPurposes(true).isEmpty()) {
                return false;
            }
            if (optionalRequiredGroup.getSelection() == null) {
                return false;
            }
            if (storedGroup.getSelection() == null) {
                return false;
            }
            if (encryptedGroup.getSelection() == null) {
                return false;
            }
            if (userDeleteGroup.getSelection() == null) {
                return false;
            }
            return userToUserEncryptionGroup.getSelection() != null;
        }
    }

    private void CreatePurposeSelectionPanel(JPanel parentPanel, Font font, JPanel questionPanel,
                                             JPanel purposeList, String questionText) {
        questionPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 10));
        questionPanel.setLayout(new BoxLayout(questionPanel, BoxLayout.Y_AXIS));

        JTextArea questionTextArea = new JTextArea(questionText);
        questionTextArea.setEditable(false);
        questionTextArea.setBackground(UIUtil.getPanelBackground());
        questionTextArea.setFont(font);

        questionPanel.add(questionTextArea);
        questionPanel.add(purposeList);

        parentPanel.add(questionPanel);
    }

    private ButtonGroup CreateBinaryQuestionPanel(JPanel parentPanel, Font font, JPanel questionPanel,
                                           JRadioButton yes, JRadioButton no, boolean initialEnabled,
                                                  String questionText) {
        questionPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 10));
        questionPanel.setLayout(new BoxLayout(questionPanel, BoxLayout.X_AXIS));

        JTextArea questionTextArea = new JTextArea(questionText);
        questionTextArea.setEditable(false);
        questionTextArea.setBackground(UIUtil.getPanelBackground());
        questionTextArea.setFont(font);

        ButtonGroup choiceGroup = new ButtonGroup();
        yes.setEnabled(initialEnabled);
        no.setEnabled(initialEnabled);
        choiceGroup.add(yes);
        choiceGroup.add(no);

        questionPanel.add(questionTextArea);
        questionPanel.add(yes);
        questionPanel.add(no);

        parentPanel.add(questionPanel);
        return choiceGroup;
    }
}
