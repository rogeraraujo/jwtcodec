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
import com.rogeraraujo.jwtcodec.components.LimitedDocument;
import com.rogeraraujo.jwtcodec.jwt.JwtClaim;
import lombok.Getter;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * This dialog box allows the user to edit JWT Claims.
 */
public class ClaimEditDialog extends JDialog {
    private JTextField jtfName;

    private JTextArea jtaValue;

    @Getter
    private boolean clickedOk;

    @Getter
    private JwtClaim claim;

    public static ClaimEditDialog createInstance(
            Frame frame, String title, boolean modal, int width, int height,
            JwtClaim claim) {
        ClaimEditDialog result = new ClaimEditDialog(frame, title, modal);
        result.buildGui(width, height);
        SwingUtils.centerWindow(result, null);

        result.setClaim(claim);

        return result;
    }

    public static ClaimEditDialog createInstance(
            Dialog dialog, String title, boolean modal, int width, int height,
            JwtClaim claim) {
        ClaimEditDialog result = new ClaimEditDialog(dialog, title, modal);
        result.buildGui(width, height);
        SwingUtils.centerWindow(result, null);

        result.setClaim(claim);

        return result;
    }

    private ClaimEditDialog(Frame frame, String title, boolean modal) {
        super(frame, Utils.stringIsEmpty(title) ?
            SwingUtils.APP_WINDOW_TITLE : title, modal);
    }

    private ClaimEditDialog(Dialog dialog, String title, boolean modal) {
        super(dialog, Utils.stringIsEmpty(title) ?
            SwingUtils.APP_WINDOW_TITLE : title, modal);
    }

    private void buildGui(int width, int height) {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new MigLayout(
            "insets dialog", "[grow, fill]", "[grow, fill] []"));

        JPanel topPanel = new JPanel(new MigLayout(
            "insets 0", "[] [grow, fill]", "[] [grow, fill]"));

        jtfName = SwingUtils.createTextField(
            new LimitedDocument(10000), "", 0, true);

        topPanel.add(new JLabel("Name"));
        topPanel.add(jtfName, "wrap");

        jtaValue = SwingUtils.createTextArea(
            new LimitedDocument(10000), "", 5, 1, true, true, true, true);

        JLabel lblValue = new JLabel("Value");
        lblValue.setVerticalAlignment(JLabel.TOP);

        topPanel.add(lblValue);
        topPanel.add(new JScrollPane(jtaValue), "growx, growy");

        mainPanel.add(topPanel, "growx, growy, wrap");

        JPanel bottomPanel = new JPanel(new MigLayout(
            "insets 0", "push [] []", ""));

        JButton btnOk = SwingUtils.createButton(
            "OK", "icons/silk/accept.png", null, null);
        btnOk.addActionListener(this::processBtnOk);

        bottomPanel.add(btnOk, "sizegroup buttons");

        getRootPane().setDefaultButton(btnOk);

        String CANCEL_BUTTON_TEXT = "Cancel";
        String CANCEL_BUTTON_ICON_RESOURCE_NAME = "icons/silk/cancel.png";
        AbstractAction cancelAction = new AbstractAction(CANCEL_BUTTON_TEXT,
                SwingUtils.loadIcon(CANCEL_BUTTON_ICON_RESOURCE_NAME)) {
            @Override public void actionPerformed(ActionEvent event) {
                processBtnCancel(event);
            }};
        JButton btnCancel = SwingUtils.createButton(CANCEL_BUTTON_TEXT,
            CANCEL_BUTTON_ICON_RESOURCE_NAME, cancelAction, null);

        SwingUtils.bindKeyStrokeToAction(getRootPane(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancelAction",
            cancelAction);

        bottomPanel.add(btnCancel, "sizegroup buttons");

        mainPanel.add(bottomPanel, "growx");

        setContentPane(mainPanel);

        if ((width > 0) && (height > 0)) {
            setSize(width, height);
        }
        else {
            pack();
        }
    }

    private void setClaim(JwtClaim newClaim) {
        claim = new JwtClaim("", "");

        if (newClaim != null) {
            claim.setKey(newClaim.getKey());
            claim.setValue(newClaim.getValue());
        }

        jtfName.setText(claim.getKey());
        jtaValue.setText(claim.getValue());

        jtaValue.setCaretPosition(0);
    }

    private void processBtnOk(ActionEvent event) {
        String name = jtfName.getText();

        if (Utils.stringIsEmptyOrBlank(name)) {
            SwingUtils.showErrorMessage(this, "Please provide a non-empty name.");
            jtfName.requestFocusInWindow();
            return;
        }

        String value = jtaValue.getText();

        if (Utils.stringIsEmptyOrBlank(value)) {
            int answer = JOptionPane.showConfirmDialog(this,
                "The value you provided is empty. Are you sure you would like to\n" +
                "proceed with an empty value?", SwingUtils.APP_WINDOW_TITLE,
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (answer != JOptionPane.YES_OPTION) {
                jtaValue.requestFocusInWindow();
                return;
            }
        }

        claim.setKey(name);
        claim.setValue(value);

        clickedOk = true;
        setVisible(false);
    }

    private void processBtnCancel(ActionEvent event) {
        clickedOk = false;
        setVisible(false);
        dispose();
    }
}
