package org.egov.chat.pre.authorization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import org.egov.chat.pre.config.JsonPointerNameConstants;
import org.egov.chat.pre.models.User;
import org.egov.chat.pre.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Iterator;

@Service
public class UserService {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private LoginService loginService;

    @Autowired
    private UserRepository userRepository;
    private Long authTokenExtraValidityThreshold = 600000L;              // 10 minutes

    public JsonNode addLoggedInUser(JsonNode chatNode) {
        String tenantId = chatNode.at(JsonPointerNameConstants.tenantId).asText();
        String mobileNumber = chatNode.at(JsonPointerNameConstants.mobileNumber).asText();

        User user = getUser(mobileNumber, tenantId);

        chatNode = addUserDataToChatNode(user, chatNode);
        return chatNode;
    }

    User getUser(String mobileNumber, String tenantId) {
        User user = userRepository.getUserForMobileNumber(mobileNumber);

        if(user != null) {                                          // has used chatbot at least once
            if(isAuthTokenValid(user)) {
                return user;
            } else {
                JsonNode loginUserObject = loginUser(mobileNumber, tenantId);
                user = updateUserDetailsFromLogin(user, loginUserObject);
                userRepository.updateUserDetails(user);
            }
        } else {                                                    // new to chatbot

            user = loginOrCreateUser(mobileNumber, tenantId);
            userRepository.insertUser(user);

        }
        return user;
    }

    User loginOrCreateUser(String mobileNumber, String tenantId) {
        User user = User.builder().mobileNumber(mobileNumber).userId(getUserNewId(mobileNumber, tenantId)).build();
        try {
            JsonNode loginUserObject = loginUser(mobileNumber, tenantId);
            user = updateUserDetailsFromLogin(user, loginUserObject);
        } catch (HttpClientErrorException.BadRequest badRequest) {                  // User doesn't exist in mSeva system
            createUserForSystem(mobileNumber, tenantId);
            JsonNode loginUserObject = loginUser(mobileNumber, tenantId);
            user = updateUserDetailsFromLogin(user, loginUserObject);
        }
        return user;
    }

    private String getUserNewId(String mobileNumber, String tenantId) {
        return mobileNumber;
    }

    boolean isAuthTokenValid(User user) {
        Long currentTime = System.currentTimeMillis();
        if(currentTime + authTokenExtraValidityThreshold < user.getExpiresAt())
            return true;
        return false;
    }

    User updateUserDetailsFromLogin(User user, JsonNode loginUserObject) {
        user.setAuthToken(loginUserObject.get("authToken").asText());
        user.setRefreshToken(loginUserObject.get("refreshToken").asText());
        user.setUserInfo(loginUserObject.get("userInfo").toString());
        user.setExpiresAt(getExpiryTimestamp(loginUserObject));
        return user;
    }

    Long getExpiryTimestamp(JsonNode loginUserObject) {
        return System.currentTimeMillis() + loginUserObject.get("expiresIn").asLong() * 1000;
    }

    JsonNode loginUser(String mobileNumber, String tenantId) {
        return loginService.getLoggedInUser(mobileNumber, tenantId);
    }

    JsonNode createUserForSystem(String mobileNumber, String tenantId) {
        return null;
    }

    JsonNode addUserDataToChatNode(User user, JsonNode chatNode) {
        ( (ObjectNode) chatNode).set("user", objectMapper.valueToTree(user));
        return chatNode;
    }

}
