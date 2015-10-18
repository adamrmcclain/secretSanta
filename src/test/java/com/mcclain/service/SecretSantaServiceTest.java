package com.mcclain.service;

import com.mcclain.config.SecretSantaConfig;
import com.mcclain.model.CompareEnum;
import com.mcclain.model.Exclusions;
import com.mcclain.model.Person;
import com.mcclain.model.SecretSanta;
import junit.framework.TestCase;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class SecretSantaServiceTest extends TestCase {

    SecretSantaService secretSantaService;

    @Mock
    SecretSantaConfig secretSantaConfig;

    Exclusions excludeHowards = new Exclusions("Howard", CompareEnum.NOTEQUALS, "lastName");
    Exclusions excludeDoes = new Exclusions("Doe", CompareEnum.NOTEQUALS, "lastName");
    Exclusions excludeJoe =  new Exclusions("Joe", CompareEnum.NOTEQUALS, "firstName");
    Exclusions excludeJane = new Exclusions("Jane", CompareEnum.NOTEQUALS, "firstName");;
    Exclusions excludeHilda = new Exclusions("Hilda", CompareEnum.NOTEQUALS, "firstName");;
    Exclusions excludeBill = new Exclusions("William", CompareEnum.NOTEQUALS, "firstName");;

    Person joeDoe = new Person("Joe", "Doe", "joe.doe");
    Person hildaDoe = new Person("Hilda", "Doe", "hilda.doe");
    Person williamDoe = new Person("William", "Doe", "bill.doe");
    Person williamHoward = new Person("William", "Howard", "bill.howard");
    Person janeHoward = new Person("Jane", "Howard", "jane.howard");

    String jsonString;

    public void setUp() throws Exception {
        super.setUp();
        initMocks(this);

        when(secretSantaConfig.getFilePath()).thenReturn("something");

        secretSantaService = new SecretSantaService(secretSantaConfig);

        jsonString = "[" +
                "{\"giver\": " +
                "{\"firstName\": \"Joe\",\"lastName\": \"Doe\",\"facebookId\": \"joe.doe\"}," +
                "\"exclusions\": [" +
                "{\"value\": \"Doe\",\"evaluator\": \"NOTEQUALS\",\"object\": \"lastName\"}," +
                "{\"value\": \"Joe\",\"evaluator\": \"NOTEQUALS\",\"object\": \"firstName\"}]," +
                "\"receiver\": {}," +
                "\"hasGiver\": false}," +
                "{\"giver\": " +
                "{\"firstName\": \"Jane\",\"lastName\": \"Doe\",\"facebookId\": \"jane.doe\"}," +
                "\"exclusions\": [" +
                "{\"value\": \"Doe\",\"evaluator\": \"NOTEQUALS\",\"object\": \"lastName\"}," +
                "{\"value\": \"Jane\",\"evaluator\": \"NOTEQUALS\",\"object\": \"firstName\"}]," +
                "\"receiver\": {}," +
                "\"hasGiver\": false}]";
    }

    public void testGetSecretSantaList() throws Exception {
        List<SecretSanta> secretSantaList = secretSantaService.getSecretSantaList(jsonString);
        assertEquals(2, secretSantaList.size());
        assertEquals("Joe", secretSantaList.get(0).getGiver().getFirstName());
        assertEquals("Jane", secretSantaList.get(1).getGiver().getFirstName());
    }

    public void testPopulateReceiver() throws Exception {

        List<SecretSanta> secretSantas = new ArrayList<>();
        secretSantas.add( new SecretSanta(joeDoe, excludeDoes));
        secretSantas.add( new SecretSanta(janeHoward, excludeHowards));
        secretSantas.add( new SecretSanta(williamHoward, excludeHowards));
        secretSantas.add( new SecretSanta(hildaDoe, excludeDoes));
        secretSantaService.populateReceiver(secretSantas);

        secretSantas.forEach(s ->{
                   System.out.println(s.getGiver().getFirstName() + " " + s.getGiver().getLastName());
                    System.out.println("has");
                    System.out.println(s.getReceiver().getFirstName() + " " + s.getReceiver().getLastName());
                }
        );

        secretSantas.forEach(s ->
                assertNotSame(s.getGiver().getLastName(), s.getReceiver().getLastName())
        );

    }
    public void testPopulateReceiver2() throws Exception {

        List<SecretSanta> secretSantas = new ArrayList<>();
        List<Exclusions> excludeJoeAndHilda = new ArrayList<>();

        excludeJoeAndHilda.add(excludeJoe);
        excludeJoeAndHilda.add(excludeHilda);
        secretSantas.add( new SecretSanta(hildaDoe, excludeJoeAndHilda));
        secretSantas.add( new SecretSanta(joeDoe, excludeJoeAndHilda));
        secretSantas.add( new SecretSanta(williamDoe, excludeBill));
        secretSantas.add( new SecretSanta(janeHoward, excludeHowards));
        secretSantaService.populateReceiver(secretSantas);

        secretSantas.forEach(s ->{
                    System.out.println(s.getGiver().getFirstName() + " " + s.getGiver().getLastName());
                    System.out.println("has");
                    System.out.println(s.getReceiver().getFirstName() + " " + s.getReceiver().getLastName());
                }
        );

        secretSantas.forEach(s ->
                        assertNotSame(s.getGiver().getFirstName(), s.getReceiver().getFirstName())
        );

    }


    public void testGenerateReceiver() throws Exception {
        List<SecretSanta> secretSantas = new ArrayList<>();
        SecretSanta secretSanta = new SecretSanta(joeDoe, excludeDoes);

        secretSantas.add( secretSanta);
        secretSantas.add( new SecretSanta(janeHoward, excludeHowards));

        secretSantaService.generateReceiver(secretSanta, secretSantas);

        assertEquals("Jane", secretSanta.getReceiver().getFirstName());
        assertEquals("Howard", secretSanta.getReceiver().getLastName());
        assertEquals("jane.howard", secretSanta.getReceiver().getFacebookId());
    }

    public void testGenerateReceiverComplex() throws Exception {
        List<SecretSanta> secretSantas = new ArrayList<>();
        SecretSanta secretSanta = new SecretSanta(joeDoe, excludeDoes);

        secretSantas.add( secretSanta);
        secretSantas.add( new SecretSanta(janeHoward, excludeHowards));
        secretSantas.add( new SecretSanta(williamHoward, excludeHowards));
        secretSantas.add( new SecretSanta(hildaDoe, excludeDoes));

        secretSantaService.generateReceiver(secretSanta, secretSantas);

        assertEquals("Howard", secretSanta.getReceiver().getLastName());
        assertNotSame("Hilda", secretSanta.getReceiver().getFirstName());
    }

    public void testGenerateReceiverComplex2() throws Exception {
        List<SecretSanta> secretSantas = new ArrayList<>();
        List<Exclusions> excludeJoeAndHilda = new ArrayList<>();

        excludeJoeAndHilda.add(excludeJoe);
        excludeJoeAndHilda.add(excludeHilda);

        SecretSanta secretSanta = new SecretSanta(joeDoe, excludeJoeAndHilda);

        secretSantas.add( secretSanta);
        secretSantas.add( new SecretSanta(janeHoward, excludeHowards));
        secretSantas.add( new SecretSanta(williamDoe, excludeDoes));
        secretSantas.add( new SecretSanta(hildaDoe, excludeJoeAndHilda));

        secretSantaService.generateReceiver(secretSanta, secretSantas);

        assertNotSame("Joe", secretSanta.getReceiver().getFirstName());
        assertNotSame("Hilda", secretSanta.getReceiver().getFirstName());
    }

    public void testFilterReceiverTrue1() throws Exception {
        SecretSanta receiver = new SecretSanta(joeDoe, excludeHowards);
        boolean b = secretSantaService.filterReceiver(receiver, receiver.getExclusions());

        assertTrue(b);
    }
    public void testFilterReceiverTrue2() throws Exception {

        List<Exclusions> exclusions = new ArrayList<>();
        exclusions.add(new Exclusions("Doe", CompareEnum.EQUALS, "lastName"));
        SecretSanta receiver = new SecretSanta(joeDoe, exclusions);
        boolean b = secretSantaService.filterReceiver(receiver, exclusions);

        assertTrue(b);
    }


    public void testFilterReceiverFalse1() throws Exception {

        List<Exclusions> exclusions = new ArrayList<>();

        exclusions.add(new Exclusions("Doe", CompareEnum.EQUALS, "lastName"));
        exclusions.add(new Exclusions("f", CompareEnum.EQUALS, "firstName"));
        exclusions.add(new Exclusions("joe.doe", CompareEnum.EQUALS, "facebookId"));

        SecretSanta receiver = new SecretSanta(joeDoe, exclusions);

        boolean b = secretSantaService.filterReceiver(receiver, exclusions);

        assertFalse(b);
    }

    public void testFilterReceiverFalse2() throws Exception {

        List<Exclusions> exclusions = new ArrayList<>();

        exclusions.add(new Exclusions("Doe", CompareEnum.NOTEQUALS, "lastName"));
        exclusions.add(new Exclusions("Joe", CompareEnum.NOTEQUALS, "firstName"));
        exclusions.add(new Exclusions("joe.doe", CompareEnum.NOTEQUALS, "facebookId"));
        SecretSanta receiver = new SecretSanta(joeDoe, exclusions);

        boolean b = secretSantaService.filterReceiver(receiver, exclusions);

        assertFalse(b);
    }

    public void testEvaluateExclusionTrue() throws Exception {
        Exclusions exclusions = new Exclusions("Doe", CompareEnum.EQUALS, "lastName");

        exclusions.setEvaluator(CompareEnum.EQUALS);
        exclusions.setObject("lastName");
        exclusions.setValue("Doe");
        SecretSanta santa = new SecretSanta(joeDoe, exclusions);
        Boolean aBoolean = secretSantaService.evaluateExclusion(exclusions, santa);
        assertTrue(aBoolean);
    }

    public void testEvaluateExclusionFalse() throws Exception {
        SecretSanta santa = new SecretSanta(joeDoe,excludeDoes);

        Boolean aBoolean = secretSantaService.evaluateExclusion(santa.getExclusions().get(0), santa);
        assertFalse(aBoolean);
    }

    public void testGetObjectValue() throws Exception {
        Person person = new Person("Joe", "Doe", "joe.doe");
        String firstName = secretSantaService.getObjectValue(person, "firstName");
        String lastName = secretSantaService.getObjectValue(person, "lastName");
        String facebookId = secretSantaService.getObjectValue(person, "facebookId");

        assertEquals("Joe", firstName);
        assertEquals("Doe", lastName);
        assertEquals("joe.doe", facebookId);
    }

}