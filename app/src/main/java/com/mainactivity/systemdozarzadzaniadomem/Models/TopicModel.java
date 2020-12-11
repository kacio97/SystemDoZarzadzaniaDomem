package com.mainactivity.systemdozarzadzaniadomem.Models;

public class TopicModel {
    private String topicName;
    private String value;
    private String typeOfTopic;

    public String getTypeOfTopic() {
        return typeOfTopic;
    }

    public void setTypeOfTopic(String typeOfTopic) {
        this.typeOfTopic = typeOfTopic;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
