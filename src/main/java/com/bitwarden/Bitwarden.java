package com.bitwarden;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Singleton
@Getter
public class Bitwarden {
    // import com.fasterxml.jackson.databind.ObjectMapper; // version 2.11.1
// import com.fasterxml.jackson.annotation.JsonProperty; // version 2.11.1
/* ObjectMapper om = new ObjectMapper();
Root[] root = om.readValue(myJsonString, Root[].class); */
    public class Login {
        public ArrayList<Uri> uris;
        public String username;
        public String password;
        public Object totp;
        public Date passwordRevisionDate;
    }

    public class PasswordHistory {
        public Date lastUsedDate;
        public String password;
    }

    public class PasswordEntry {
        public String object;
        public String id;
        public Object organizationId;
        public String folderId;
        public int type;
        public int reprompt;
        public String name;
        public Object notes;
        public boolean favorite;
        public Login login;
        public ArrayList<Object> collectionIds;
        public Date revisionDate;
        public Date creationDate;
        public Object deletedDate;
        public ArrayList<PasswordHistory> passwordHistory;
    }

    public class Uri {
        public Object match;
        public String uri;
    }

    public static class Config {
        @Expose
        @SerializedName("email")
        public String emailAddress;

        @Expose
        @SerializedName("token")
        public String token;

        Config(String token, String emailAddress){
            this.token = token;
            this.emailAddress = emailAddress;
        }
    }


    final private static String BW_LOGOUT_CMD = "bw logout";
    final private static String BW_SYNC_CMD = "bw sync";
    final private static String BW_UNLOCK_VAULT_CMD = "bw unlock %s";
    final private static String BW_EMAIL_LOGIN_CMD = "bw login %s %s --method %d";
    final private static String BW_LIST_CMD = "bw list --search runescape items --session %s";

    private String masterPassword;
    private String sessionToken;

    private String emailAddress;

    public class BitwardenFailedToExecCommand extends RuntimeException {
        public BitwardenFailedToExecCommand() {
            super("failed to exec command");
        }
    }

    public class BitwardenNotLoggedInError extends RuntimeException {
        public BitwardenNotLoggedInError() {
            super("not logged in");
        }
    }

    public class BitwardenInvalidMasterPassword extends RuntimeException {
        public BitwardenInvalidMasterPassword() {
            super("invalid master password");
        }
    }

    public class BitwardenFailedToLoginError extends RuntimeException {
        public BitwardenFailedToLoginError() {
            super("failed to login");
        }
    }
    public class BitwardenFailedToLogoutError extends RuntimeException {
        public BitwardenFailedToLogoutError() {
            super("failed to logout");
        }
    }

    public class BitwardenAlreadyLoggedInError extends RuntimeException {
        public BitwardenAlreadyLoggedInError() {
            super("already logged in");
        }
    }

    public class BitwardenFailedToGetSessionTokenError extends RuntimeException {
        public BitwardenFailedToGetSessionTokenError() {
            super("failed to get session token");
        }
    }

    private class CmdResult {
        private final String stdout;
        private final String stderr;

        public CmdResult(String stdout, String stderr) {
            this.stdout = stdout;
            this.stderr = stderr;
        }

        public boolean hasError() {
            return this.getStderr().length() > 0;
        }

        public String getStdout() {
            return stdout;
        }

        public String getStderr() {
            return stderr;
        }
    }

    private CmdResult execCmd(String cmd) throws java.io.IOException {
        Process process = Runtime.getRuntime().exec(cmd);
        BufferedReader outputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = outputReader.readLine()) != null) {
            output.append(line).append("\n");
        }

        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        StringBuilder error = new StringBuilder();
        while ((line = errorReader.readLine()) != null) {
            error.append(line).append("\n");
        }

        return new CmdResult(output.toString(), error.toString());
    }

    public Config getConfig() {
        if (!this.isLoggedIn()) {
            throw new BitwardenNotLoggedInError();
        }

        return new Config(this.sessionToken, this.emailAddress);
    }

    public void sync() {
        try {
            execCmd(BW_SYNC_CMD);
        } catch (IOException e) {
            throw new BitwardenFailedToExecCommand();
        }
    }

    public void unlock(String password) {
        String unlockCMD = String.format(BW_UNLOCK_VAULT_CMD, password);
        CmdResult result = null;
        try {
            result = execCmd(unlockCMD);
        } catch (IOException e) {
            throw new BitwardenFailedToExecCommand();
        }

        if (result.hasError()) {
            if (result.getStderr().contains("invalid master password")) {
                throw new BitwardenInvalidMasterPassword();
            }
        }

        this.masterPassword = password;
        grabSessionToken(result.getStdout());
    }

    private void grabSessionToken(String stdout) {
        String tokenRegex = "BW_SESSION=\"(.*)\""; // matching BW_SESSION="..." and capturing the ... part

        // get session token from stdout
        Pattern pattern = Pattern.compile(tokenRegex);
        Matcher matcher = pattern.matcher(stdout);

        if (matcher.find()) {
            this.sessionToken = matcher.group(1);
            Config config = this.getConfig();
            Storage.saveCredentials(config);

            log.info(String.format("Token: %s, Master password: %s", this.sessionToken, this.masterPassword));
        } else {
            throw new BitwardenFailedToGetSessionTokenError();
        }
    }

    public List<PasswordEntry> getRunescapePasswords() {
        if (!isLoggedIn()) {
            throw new BitwardenNotLoggedInError();
        }
        String listCmd = String.format(BW_LIST_CMD, this.sessionToken);
        CmdResult result = null;
        try {
            result = execCmd(listCmd);
        } catch (IOException e) {
            throw new BitwardenFailedToExecCommand();
        }

        if (result.hasError()) {
            throw new BitwardenInvalidMasterPassword();
        }

        Gson gson = new Gson();
        String jsonString = result.getStdout();

        return Arrays.asList(gson.fromJson(jsonString, PasswordEntry[].class));
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public void login(String email, String password) {
        String loginCmd = String.format(BW_EMAIL_LOGIN_CMD, email, password, 1);
        log.info(loginCmd);

        CmdResult result = null;
        try {
            result = execCmd(loginCmd);
        } catch (IOException e) {
            throw new BitwardenFailedToExecCommand();
        }

        if (result.hasError()) {
            if (result.getStderr().contains("invalid master password")) {
                throw new BitwardenInvalidMasterPassword();
            } else if (result.getStderr().contains("You are already logged in")) {
                this.unlock(password);
                return;
            } else {
                throw new BitwardenFailedToLoginError();
            }
        }

        this.emailAddress = email;
        this.masterPassword = password;

        grabSessionToken(result.getStdout());
    }

    public boolean isLoggedIn() {
        return this.sessionToken.length() > 0;
    }

    public void logout() {
        if (!isLoggedIn()) {
            throw new BitwardenNotLoggedInError();
        }

        try {
            execCmd(BW_LOGOUT_CMD);
            Storage.clearCredentials();
        } catch (IOException e) {
            throw new BitwardenFailedToLogoutError();
        }
    }
}
