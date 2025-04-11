package org.springframework.samples.petclinic.vets.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.vets.model.Specialty;
import org.springframework.samples.petclinic.vets.model.Vet;
import org.springframework.samples.petclinic.vets.model.VetRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@WebMvcTest(VetResource.class)
@ActiveProfiles("test")
class VetResourceTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    VetRepository vetRepository;

    @Test
    void shouldGetAListOfVets() throws Exception {
        Vet vet = new Vet();
        vet.setId(1);
        vet.setFirstName("John");
        vet.setLastName("Doe");

        given(vetRepository.findAll()).willReturn(List.of(vet));

        mvc.perform(get("/vets").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].firstName").value("John"))
            .andExpect(jsonPath("$[0].lastName").value("Doe"));
    }

    @Test
    void testGetAndSetName() {
        Specialty specialty = new Specialty();
        specialty.setName("Dentistry");

        assertEquals("Dentistry", specialty.getName());
    }

    @Test
    void testGetIdInitiallyNull() {
        Specialty specialty = new Specialty();
        assertNull(specialty.getId());
    }

    @Test
    void testAddAndGetSpecialties() {
        Vet vet = new Vet();
        Specialty specialty = new Specialty();
        specialty.setName("Dentistry");

        vet.addSpecialty(specialty);
        List<Specialty> specialties = vet.getSpecialties();

        assertEquals(1, specialties.size());
        assertEquals("Dentistry", specialties.get(0).getName());
    }

    @Test
    void testGetNrOfSpecialties() {
        Vet vet = new Vet();
        assertEquals(0, vet.getNrOfSpecialties());

        Specialty spec1 = new Specialty();
        Specialty spec2 = new Specialty();

        vet.addSpecialty(spec1);
        vet.addSpecialty(spec2);

        assertEquals(2, vet.getNrOfSpecialties());
    }

    @Test
    void testGetAndSetNames() {
        Vet vet = new Vet();
        vet.setFirstName("John");
        vet.setLastName("Doe");

        assertEquals("John", vet.getFirstName());
        assertEquals("Doe", vet.getLastName());
    }

    @Test
    void testGetIdInitiallyNull1() {
        Vet vet = new Vet();
        assertNull(vet.getId());
    }

    @Test
    void testFindById() {
        Vet vet = new Vet();
        vet.setId(1);
        vet.setFirstName("Alice");

        given(vetRepository.findById(1)).willReturn(Optional.of(vet));

        Optional<Vet> result = vetRepository.findById(1);
        assertTrue(result.isPresent());
        assertEquals("Alice", result.get().getFirstName());
    }

    @Test
    void testFindAll() {
        Vet vet1 = new Vet();
        vet1.setFirstName("Anna");

        Vet vet2 = new Vet();
        vet2.setFirstName("Bob");

        given(vetRepository.findAll()).willReturn(asList(vet1, vet2));

        List<Vet> result = vetRepository.findAll();
        assertEquals(2, result.size());
    }

    @Test
    void testSave() {
        Vet vet = new Vet();
        vet.setFirstName("Clara");

        given(vetRepository.save(vet)).willReturn(vet);

        Vet saved = vetRepository.save(vet);
        assertEquals("Clara", saved.getFirstName());
    }
}
