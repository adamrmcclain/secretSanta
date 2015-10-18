package com.mcclain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.mcclain.config.SecretSantaConfig;
import com.mcclain.model.CompareEnum;
import com.mcclain.model.Exclusions;
import com.mcclain.model.Person;
import com.mcclain.model.SecretSanta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SecretSantaService {

    private SecretSantaConfig config;

    @Autowired
    public SecretSantaService(SecretSantaConfig config){
        this.config = config;
    }

    public void run() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(this.getClass().getResource("secretSanta.json").getPath()));
        StringBuilder sb = new StringBuilder();
        lines.forEach(line -> sb.append(line));
        List<SecretSanta> secretSantaList = getSecretSantaList(sb.toString());
        populateReceiver(secretSantaList);
        secretSantaList.forEach(secretSanta -> {
            System.out.println(secretSanta.getGiver().getFirstName() + " " + secretSanta.getGiver().getLastName());
            System.out.println("Has");
            System.out.println(secretSanta.getReceiver().getFirstName() + " " + secretSanta.getReceiver().getLastName());
        });
    }

    public List<SecretSanta> getSecretSantaList(String jsonString){
        List<SecretSanta> secretSantas = null;
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        try {
            secretSantas = mapper.readValue(jsonString, new TypeReference<List<SecretSanta>>(){});
        } catch (IOException e) {
            e.printStackTrace();
        }
        return secretSantas;
    }

    public void populateReceiver(List<SecretSanta> secretSantas){
        secretSantas.forEach(secretSanta -> generateReceiver(secretSanta, secretSantas));
    }

    public void generateReceiver(SecretSanta secretSanta, List<SecretSanta> secretSantas) {
        SecretSanta randomReceiver = secretSantas
                .parallelStream()
                .filter(receiver -> filterReceiver(receiver, secretSanta.getExclusions()))
                .findAny()
                .get();

        secretSanta.setReceiver(randomReceiver.getGiver());
        randomReceiver.setHasGiver(true);
    }

    public boolean filterReceiver(SecretSanta receiver, List<Exclusions> exclusions) {
        if(receiver.getHasGiver()){
            return false;
        }
        List<Boolean> passesExclusion = new ArrayList<>();
        exclusions
                .stream()
                .forEach(exclusion -> passesExclusion.add(evaluateExclusion(exclusion, receiver)));
        long numberOfFailedExclusions = passesExclusion.stream().filter(pass -> !pass).count();
        return numberOfFailedExclusions == 0;
    }

    public Boolean evaluateExclusion(Exclusions exclusion, SecretSanta receiver) {
        String value = getObjectValue(receiver.getGiver(),exclusion.getObject());
        int comparer = exclusion.getValue().compareTo(value);
        switch (exclusion.getEvaluator()) {
            case EQUALS:
                return comparer == 0;
            case NOTEQUALS:
                return comparer != 0;
        }
        return false;
    }

    public String getObjectValue(Person giver, String object) {
        String objectValue = null;
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectWriter writer = objectMapper.writer().withDefaultPrettyPrinter();
        try {
            String giverJson = writer.writeValueAsString(giver);
            JsonNode jsonNode = objectMapper.readTree(giverJson);
            objectValue = jsonNode.get(object).textValue();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return objectValue;
    }


}
