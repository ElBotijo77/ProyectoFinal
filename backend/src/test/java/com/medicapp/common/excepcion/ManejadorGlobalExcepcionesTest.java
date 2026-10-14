package com.medicapp.common.excepcion;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Comprueba el formato de error uniforme sin levantar la base de datos
 * (@WebMvcTest solo carga la capa web).
 */
@WebMvcTest(controllers = ManejadorGlobalExcepcionesTest.ControladorPrueba.class)
@Import(ManejadorGlobalExcepcionesTest.ControladorPrueba.class)
class ManejadorGlobalExcepcionesTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void recursoNoEncontradoDevuelve404ConFormatoUniforme() throws Exception {
        mockMvc.perform(get("/prueba/no-existe"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.estado").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.mensaje").value("Paciente 7 no encontrado"))
                .andExpect(jsonPath("$.ruta").value("/prueba/no-existe"))
                .andExpect(jsonPath("$.fecha").exists())
                .andExpect(jsonPath("$.errores").doesNotExist());
    }

    @Test
    void validacionDevuelve400ConErroresPorCampo() throws Exception {
        mockMvc.perform(post("/prueba/validar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value(400))
                .andExpect(jsonPath("$.errores[0].campo").value("nombre"));
    }

    @Test
    void jsonMalFormadoDevuelve400() throws Exception {
        mockMvc.perform(post("/prueba/validar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{no es json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value(400));
    }

    @Test
    void errorInesperadoDevuelve500SinDetallesInternos() throws Exception {
        mockMvc.perform(get("/prueba/fallo"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.estado").value(500))
                .andExpect(jsonPath("$.mensaje").value("Se ha producido un error interno"));
    }

    @RestController
    static class ControladorPrueba {

        record DatosPrueba(@NotBlank String nombre) {
        }

        @GetMapping("/prueba/no-existe")
        void noExiste() {
            throw new RecursoNoEncontradoException("Paciente 7 no encontrado");
        }

        @PostMapping("/prueba/validar")
        void validar(@Valid @RequestBody DatosPrueba datos) {
        }

        @GetMapping("/prueba/fallo")
        void fallo() {
            throw new IllegalStateException("detalle interno que no debe salir");
        }
    }
}
