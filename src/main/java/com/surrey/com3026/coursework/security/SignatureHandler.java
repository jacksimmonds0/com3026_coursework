package com.surrey.com3026.coursework.security;

import com.surrey.com3026.coursework.security.generator.KeyGenerator;

import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;

/**
 * Allows for handling of digital signatures for messages, both signing and verifying
 */
public class SignatureHandler
{
    private static final String SHA_256 = "SHA-256";

    private KeyStore keyStore;

    /**
     * Instantiate a SignatureHandler object
     *
     * @param keyStore
     *          the {@link KeyStore} to access the certificate chain to verify the signature and this nodes
     *          private key to sign messages
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
    public byte[] sign(byte[] messageToSign)
    {
        try
        {
            Signature dsa = Signature.getInstance(KeyGenerator.SIG_ALGORITHM, "SUN");
            PrivateKey privateKey = (PrivateKey) keyStore.getKey(KeyGenerator.CERT_CHAIN_ALIAS, KeyGenerator.PASSWORD);

            dsa.initSign(privateKey);
            dsa.update(createMessageDigest(messageToSign));

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
     *          the ID of the responder node to get their public key from the certificate chain
     * @param signedMessage
     *          the signed message sent
     * @param expectedMessage
     *          the message expected to be received
     * @return true if the sign is verified, false otherwise or if exception is thrown
     */
    public boolean verify(int nodeId, byte[] signedMessage, byte[] expectedMessage)
    {
        try
        {
            Certificate[] chain = keyStore.getCertificateChain(KeyGenerator.CERT_CHAIN_ALIAS);
            Certificate responderCert = chain[nodeId-1];

            Signature sig = Signature.getInstance(KeyGenerator.SIG_ALGORITHM, "SUN");
            sig.initVerify(responderCert);
            sig.update(createMessageDigest(expectedMessage));

            return sig.verify(signedMessage);
        }
        catch (KeyStoreException | NoSuchAlgorithmException | NoSuchProviderException
                | SignatureException | InvalidKeyException e)
        {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Creates a hash of a message using SHA-256 for non-repudiation and integrity checks
     *
     * @param message
     *          the message to hash
     * @return the hashed message
     */
    private byte[] createMessageDigest(byte[] message) throws NoSuchAlgorithmException
    {
        MessageDigest messageDigest = MessageDigest.getInstance(SHA_256);
        return messageDigest.digest(message);
    }

}
