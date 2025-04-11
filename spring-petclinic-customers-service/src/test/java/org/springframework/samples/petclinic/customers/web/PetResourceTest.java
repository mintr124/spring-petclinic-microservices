package org.springframework.samples.petclinic.customers.web;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.customers.model.Owner;
import org.springframework.samples.petclinic.customers.model.OwnerRepository;
import org.springframework.samples.petclinic.customers.model.Pet;
import org.springframework.samples.petclinic.customers.model.PetRepository;
import org.springframework.samples.petclinic.customers.model.PetType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.test.util.ReflectionTestUtils;


@ExtendWith(SpringExtension.class)
@WebMvcTest(PetResource.class)
@ActiveProfiles("test")
class PetResourceTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    PetRepository petRepository;

    @MockBean
    OwnerRepository ownerRepository;

    @Test
    void shouldGetAPetInJSonFormat() throws Exception {
        Pet pet = setupPet();
        given(petRepository.findById(2)).willReturn(Optional.of(pet));

        mvc.perform(get("/owners/2/pets/2").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value(2))
            .andExpect(jsonPath("$.name").value("Basil"))
            .andExpect(jsonPath("$.type.id").value(6));
    }

    private Pet setupPet() {
        Owner owner = new Owner();
        owner.setFirstName("George");
        owner.setLastName("Bush");

        Pet pet = new Pet();
        pet.setName("Basil");
        pet.setId(2);

        PetType petType = new PetType();
        petType.setId(6);
        pet.setType(petType);

        owner.addPet(pet);
        return pet;
    }

    @Test
    void testAddPetToOwner() {
        Owner owner = new Owner();
        Pet pet = new Pet();
        pet.setName("Fluffy");
        pet.setBirthDate(new Date());

        owner.addPet(pet);

        assertEquals(1, owner.getPets().size());
        assertTrue(owner.getPets().stream().anyMatch(p -> p.getName().equals("Fluffy")));
        assertEquals(owner, pet.getOwner());
    }

    @Test
    void testPetProperties() {
        Pet pet = new Pet();
        PetType type = new PetType();
        type.setName("Dog");

        pet.setName("Buddy");
        pet.setBirthDate(new Date());
        pet.setType(type);

        assertEquals("Buddy", pet.getName());
        assertEquals("Dog", pet.getType().getName());
        assertNotNull(pet.getBirthDate());
    }

    @Test
    void testPetTypeProperties() {
        PetType type = new PetType();
        type.setId(1);
        type.setName("Cat");

        assertEquals(1, type.getId());
        assertEquals("Cat", type.getName());
    }

    @Test
    void testSaveOwner() {
        Owner owner = new Owner();
        ReflectionTestUtils.setField(owner, "id", 1); // set id dù không có setter
        owner.setFirstName("Jane");
        owner.setLastName("Doe");
        owner.setAddress("456 Another St");
        owner.setCity("Metropolis");
        owner.setTelephone("9876543210");

        given(ownerRepository.save(owner)).willReturn(owner);
        given(ownerRepository.findById(1)).willReturn(Optional.of(owner));

        Owner saved = ownerRepository.save(owner);
        assertThat(saved.getId()).isNotNull();
        assertThat(ownerRepository.findById(saved.getId())).isPresent();
    }

    @Test
    void testFindPetTypes() {
        PetType type1 = new PetType();
        type1.setId(1);
        type1.setName("Cat");

        PetType type2 = new PetType();
        type2.setId(2);
        type2.setName("Dog");

        given(petRepository.findPetTypes()).willReturn(List.of(type1, type2));

        List<PetType> types = petRepository.findPetTypes();
        assertThat(types).isNotNull();
        assertThat(types).isNotEmpty();
        assertThat(types).hasSize(2);
    }
}
