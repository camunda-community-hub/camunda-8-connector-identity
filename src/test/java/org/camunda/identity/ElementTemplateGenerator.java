package org.camunda.identity;

import io.camunda.cherry.definition.RunnerDecorationTemplate;
import org.camunda.connector.identity.IdentityFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElementTemplateGenerator {
    private static final Logger logger = LoggerFactory.getLogger(ElementTemplateGenerator.class.getName());

    public static void generate() {
        try {
            logger.info("Generating ElementTemplate");
            RunnerDecorationTemplate runnerDecorationTemplate = new RunnerDecorationTemplate(new IdentityFunction());
            runnerDecorationTemplate.generateElementTemplate("./element-templates/", "identity-function.json");
        } catch (Exception e) {
            logger.error("Error during generation", e);
        }
    }

    public static void main(String[] args) {
        generate();
    }
}
