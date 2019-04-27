package com.surrey.com3026.coursework.security;

import com.surrey.com3026.coursework.security.generator.KeyGenerator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

public class SignatureHandler
{
    private static final byte[] SIGNATURE_PLACEHOLDER = "Signature here!".getBytes();

    private KeyStore keyStore;

    /**
     * Instantiate a MessageVerifier object
     *
     * @param keyStore
     *          the {@link KeyStore} to access the certificate chain to verify the sign, and private key
     *          to sign messages
     */
    public SignatureHandler(KeyStore keyStore)
    {
        this.keyStore = keyStore;
    }

    /**
     * Get the signature to sign the message with
     *
     * @return the byte array for the signed message
     */
    public byte[] sign()
    {
        try
        {
            Signature dsa = Signature.getInstance(KeyGenerator.SIG_ALGORITHM, "SUN");
            PrivateKey priv = (PrivateKey) keyStore.getKey(KeyGenerator.CERT_CHAIN_ALIAS, KeyGenerator.PASSWORD);

            System.out.println(keyStore.getCreationDate(KeyGenerator.CERT_CHAIN_ALIAS));

            dsa.initSign(priv);
            dsa.update(SIGNATURE_PLACEHOLDER);

            System.out.println(
                    new String(dsa.sign())
            );

            return dsa.sign();
        }
        catch (NoSuchAlgorithmException | NoSuchProviderException | KeyStoreException | UnrecoverableKeyException
                | InvalidKeyException | SignatureException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Verify the signature from the message sent by a member within the group
     *
     * @param nodeId
     *          the ID of the responder node
     * @param signedMessage
     *          the signed message sent
     * @return true if the sign is verified, false otherwise or if exception is thrown
     */
    public boolean verify(int nodeId, byte[] signedMessage)
    {
        try
        {
            Certificate[] chain = keyStore.getCertificateChain(KeyGenerator.CERT_CHAIN_ALIAS);
            Certificate responderCert = chain[nodeId];

            Signature sig = Signature.getInstance(KeyGenerator.SIG_ALGORITHM, "SUN");
            sig.initVerify(responderCert);
            sig.update(SIGNATURE_PLACEHOLDER);

            return sig.verify(signedMessage);
        }
        catch (KeyStoreException | NoSuchAlgorithmException | NoSuchProviderException
                | SignatureException | InvalidKeyException e)
        {
            e.printStackTrace();
            return false;
        }
    }

}
