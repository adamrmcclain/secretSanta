package com.mcclain.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Data
public class SecretSanta {
    private Person giver;
    List<Exclusions> exclusions;
    private Person receiver;
    private Boolean hasGiver;

    public SecretSanta(Person giver, List<Exclusions> exclusions){
        this.giver = giver;
        this.exclusions = exclusions;
        this.hasGiver = false;
    }

    public SecretSanta(Person giver, Exclusions exclusion){
        this.giver = giver;
        this.exclusions = new ArrayList<>();
        this.exclusions.add(exclusion);
        this.hasGiver = false;
    }

    public SecretSanta(){
        giver = new Person();
        receiver = new Person();
        exclusions = new ArrayList<>();
    }
}
