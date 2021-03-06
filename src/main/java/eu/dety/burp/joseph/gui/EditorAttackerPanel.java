/**
 * JOSEPH - JavaScript Object Signing and Encryption Pentesting Helper
 * Copyright (C) 2016 Dennis Detering
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package eu.dety.burp.joseph.gui;

import burp.IBurpExtenderCallbacks;
import eu.dety.burp.joseph.attacks.AttackLoader;
import eu.dety.burp.joseph.attacks.AttackPreparationFailedException;
import eu.dety.burp.joseph.attacks.IAttackInfo;
import eu.dety.burp.joseph.editor.JwsEditor;
import eu.dety.burp.joseph.utilities.JoseParameter;
import eu.dety.burp.joseph.utilities.Logger;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class EditorAttackerPanel extends JPanel {
    private static final Logger loggerInstance = Logger.getInstance();
    private static final ResourceBundle bundle = ResourceBundle.getBundle("JOSEPH");
    private JwsEditor.JwsEditorTab jwsEditorReference;

    private HashMap<String, IAttackInfo> registeredAttacks = new HashMap<>();
    private DefaultComboBoxModel<String> attackListModel = new DefaultComboBoxModel<>();
    private String algorithm = "?";
    private IAttackInfo selectedAttack = null;

    private HashMap<String, ? extends Enum> payloads;
    private JComboBox<String> payloadSelection = new JComboBox<>();
    private DefaultComboBoxModel<String> payloadSelectionListModel = new DefaultComboBoxModel<>();

    private String header = "";
    private String payload = "";
    private String signature = "";

    /**
     * AttackerPanel constructor
     * <p>
     * Register available attacks, extract "alg" and "typ" header fields and generate attackListModel based on type and suitableness of the
     * attack.
     * 
     * @param callbacks
     *            {@link IBurpExtenderCallbacks} extender callbacks
     */
    public EditorAttackerPanel(IBurpExtenderCallbacks callbacks, JwsEditor.JwsEditorTab jwsEditorReference) {
        this.jwsEditorReference = jwsEditorReference;

        // Register all available attacks
        registeredAttacks = AttackLoader.getRegisteredAttackInstances(callbacks);

        // Initialize UI components
        initComponents();
    }

    /**
     * Update the attack list
     */
    public void updateAttackList() {
        attackListModel.removeAllElements();

        this.header = jwsEditorReference.getHeader();

        // If the keys "alg" and "typ" exist, get their value and update
        // informational fields
        JSONObject headerJson = new JSONObject(header);
        if (headerJson.has("alg"))
            algorithm = headerJson.getString("alg");

        // Build available attacks list
        for (Map.Entry<String, IAttackInfo> attack : this.registeredAttacks.entrySet()) {
            // If attack is suitable for given JOSE type, add it to
            // attackListModel
            if (attack.getValue().isSuitable(JoseParameter.JoseType.JWS, algorithm)) {
                attackListModel.addElement(attack.getKey());
            }
        }
    }

    /**
     * Clean up attack specific UI changes
     */
    private void clearAttackSelection() {
        extraPanel.removeAll();
        extraPanel.revalidate();
        extraPanel.repaint();
        extraPanel.setEnabled(false);

        payloadSelectionListModel.removeAllElements();

        updateButton.setEnabled(false);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this
     * method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed"
    // desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        attackListLabel = new javax.swing.JLabel();
        attackList = new javax.swing.JComboBox<>();
        loadButton = new javax.swing.JButton();
        extraPanel = new javax.swing.JPanel();
        updateButton = new javax.swing.JButton();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("JOSEPH"); // NOI18N
        attackListLabel.setText(bundle.getString("ATTACKLISTLABEL")); // NOI18N

        attackList.setModel(attackListModel);
        attackList.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                attackListItemStateChanged(evt);
            }
        });

        loadButton.setText(bundle.getString("LOADBUTTON")); // NOI18N
        loadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadButtonActionPerformed(evt);
            }
        });

        extraPanel.setEnabled(false);
        extraPanel.setLayout(new java.awt.GridBagLayout());

        updateButton.setText(bundle.getString("UPDATEBUTTON")); // NOI18N
        updateButton.setEnabled(false);
        updateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(
                                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(attackListLabel)
                                        .addGroup(
                                                layout.createSequentialGroup()
                                                        .addComponent(attackList, javax.swing.GroupLayout.PREFERRED_SIZE, 351,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(loadButton))
                                        .addComponent(extraPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(updateButton)).addContainerGap(84, Short.MAX_VALUE)));
        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(attackListLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(
                                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(attackList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(loadButton))
                        .addGap(18, 18, 18)
                        .addComponent(extraPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.PREFERRED_SIZE).addGap(18, 18, 18).addComponent(updateButton).addContainerGap(180, Short.MAX_VALUE)));
    }// </editor-fold>//GEN-END:initComponents

    private void attackListItemStateChanged(java.awt.event.ItemEvent evt) {// GEN-FIRST:event_attackListItemStateChanged
        clearAttackSelection();
    }// GEN-LAST:event_attackListItemStateChanged

    private void loadButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_loadButtonActionPerformed
        loggerInstance.log(getClass(), "Load button clicked, chosen attack: " + attackListModel.getSelectedItem(), Logger.LogLevel.DEBUG);

        clearAttackSelection();

        // Get selected Attack
        selectedAttack = registeredAttacks.get(attackListModel.getSelectedItem());

        // Set attack information
        loggerInstance.log(selectedAttack.getClass(), "Loading attack information and additional UI components...", Logger.LogLevel.DEBUG);

        // Check if attack has extra UI components and update UI
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        selectedAttack.getExtraUI(extraPanel, constraints);

        payloads = selectedAttack.getPayloadList();

        payloadSelection.setPreferredSize(new Dimension(350, 25));

        for (Map.Entry<String, ? extends Enum> payload : payloads.entrySet()) {
            payloadSelectionListModel.addElement(payload.getKey());
        }

        payloadSelection.setModel(payloadSelectionListModel);

        constraints.gridy++;
        extraPanel.add(new JLabel(bundle.getString("CHOOSE_PAYLOAD")), constraints);

        constraints.gridy++;
        extraPanel.add(payloadSelection, constraints);

        extraPanel.setEnabled(true);
        extraPanel.revalidate();
        extraPanel.repaint();

        // Enable attack button
        updateButton.setEnabled(true);
    }// GEN-LAST:event_loadButtonActionPerformed

    private void updateButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_updateButtonActionPerformed
        loggerInstance.log(getClass(), "Update button clicked, modify request!", Logger.LogLevel.DEBUG);

        this.header = jwsEditorReference.getHeader();
        this.payload = jwsEditorReference.getPayload();
        this.signature = jwsEditorReference.getSignature();

        try {
            HashMap<String, String> updatedValues = selectedAttack.updateValuesByPayload(payloads.get(payloadSelectionListModel.getSelectedItem()),
                    this.header, this.payload, this.signature);
            this.jwsEditorReference.updateSourceViewer(updatedValues.get("header"), updatedValues.get("payload"), updatedValues.get("signature"));

            loggerInstance.log(selectedAttack.getClass(), "Selected payload: " + payloadSelectionListModel.getSelectedItem(), Logger.LogLevel.DEBUG);
        } catch (AttackPreparationFailedException e) {
            // Show error popup with exception message
            JOptionPane.showMessageDialog(new JFrame(), e.getMessage(), bundle.getString("ATTACK_PREPARATION_FAILED"), JOptionPane.ERROR_MESSAGE);
            loggerInstance.log(selectedAttack.getClass(), e.getMessage(), Logger.LogLevel.ERROR);
        }

    }// GEN-LAST:event_updateButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> attackList;
    private javax.swing.JLabel attackListLabel;
    private javax.swing.JPanel extraPanel;
    private javax.swing.JButton loadButton;
    private javax.swing.JButton updateButton;
    // End of variables declaration//GEN-END:variables
}
