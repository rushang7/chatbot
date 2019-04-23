package org.egov.chat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.egov.chat.config.graph.GraphReader;
import org.egov.chat.config.graph.TopicNameGetter;
import org.egov.chat.streams.CreateBranchStream;
import org.egov.chat.streams.CreateEndpointStream;
import org.egov.chat.streams.CreateStepStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

@ComponentScan(basePackages = { "org.egov.chat" , "org.egov.chat.config"})
@SpringBootApplication
public class ChatBot {

    @Autowired
    private CreateStepStream createStepStream;
    @Autowired
    private CreateBranchStream createBranchStream;
    @Autowired
    private CreateEndpointStream createEndpointStream;
    @Autowired
    private GraphReader graphReader;
    @Autowired
    private TopicNameGetter topicNameGetter;

    String rootFolder = "graph/";
    String fileExtension = ".yaml";

    public static void main(String args[]) {

        SpringApplication.run(ChatBot.class, args);

    }

    @PostConstruct
    public void run() throws IOException {
        Set<String> vertices = graphReader.getAllVertices();
        Iterator<String> vertexIterator = vertices.iterator();

        while (vertexIterator.hasNext()) {
            String node = vertexIterator.next();

            String pathToFile = getPathToConfigFileForNode(node);

            JsonNode config = getConfigForFile(pathToFile);

            String nodeType = config.get("nodeType").asText();

            if(nodeType.equalsIgnoreCase("step")) {
                createStepStream.createEvaluateAnswerStreamForConfig(config,
                        topicNameGetter.getAnswerInputTopicNameForNode(node),
                        topicNameGetter.getAnswerOutputTopicNameForNode(node),
                        topicNameGetter.getQuestionTopicNameForNode(node));

                createStepStream.createQuestionStreamForConfig(config,
                        topicNameGetter.getQuestionTopicNameForNode(node),
                        "send-message");

            } else if(nodeType.equalsIgnoreCase("branch")) {
                createBranchStream.createEvaluateAnswerStreamForConfig(config,
                        topicNameGetter.getAnswerInputTopicNameForNode(node),
                        topicNameGetter.getQuestionTopicNameForNode(node));

                createBranchStream.createQuestionStreamForConfig(config,
                        topicNameGetter.getQuestionTopicNameForNode(node),
                        "send-message");
            } else if(nodeType.equalsIgnoreCase("endpoint")) {

                createEndpointStream.createEndpointStream(config, topicNameGetter.getQuestionTopicNameForNode(node),
                        "send-message");

            }
        }
    }

    private String getPathToConfigFileForNode(String node) {
        String path = "";
        path += rootFolder;
        String subFolders[] = node.split("\\.");
        for(int i = 0; i < subFolders.length - 1; i++) {
            path += subFolders[i] + "/";
        }
        path += node;
        path += fileExtension;
        return path;
    }

    private JsonNode getConfigForFile(String pathToFile) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        JsonNode config = mapper.readTree(ChatBot.class.getClassLoader().getResource(pathToFile));
        return config;
    }

}
