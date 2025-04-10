/*
 * Copyright 2002-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.vets.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.vets.model.Vet;
import org.springframework.samples.petclinic.vets.model.VetRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static java.util.Arrays.asList;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit test for the VetResource class.
 */
@ExtendWith(SpringExtension.class)
@WebMvcTest(VetResource.class)  // Chỉ test các web controller, không khởi tạo toàn bộ ứng dụng Spring Boot.
@ActiveProfiles("test")  // Sử dụng cấu hình cho môi trường test
class VetResourceTest {

    @Autowired
    MockMvc mvc;  // MockMvc là công cụ để kiểm tra các HTTP request.

    @MockBean
    VetRepository vetRepository;  // Mock VetRepository để không phải truy cập cơ sở dữ liệu thực.

    @Test
    void shouldGetAListOfVets() throws Exception {

        // Tạo một đối tượng Vet mẫu để trả về trong kết quả.
        Vet vet = new Vet();
        vet.setId(1);
        vet.setFirstName("John");
        vet.setLastName("Doe");

        // Cấu hình để mock hành vi của vetRepository.findAll()
        given(vetRepository.findAll()).willReturn(asList(vet));

        // Thực hiện một HTTP GET request đến endpoint /vets và kiểm tra kết quả trả về.
        mvc.perform(get("/vets")
                .accept(MediaType.APPLICATION_JSON))  // Đảm bảo yêu cầu nhận dữ liệu dưới dạng JSON
            .andExpect(status().isOk())  // Kiểm tra HTTP status code trả về là 200 OK
            .andExpect(jsonPath("$[0].id").value(1))  // Kiểm tra ID của Vet đầu tiên trong mảng trả về.
            .andExpect(jsonPath("$[0].firstName").value("John"))  // Kiểm tra firstName
            .andExpect(jsonPath("$[0].lastName").value("Doe"));  // Kiểm tra lastName
    }

    @Test
    void shouldReturnEmptyListWhenNoVets() throws Exception {

        // Mock hành vi của repository khi không có vet nào.
        given(vetRepository.findAll()).willReturn(asList());

        // Kiểm tra rằng nếu không có vet nào thì trả về danh sách rỗng.
        mvc.perform(get("/vets")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())  // Kiểm tra HTTP status code trả về là 200 OK
            .andExpect(jsonPath("$").isEmpty());  // Kiểm tra rằng kết quả là một danh sách rỗng.
    }
}
