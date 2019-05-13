package com.surrey.com3026.coursework.security.generator;

import com.surrey.com3026.coursework.security.SecurityConstants;
import sun.security.tools.keytool.CertAndKeyGen;
import sun.security.x509.X500Name;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class KeyGenerator
{
    private static final long ONE_YEAR = (long) 365 * 24 * 60 * 60;

    public static void main(String[] args)
    {
        // take command line input for number of keystore files to generate
        // 1 for each node for the capacity of the group
        final int noKeysToGen = Integer.parseInt(args[0]);
        KeyStore[] keyStores = new KeyStore[noKeysToGen];

        for (int i = 0; i < noKeysToGen; i++)
        {
            try
            {
                KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
                ks.load(null, SecurityConstants.PASSWORD);
                keyStores[i] = ks;
            }
            catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e)
            {
                e.printStackTrace();
            }
        }

        // use method to create all the keystore objects we need
        addToAllKeystores(noKeysToGen, keyStores);

        // save all keystore objects out to files for each nodes copy
        for (int i = 0; i < noKeysToGen; i++)
        {
            try (OutputStream fos = new FileOutputStream("node-" + (i+1) + ".jks"))
            {
                KeyStore ks = keyStores[i];
                ks.store(fos, SecurityConstants.PASSWORD);
            }
            catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * Adding the whole certificate chain and private key entry for each keystore object in the array
     *
     * @param noKeysToGen
     *          the number of keys to generate
     * @param keyStores
     *          the array of keystore objects to add the certificate chain + private key for each node
     */
    private static void addToAllKeystores(int noKeysToGen, KeyStore[] keyStores)
    {
        X509Certificate[] certificateChain = new X509Certificate[noKeysToGen];
        CertAndKeyGen[] gens = new CertAndKeyGen[noKeysToGen];

        // first generate the CertAndKey gen objects and the certificate chain
        for (int i = 0; i < noKeysToGen; i++)
        {
            try
            {
                // generate it with 2048 bits
                CertAndKeyGen certGen = new CertAndKeyGen(SecurityConstants.KEY_TYPE, SecurityConstants.SIG_ALGORITHM);
                certGen.generate(2048);

                gens[i] = certGen;

                X509Certificate cert = certGen.getSelfCertificate(
                        new X500Name("CN=Distributed Systems,O=University of Surrey,L=Guildford,C=UK"), ONE_YEAR);

                certificateChain[i] = cert;
            }
            catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException |
                    CertificateException | SignatureException | IOException e)
            {
                e.printStackTrace();
            }
        }

        // then use arrays to add to each keystore the entire certificate chain and private key for a single node
        // so we can create a keystore for each node
        for (int i = 0; i < noKeysToGen; i++)
        {
            CertAndKeyGen certGen = gens[i];
            KeyStore keyStore = keyStores[i];

            try
            {
                keyStore.setKeyEntry(SecurityConstants.CERT_CHAIN_ALIAS, certGen.getPrivateKey(),
                        SecurityConstants.PASSWORD, certificateChain);
            }
            catch (KeyStoreException e)
            {
                e.printStackTrace();
            }

        }
    }


}
