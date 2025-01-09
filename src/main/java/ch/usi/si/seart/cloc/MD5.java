package ch.usi.si.seart.cloc;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class MD5 {

    private MD5() {
    }

    @SuppressWarnings({"checkstyle:EmptyStatement", "StatementWithEmptyBody"})
    static String hash(File file) throws IOException, NoSuchAlgorithmException {
        try (
            InputStream fileStream = Files.newInputStream(file.toPath());
            DigestInputStream digestStream = new DigestInputStream(fileStream, MessageDigest.getInstance("MD5"))
        ) {
            byte[] buffer = new byte[8192];
            while (digestStream.read(buffer, 0, buffer.length) != -1);
            byte[] digested = digestStream.getMessageDigest().digest();
            return new BigInteger(1, digested).toString(16);
        }
    }
}
