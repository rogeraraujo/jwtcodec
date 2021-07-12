/*
 * Copyright (c) 2021, Roger Araújo, All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */

package com.rogeraraujo.jwtcodec.gui;

import com.rogeraraujo.jwtcodec.Utils;
import com.rogeraraujo.jwtcodec.components.*;
import com.rogeraraujo.jwtcodec.jwt.*;
import io.fusionauth.jwt.JWTUtils;
import io.fusionauth.jwt.Signer;
import io.fusionauth.jwt.Verifier;
import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.jwt.domain.KeyPair;
import io.fusionauth.jwt.ec.ECSigner;
import io.fusionauth.jwt.ec.ECVerifier;
import io.fusionauth.jwt.hmac.HMACSigner;
import io.fusionauth.jwt.hmac.HMACVerifier;
import io.fusionauth.jwt.rsa.RSAPSSSigner;
import io.fusionauth.jwt.rsa.RSAPSSVerifier;
import io.fusionauth.jwt.rsa.RSASigner;
import io.fusionauth.jwt.rsa.RSAVerifier;
import lombok.extern.slf4j.Slf4j;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This is the main window of JWT Codec.
 */
@Slf4j
public class MainWindow extends JFrame {
    private final String DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private static final int EXCEPTION_DLG_BOX_WIDTH = 600;
    private static final int EXCEPTION_DLG_BOX_HEIGHT = 500;

    // Encoding panel.
    // Claims panel
    private JCheckBox ckbIssuer;
    private JLabel lblIssuer;
    private JTextField jtfEncIssuer;

    private JCheckBox ckbSubject;
    private JLabel lblSubject;
    private JTextField jtfEncSubject;

    private JCheckBox ckbAudience;
    private JLabel lblAudience;
    private JTextField jtfEncAudience;

    private JCheckBox ckbIssuedAt;
    private JLabel lblIssuedAt;
    private JSpinner spnIssuedAt;
    private JButton btnIssuedAtNow;

    private JCheckBox ckbExpiration;
    private JLabel lblExpiration;
    private JSpinner spnExpiration;
    private JButton btnExpirationNow;

    private JCheckBox ckbNotBefore;
    private JLabel lblNotBefore;
    private JSpinner spnNotBefore;
    private JButton btnNotBeforeNow;

    private JCheckBox ckbUniqueId;
    private JLabel lblUniqueId;
    private JTextField jtfEncUniqueId;
    private JButton btnGenerateUniqueId;

    private JLabel lblOtherClaimsSelData;
    private ListTableModel<JwtClaim> encOtherClaimsModel;
    private CustomTable tblEncOtherClaims;

    // Signature panel
    private JComboBox<ComboBoxItem<JwtSignatureAlgorithm>> cboEncSignatureAlgo;
    private JLabel lblEncSignaturePrivateKey;
    private JTextArea jtaEncSignaturePrivateKey;
    private JButton btnEncSignatureLoadPrivateKey;

    // Encoded token panel
    private JTextArea jtaEncodedTokenOutput;

    // Decoding panel
    private JComboBox<ComboBoxItem<JwtSignatureAlgorithm>> cboDecSignatureAlgo;
    private JLabel lblDecSignaturePublicKey;
    private JTextArea jtaDecSignaturePublicKey;
    private JButton btnDecSignatureLoadPublicKey;
    private JTextArea jtaEncodedTokenInput;

    private JTextField jtfDecIssuer;
    private JTextField jtfDecSubject;
    private JTextField jtfDecAudience;
    private JTextField jtfDecIssuedAt;
    private JTextField jtfDecExpiration;
    private JTextField jtfDecNotBefore;
    private JTextField jtfDecUniqueId;
    private JTextArea jtaDecOtherClaims;

    // Random key generation panel
    private JComboBox<ComboBoxItem<KeyGenerationType>> cboKeyGenerationType;
    private JButton btnGenerateKey;

    private JLabel lblKgPrivateKey;
    private JTextArea jtaKgPrivateKey;
    private JButton btnKgSavePrivateKey;

    private JLabel lblKgPublicKey;
    private JTextArea jtaKgPublicKey;
    private JButton btnKgCopyPublicKey;
    private JButton btnKgSavePublicKey;

    // Other variables
    private File defaultPemFileDir;

    private JFileChooser fileChooser;

    private File encPublicKeyFile;
    private File decPublicKeyFile;
    private File kgPrivateKeyFile;
    private File kgPublicKeyFile;

    public static MainWindow createInstance(Properties config) {
        MainWindow result = new MainWindow();
        result.configure(config);
        SwingUtils.centerWindow(result, null);

        return result;
    }

    private MainWindow() {
        super(SwingUtils.APP_WINDOW_TITLE);

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent event) {
                confirmExit();
            }});

        setLayout(new MigLayout(
            "", "[grow, fill]", "[grow, fill] []"));

        JTabbedPane tabPane = new JTabbedPane(
            JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        tabPane.addTab("Encoding", createEncodingPanel());
        tabPane.addTab("Decoding", createDecodingPanel());
        tabPane.addTab("Random Key Generation", createKeyGenerationPanel());

        add(tabPane, "growx, growy, wrap");
        add(createBottomPanel());

        setSize(800, 600);
    }

    private void confirmExit() {
        int answer = JOptionPane.showConfirmDialog(
            this, "Close JWT Codec?",
            SwingUtils.APP_WINDOW_TITLE, JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);

        if (answer == JOptionPane.YES_OPTION) {
            setVisible(false);
            dispose();
        }
    }

    private void configure(Properties config) {
        // Sets window size
        Integer width = Utils.stringToInt(
            config.getProperty("main-window-width", "-1"));
        Integer height = Utils.stringToInt(
            config.getProperty("main-window-height", "-1"));

        if ((width  != null) && (width  > 0) &&
            (height != null) && (height > 0)) {
            setSize(width, height);
        }

        // Sets default claim values
        jtfEncIssuer.setText(config.getProperty("default-claim-issuer", ""));
        jtfEncSubject.setText(config.getProperty("default-claim-subject", ""));
        jtfEncAudience.setText(config.getProperty("default-claim-audience", ""));

        // Other values
        String str = config.getProperty("default-pem-file-directory", "");
        defaultPemFileDir = Utils.stringIsEmptyOrBlank(str) ?
            null : new File(str);
    }

    private JPanel createEncodingPanel() {
        JPanel result = new JPanel(new MigLayout("",
            "[0:0, grow 33, fill] [pref!] " +
            "[0:0, grow 33, fill] [pref!] " +
            "[0:0, grow 33, fill]",
            "[grow, fill]"));

        result.add(createEncodingClaimsPanel(), "w 0:0");

        JSeparator sep1 = new JSeparator(JSeparator.VERTICAL);
        sep1.setForeground(Color.BLACK);

        result.add(sep1);

        result.add(createEncodingSignaturePanel(), "w 0:0");

        JSeparator sep2 = new JSeparator(JSeparator.VERTICAL);
        sep2.setForeground(Color.BLACK);

        result.add(sep2);

        result.add(createEncodedTokenPanel(), "w 0:0");

        return result;
    }

    private JPanel createEncodingClaimsPanel() {
        JPanel result = new JPanel(new MigLayout(
            "insets 0", "[grow, fill]", "[grow, fill]"));

        JTabbedPane tabPane = new JTabbedPane(
            JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        tabPane.addTab("Main Claims", createMainEncodingClaimsPanel());
        tabPane.addTab("Other Claims", createOtherEncodingClaimsPanel());

        result.add(tabPane, "growx, growy");

        return result;
    }

    private JPanel createMainEncodingClaimsPanel() {
        JPanel result = new JPanel(new MigLayout(
            "", "[] [grow]", ""));

        String NOW_BUTTON_TEXT = "";
        String NOW_BUTTON_ICON_RESOURCE_NAME = "icons/silk/time.png";
        String NOW_BUTTON_TOOLTIP_TEXT = "Use current date/time for this claim";

        ckbIssuer = SwingUtils.createCheckBox("", true, this::processCkbIssuer);
        lblIssuer = new JLabel("Issuer (iss):");
        lblIssuer.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent event) {
                ckbIssuer.setSelected(!ckbIssuer.isSelected());
            }});
        jtfEncIssuer = SwingUtils.createTextField(
            new LimitedDocument(10000), "", 0, true);

        result.add(ckbIssuer);
        result.add(lblIssuer, "wrap");
        result.add(jtfEncIssuer, "skip 1, growx, wrap");

        ckbSubject = SwingUtils.createCheckBox("", true, this::processCkbSubject);
        lblSubject = new JLabel("Subject (sub):");
        lblSubject.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent event) {
                ckbSubject.setSelected(!ckbSubject.isSelected());
            }});
        jtfEncSubject = SwingUtils.createTextField(
            new LimitedDocument(10000), "", 0, true);

        result.add(ckbSubject);
        result.add(lblSubject, "wrap");
        result.add(jtfEncSubject, "skip 1, growx, wrap");

        ckbAudience = SwingUtils.createCheckBox("", true, this::processCkbAudience);
        lblAudience = new JLabel("Audience (aud):");
        lblAudience.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent event) {
                ckbAudience.setSelected(!ckbAudience.isSelected());
            }});
        jtfEncAudience = SwingUtils.createTextField(
            new LimitedDocument(10000), "", 0, true);

        result.add(ckbAudience);
        result.add(lblAudience, "wrap");
        result.add(jtfEncAudience, "skip 1, growx,wrap");

        ZonedDateTime zdtNow = ZonedDateTime.now();
        Date dtNow = Date.from(zdtNow.toInstant());

        ckbIssuedAt = SwingUtils.createCheckBox(
            "", true, this::processCkbIssuedAt);
        lblIssuedAt = new JLabel("Issued At (iat):");
        lblIssuedAt.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent event) {
                ckbIssuedAt.setSelected(!ckbIssuedAt.isSelected());
            }});
        spnIssuedAt = SwingUtils.createDateSpinner(dtNow, DATE_FORMAT_PATTERN);
        btnIssuedAtNow = SwingUtils.createButton(
            NOW_BUTTON_TEXT, NOW_BUTTON_ICON_RESOURCE_NAME, null,
            NOW_BUTTON_TOOLTIP_TEXT);
        btnIssuedAtNow.addActionListener(this::processBtnIssuedAtNow);

        result.add(ckbIssuedAt);
        result.add(lblIssuedAt, "wrap");
        result.add(spnIssuedAt, "skip 1, split 2, growx");
        result.add(btnIssuedAtNow, "wrap");

        ckbExpiration = SwingUtils.createCheckBox(
            "", true, this::processCkbExpiration);
        lblExpiration = new JLabel("Expiration (exp):");
        lblExpiration.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent event) {
                ckbExpiration.setSelected(!ckbExpiration.isSelected());
            }});
        spnExpiration = SwingUtils.createDateSpinner(
            Date.from(zdtNow.plusMinutes(15L).toInstant()), DATE_FORMAT_PATTERN);
        btnExpirationNow = SwingUtils.createButton(
            NOW_BUTTON_TEXT, NOW_BUTTON_ICON_RESOURCE_NAME, null,
            NOW_BUTTON_TOOLTIP_TEXT);
        btnExpirationNow.addActionListener(this::processBtnExpirationNow);

        result.add(ckbExpiration);
        result.add(lblExpiration, "wrap");
        result.add(spnExpiration, "skip 1, split 2, growx");
        result.add(btnExpirationNow, "wrap");

        ckbNotBefore = SwingUtils.createCheckBox(
            "", true, this::processCkbNotBefore);
        lblNotBefore = new JLabel("Not Valid Before (nbf):");
        lblNotBefore.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent event) {
                ckbNotBefore.setSelected(!ckbNotBefore.isSelected());
            }});
        spnNotBefore = SwingUtils.createDateSpinner(dtNow, DATE_FORMAT_PATTERN);
        btnNotBeforeNow = SwingUtils.createButton(
            NOW_BUTTON_TEXT, NOW_BUTTON_ICON_RESOURCE_NAME, null,
            NOW_BUTTON_TOOLTIP_TEXT);
        btnNotBeforeNow.addActionListener(this::processBtnNotBeforeNow);

        result.add(ckbNotBefore);
        result.add(lblNotBefore, "wrap");
        result.add(spnNotBefore, "skip 1, split 2, growx");
        result.add(btnNotBeforeNow, "wrap");

        ckbUniqueId = SwingUtils.createCheckBox(
            "", true, this::processCkbUniqueId);
        lblUniqueId = new JLabel("Unique ID (jti):");
        lblUniqueId.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent event) {
                ckbUniqueId.setSelected(!ckbUniqueId.isSelected());
            }});
        jtfEncUniqueId = SwingUtils.createTextField(
            new LimitedDocument(10000), "", 0, true);
        btnGenerateUniqueId = SwingUtils.createButton(
            "", "icons/silk/lightning.png", null, "Generate random unique ID");
        btnGenerateUniqueId.addActionListener(this::processBtnGenerateUniqueId);

        result.add(ckbUniqueId);
        result.add(lblUniqueId, "wrap");
        result.add(jtfEncUniqueId, "skip 1, split 2, growx");
        result.add(btnGenerateUniqueId, "wrap");

        // Additional procedures
        processCkbIssuer(generateCheckBoxStateChange(ckbIssuer));
        processCkbSubject(generateCheckBoxStateChange(ckbSubject));
        processCkbAudience(generateCheckBoxStateChange(ckbAudience));
        processCkbIssuedAt(generateCheckBoxStateChange(ckbIssuedAt));
        processCkbExpiration(generateCheckBoxStateChange(ckbExpiration));
        processCkbNotBefore(generateCheckBoxStateChange(ckbNotBefore));
        processCkbUniqueId(generateCheckBoxStateChange(ckbUniqueId));

        return result;
    }

    private ItemEvent generateCheckBoxStateChange(JCheckBox checkBox) {
        return new ItemEvent(checkBox, 0, null,
            SwingUtils.getCheckBoxStateChange(checkBox.isSelected()));
    }

    private void processCkbIssuer(ItemEvent event) {
        boolean selected = (event.getStateChange() == ItemEvent.SELECTED);
        lblIssuer.setEnabled(selected);
        jtfEncIssuer.setEnabled(selected);

        if (selected) {
            jtfEncIssuer.requestFocusInWindow();
        }
    }

    private void processCkbSubject(ItemEvent event) {
        boolean selected = (event.getStateChange() == ItemEvent.SELECTED);
        lblSubject.setEnabled(selected);
        jtfEncSubject.setEnabled(selected);

        if (selected) {
            jtfEncSubject.requestFocusInWindow();
        }
    }

    private void processCkbAudience(ItemEvent event) {
        boolean selected = (event.getStateChange() == ItemEvent.SELECTED);
        lblAudience.setEnabled(selected);
        jtfEncAudience.setEnabled(selected);

        if (selected) {
            jtfEncAudience.requestFocusInWindow();
        }
    }

    private void processCkbIssuedAt(ItemEvent event) {
        boolean selected = (event.getStateChange() == ItemEvent.SELECTED);
        lblIssuedAt.setEnabled(selected);
        spnIssuedAt.setEnabled(selected);
        btnIssuedAtNow.setEnabled(selected);

        if (selected) {
            spnIssuedAt.requestFocusInWindow();
        }
    }

    private void processBtnIssuedAtNow(ActionEvent event) {
        spnIssuedAt.setValue(new Date());
    }

    private void processCkbExpiration(ItemEvent event) {
        boolean selected = (event.getStateChange() == ItemEvent.SELECTED);
        lblExpiration.setEnabled(selected);
        spnExpiration.setEnabled(selected);
        btnExpirationNow.setEnabled(selected);

        if (selected) {
            spnExpiration.requestFocusInWindow();
        }
    }

    private void processBtnExpirationNow(ActionEvent event) {
        spnExpiration.setValue(new Date());
    }

    private void processCkbNotBefore(ItemEvent event) {
        boolean selected = (event.getStateChange() == ItemEvent.SELECTED);
        lblNotBefore.setEnabled(selected);
        spnNotBefore.setEnabled(selected);
        btnNotBeforeNow.setEnabled(selected);

        if (selected) {
            spnNotBefore.requestFocusInWindow();
        }
    }

    private void processBtnNotBeforeNow(ActionEvent event) {
        spnNotBefore.setValue(new Date());
    }

    private void processCkbUniqueId(ItemEvent event) {
        boolean selected = (event.getStateChange() == ItemEvent.SELECTED);
        lblUniqueId.setEnabled(selected);
        jtfEncUniqueId.setEnabled(selected);
        btnGenerateUniqueId.setEnabled(selected);

        if (selected) {
            jtfEncUniqueId.requestFocusInWindow();
        }
    }

    private void processBtnGenerateUniqueId(ActionEvent event) {
        jtfEncUniqueId.setText(UUID.randomUUID().toString());
    }

    private JPanel createOtherEncodingClaimsPanel() {
        JPanel result = new JPanel(new MigLayout(
            "", "[grow, fill]", "[] [grow, fill] []"));

        lblOtherClaimsSelData = new JLabel("?");

        result.add(lblOtherClaimsSelData, "wrap");

        encOtherClaimsModel = new ListTableModel<>(
            new ArrayList<>(), new JwtClaimTableFormat());
        tblEncOtherClaims = new CustomTable(encOtherClaimsModel);
        tblEncOtherClaims.getSelectionModel().addListSelectionListener(
            this::processTblEncOtherClaimsCellSelection);
        tblEncOtherClaims.getColumnModel().getSelectionModel()
            .addListSelectionListener(this::processTblEncOtherClaimsCellSelection);
        tblEncOtherClaims.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent event) {
                processTblEncOtherClaimsMouseClick(event);
            }});

        AbstractAction deleteAction = new AbstractAction() {
            @Override public void actionPerformed(ActionEvent event) {
                if (tblEncOtherClaims.getRowCount() > 0) {
                    processBtnDeleteClaims(event);
                }
            }};
        SwingUtils.bindKeyStrokeToAction(tblEncOtherClaims,
            KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteAction",
            deleteAction);

        result.add(new JScrollPane(tblEncOtherClaims), "growx, growy, wrap");

        JPanel bottomPanel = new JPanel(new MigLayout(
            "insets 0", "[] [] []", ""));

        JButton btnAddClaim = SwingUtils.createButton(
            "Add..", "icons/silk/add.png", null, "Add a new Claim");
        btnAddClaim.addActionListener(this::processBtnAddClaim);

        bottomPanel.add(btnAddClaim, "sizegroup buttons");

        JButton btnEditClaim = SwingUtils.createButton(
            "Edit...", "icons/silk/pencil.png", null, "Edit selected Claim");
        btnEditClaim.addActionListener(this::processBtnEditClaim);

        bottomPanel.add(btnEditClaim, "sizegroup buttons");

        JButton btnDeleteClaims = SwingUtils.createButton(
            "Delete", "icons/silk/delete.png", null, "Delete selected Claim(s)");
        btnDeleteClaims.addActionListener(this::processBtnDeleteClaims);

        bottomPanel.add(btnDeleteClaims, "sizegroup buttons");

        result.add(bottomPanel);

        // Additional procedures
        processTblEncOtherClaimsCellSelection(null);

        return result;
    }

    private void processTblEncOtherClaimsCellSelection(
            ListSelectionEvent event) {
        int selRow = tblEncOtherClaims.getSelectedRow();
        int rowCount = tblEncOtherClaims.getRowCount();
        String message;

        if ((selRow >= 0) && (rowCount > 0)) {
            message = "Claim " + (selRow + 1) + "/" + rowCount;
        }
        else {
            switch (rowCount) {
                case 0:
                    message = "No claims";
                    break;

                case 1:
                    message = "One claim";
                    break;

                default:
                    message = "" + rowCount + " claims";
                    break;
            }
        }

        lblOtherClaimsSelData.setText(message);
    }

    private void processTblEncOtherClaimsMouseClick(MouseEvent event) {
        if ((event.getClickCount() == 2) &&
            (tblEncOtherClaims.getRowCount() > 0)) {
            processBtnEditClaim(null);
        }
    }

    private void processBtnAddClaim(ActionEvent event) {
        // Creates the dialog box
        ClaimEditDialog dlg;

        try {
            dlg = ClaimEditDialog.createInstance(
                this, "New Claim", true, 300, 250, new JwtClaim("", ""));
        } catch (Exception ex) {
            log.error("Error creating window:", ex);
            ExceptionDialog.createInstance(this, true,
                EXCEPTION_DLG_BOX_WIDTH, EXCEPTION_DLG_BOX_HEIGHT,
                "Error creating window.", ex).setVisible(true);
            return;
        }

        // Requests the data for the new claim
        while (true) {
            dlg.setVisible(true);

            if (!dlg.isClickedOk()) {
                return;
            }

            JwtClaim newClaim = dlg.getClaim();

            if (encOtherClaimsModel.contains(newClaim)) {
                SwingUtils.showErrorMessage(this,
                    "A claim with the name \"" + newClaim.getKey() +
                    "\" already exists.\n" +
                    "Please change the claim name and try again.");
                continue;
            }

            break;
        }

        // Inserts the new claim
        int newRowIdx = encOtherClaimsModel.getRowCount();
        encOtherClaimsModel.addRow(dlg.getClaim(), true);
        tblEncOtherClaims.getSelectionModel().setSelectionInterval(
            newRowIdx, newRowIdx);
    }

    private void processBtnEditClaim(ActionEvent event) {
        int selRow = tblEncOtherClaims.getSelectedRow();

        if (selRow == -1) {
            SwingUtils.showErrorMessage(this, "Please select a claim to edit.");
            tblEncOtherClaims.requestFocusInWindow();
            return;
        }

        selRow = tblEncOtherClaims.convertRowIndexToModel(selRow);
        JwtClaim origClaim = encOtherClaimsModel.getRow(selRow);

        // Creates the dialog box
        ClaimEditDialog dlg;

        try {
            dlg = ClaimEditDialog.createInstance(
                this, "Edit Claim", true, 300, 250, origClaim);
        } catch (Exception ex) {
            log.error("Error creating window:", ex);
            ExceptionDialog.createInstance(this, true,
                EXCEPTION_DLG_BOX_WIDTH, EXCEPTION_DLG_BOX_HEIGHT,
                "Error creating window.", ex).setVisible(true);
            return;
        }

        // Requests the new data for them claim
outerWhile:
        while (true) {
            dlg.setVisible(true);

            if (!dlg.isClickedOk()) {
                return;
            }

            JwtClaim newClaim = dlg.getClaim();

            for (JwtClaim claim : encOtherClaimsModel.getItems()) {
                if ((claim != origClaim) &&
                    Utils.safeEquals(newClaim, claim)) {
                    SwingUtils.showErrorMessage(this,
                        "A claim with the name \"" + newClaim.getKey() + "\" " +
                        "already exists.\n" +
                        "Please change the claim name and try again.");
                    continue outerWhile;
                }
            }

            break;
        }

        // Updates the claim
        encOtherClaimsModel.setRow(selRow, dlg.getClaim(), true);
        tblEncOtherClaims.getSelectionModel().setSelectionInterval(
            selRow, selRow);
    }

    private void processBtnDeleteClaims(ActionEvent event) {
        int[] selRows = tblEncOtherClaims.getSelectedRows();

        if ((selRows == null) || (selRows.length < 1)) {
            SwingUtils.showErrorMessage(this,
                "Please select one or more claims to delete.");
            tblEncOtherClaims.requestFocusInWindow();
            return;
        }

        for (int i = 0; i < selRows.length; ++i) {
            selRows[i] = tblEncOtherClaims.convertRowIndexToModel(selRows[i]);
        }

        // Confirms the deletion
        String question;

        if (selRows.length == 1) {
            JwtClaim claim = encOtherClaimsModel.getRow(selRows[0]);
            question = "Are you sure you would like to delete this claim?\n" +
                "Claim name = \"" + claim.getKey() + "\"";
        }
        else {
            question = "Are you sure you would like to delete these claims?";
        }

        int answer = JOptionPane.showConfirmDialog(this,
            question, SwingUtils.APP_WINDOW_TITLE,
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        if (selRows.length == 1) {
            int selRow = selRows[0];

            encOtherClaimsModel.removeRow(selRow, true);
        }
        else {
            Arrays.sort(selRows);

            for (int i = selRows.length - 1; i >= 0; --i) {
                encOtherClaimsModel.removeRow(selRows[i], false);
            }

            encOtherClaimsModel.fireTableDataChanged();
        }

        int rowCount = tblEncOtherClaims.getRowCount();

        if (rowCount > 0) {
            int firstSelRow = selRows[0];

            if (firstSelRow >= rowCount) {
                firstSelRow = Math.max(firstSelRow - 1, 0);
            }

            tblEncOtherClaims.getSelectionModel().setSelectionInterval(
                firstSelRow, firstSelRow);
        }
    }

    private JComboBox<ComboBoxItem<JwtSignatureAlgorithm>>
            createSignatureAlgoComboBox() {
        List<JwtSignatureAlgorithm> comboOptions =
            Arrays.stream(JwtSignatureAlgorithm.values())
                .sorted(new JwtSignatureAlgorithm.ShortNameComparator())
                .collect(Collectors.toList());

        return SwingUtils.createComboBox(
            ComboBoxItem.createComboBoxModel(comboOptions),
            new CustomListCellRenderer.AlternateRowColorTransformer(null), 0);
    }

    private JPanel createEncodingSignaturePanel() {
        JPanel result = new JPanel(new MigLayout(
            "insets 0", "[grow, fill]", "[] [] [grow, fill] []"));

        // Top panel
        JPanel topPanel = new JPanel(new MigLayout(
            "insets 0", "[] []", ""));

        topPanel.add(new JLabel("Signature Algorithm"));

        cboEncSignatureAlgo = createSignatureAlgoComboBox();
        cboEncSignatureAlgo.addActionListener(this::processCboEncSignatureAlgo);

        topPanel.add(cboEncSignatureAlgo);

        result.add(topPanel, "wrap");

        // Secret Key / Private Key
        lblEncSignaturePrivateKey = new JLabel("Secret Key / Private Key:");

        result.add(lblEncSignaturePrivateKey, "wrap");

        jtaEncSignaturePrivateKey = SwingUtils.createTextArea(
            new LimitedDocument(10000), "", 1, 1, true, true, true, true);

        result.add(new JScrollPane(jtaEncSignaturePrivateKey),
            "growx, growy, wrap");

        btnEncSignatureLoadPrivateKey = SwingUtils.createButton(
            "Load From PEM File", "icons/silk/folder_table.png", null, null);
        btnEncSignatureLoadPrivateKey.addActionListener(
            this::processBtnSignatureLoadPublicKey);

        result.add(btnEncSignatureLoadPrivateKey, "w pref!");

        // Additional procedures
        ComboBoxItem.setSelectedItem(cboEncSignatureAlgo,
            JwtSignatureAlgorithm.HMAC_SHA_256);
        processCboEncSignatureAlgo(null);

        return result;
    }

    private void processCboEncSignatureAlgo(ActionEvent event) {
        Object selItem = cboEncSignatureAlgo.getSelectedItem();
        @SuppressWarnings("unchecked")
        JwtSignatureAlgorithm signatureAlgo = (selItem != null) ?
            ((ComboBoxItem<JwtSignatureAlgorithm>) selItem).getItem() : null;

        if (signatureAlgo == null) {
            return;
        }

        boolean isKeyPair = signatureAlgo.isAsymmetric();

        lblEncSignaturePrivateKey.setText(isKeyPair ? "Private Key:" : "Secret Key:");
        btnEncSignatureLoadPrivateKey.setEnabled(isKeyPair);
    }

    private void createFileChooser() {
        if (fileChooser != null) {
            return;
        }

        fileChooser = new JFileChooser();
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.addChoosableFileFilter(
            new FileNameExtensionFilter("All files", "*", "*.*"));

        FileNameExtensionFilter pemFileFilter =
            new FileNameExtensionFilter("PEM files", "pem", "PEM");
        fileChooser.addChoosableFileFilter(pemFileFilter);

        fileChooser.setFileFilter(pemFileFilter);

        if (defaultPemFileDir != null) {
            fileChooser.setCurrentDirectory(defaultPemFileDir);
        }
    }

    private void processBtnSignatureLoadPublicKey(ActionEvent event) {
        createFileChooser();
        fileChooser.setDialogTitle("Choose the key file");
        fileChooser.setSelectedFile(encPublicKeyFile);

        if (fileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        encPublicKeyFile = fileChooser.getSelectedFile();

        try {
            List<String> lines = Files.readAllLines(encPublicKeyFile.toPath());
            jtaEncSignaturePrivateKey.setText(String.join("\n", lines));
        } catch (Exception ex) {
            log.error("Error reading file:", ex);
            ExceptionDialog.createInstance(this, true,
                EXCEPTION_DLG_BOX_WIDTH, EXCEPTION_DLG_BOX_HEIGHT,
                "Error reading file.", ex).setVisible(true);
        }
    }

    private JPanel createEncodedTokenPanel() {
        JPanel result = new JPanel(new MigLayout(
            "insets 0", "[grow, fill]", "[] [] [grow, fill] []"));

        JButton btnEncodeToken = SwingUtils.createButton(
            "Encode Token", "icons/silk/lock.png", null, null);
        btnEncodeToken.addActionListener(this::processBtnEncodeToken);

        result.add(btnEncodeToken, "w pref!, wrap");

        result.add(new JLabel("Encoded Token:"), "wrap");

        jtaEncodedTokenOutput = SwingUtils.createTextArea(
            null, "", 1, 1, false, true, true, true);

        result.add(new JScrollPane(jtaEncodedTokenOutput), "growx, growy, wrap");

        JButton btnCopyEncodedToken = SwingUtils.createButton(
            "Copy", "icons/silk/page_copy.png", null, null);
        btnCopyEncodedToken.addActionListener(this::processBtnCopyEncodedToken);

        result.add(btnCopyEncodedToken, "w pref!");

        return result;
    }

    private void processBtnEncodeToken(ActionEvent event) {
        // Basic validation.
        // Issuer
        if (jtfEncIssuer.isEnabled() &&
            Utils.stringIsEmpty(jtfEncIssuer.getText())) {
            int answer = JOptionPane.showConfirmDialog(this,
                "The issuer is empty. Are you sure you would like to\n" +
                "proceed with an empty issuer?", SwingUtils.APP_WINDOW_TITLE,
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (answer != JOptionPane.YES_OPTION) {
                jtfEncIssuer.requestFocusInWindow();
                return;
            }
        }

        // Subject
        if (jtfEncSubject.isEnabled() &&
            Utils.stringIsEmpty(jtfEncSubject.getText())) {
            int answer = JOptionPane.showConfirmDialog(this,
                "The subject is empty. Are you sure you would like to\n" +
                "proceed with an empty subject?", SwingUtils.APP_WINDOW_TITLE,
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (answer != JOptionPane.YES_OPTION) {
                jtfEncSubject.requestFocusInWindow();
                return;
            }
        }

        // Audience
        if (jtfEncAudience.isEnabled() &&
            Utils.stringIsEmpty(jtfEncAudience.getText())) {
            int answer = JOptionPane.showConfirmDialog(this,
                "The audience is empty. Are you sure you would like to\n" +
                "proceed with an empty audience?", SwingUtils.APP_WINDOW_TITLE,
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (answer != JOptionPane.YES_OPTION) {
                jtfEncAudience.requestFocusInWindow();
                return;
            }
        }

        // Unique ID
        if (jtfEncUniqueId.isEnabled() &&
            Utils.stringIsEmpty(jtfEncUniqueId.getText())) {
            int answer = JOptionPane.showConfirmDialog(this,
                "The unique ID is empty. Are you sure you would like to\n" +
                "proceed with an empty unique ID?", SwingUtils.APP_WINDOW_TITLE,
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (answer != JOptionPane.YES_OPTION) {
                jtfEncUniqueId.requestFocusInWindow();
                return;
            }
        }

        // Signature key
        String key = jtaEncSignaturePrivateKey.getText();

        if (Utils.stringIsEmpty(key)) {
            SwingUtils.showErrorMessage(this, "Please provide a non-empty key.");
            jtaEncSignaturePrivateKey.requestFocusInWindow();
            return;
        }

        // Creates the token
        JWT unsignedToken = new JWT();

        if (jtfEncIssuer.isEnabled()) {
            unsignedToken.setIssuer(jtfEncIssuer.getText());
        }

        if (jtfEncSubject.isEnabled()) {
            unsignedToken.setSubject(jtfEncSubject.getText());
        }

        if (jtfEncAudience.isEnabled()) {
            unsignedToken.setAudience(jtfEncAudience.getText());
        }

        ZoneId zoneId = ZoneId.of("UTC");

        if (spnIssuedAt.isEnabled()) {
            unsignedToken.setIssuedAt(Utils.convertDateToZonedDateTime(
                (Date) spnIssuedAt.getValue(), zoneId));
        }

        if (spnExpiration.isEnabled()) {
            unsignedToken.setExpiration(Utils.convertDateToZonedDateTime(
                (Date) spnExpiration.getValue(), zoneId));
        }

        if (spnNotBefore.isEnabled()) {
            unsignedToken.setNotBefore(Utils.convertDateToZonedDateTime(
                (Date) spnNotBefore.getValue(), zoneId));
        }

        if (jtfEncUniqueId.isEnabled()) {
            unsignedToken.setUniqueId(jtfEncUniqueId.getText());
        }

        for (JwtClaim claim : encOtherClaimsModel.getItems()) {
            unsignedToken.addClaim(claim.getKey(), claim.getValue());
        }

        // Signs the token
        Object selItem = cboEncSignatureAlgo.getSelectedItem();
        @SuppressWarnings("unchecked")
        JwtSignatureAlgorithm signatureAlgo = (selItem != null) ?
            ((ComboBoxItem<JwtSignatureAlgorithm>) selItem).getItem() : null;

        if (signatureAlgo == null) {
            SwingUtils.showErrorMessage(this,
                "Please choose a signature algorithm.");
            cboEncSignatureAlgo.requestFocusInWindow();
            return;
        }

        Signer signer = null;

        try {
            switch (signatureAlgo) {
                case HMAC_SHA_256:
                    signer = HMACSigner.newSHA256Signer(key);
                    break;

                case HMAC_SHA_384:
                    signer = HMACSigner.newSHA384Signer(key);
                    break;

                case HMAC_SHA_512:
                    signer = HMACSigner.newSHA512Signer(key);
                    break;

                case RSA_SSA_PKCS_V1_5_SHA_256:
                    signer = RSASigner.newSHA256Signer(key);
                    break;

                case RSA_SSA_PKCS_V1_5_SHA_384:
                    signer = RSASigner.newSHA384Signer(key);
                    break;

                case RSA_SSA_PKCS_V1_5_SHA_512:
                    signer = RSASigner.newSHA512Signer(key);
                    break;

                case ECDSA_SHA_256:
                    signer = ECSigner.newSHA256Signer(key);
                    break;

                case ECDSA_SHA_384:
                    signer = ECSigner.newSHA384Signer(key);
                    break;

                case ECDSA_SHA_512:
                    signer = ECSigner.newSHA512Signer(key);
                    break;

                case RSA_SSA_PSS_SHA_256:
                    signer = RSAPSSSigner.newSHA256Signer(key);
                    break;

                case RSA_SSA_PSS_SHA_384:
                    signer = RSAPSSSigner.newSHA384Signer(key);
                    break;

                case RSA_SSA_PSS_SHA_512:
                    signer = RSAPSSSigner.newSHA512Signer(key);
                    break;
            }
        } catch (Exception ex) {
            log.error("Could not create signer:", ex);
            ExceptionDialog.createInstance(this, true,
                EXCEPTION_DLG_BOX_WIDTH, EXCEPTION_DLG_BOX_HEIGHT,
                "Could not create signer. Please check whether the key is " +
                "in a valid format.", ex).setVisible(true);
            return;
        }

        if (signer == null) {
            SwingUtils.showErrorMessage(this,
                "Could not create signer. Please check whether the key is " +
                "in a valid format.");
            jtaEncSignaturePrivateKey.requestFocusInWindow();
            return;
        }

        try {
            String signedToken = JWT.getEncoder().encode(unsignedToken, signer);
            jtaEncodedTokenOutput.setText(signedToken);
        } catch (Exception ex) {
            log.error("Error signing token:", ex);
            ExceptionDialog.createInstance(this, true,
                EXCEPTION_DLG_BOX_WIDTH, EXCEPTION_DLG_BOX_HEIGHT,
                "Error signing token.", ex).setVisible(true);
        }
    }

    private void processBtnCopyEncodedToken(ActionEvent event) {
        SwingUtils.copyTextToClipboard(jtaEncodedTokenOutput.getText(), null);
    }

    private JPanel createDecodingPanel() {
        JPanel result = new JPanel(new MigLayout(
            "", "[0:0, grow 50, fill] [pref!] [0:0, grow 50, fill]",
            "[grow, fill]"));

        result.add(createDecodingParamsPanel(), "w 0:0");

        JSeparator sep = new JSeparator(JSeparator.VERTICAL);
        sep.setForeground(Color.BLACK);

        result.add(sep);

        result.add(createDecodingResultPanel(), "w 0:0");

        return result;
    }

    private JPanel createDecodingParamsPanel() {
        JPanel result = new JPanel(new MigLayout(
            "insets 0", "[grow, fill]",
            "[] [] [grow 50, fill] [] [] [grow 50, fill] []"));

        JPanel topPanel = new JPanel(new MigLayout(
            "insets 0", "[] []", ""));

        topPanel.add(new JLabel("Signature Algorithm"));

        cboDecSignatureAlgo = createSignatureAlgoComboBox();
        cboDecSignatureAlgo.addActionListener(this::processCboDecSignatureAlgo);

        topPanel.add(cboDecSignatureAlgo);

        result.add(topPanel, "wrap");
        
        lblDecSignaturePublicKey = new JLabel("Secret Key / Public Key:");

        result.add(lblDecSignaturePublicKey, "wrap");

        jtaDecSignaturePublicKey = SwingUtils.createTextArea(
            new LimitedDocument(10000), "", 1, 1, true, true, true, true);

        result.add(new JScrollPane(jtaDecSignaturePublicKey),
            "grow, growy, wrap");

        btnDecSignatureLoadPublicKey = SwingUtils.createButton(
            "Load From PEM file", "icons/silk/folder_table.png", null, null);
        btnDecSignatureLoadPublicKey.addActionListener(
            this::processBtnDecSignatureLoadPublicKey);

        result.add(btnDecSignatureLoadPublicKey, "w pref!, wrap");

        result.add(new JLabel("Encoded Token:"), "wrap");

        jtaEncodedTokenInput = SwingUtils.createTextArea(
            new LimitedDocument(10000), "", 1, 1, true, true, true, true);

        result.add(new JScrollPane(jtaEncodedTokenInput),
            "grow, growy, wrap");

        JButton btnDecodeToken = SwingUtils.createButton(
            "Decode Token", "icons/silk/lock_open.png", null, null);
        btnDecodeToken.addActionListener(this::processBtnDecodeToken);

        result.add(btnDecodeToken, "w pref!");

        // Additional procedures
        ComboBoxItem.setSelectedItem(cboDecSignatureAlgo,
            JwtSignatureAlgorithm.HMAC_SHA_256);
        processCboDecSignatureAlgo(null);

        return result;
    }

    private void processCboDecSignatureAlgo(ActionEvent event) {
        Object selItem = cboDecSignatureAlgo.getSelectedItem();
        @SuppressWarnings("unchecked")
        JwtSignatureAlgorithm signatureAlgo = (selItem != null) ?
            ((ComboBoxItem<JwtSignatureAlgorithm>) selItem).getItem() : null;

        if (signatureAlgo == null) {
            return;
        }

        boolean isKeyPair = signatureAlgo.isAsymmetric();

        lblDecSignaturePublicKey.setText(isKeyPair ? "Public Key:" : "Secret Key:");
        btnDecSignatureLoadPublicKey.setEnabled(isKeyPair);
    }

    private void processBtnDecSignatureLoadPublicKey(ActionEvent event) {
        createFileChooser();
        fileChooser.setDialogTitle("Choose the key file");
        fileChooser.setSelectedFile(decPublicKeyFile);

        if (fileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        decPublicKeyFile = fileChooser.getSelectedFile();

        try {
            List<String> lines = Files.readAllLines(decPublicKeyFile.toPath());
            jtaDecSignaturePublicKey.setText(String.join("\n", lines));
        } catch (Exception ex) {
            log.error("Error reading file:", ex);
            ExceptionDialog.createInstance(this, true,
                EXCEPTION_DLG_BOX_WIDTH, EXCEPTION_DLG_BOX_HEIGHT,
                "Error reading file.", ex).setVisible(true);
        }
    }

    private void processBtnDecodeToken(ActionEvent event) {
        // Basic validation
        Object selItem = cboDecSignatureAlgo.getSelectedItem();
        @SuppressWarnings("unchecked")
        JwtSignatureAlgorithm signatureAlgo = (selItem != null) ?
            ((ComboBoxItem<JwtSignatureAlgorithm>) selItem).getItem() : null;

        if (signatureAlgo == null) {
            SwingUtils.showErrorMessage(this,
                "Please choose a signature algorithm.");
            cboDecSignatureAlgo.requestFocusInWindow();
            return;
        }

        String key = jtaDecSignaturePublicKey.getText();

        if (Utils.stringIsEmpty(key)) {
            SwingUtils.showErrorMessage(this, "Please provide a non-empty key.");
            jtaDecSignaturePublicKey.requestFocusInWindow();
            return;
        }

        String encodedToken = jtaEncodedTokenInput.getText();

        if (Utils.stringIsEmpty(encodedToken)) {
            SwingUtils.showErrorMessage(this, "Please provide an encoded token.");
            jtaEncodedTokenInput.requestFocusInWindow();
            return;
        }

        // Decodes and verifies the token
        Verifier verifier = null;

        try {
            switch (signatureAlgo) {
                case HMAC_SHA_256:
                case HMAC_SHA_384:
                case HMAC_SHA_512:
                    verifier = HMACVerifier.newVerifier(key);
                    break;

                case RSA_SSA_PKCS_V1_5_SHA_256:
                case RSA_SSA_PKCS_V1_5_SHA_384:
                case RSA_SSA_PKCS_V1_5_SHA_512:
                    verifier = RSAVerifier.newVerifier(key);
                    break;

                case ECDSA_SHA_256:
                case ECDSA_SHA_384:
                case ECDSA_SHA_512:
                    verifier = ECVerifier.newVerifier(key);
                    break;

                case RSA_SSA_PSS_SHA_256:
                case RSA_SSA_PSS_SHA_384:
                case RSA_SSA_PSS_SHA_512:
                    verifier = RSAPSSVerifier.newVerifier(key);
                    break;
            }
        } catch (Exception ex) {
            log.error("Could not create verifier:", ex);
            ExceptionDialog.createInstance(this, true,
                EXCEPTION_DLG_BOX_WIDTH, EXCEPTION_DLG_BOX_HEIGHT,
                "Could not create verifier. Please check whether the key is " +
                "in a valid format.", ex).setVisible(true);
            return;
        }

        if (verifier == null) {
            SwingUtils.showErrorMessage(this,
                "Could not create verifier. Please check whether the key is " +
                "in a valid format.");
            jtaDecSignaturePublicKey.requestFocusInWindow();
            return;
        }

        JWT signedToken;

        try {
            signedToken = JWT.getDecoder().decode(encodedToken, verifier);
        } catch (Exception ex) {
            ex = JwtUtils.translateDecodingException(ex);
            log.error("Could not decode or verify token:", ex);
            ExceptionDialog.createInstance(this, true,
                EXCEPTION_DLG_BOX_WIDTH, EXCEPTION_DLG_BOX_HEIGHT,
                "Could not create decode or verify token.", ex)
                .setVisible(true);
            return;
        }

        // Fills the fields
        jtfDecIssuer.setText(signedToken.issuer);
        jtfDecSubject.setText(signedToken.subject);
        jtfDecAudience.setText(Utils.objectToStr(signedToken.audience, ""));

        ZoneId zoneId = ZoneId.systemDefault();

        jtfDecIssuedAt.setText(Utils.formatZonedDateTime(
            signedToken.issuedAt, zoneId, DATE_FORMAT_PATTERN));
        jtfDecExpiration.setText(Utils.formatZonedDateTime(
            signedToken.expiration, zoneId, DATE_FORMAT_PATTERN));
        jtfDecNotBefore.setText(Utils.formatZonedDateTime(
            signedToken.notBefore, zoneId, DATE_FORMAT_PATTERN));

        jtfDecUniqueId.setText(signedToken.uniqueId);

        Map<String, Object> otherClaims = signedToken.getOtherClaims();
        List<String> claimNames = otherClaims.keySet().stream().sorted()
            .collect(Collectors.toList());
        StringBuilder builder = new StringBuilder();

        for (int i = 0, len = claimNames.size(); i < len; ++i) {
            String claimName = claimNames.get(i);
            String claimValue = Utils.objectToStr(otherClaims.get(claimName), "");

            builder.append('\"').append(claimName).append('\"').append(": ");

            if (Utils.stringContainsDigitsOnly(claimValue)) {
                builder.append(claimValue);
            }
            else if (claimValue.length() > 0) {
                builder.append('\"').append(claimValue).append('\"');
            }

            if (i < len - 1) {
                builder.append('\n');
            }
        }

        jtaDecOtherClaims.setText(builder.toString());
    }

    private JPanel createDecodingResultPanel() {
        JPanel result = new JPanel(new MigLayout(
            "insets 0", "[] [grow, fill]",
            "[] [] [] [] [] [] [] [] [] [grow, fill]"));

        JPanel topPanel = new JPanel(new MigLayout(
            "insets 0", "[] [grow, fill]", ""));

        JLabel lblDecodedData = new JLabel("Decoded Data");
        lblDecodedData.setFont(lblDecodedData.getFont().deriveFont(Font.BOLD));

        topPanel.add(lblDecodedData);

        JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);
        sep.setForeground(Color.BLACK);

        topPanel.add(sep, "aligny center, growx");

        result.add(topPanel, "growx, span, wrap");

        jtfDecIssuer = SwingUtils.createTextField(null, "", 0, false);

        result.add(new JLabel("Issuer (iss):"));
        result.add(jtfDecIssuer, "growx, wrap");

        jtfDecSubject = SwingUtils.createTextField(null, "", 0, false);

        result.add(new JLabel("Subject (sub):"));
        result.add(jtfDecSubject, "growx, wrap");

        jtfDecAudience = SwingUtils.createTextField(null, "", 0, false);

        result.add(new JLabel("Audience (aud):"));
        result.add(jtfDecAudience, "growx, wrap");

        jtfDecIssuedAt = SwingUtils.createTextField(null, "", 0, false);

        result.add(new JLabel("Issued At (iat):"));
        result.add(jtfDecIssuedAt, "growx, wrap");

        jtfDecExpiration = SwingUtils.createTextField(null, "", 0, false);

        result.add(new JLabel("Expiration (exp):"));
        result.add(jtfDecExpiration, "growx, wrap");

        jtfDecNotBefore = SwingUtils.createTextField(null, "", 0, false);

        result.add(new JLabel("<html>Not Valid<br>Before (nbf):</html>"));
        result.add(jtfDecNotBefore, "growx, wrap");

        jtfDecUniqueId = SwingUtils.createTextField(null, "", 0, false);

        result.add(new JLabel("Unique ID (jti):"));
        result.add(jtfDecUniqueId, "growx, wrap");

        jtaDecOtherClaims = SwingUtils.createTextArea(
            null, "", 1, 1, false, true, true, true);

        result.add(new JLabel("Other Claims:"), "span, wrap");
        result.add(new JScrollPane(jtaDecOtherClaims), "span, growx, growy");

        return result;
    }

    private JComboBox<ComboBoxItem<KeyGenerationType>>
            createKeyGenerationTypesComboBox() {
        List<KeyGenerationType> comboOptions =
            Arrays.stream(KeyGenerationType.values())
                .sorted(new KeyGenerationType.DescriptionComparator())
                .collect(Collectors.toList());

        return SwingUtils.createComboBox(
            ComboBoxItem.createComboBoxModel(comboOptions),
            new CustomListCellRenderer.AlternateRowColorTransformer(null), 0);
    }

    private JPanel createKeyGenerationPanel() {
        JPanel result = new JPanel(new MigLayout(
            "", "[grow, fill]", "[] [grow, fill]"));

        // Top panel
        JPanel topPanel = new JPanel(new MigLayout(
            "insets 0", "[]", ""));

        topPanel.add(new JLabel("Key Type"), "split 2");

        cboKeyGenerationType = createKeyGenerationTypesComboBox();
        cboKeyGenerationType.addActionListener(this::processCboKeyGenerationType);

        topPanel.add(cboKeyGenerationType, "wrap");

        btnGenerateKey = SwingUtils.createButton(
            "Generate Random Secret Key / Key Pair",
            "icons/silk/lightning.png", null, null);
        btnGenerateKey.addActionListener(this::processBtnGenerateKey);

        topPanel.add(btnGenerateKey);

        result.add(topPanel, "growx, wrap");

        // Key panel.
        // Secret Key / Private Key panel
        JPanel keyPanel = new JPanel(new MigLayout(
            "insets 0", "[0:0, grow, fill] [] [0:0, grow, fill]", "[grow, fill]"));

        JPanel privateKeyPanel = new JPanel(new MigLayout(
            "insets 0", "[grow, fill]", "[] [grow, fill] []"));

        lblKgPrivateKey = new JLabel("Secret Key / Private Key");
        jtaKgPrivateKey = SwingUtils.createTextArea(
            null, "", 1, 1, false, true, true, true);

        privateKeyPanel.add(lblKgPrivateKey, "wrap");
        privateKeyPanel.add(new JScrollPane(jtaKgPrivateKey), "growx, growy, wrap");

        JPanel privateKeyButtonPanel = new JPanel(
            new MigLayout("insets 0", "[] []", ""));

        JButton btnCopyPrivateKey = SwingUtils.createButton(
            "Copy", "icons/silk/page_copy.png", null, null);
        btnCopyPrivateKey.addActionListener(this::processBtnKgCopyPrivateKey);

        privateKeyButtonPanel.add(btnCopyPrivateKey);

        btnKgSavePrivateKey = SwingUtils.createButton(
            "Save to PEM File", "icons/silk/disk.png", null, null);
        btnKgSavePrivateKey.addActionListener(this::processBtnKgSavePrivateKey);

        privateKeyButtonPanel.add(btnKgSavePrivateKey);

        privateKeyPanel.add(privateKeyButtonPanel);

        keyPanel.add(privateKeyPanel, "w 0:0");

        // Separator between key panels
        JSeparator sep = new JSeparator(JSeparator.VERTICAL);
        sep.setForeground(Color.BLACK);

        keyPanel.add(sep);

        // Public Key panel
        JPanel publicKeyPanel = new JPanel(new MigLayout(
            "insets 0", "[grow, fill]", "[] [grow, fill] []"));

        lblKgPublicKey = new JLabel("Public Key");
        jtaKgPublicKey = SwingUtils.createTextArea(
            null, "", 1, 1, false, true, true, true);

        publicKeyPanel.add(lblKgPublicKey, "wrap");
        publicKeyPanel.add(new JScrollPane(jtaKgPublicKey), "growx, growy, wrap");

        JPanel publicKeyButtonPanel = new JPanel(
            new MigLayout("insets 0", "[] []", ""));

        btnKgCopyPublicKey = SwingUtils.createButton(
            "Copy", "icons/silk/page_copy.png", null, null);
        btnKgCopyPublicKey.addActionListener(this::processBtnKgCopyPublicKey);

        publicKeyButtonPanel.add(btnKgCopyPublicKey);

        btnKgSavePublicKey = SwingUtils.createButton(
            "Save to PEM File", "icons/silk/disk.png", null, null);
        btnKgSavePublicKey.addActionListener(this::processBtnKgSavePublicKey);

        publicKeyButtonPanel.add(btnKgSavePublicKey);

        publicKeyPanel.add(publicKeyButtonPanel);

        keyPanel.add(publicKeyPanel, "w 0:0");

        result.add(keyPanel, "span, growx, growy");

        // Additional procedures
        ComboBoxItem.setSelectedItem(cboKeyGenerationType,
            KeyGenerationType.HMAC_SHA_256);
        processCboKeyGenerationType(null);

        return result;
    }

    private void processCboKeyGenerationType(ActionEvent event) {
        Object selItem = cboKeyGenerationType.getSelectedItem();
        @SuppressWarnings("unchecked")
        KeyGenerationType kgType = (selItem != null) ?
            ((ComboBoxItem<KeyGenerationType>) selItem).getItem() : null;

        if (kgType == null) {
            return;
        }

        boolean isKeyPair = kgType.isAsymmetric();

        btnGenerateKey.setText(isKeyPair ?
            "Generate Random Key Pair" : "Generate Random Secret Key");
        lblKgPrivateKey.setText(isKeyPair ? "Private Key" : "Secret Key");
        btnKgSavePrivateKey.setEnabled(isKeyPair);
        lblKgPublicKey.setEnabled(isKeyPair);
        jtaKgPublicKey.setEnabled(isKeyPair);
        btnKgCopyPublicKey.setEnabled(isKeyPair);
        btnKgSavePublicKey.setEnabled(isKeyPair);
    }

    private void processBtnGenerateKey(ActionEvent event) {
        Object selItem = cboKeyGenerationType.getSelectedItem();
        @SuppressWarnings("unchecked")
        KeyGenerationType kgType = (selItem != null) ?
            ((ComboBoxItem<KeyGenerationType>) selItem).getItem() : null;

        if (kgType == null) {
            SwingUtils.showErrorMessage(this, "Please choose a key type.");
            cboKeyGenerationType.requestFocusInWindow();
            return;
        }

        String key = null;
        KeyPair keyPair = null;

        switch (kgType) {
            case HMAC_SHA_256:
                key = JWTUtils.generateSHA256_HMACSecret();
                break;

            case HMAC_SHA_384:
                key = JWTUtils.generateSHA384_HMACSecret();
                break;

            case HMAC_SHA_512:
                key = JWTUtils.generateSHA512_HMACSecret();
                break;

            case RSA_2048:
                keyPair = JWTUtils.generate2048_RSAKeyPair();
                break;

            case RSA_3072:
                keyPair = JWTUtils.generate3072_RSAKeyPair();
                break;

            case RSA_4096:
                keyPair = JWTUtils.generate4096_RSAKeyPair();
                break;

            case EC_256:
                keyPair = JWTUtils.generate256_ECKeyPair();
                break;

            case EC_384:
                keyPair = JWTUtils.generate384_ECKeyPair();
                break;

            case EC_521:
                keyPair = JWTUtils.generate521_ECKeyPair();
                break;
        }

        if (key != null) {
            jtaKgPrivateKey.setText(key);
            jtaKgPrivateKey.setCaretPosition(0);
        }
        else if (keyPair != null) {
            jtaKgPrivateKey.setText(keyPair.privateKey);
            jtaKgPublicKey.setText(keyPair.publicKey);

            jtaKgPrivateKey.setCaretPosition(0);
            jtaKgPublicKey.setCaretPosition(0);
        }
    }

    private void processBtnKgCopyPrivateKey(ActionEvent event) {
        SwingUtils.copyTextToClipboard(jtaKgPrivateKey.getText(), null);
    }

    private void processBtnKgSavePrivateKey(ActionEvent event) {
        String key = jtaKgPrivateKey.getText();

        if (Utils.stringIsEmpty(key)) {
            SwingUtils.showErrorMessage(this,
                "It is not allowed to save an empty key.");
            return;
        }

        createFileChooser();
        fileChooser.setDialogTitle("Choose the key file");
        fileChooser.setSelectedFile(kgPrivateKeyFile);

        if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        kgPrivateKeyFile = fileChooser.getSelectedFile();

        try {
            Files.write(kgPrivateKeyFile.toPath(), Arrays.asList(key),
                StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                StandardOpenOption.WRITE);
        } catch (Exception ex) {
            log.error("Error writing file:", ex);
            ExceptionDialog.createInstance(this, true,
                EXCEPTION_DLG_BOX_WIDTH, EXCEPTION_DLG_BOX_HEIGHT,
                "Error writing file.", ex).setVisible(true);
        }
    }

    private void processBtnKgCopyPublicKey(ActionEvent event) {
        SwingUtils.copyTextToClipboard(jtaKgPublicKey.getText(), null);
    }

    private void processBtnKgSavePublicKey(ActionEvent event) {
        String key = jtaKgPublicKey.getText();

        if (Utils.stringIsEmpty(key)) {
            SwingUtils.showErrorMessage(this,
                "It is not allowed to save an empty key.");
            return;
        }

        createFileChooser();
        fileChooser.setDialogTitle("Choose the key file");
        fileChooser.setSelectedFile(kgPublicKeyFile);

        if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        kgPublicKeyFile = fileChooser.getSelectedFile();

        try {
            Files.write(kgPublicKeyFile.toPath(), Arrays.asList(key),
                StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                StandardOpenOption.WRITE);
        } catch (Exception ex) {
            log.error("Error writing file:", ex);
            ExceptionDialog.createInstance(this, true,
                EXCEPTION_DLG_BOX_WIDTH, EXCEPTION_DLG_BOX_HEIGHT,
                "Error writing file.", ex).setVisible(true);
        }
    }

    private JPanel createBottomPanel() {
        JPanel result = new JPanel(new MigLayout(
            "insets 0", "[] push []", ""));

        JButton btnAbout = SwingUtils.createButton(
            "About...", "icons/silk/information.png", null, null);
        btnAbout.addActionListener(this::processBtnAbout);

        result.add(btnAbout, "sizegroup buttons");

        JButton btnClose = SwingUtils.createButton(
            "Close", "icons/silk/door_in.png", null, null);
        btnClose.addActionListener(this::processBtnClose);

        result.add(btnClose, "sizegroup buttons");

        return result;
    }

    private void processBtnAbout(ActionEvent event) {
        AboutDialog.createInstance(this, true, 600, 500).setVisible(true);
    }

    private void processBtnClose(ActionEvent event) {
        confirmExit();
    }
}
