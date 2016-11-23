package test.kinesis.utils;

import com.amazonaws.auth.*;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;

/**
 * Created by christian on 11/23/16.
 */
public class CustomCredentialsProviderChain extends AWSCredentialsProviderChain {

    private String awsAccessKeyId;
    private String awsSecretAccessKey;
    private String awsProfileName;

    public CustomCredentialsProviderChain() {
        super(
                new EnvironmentVariableCredentialsProvider(),
                new SystemPropertiesCredentialsProvider(),
                new ClasspathPropertiesFileCredentialsProvider(),
                new ProfileCredentialsProvider()
        );
    }

    public CustomCredentialsProviderChain(String awsAccessKeyId, String awsSecretAccessKey) {
        super(new EnvironmentVariableCredentialsProvider(),
                new SystemPropertiesCredentialsProvider(),
                new ClasspathPropertiesFileCredentialsProvider());
        this.awsAccessKeyId = awsAccessKeyId;
        this.awsSecretAccessKey = awsSecretAccessKey;
    }

    public CustomCredentialsProviderChain(String profileName) {
        super(
                new EnvironmentVariableCredentialsProvider(),
                new SystemPropertiesCredentialsProvider(),
                new ClasspathPropertiesFileCredentialsProvider(),
                new ProfileCredentialsProvider(profileName)
        );
        this.awsProfileName = profileName;
    }

    @Override
    public AWSCredentials getCredentials() {
        return super.getCredentials();
    }
}